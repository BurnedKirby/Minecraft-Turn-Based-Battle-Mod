package burnedkirby.TurnBasedBattleMod;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import burnedkirby.TurnBasedBattleMod.core.Utility;
import burnedkirby.TurnBasedBattleMod.core.network.BattlePhaseEndedPacket;
import burnedkirby.TurnBasedBattleMod.core.network.InitiateBattlePacket;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.MinecraftException;

public class Battle {
	private Vector<Integer> sideOne;
	private Vector<Integer> sideTwo;
	private int battleID;

	/**
	 * Mapping from entity ID to PlayerStatus, which holds information on
	 * if the player has finished their turn and what action they will take in battle.
	 */
	private Map<Integer,PlayerStatus> playerStatus;
	
	/**
	 * This is set to false when the battle ends.
	 * A battle ends when there are no more players in battle or if there are no
	 * more entities on one side of the battle.
	 */
	private boolean battleInProgress;
	
	/**
	 * Set to true briefly when calling EntityPlayer.attackTargetEntityWithCurrentItem() so
	 * that the BattleEventListener doesn't cancel the attack.
	 */
	protected static Boolean playerAttacking = false;
	
	public Battle(int battleID)
	{
		this.battleID = battleID;
		sideOne = new Vector<Integer>();
		sideTwo = new Vector<Integer>();
		playerStatus = new TreeMap<Integer,PlayerStatus>();
		
		battleInProgress = true;
	}
	
	/**
	 * Adds an entity to a battle's side.
	 * @param entityID The entity ID of the entity to add.
	 * @param sideOne If true, adds the entity to side One. If false, adds the entity to side Two.
	 */
	public void addCombatant(int entityID, boolean sideOne)
	{
		if(sideOne)
			this.sideOne.add(entityID);
		else
			this.sideTwo.add(entityID);

		EntityLiving combatant = null;
		
		combatant = (EntityLiving) Utility.getEntityByID(entityID);
		
		if(combatant == null)
		{
			System.out.println("Battle (" + battleID + "): Combatant not found!!"); //TODO debug
			return;
		}
		
		if(!(combatant instanceof EntityPlayer))
		{
			combatant.clearActivePotions(); //TODO keep track of potion effects before clearing them
//			combatant.addPotionEffect(new PotionEffect(2, 2000000, 127, false)); //Absolute stop
			combatant.addPotionEffect(new PotionEffect(2, 2000000, 100, false));
			System.out.println("Set potion effect slow on " + combatant.getEntityName()); //TODO debug
			//TODO possibly find a way to keep mobs persistent
		}
		else
		{
			playerStatus.put(entityID, new PlayerStatus());
			PacketDispatcher.sendPacketToPlayer(new InitiateBattlePacket(battleID).makePacket(), (Player)combatant);
		}
	}
	
	/**
	 * Removes a combatant from battle. This method is usually called during handling
	 * an entity's death event.
	 * @param entityID The entity ID of the entity to remove from battle.
	 * @param isPlayer True if the entity is a player.
	 */
	public void removeCombatant(int entityID, boolean isPlayer)
	{
		if(sideOne.contains(entityID))
			sideOne.removeElement(entityID);
		else if(sideTwo.contains(entityID))
			sideTwo.removeElement(entityID);
		else
			return;
		
		if(isPlayer)
		{
			playerStatus.remove(entityID);
		}
		
		if(playerStatus.isEmpty() || sideOne.isEmpty() || sideTwo.isEmpty())
			endBattle();
	}
	
	/**
	 * Returns the battle ID of this Battle
	 * @return The Battle ID.
	 */
	public int getID()
	{
		return battleID;
	}
	
	/**
	 * Sets the status of the battle to ended.
	 */
	public void endBattle()
	{
		battleInProgress = false;
	}
	
	/**
	 * Returns the combatants on side One of the Battle.
	 * @return A Vector<<Integer>> of entity IDs of entities in side One.
	 */
	public Vector<Integer> getSideOne()
	{
		return sideOne;
	}

	/**
	 * Returns the combatants on side Two of the Battle.
	 * @return A Vector<<Integer>> of entity IDs of entities in side Two.
	 */
	public Vector<Integer> getSideTwo()
	{
		return sideTwo;
	}
	
	/**
	 * Checks if battle is in progress.
	 * @return True if Battle is still in progress.
	 */
	public boolean isBattleInProgress()
	{
		return battleInProgress;
	}
	
	/**
	 * Updates the PlayerStaus with information on what the player will do on their turn.
	 * Also calls battlePhase() to enact the turn. battlePhase() checks if all players are ready.
	 * @param ID The entity ID of the player.
	 * @param type The type of action taken by the player.
	 * @param targetID The target entity ID by the action taken, if applicable.
	 */
	public void updatePlayer(int ID, int type, int targetID)
	{
		synchronized(playerStatus)
		{
			if(!playerStatus.containsKey(ID))
				return;
		}
		
		PlayerStatus ps = new PlayerStatus(true,type,targetID);

		synchronized(playerStatus)
		{
			playerStatus.put(ID, ps);
		}
		
		battlePhase();
	}
	
	/**
	 * Returns the list of players in this battle.
	 * @return A Collection<<Integer>> of entity IDs of players in battle.
	 */
	public Collection<Integer> getListOfPlayers()
	{
		return playerStatus.keySet();
	}
	
	/**
	 * Checks if all players are finished deciding their action for their turn.
	 * @return True if all players have decided their actions.
	 */
	private boolean allPlayersReady()
	{
		synchronized(playerStatus)
		{
			for(PlayerStatus ps : playerStatus.values())
				if(!ps.ready)
					return false;
		}
		return true;
	}
	
	/**
	 * If all players are ready and battle is in progress, this method enacts the turn
	 * of the battle based on the player actions and mobs in battle.
	 * After all the actions have taken place, the method notifies players that the
	 * turn has ended so that their GUIs can go to the main menu.
	 */
	public synchronized void battlePhase()
	{
		if(!allPlayersReady() || !battleInProgress)
			return;
		
		//TODO player and mob actions
		
		synchronized(playerStatus)
		{
			PlayerStatus ps = null;
			Set<Integer> keys = playerStatus.keySet();
			EntityPlayer player;
			Entity target;
			int damage;
			Collection<Integer> fleeing = new Vector<Integer>();
			
			//player attack phase
			for(Integer key : keys)
			{
				ps = playerStatus.get(key);
				player = (EntityPlayer) Utility.getEntityByID(key);
				target = Utility.getEntityByID(ps.target);
				if(target == null)
				{
					System.out.println("Player attempted to attack null entity!"); //TODO debug
					continue;
				}
				switch(ps.type)
				{
				case 1: //attack
					//TODO next line causes null pointer exception
					//damage = player.getItemInUse().getItem().getDamageVsEntity(target, player.getItemInUse());
					synchronized(playerAttacking){
						playerAttacking = true;
						System.out.println(player.getEntityName() + "is turn-based attacking" + target.getEntityName()); //TODO debug
						player.attackTargetEntityWithCurrentItem(target);
						playerAttacking = false;
					}
//					((EntityLiving)target).attackEntityFrom(DamageSource.causePlayerDamage(player), damage);
					break;
				case 2: //flee
					if(fleeCheck(key))
					{
						//TODO after fleeCheck has been finalized, do this
					}
					break;
				default:
					break;
				}
			}
			
			//end turn phase
			for(Integer key : keys)
			{
				ps = playerStatus.get(key);
				ps.ready = false;
				playerStatus.put(key, ps);
				PacketDispatcher.sendPacketToPlayer(new BattlePhaseEndedPacket().makePacket(), (Player)Utility.getEntityByID(key));
			}
		}
	}
	
	/**
	 * Returns if an entity can flee from battle based on the sizes of each side.
	 * @param entityID The entity ID of the entity that wishes to flee.
	 * @return True if fleeing is successful.
	 */
	private boolean fleeCheck(int entityID)
	{
		boolean sideOne = this.sideOne.contains(entityID);
		int enemySide = sideOne ? sideTwo.size() : this.sideOne.size();
		int ownSide = sideOne ? this.sideOne.size() : sideTwo.size();
		int diff;
		double rand;
		
		if(enemySide == ownSide)
		{
			return BattleSystemServer.random.nextBoolean();
		}
		else
		{
			diff = Math.abs(ownSide - enemySide);
			rand = BattleSystemServer.random.nextDouble() / Math.log((double)(diff + Math.E)) ;
			System.out.println("FleeCheck: Enemy side larger, rand is " + rand); //TODO debug
			return ownSide > enemySide ? rand <= 0.67d : rand > 0.67d;
		}
	}
}
