package burnedkirby.TurnBasedBattleMod;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import burnedkirby.TurnBasedBattleMod.CombatantInfo.Type;
import burnedkirby.TurnBasedBattleMod.core.Utility;
import burnedkirby.TurnBasedBattleMod.core.network.BattleCombatantPacket;
import burnedkirby.TurnBasedBattleMod.core.network.BattlePhaseEndedPacket;
import burnedkirby.TurnBasedBattleMod.core.network.BattleStatusPacket;
import burnedkirby.TurnBasedBattleMod.core.network.InitiateBattlePacket;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.MinecraftException;

public class Battle{
	private Map<Integer,CombatantInfo> combatants;
	private Stack<CombatantInfo> newCombatantQueue;
	private Stack<CombatantInfo> removeCombatantQueue;
	private int battleID;

	protected enum BattleStatus {
		PLAYER_PHASE, CALCULATIONS_PHASE, END_CHECK_PHASE
	}
	
	private BattleStatus status;
	
//	private boolean phaseInProgress;
	
	protected boolean battleEnded;
	
	public Battle(int id)
	{
		battleID = id;
	}
	
	public Battle(int id, Stack<CombatantInfo> newCombatants)
	{
		battleID = id;
		combatants = new TreeMap<Integer,CombatantInfo>();
		newCombatantQueue = new Stack<CombatantInfo>();
		removeCombatantQueue = new Stack<CombatantInfo>();
		
		CombatantInfo combatant;
		while(!newCombatants.isEmpty())
		{
			combatant = newCombatants.pop();
			System.out.println("Initializing battle with combatant " + combatant.name);
			if(combatant.isPlayer)
			{
				PacketDispatcher.sendPacketToPlayer(new InitiateBattlePacket(battleID,combatant).makePacket(), (Player)Utility.getEntityByID(combatant.id));
			}
			combatants.put(combatant.id, combatant);
		}
		
		status = BattleStatus.PLAYER_PHASE;
		battleEnded = false;
//		phaseInProgress = false;
	}
	
	public void addCombatant(CombatantInfo newCombatant)
	{
		if(status == BattleStatus.PLAYER_PHASE)
		{
			combatants.put(newCombatant.id, newCombatant);

			if(newCombatant.isPlayer)
				PacketDispatcher.sendPacketToPlayer(new InitiateBattlePacket(battleID,newCombatant).makePacket(), (Player)Utility.getEntityByID(newCombatant.id));
			
			notifyPlayers(true);
		}
		else
		{
			newCombatantQueue.push(newCombatant);
		}
	}
	
	public void manageDeath(int id)
	{
		if(status == BattleStatus.PLAYER_PHASE)
		{
			CombatantInfo removed = combatants.remove(id);
			
			if(removed.isPlayer)
				PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(false, false, combatants.size()).makePacket(), (Player)Utility.getEntityByID(id));
			
			notifyPlayers(true);
		}
		else
		{
			removeCombatantQueue.add(combatants.get(id));
		}
	}
	
	public CombatantInfo getCombatant(CombatantInfo combatantQuery)
	{
		for(CombatantInfo combatant : combatants.values())
		{
			if(combatant == combatantQuery)
				return combatant;
		}
		return null;
	}
	
	public void updatePlayerStatus(CombatantInfo combatant)
	{
		if(this.status != BattleStatus.PLAYER_PHASE || !combatants.containsValue(combatant))
		{
			System.out.println("WARNING: Battle " + battleID + " failed updatePlayerStatus."); //TODO debug
			return;
		}
		
		combatants.put(combatant.id, combatant);
		
		update();
	}
	
	public boolean isInBattle(int id)
	{
		return combatants.containsKey(id);
	}
	
	private boolean getPlayersReady()
	{
		for(CombatantInfo combatant : combatants.values())
		{
			System.out.println("getPlayersReady: " + combatant.ready);
			if(combatant.isPlayer && !combatant.ready)
				return false;
		}
		return true;
	}
	
	public synchronized void update()
	{
		switch(status)
		{
		case PLAYER_PHASE:
			playerPhase();
			break;
		case CALCULATIONS_PHASE:
			calculationsPhase();
			break;
		case END_CHECK_PHASE:
			endCheckPhase();
			break;
		}
	}

	private void playerPhase()
	{
//		if(phaseInProgress)
//			return;
//		phaseInProgress = true;
		
		boolean forceUpdate = false;
		
		if(!newCombatantQueue.isEmpty())
			forceUpdate = true;
		while(!newCombatantQueue.isEmpty())
		{
			CombatantInfo combatant = newCombatantQueue.pop();
			combatants.put(combatant.id, combatant);
		}
		
		notifyPlayers(forceUpdate);
		
		if(getPlayersReady())
		{
			status = BattleStatus.CALCULATIONS_PHASE;
			System.out.println("PlayerPhase ended.");
		}
		
//		phaseInProgress = false;
	}
	
	private void calculationsPhase()
	{
//		if(phaseInProgress)
//			return;
//		phaseInProgress = true;
		
		//Combatant flee phase
		for(CombatantInfo combatant : combatants.values())
		{
			if(combatant.type != Type.FLEE)
				continue;
			
			if(fleeCheck(combatant))
			{
				//TODO flee
			}
			
			combatant.type = Type.DO_NOTHING;
		}
		
		//Combatant attack phase
		Entity combatantEntity;
		Entity targetEntity;
		for(CombatantInfo combatant : combatants.values())
		{
			if(combatant.type != Type.ATTACK)
				continue;
			
			combatantEntity = Utility.getEntityByID(combatant.id);
			if(combatantEntity == null)
				continue;
			targetEntity = Utility.getEntityByID(combatant.target);
			
			if(combatant.isPlayer)
				((EntityPlayer)Utility.getEntityByID(combatant.id)).attackTargetEntityWithCurrentItem(Utility.getEntityByID(combatant.target));
			else if(combatantEntity instanceof EntityMob)
				((EntityMob)combatantEntity).attackEntityAsMob(targetEntity);
			else
				;//TODO
		}
		
		status = BattleStatus.END_CHECK_PHASE;
		System.out.println("Calculations phase ended.");
		
//		phaseInProgress = false;
	}
	
	private void endCheckPhase()
	{
//		if(phaseInProgress)
//			return;
//		phaseInProgress = true;
		CombatantInfo combatantRef;
		Iterator<CombatantInfo> iter = combatants.values().iterator();
		Stack<CombatantInfo> replacementQueue = new Stack<CombatantInfo>();
		
		
		while(iter.hasNext())
		{
			combatantRef = iter.next();
			combatantRef.ready = false;
			replacementQueue.push(combatantRef);
		}
		
		while(!replacementQueue.isEmpty())
		{
			combatantRef = replacementQueue.pop();
			combatants.put(combatantRef.id, combatantRef);
			System.out.println("repQueue: " + combatantRef.ready);
		}
		
		if(combatants.isEmpty()) //TODO finish this
			battleEnded = true;
		
		notifyPlayersTurnEnded();
		
		status = BattleStatus.PLAYER_PHASE;
		System.out.println("End phase ended.");
		
//		phaseInProgress = false;
	}
	
	private boolean fleeCheck(CombatantInfo fleeingCombatant)
	{
		int enemySide = 0;
		int ownSide = 0;
		int diff;
		double rand;

		if(enemySide == ownSide)
		{
			return BattleSystemServer.random.nextBoolean();
		}
		else
		{
			diff = Math.abs(ownSide - enemySide);
			rand = BattleSystemServer.random.nextDouble() / Math.log((double)(diff + Math.E));
			return ownSide > enemySide ? rand <= 0.67d : rand > 0.67d;
		}
	}
	
	protected void notifyPlayers(boolean forceUpdate)
	{
		for(CombatantInfo combatant : combatants.values())
		{
			if(combatant.isPlayer)
				PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(true, forceUpdate, combatants.size()).makePacket(), (Player)Utility.getEntityByID(combatant.id));
		}
	}
	
	protected void notifyPlayersTurnEnded()
	{
		for(CombatantInfo combatant : combatants.values())
		{
			if(combatant.isPlayer)
			{
				System.out.println("Notify player turn ended packet sent.");
				if(Utility.getEntityByID(combatant.id) == null)
					System.out.println("WARNING: player id returned null");
				PacketDispatcher.sendPacketToPlayer(new BattlePhaseEndedPacket().makePacket(), (Player)Utility.getEntityByID(combatant.id));
			}
		}
	}
	
	protected void notifyPlayerOfCombatants(EntityLiving player)
	{
		for(CombatantInfo combatant : combatants.values())
		{
			PacketDispatcher.sendPacketToPlayer(new BattleCombatantPacket(combatant).makePacket(), (Player)player);
		}
	}
	
	
}
