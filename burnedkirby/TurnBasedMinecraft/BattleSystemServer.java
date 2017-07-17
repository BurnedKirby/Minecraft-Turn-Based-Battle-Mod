package burnedkirby.TurnBasedMinecraft;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.TreeMap;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityElderGuardian;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.monster.EntityEvoker;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityHusk;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityStray;
import net.minecraft.entity.monster.EntityVex;
import net.minecraft.entity.monster.EntityVindicator;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityDonkey;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityLlama;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityMule;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySkeletonHorse;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.passive.EntityZombieHorse;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import burnedkirby.TurnBasedMinecraft.CombatantInfo.Type;
import burnedkirby.TurnBasedMinecraft.core.CombatantInfoSet;
import burnedkirby.TurnBasedMinecraft.core.Utility;
import burnedkirby.TurnBasedMinecraft.core.network.BattleStatusPacket;

public class BattleSystemServer {
	/**
	 * battles data-structure: BattleID to Battle 
	 */
	
	public Map<String, Boolean> ignoreSystemEntityMap;
	
	private Map<Integer,Battle> battles;
	private Stack<List<Integer>> justEntered;
	protected CombatantInfoSet inBattle;
	protected CombatantInfoSet exitedBattle;

	protected static int battleIDCounter = 0;
	
	protected static Random random;
	
	protected static EntityLivingBase attackingEntity;
	protected static Object attackingLock;
	
	private Thread battleUpdateThread;
	private Thread recentlyAddedUpdateThread;
	
	protected static final int exitCooldownTime = 5;
	
	private MinecraftServer serverInstance = null;
	
	public void setServerInstance(MinecraftServer serverInstance) {
		this.serverInstance = serverInstance;
	}

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
		ignoreSystemEntityMap.put("Donkey", false);
		ignoreSystemEntityMap.put("Llama", false);
		ignoreSystemEntityMap.put("Mule", false);
		ignoreSystemEntityMap.put("SkeletonHorse", false);
		ignoreSystemEntityMap.put("ZombieHorse", false);
		ignoreSystemEntityMap.put("Rabbit", false);
		ignoreSystemEntityMap.put("ElderGuardian", true);
		ignoreSystemEntityMap.put("Endermite", false);
		ignoreSystemEntityMap.put("Evoker", false);
		ignoreSystemEntityMap.put("Vindicator", false);
		ignoreSystemEntityMap.put("Husk", false);
		ignoreSystemEntityMap.put("PolarBear", false);
		ignoreSystemEntityMap.put("Shulker", false);
		ignoreSystemEntityMap.put("Stray", false);
		ignoreSystemEntityMap.put("Vex", false);
		ignoreSystemEntityMap.put("EntityZombieVillager", false);
		
		battles = new TreeMap<Integer,Battle>();
		inBattle = new CombatantInfoSet();
		exitedBattle = new CombatantInfoSet();
		justEntered = new Stack<List<Integer>>();
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
		else if(exitedBattle.contains(entityAttacker.getEntityId()) || exitedBattle.contains(entityAttacked.getEntityId()))
		{
			Utility.log("Canceled attack due to exitedbattle containing entity.");
			return true;
		}
		
		boolean returnValue = false;
		short inBattle = 0x0;
		inBattle |= isInBattle(entityAttacker.getEntityId()) ? 0x1 : 0x0;
		inBattle |= isInBattle(entityAttacked.getEntityId()) ? 0x2 : 0x0;
		
		List<Integer> recentlyAdded;
		synchronized(justEntered)
		{
			recentlyAdded = justEntered.isEmpty() ? null : justEntered.pop();
		}
		List<Integer> justAdded;
		
		Utility.log("attack event status " + inBattle);

		switch(inBattle)
		{
		case 0x0:
			if(!(entityAttacker instanceof EntityPlayer) && !(entityAttacked instanceof EntityPlayer))
			{
				returnValue = false;
				break;
			}
			
			boolean silly = isPassive(entityAttacker) || isPassive(entityAttacked);
			
			Stack<CombatantInfo> combatants = new Stack<CombatantInfo>();

			String attackerName = null;
			String attackedName = null;
			
			if(entityAttacker.hasCustomName())
				attackerName = entityAttacker.getCustomNameTag();
			else if((attackerName = EntityList.getEntityString(entityAttacker)) == null)
				attackerName = entityAttacker.getName();

			if(entityAttacked.hasCustomName())
				attackedName = entityAttacked.getCustomNameTag();
			else if((attackedName = EntityList.getEntityString(entityAttacked)) == null)
				attackedName = entityAttacked.getName();
			
			combatants.push(new CombatantInfo(entityAttacker instanceof EntityPlayer, entityAttacker.getEntityId(), entityAttacker, true, attackerName, false, Type.DO_NOTHING, entityAttacked.getEntityId()));
			combatants.push(new CombatantInfo(entityAttacked instanceof EntityPlayer, entityAttacked.getEntityId(), entityAttacked, false, attackedName, false, Type.DO_NOTHING, entityAttacker.getEntityId()));
			synchronized(battles)
			{
				battles.put(battleIDCounter,new Battle(battleIDCounter, combatants, silly));
			}
			Utility.log("New battle " + battleIDCounter + " created.");
			battleIDCounter++;
			returnValue = false;
			justAdded = new LinkedList<Integer>();
			justAdded.add(entityAttacker.getEntityId());
			justAdded.add(entityAttacked.getEntityId());
			synchronized(justEntered)
			{
				justEntered.push(justAdded);
			}
			if(recentlyAddedUpdateThread == null || !recentlyAddedUpdateThread.isAlive())
			{
				recentlyAddedUpdateThread = new Thread(new RecentlyAddedUpdate());
				recentlyAddedUpdateThread.start();
			}
			break;
		case 0x1:
		case 0x2:
			EntityLivingBase newCombatant = (inBattle == 0x1 ? entityAttacked : entityAttacker);
			EntityLivingBase inBattleCombatant = (inBattle == 0x1 ? entityAttacker : entityAttacked);
			
			synchronized(battles)
			{
				Battle battleToJoin;
				boolean isSideOne = !((battleToJoin = findBattleByEntityID(inBattleCombatant.getEntityId())).getCombatant(inBattleCombatant.getEntityId()).isSideOne);
				
				String newName = null;
				
				if(CombatantInfo.maxParticipantsInBattle > 1 && battleToJoin.getNumberOfCombatants() >= CombatantInfo.maxParticipantsInBattle)
				{
					// Limit number of combatants in battle if setting is greater than 2
					returnValue = true;
					break;
				}
				
				if(newCombatant.hasCustomName())
					newName = newCombatant.getCustomNameTag();
				else if((newName = EntityList.getEntityString(newCombatant)) == null)
					newName = newCombatant.getName();
				
				battleToJoin.addCombatant(new CombatantInfo(newCombatant instanceof EntityPlayer, newCombatant.getEntityId(), newCombatant, isSideOne, newName, false, Type.DO_NOTHING, inBattleCombatant.getEntityId()));
			}
			returnValue = true;
			justAdded = new LinkedList<Integer>();
			justAdded.add(newCombatant.getEntityId());
			synchronized(justEntered)
			{
				justEntered.push(justAdded);
			}
			if(recentlyAddedUpdateThread == null || !recentlyAddedUpdateThread.isAlive())
			{
				recentlyAddedUpdateThread = new Thread(new RecentlyAddedUpdate());
				recentlyAddedUpdateThread.start();
			}
			break;
		case 0x3:
			if(entityAttacker == attackingEntity)
			{
				returnValue = false;
				break;
			}
			else if(recentlyAdded != null)
			{
				for(int i : recentlyAdded)
				{
					if(i == entityAttacker.getEntityId() || i == entityAttacked.getEntityId())
					{
						returnValue = false;
						break;
					}
				}
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
				ModMain.network.sendTo(new BattleStatusPacket(false), (EntityPlayerMP)player);
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
				ModMain.network.sendTo(new BattleStatusPacket(false), (EntityPlayerMP)player.entityReference);
				return;
			}
			battles.get(battleID).updatePlayerStatus(player);
		}
	}
	
	public class RecentlyAddedUpdate implements Runnable
	{
		@Override
		public void run() {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
			
			synchronized(justEntered)
			{
				if(!justEntered.isEmpty())
					justEntered.pop();
			}
		}
	}
	
	public class BattleUpdate implements Runnable
	{
		Stack<Integer> removalQueue = new Stack<Integer>();
		short empty;

		@Override
		public synchronized void run() {
			while(serverInstance != null && serverInstance.isServerRunning())
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
						Utility.log("Battle removed.");
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
	
	public boolean isPassive(EntityLivingBase entity)
	{
		if(entity instanceof EntityBat)
			return true;
		else if(entity instanceof EntityChicken)
			return true;
		else if(entity instanceof EntityCow)
			return true;
		else if(entity instanceof EntityHorse)
			return true;
		else if(entity instanceof EntityMooshroom)
			return true;
		else if(entity instanceof EntityOcelot)
			return true;
		else if(entity instanceof EntityPig)
			return true;
		else if(entity instanceof EntitySheep)
			return true;
		else if(entity instanceof EntitySquid)
			return true;
		else if(entity instanceof EntityVillager)
			return true;
		else if(entity instanceof EntitySlime)
			return true;
		else if(entity instanceof EntityDonkey)
			return true;
		else if(entity instanceof EntityLlama)
			return true;
		else if(entity instanceof EntityMule)
			return true;
		else if(entity instanceof EntityRabbit)
			return true;
		
		return false;
			
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
				return ignoreSystemEntityMap.get("Skeleton");
		else if(entity instanceof EntityWitherSkeleton)
			return ignoreSystemEntityMap.get("WitherSkeleton");
		else if(entity instanceof EntitySlime)
			return ignoreSystemEntityMap.get("Slime");
		else if(entity instanceof EntitySnowman)
			return ignoreSystemEntityMap.get("Snowman");
		else if(entity instanceof EntitySpider) //Potential bug here since CaveSpiders are also Spiders, but it gets checked against Cave Spiders first in this ordering so it's fine
			return ignoreSystemEntityMap.get("Spider");
		else if(entity instanceof EntityWitch)
			return ignoreSystemEntityMap.get("Witch");
		else if(entity instanceof EntityZombieVillager)
			return ignoreSystemEntityMap.get("ZombieVillager");
		else if(entity instanceof EntityZombie)
			return ignoreSystemEntityMap.get("Zombie");
		else if(entity instanceof EntityDragon)
			return ignoreSystemEntityMap.get("Dragon");
		else if(entity instanceof EntityWither)
			return ignoreSystemEntityMap.get("WitherBoss");
		else if(entity instanceof EntityDonkey)
			return ignoreSystemEntityMap.get("Donkey");
		else if(entity instanceof EntityLlama)
			return ignoreSystemEntityMap.get("Llama");
		else if(entity instanceof EntityMule)
			return ignoreSystemEntityMap.get("EntityMule");
		else if(entity instanceof EntityZombieHorse)
			return ignoreSystemEntityMap.get("ZombieHorse");
		else if(entity instanceof EntitySkeletonHorse)
			return ignoreSystemEntityMap.get("SkeletonHorse");
		else if(entity instanceof EntityRabbit)
			return ignoreSystemEntityMap.get("Rabbit");
		else if(entity instanceof EntityElderGuardian)
			return ignoreSystemEntityMap.get("ElderGuardian");
		else if(entity instanceof EntityEndermite)
			return ignoreSystemEntityMap.get("Endermite");
		else if(entity instanceof EntityEvoker)
			return ignoreSystemEntityMap.get("Evoker");
		else if(entity instanceof EntityVindicator)
			return ignoreSystemEntityMap.get("Vindicator");
		else if(entity instanceof EntityHusk)
			return ignoreSystemEntityMap.get("Husk");
		else if(entity instanceof EntityPolarBear)
			return ignoreSystemEntityMap.get("PolarBear");
		else if(entity instanceof EntityShulker)
			return ignoreSystemEntityMap.get("Shulker");
		else if(entity instanceof EntityStray)
			return ignoreSystemEntityMap.get("Stray");
		else if(entity instanceof EntityVex)
			return ignoreSystemEntityMap.get("Vex");
		else
			new Exception("Tell BurnedKirby that he needs to update the mod because there are new mobs!").printStackTrace();
		return false;
	}
}
