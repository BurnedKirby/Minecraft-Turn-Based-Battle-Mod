package burnedkirby.TurnBasedBattleMod;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import burnedkirby.TurnBasedBattleMod.CombatantInfo.Type;
import burnedkirby.TurnBasedBattleMod.core.Utility;
import burnedkirby.TurnBasedBattleMod.core.network.BattleStatusPacket;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.MinecraftException;

public class BattleSystemServer {
	/**
	 * battles data-structure: BattleID to Battle 
	 */
	private Map<Integer,Battle> battles;
	

	protected static int battleIDCounter = 0; //TODO maybe split this per world
	
	protected static Random random;
	
	private Thread battleUpdateThread;
	
	public BattleSystemServer()
	{
		battles = new TreeMap<Integer,Battle>();
		random = new Random(Minecraft.getMinecraft().getSystemTime());
		battleUpdateThread = new Thread(new BattleUpdate());
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
	public boolean manageCombatants(EntityLiving entityAttacker, EntityLiving entityAttacked)
	{
		short inBattle = 0x0;
		inBattle |= isInBattle(entityAttacker.entityId) ? 0x1 : 0x0;
		inBattle |= isInBattle(entityAttacked.entityId) ? 0x2 : 0x0;
		
		System.out.println("inbattle: " + inBattle);
		
		if(!battleUpdateThread.isAlive())
			battleUpdateThread.start();
		switch(inBattle)
		{
		case 0x0:
			if(!(entityAttacker instanceof EntityPlayer) && !(entityAttacked instanceof EntityPlayer))
				return false;
			
			Stack<CombatantInfo> combatants = new Stack<CombatantInfo>();
			combatants.push(new CombatantInfo(entityAttacker instanceof EntityPlayer, entityAttacker.entityId, true, entityAttacker.getEntityName(), false, Type.DO_NOTHING, entityAttacker.getAttackTarget() != null ? entityAttacker.getAttackTarget().entityId : 0));
			combatants.push(new CombatantInfo(entityAttacked instanceof EntityPlayer, entityAttacked.entityId, false, entityAttacked.getEntityName(), false, Type.DO_NOTHING, entityAttacked.getAttackTarget() != null ? entityAttacked.getAttackTarget().entityId : 0));
			synchronized(battles)
			{
				battles.put(battleIDCounter,new Battle(battleIDCounter, combatants));
			}
			System.out.println("New battle " + battleIDCounter + " created.");
			battleIDCounter++;
			return true;
		case 0x1:
		case 0x2:
			EntityLiving newCombatant = (inBattle == 0x1 ? entityAttacked : entityAttacker);
			EntityLiving inBattleCombatant = (inBattle == 0x1 ? entityAttacker : entityAttacked);
			
			synchronized(battles)
			{
				Battle battleToJoin;
				boolean isSideOne = !((battleToJoin = findBattleByEntityID(inBattleCombatant.entityId)).getCombatant(new CombatantInfo(inBattleCombatant.entityId)).isSideOne);
				
				battleToJoin.addCombatant(new CombatantInfo(newCombatant instanceof EntityPlayer, newCombatant.entityId, isSideOne, newCombatant.getEntityName(), false, Type.DO_NOTHING, newCombatant.getAttackTarget() != null ? newCombatant.getAttackTarget().entityId : 0));
			}
			return true;
		case 0x3:
			return true; //TODO
		default:
			return true;
		}
	}
	
	public void manageCombatantDeath(Entity entity)
	{
		Battle b = findBattleByEntityID(entity.entityId);
		
		if(b == null)
		{
			System.out.println("Dead Entity (" + entity.entityId + ") " + entity.getEntityName() + " not in battle.");
		}
		else
		{
			b.manageDeath(entity.entityId);
		}
	}
	
	/**
	 * Checks if the entity is currently in battle.
	 * @param entityID The entity that is checked.
	 * @return True if the entity is in battle.
	 */
	private boolean isInBattle(int entityID)
	{
		synchronized(battles)
		{
			for(Battle b : battles.values())
			{
				if(b.isInBattle(entityID))
					return true;
			}
			return false;
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
			battles.get(battleID).updatePlayerStatus(player);
		}
	}
	
	public class BattleUpdate implements Runnable
	{

		@Override
		public void run() {
			while(MinecraftServer.getServer().isServerRunning())
			{
				synchronized(battles)
				{
					for(Battle b : battles.values())
					{
						b.update();
					}
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {}
			}
		}
		
	}
}
