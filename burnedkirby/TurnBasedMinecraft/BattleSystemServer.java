package burnedkirby.TurnBasedMinecraft;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import burnedkirby.TurnBasedMinecraft.CombatantInfo.Type;
import burnedkirby.TurnBasedMinecraft.core.Utility;
import burnedkirby.TurnBasedMinecraft.core.network.BattleStatusPacket;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
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
	
	protected static EntityLivingBase attackingEntity;
	protected static Object attackingLock;
	
	private Thread battleUpdateThread;
	
	public BattleSystemServer()
	{
		battles = new TreeMap<Integer,Battle>();
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
		if(entityAttacker instanceof EntityCreeper)
			return false;
		
		boolean returnValue = false;
		short inBattle = 0x0;
		inBattle |= isInBattle(entityAttacker.entityId) ? 0x1 : 0x0;
		inBattle |= isInBattle(entityAttacked.entityId) ? 0x2 : 0x0;

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
			returnValue = true;
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
			returnValue = true;
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
	
//	public void manageCombatantDeath(Entity entity)
//	{
//		Battle b = findBattleByEntityID(entity.entityId);
//		
//		if(b == null)
//		{
//			System.out.println("Dead Entity (" + entity.entityId + ") " + entity.getEntityName() + " not in battle.");
//		}
//		else
//		{
//			b.manageDeath(entity.entityId);
//		}
//	}
	
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

		@Override
		public synchronized void run() {
			while(MinecraftServer.getServer().isServerRunning())
			{
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
					
					if(battles.isEmpty())
						return;
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {}
			}
		}
		
	}
}
