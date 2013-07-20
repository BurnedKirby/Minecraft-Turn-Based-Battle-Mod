package burnedkirby.TurnBasedMinecraft;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import burnedkirby.TurnBasedMinecraft.CombatantInfo.Type;
import burnedkirby.TurnBasedMinecraft.core.CombatantInfoSet;
import burnedkirby.TurnBasedMinecraft.core.network.BattleStatusPacket;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class BattleSystemServer {
	/**
	 * battles data-structure: BattleID to Battle 
	 */
	
	public Map<String, Boolean> ignoreSystemEntityMap;
	private Map<Integer,Battle> battles;
	protected CombatantInfoSet inBattle;
	protected CombatantInfoSet exitedBattle;

	protected static int battleIDCounter = 0; //TODO maybe split this per world
	
	protected static Random random;
	
	protected static EntityLivingBase attackingEntity;
	protected static Object attackingLock;
	
	private Thread battleUpdateThread;
	
	protected static final int exitCooldownTime = 5;
	
	public BattleSystemServer()
	{
		ignoreSystemEntityMap = new TreeMap<String,Boolean>();
		
		ignoreSystemEntityMap.put("Bat", false);
		ignoreSystemEntityMap.put("Chicken", false);
		ignoreSystemEntityMap.put("Cow", false);
		ignoreSystemEntityMap.put("Horse", false);
		ignoreSystemEntityMap.put("Mooshroom", false);
		ignoreSystemEntityMap.put("Ocelot", false);
		ignoreSystemEntityMap.put("Pig", false);
		ignoreSystemEntityMap.put("Sheep", false);
		ignoreSystemEntityMap.put("Squid", false);
		ignoreSystemEntityMap.put("Villager", false);
		ignoreSystemEntityMap.put("Wolf", false);
		ignoreSystemEntityMap.put("Blaze", false);
		ignoreSystemEntityMap.put("CaveSpider", false);
		ignoreSystemEntityMap.put("Creeper", true);
		ignoreSystemEntityMap.put("Enderman", false);
		ignoreSystemEntityMap.put("Ghast", true);
		ignoreSystemEntityMap.put("GiantZombie", false);
		ignoreSystemEntityMap.put("IronGolem", false);
		ignoreSystemEntityMap.put("MagmaCube", false);
		ignoreSystemEntityMap.put("PigZombie", false);
		ignoreSystemEntityMap.put("Silverfish", true);
		ignoreSystemEntityMap.put("Skeleton", false);
		ignoreSystemEntityMap.put("Slime", false);
		ignoreSystemEntityMap.put("Snowman", false);
		ignoreSystemEntityMap.put("Spider", false);
		ignoreSystemEntityMap.put("Witch", false);
		ignoreSystemEntityMap.put("WitherSkeleton", false);
		ignoreSystemEntityMap.put("Zombie", false);
		ignoreSystemEntityMap.put("Dragon", true);
		ignoreSystemEntityMap.put("WitherBoss", true);
		
		battles = new TreeMap<Integer,Battle>();
		inBattle = new CombatantInfoSet();
		exitedBattle = new CombatantInfoSet();
		random = new Random(System.currentTimeMillis());
		battleUpdateThread = null;
		attackingEntity = null;
		attackingLock = new Object();
	}
	
	/**
	 * Called by the BattleEventListener, this class handles the creation of battles and placement
	 * of combatants.
	 * 
	 * 
	 * @param entityAttacker The attacking entity.
	 * @param entityAttacked The attacked entity.
	 * @return True if the LivingAttackEvent that called this method should be canceled.
	 */
	public boolean manageCombatants(EntityLivingBase entityAttacker, EntityLivingBase entityAttacked)
	{
		if(ignoreSystem(entityAttacker) || ignoreSystem(entityAttacked)
				|| (entityAttacker instanceof EntityPlayer && ((EntityPlayer)entityAttacker).capabilities.isCreativeMode)
				|| (entityAttacked instanceof EntityPlayer && ((EntityPlayer)entityAttacked).capabilities.isCreativeMode))
			return false;
		else if(exitedBattle.contains(entityAttacker.entityId) || exitedBattle.contains(entityAttacked.entityId))
		{
			System.out.println("Canceled attack due to exitedbattle containing entity.");
			return true;
		}
		
		boolean returnValue = false;
		short inBattle = 0x0;
		inBattle |= isInBattle(entityAttacker.entityId) ? 0x1 : 0x0;
		inBattle |= isInBattle(entityAttacked.entityId) ? 0x2 : 0x0;
		
		System.out.println("attack event status " + inBattle);

		switch(inBattle)
		{
		case 0x0:
			if(!(entityAttacker instanceof EntityPlayer) && !(entityAttacked instanceof EntityPlayer))
			{
				returnValue = false;
				break;
			}
			
			Stack<CombatantInfo> combatants = new Stack<CombatantInfo>();
			combatants.push(new CombatantInfo(entityAttacker instanceof EntityPlayer, entityAttacker.entityId, entityAttacker, true, entityAttacker.getEntityName(), false, Type.DO_NOTHING, entityAttacked.entityId));
			combatants.push(new CombatantInfo(entityAttacked instanceof EntityPlayer, entityAttacked.entityId, entityAttacked, false, entityAttacked.getEntityName(), false, Type.DO_NOTHING, entityAttacked.getAITarget() != null ? entityAttacked.getAITarget().entityId : 0));
			synchronized(battles)
			{
				battles.put(battleIDCounter,new Battle(battleIDCounter, combatants));
			}
			System.out.println("New battle " + battleIDCounter + " created.");
			battleIDCounter++;
			returnValue = false;
			break;
		case 0x1:
		case 0x2:
			EntityLivingBase newCombatant = (inBattle == 0x1 ? entityAttacked : entityAttacker);
			EntityLivingBase inBattleCombatant = (inBattle == 0x1 ? entityAttacker : entityAttacked);
			
			synchronized(battles)
			{
				Battle battleToJoin;
				boolean isSideOne = !((battleToJoin = findBattleByEntityID(inBattleCombatant.entityId)).getCombatant(inBattleCombatant.entityId).isSideOne);
				
				battleToJoin.addCombatant(new CombatantInfo(newCombatant instanceof EntityPlayer, newCombatant.entityId, newCombatant, isSideOne, newCombatant.getEntityName(), false, Type.DO_NOTHING, inBattleCombatant.entityId));
			}
			returnValue = false;
			break;
		case 0x3:
			if(entityAttacker == attackingEntity)
			{
				returnValue = false;
				break;
			}
			returnValue = true;
			break;
		default:
			returnValue = true;
			break;
		}
		
		if(battleUpdateThread == null || !battleUpdateThread.isAlive())
		{
			battleUpdateThread = new Thread(new BattleUpdate());
			battleUpdateThread.start();
		}
		return returnValue;
	}
	
	/**
	 * Checks if the entity is currently in battle.
	 * @param entityID The entity that is checked.
	 * @return True if the entity is in battle.
	 */
	private boolean isInBattle(int entityID)
	{
		synchronized(inBattle)
		{
			return inBattle.contains(entityID);
		}
	}

	private Battle findBattleByEntityID(int id)
	{
		synchronized(battles)
		{
			for(Battle b : battles.values())
			{
				if(b.isInBattle(id))
					return b;
			}
			return null;
		}
	}
	
	public void manageQuery(int battleID, short type, EntityPlayer player)
	{
		synchronized(battles)
		{

			if(battles.get(battleID) == null)
			{
				PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(false).makePacket(), (Player)player);
				return;
			}
			switch(type)
			{
			case 0:
				battles.get(battleID).notifyPlayers(false);
				break;
			case 1:
				battles.get(battleID).notifyPlayerOfCombatants(player);
				break;
			default:
				break;
			}
		}
	}
	
	public void managePlayerUpdate(int battleID, CombatantInfo player)
	{
		synchronized(battles)
		{
			if(battles.get(battleID) == null)
			{
				PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(false).makePacket(), (Player)player.entityReference);
				return;
			}
			battles.get(battleID).updatePlayerStatus(player);
		}
	}
	
	public class BattleUpdate implements Runnable
	{
		Stack<Integer> removalQueue = new Stack<Integer>();
		short empty;

		@Override
		public synchronized void run() {
			while(MinecraftServer.getServer().isServerRunning())
			{
				empty = 0x0;
				synchronized(battles)
				{
					for(Battle b : battles.values())
					{
						if(b.update())
						{
							removalQueue.add(b.getBattleID());
						}
					}
					
					while(!removalQueue.isEmpty())
					{
						System.out.println("Battle removed.");
						battles.remove(removalQueue.pop());
					}
					
					empty |= battles.isEmpty() ? 0x1 : 0x0;
				}
				synchronized(exitedBattle)
				{
					Iterator<CombatantInfo> iter = exitedBattle.iterator();
					while(iter.hasNext())
					{
						CombatantInfo combatant = iter.next();
						if(combatant.target > 0)
							combatant.setTarget(combatant.target - 1);
						else
							iter.remove();
					}

					empty |= exitedBattle.isEmpty() ? 0x2 : 0x0;
				}
				
				if(empty == 0x3)
					return;
				
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			}
		}
		
	}
	
	public boolean ignoreSystem(EntityLivingBase entity)
	{
		if(entity instanceof EntityPlayer)
			return false;
		else if(entity instanceof EntityBat)
			return ignoreSystemEntityMap.get("Bat");
		else if(entity instanceof EntityChicken)
			return ignoreSystemEntityMap.get("Chicken");
		else if(entity instanceof EntityCow)
			return ignoreSystemEntityMap.get("Cow");
		else if(entity instanceof EntityHorse)
			return ignoreSystemEntityMap.get("Horse");
		else if(entity instanceof EntityMooshroom)
			return ignoreSystemEntityMap.get("Mooshroom");
		else if(entity instanceof EntityOcelot)
			return ignoreSystemEntityMap.get("Ocelot");
		else if(entity instanceof EntityPig)
			return ignoreSystemEntityMap.get("Pig");
		else if(entity instanceof EntitySheep)
			return ignoreSystemEntityMap.get("Sheep");
		else if(entity instanceof EntitySquid)
			return ignoreSystemEntityMap.get("Squid");
		else if(entity instanceof EntityVillager)
			return ignoreSystemEntityMap.get("Villager");
		else if(entity instanceof EntityWolf)
			return ignoreSystemEntityMap.get("Wolf");
		else if(entity instanceof EntityBlaze)
			return ignoreSystemEntityMap.get("Blaze");
		else if(entity instanceof EntityCaveSpider)
			return ignoreSystemEntityMap.get("CaveSpider");
		else if(entity instanceof EntityCreeper)
			return ignoreSystemEntityMap.get("Creeper");
		else if(entity instanceof EntityEnderman)
			return ignoreSystemEntityMap.get("Enderman");
		else if(entity instanceof EntityGhast)
			return ignoreSystemEntityMap.get("Ghast");
		else if(entity instanceof EntityGiantZombie)
			return ignoreSystemEntityMap.get("GiantZombie");
		else if(entity instanceof EntityIronGolem)
			return ignoreSystemEntityMap.get("IronGolem");
		else if(entity instanceof EntityMagmaCube)
			return ignoreSystemEntityMap.get("MagmaCube");
		else if(entity instanceof EntityPigZombie)
			return ignoreSystemEntityMap.get("PigZombie");
		else if(entity instanceof EntitySilverfish)
			return ignoreSystemEntityMap.get("Silverfish");
		else if(entity instanceof EntitySkeleton)
		{
			if(((EntitySkeleton)entity).getSkeletonType() == 1)
				return ignoreSystemEntityMap.get("WitherSkeleton");
			else
				return ignoreSystemEntityMap.get("Skeleton");
		}
		else if(entity instanceof EntitySlime)
			return ignoreSystemEntityMap.get("Slime");
		else if(entity instanceof EntitySnowman)
			return ignoreSystemEntityMap.get("Snowman");
		else if(entity instanceof EntitySpider)
			return ignoreSystemEntityMap.get("Spider");
		else if(entity instanceof EntityWitch)
			return ignoreSystemEntityMap.get("Witch");
		else if(entity instanceof EntityZombie)
			return ignoreSystemEntityMap.get("Zombie");
		else if(entity instanceof EntityDragon)
			return ignoreSystemEntityMap.get("Dragon");
		else if(entity instanceof EntityWither)
			return ignoreSystemEntityMap.get("WitherBoss");
		else
			new Exception("Tell BurnedKirby that he needs to update the mod because there are new mobs!").printStackTrace();
		return false;
	}
}
