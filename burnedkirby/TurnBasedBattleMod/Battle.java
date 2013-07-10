package burnedkirby.TurnBasedBattleMod;

import java.util.Collection;
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

public class Battle implements Comparable<Battle>{
	private Set<CombatantInfo> combatants;
	private Stack<CombatantInfo> newCombatantQueue;
	private int battleID;

	protected enum BattleStatus {
		PLAYER_PHASE, CALCULATIONS_PHASE, END_CHECK_PHASE
	}
	
	private BattleStatus status;
	
	private boolean phaseInProgress;
	
	protected boolean battleEnded;
	
	public Battle(int id, Stack<CombatantInfo> newCombatants)
	{
		battleID = id;
		combatants = new TreeSet<CombatantInfo>();
		newCombatantQueue = new Stack<CombatantInfo>();
		
		CombatantInfo combatant;
		while(!combatants.isEmpty())
		{
			combatant = newCombatants.pop();
			if(combatant.isPlayer)
			{
				PacketDispatcher.sendPacketToPlayer(new InitiateBattlePacket(battleID,combatant).makePacket(), (Player)Utility.getEntityByID(combatant.id));
			}
			combatants.add(combatant);
		}
		
		status = BattleStatus.PLAYER_PHASE;
		battleEnded = false;
		phaseInProgress = false;
	}
	
	public void addCombatant(CombatantInfo newCombatant)
	{
		if(status == BattleStatus.PLAYER_PHASE)
		{
			combatants.add(newCombatant);

			if(newCombatant.isPlayer)
				PacketDispatcher.sendPacketToPlayer(new InitiateBattlePacket(battleID,newCombatant).makePacket(), (Player)Utility.getEntityByID(newCombatant.id));
			
			notifyPlayers(true);
		}
		else
		{
			newCombatantQueue.push(newCombatant);
		}
	}
	
	public CombatantInfo getCombatant(CombatantInfo combatantQuery)
	{
		for(CombatantInfo combatant : combatants)
		{
			if(combatant == combatantQuery)
				return combatant;
		}
		return null;
	}
	
	public void updatePlayerStatus(CombatantInfo combatant)
	{
		if(this.status != BattleStatus.PLAYER_PHASE || !combatants.contains(combatant))
		{
			System.out.println("WARNING: Battle " + battleID + " failed updatePlayerStatus."); //TODO debug
			return;
		}
		
		if(!combatants.remove(combatant))
			System.out.println("WARNING: Battle " + battleID + " added new player on updatePlayerStatus.");
		combatants.add(combatant);
	}
	
	public boolean isInBattle(CombatantInfo combatant)
	{
		return combatants.contains(combatant);
	}
	
	public boolean isInBattle(int id)
	{
		return isInBattle(new CombatantInfo(false, id, false, false, Type.DO_NOTHING, 0));
	}
	
	private boolean getPlayersReady()
	{
		for(CombatantInfo combatant : combatants)
		{
			if(combatant.isPlayer && !combatant.ready)
				return false;
		}
		return true;
	}
	
	public void update()
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
		if(phaseInProgress)
			return;
		phaseInProgress = true;
		
		
		while(!newCombatantQueue.isEmpty())
		{
			combatants.add(newCombatantQueue.pop());
		}
		
		notifyPlayers(true);
		
		if(getPlayersReady())
		{
			status = BattleStatus.CALCULATIONS_PHASE;
		}
		
		phaseInProgress = false;
	}
	
	private void calculationsPhase()
	{
		if(phaseInProgress)
			return;
		phaseInProgress = true;
		
		//Combatant flee phase
		for(CombatantInfo combatant : combatants)
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
		for(CombatantInfo combatant : combatants)
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
		
		phaseInProgress = false;
	}
	
	private void endCheckPhase()
	{
		if(phaseInProgress)
			return;
		phaseInProgress = true;
		
		Stack<CombatantInfo> removalQueue = new Stack<CombatantInfo>();
		Entity combatantEntity;
		for(CombatantInfo combatant : combatants)
		{
			if(Utility.getEntityByID(combatant.id) == null)
				removalQueue.push(combatant);
		}
		
		while(!removalQueue.isEmpty())
			combatants.remove(removalQueue.pop());
		
		if(combatants.isEmpty())
			battleEnded = true;
		
		notifyPlayers(false);
		
		status = BattleStatus.PLAYER_PHASE;
		
		phaseInProgress = false;
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
	
	private void notifyPlayers(boolean forceUpdate)
	{
		for(CombatantInfo combatant : combatants)
		{
			if(combatant.isPlayer)
				PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(true, forceUpdate, combatants.size()).makePacket(), (Player)Utility.getEntityByID(combatant.id));
		}
	}

	@Override
	public int compareTo(Battle other) {
		return battleID - other.battleID;
	}
}
