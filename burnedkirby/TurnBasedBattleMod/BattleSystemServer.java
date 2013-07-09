package burnedkirby.TurnBasedBattleMod;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import burnedkirby.TurnBasedBattleMod.core.BattleStatusPacket;
import burnedkirby.TurnBasedBattleMod.core.Utility;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.MinecraftException;

public class BattleSystemServer {
	/**
	 * battles data-structure: BattleID to Battle 
	 */
	private Map<Integer,Battle> battles;
	
	/**
	 * Separate collection of entityIDs of entities in battle
	 */
	private Set<Integer> inBattle;
	
	protected static Random random;
	
	public BattleSystemServer()
	{
		battles = new TreeMap<Integer,Battle>();
		inBattle = new TreeSet<Integer>();
		random = new Random(Minecraft.getMinecraft().getSystemTime());
	}
	
	/**
	 * Creates a new battle.
	 * If a battle already exists with the ID given, it will cycle the battleIDCounter
	 * until a valid ID is found.
	 * 
	 * @param ID The battleID for the new Battle to use.
	 * @param sideOne Entities (as entityID) to place in side one of the Battle.
	 * @param sideTwo Entities (as entityID) to place in side two of the Battle.
	 * @return true if successful in creating a new battle.
	 * @throws MinecraftException
	 */
	public boolean createNewBattle(int ID, int[] sideOne, int[] sideTwo)
	{
		System.out.println("CreateNewBattle called with sides sizes ("+sideOne.length+","+sideTwo.length+")");
		
//		for(int combatant : sideOne)
//			if(isInBattle(combatant))
//				return false;
//		for(int combatant : sideTwo)
//			if(isInBattle(combatant))
//				return false;
		
//		if(battles.containsKey(Integer.valueOf(ID)))
//			return false;
		while(battles.containsKey(Integer.valueOf(ID)))
		{
			ID = BattleEventListener.battleIDCounter++;
		}
		
		Battle newBattle = new Battle(ID);
		battles.put(Integer.valueOf(ID), newBattle);
		System.out.println("createNewBattle: battle created with ID " + ID); //TODO debug
		
		//TODO rewrite this part to avoid reuse of isInBattle method
		for(int combatant : sideOne)
			addCombatant(ID, combatant, true);
		for(int combatant : sideTwo)
			addCombatant(ID, combatant, false);
		return true;
	}
	
	/**
	 * Called by the BattleEventListener, this class handles the creation of battles and placement
	 * of combatants.
	 * 
	 * When both entities are already in battle, the method returns false.
	 * 
	 * When one entity is in battle, this method finds the Battle that one entity is in
	 * and calls the appropriate method to place the entity in that Battle.
	 * 
	 * When both entities are not in battle, the method attempts to create a new battle and
	 * place the attacker/attackee in it unless both entities are not players.
	 * 
	 * @param entityAttacker The attacking entity.
	 * @param entityAttacked The attacked entity.
	 * @return false if there are no new additions to a battle.
	 * @throws MinecraftException
	 */
	public boolean manageCombatants(Entity entityAttacker, Entity entityAttacked)
	{
		short inBattle = 0x0;
		inBattle |= isInBattle(entityAttacker.entityId) ? 0x1 : 0x0;
		inBattle |= isInBattle(entityAttacked.entityId) ? 0x2 : 0x0;
		
		if(inBattle == 0x3)
			return false;
		
		if(inBattle == 0x0)
		{
			if(!(entityAttacker instanceof EntityPlayer) && !(entityAttacked instanceof EntityPlayer))
				return false;
			
			int[] sideOne = {entityAttacker.entityId};
			int[] sideTwo = {entityAttacked.entityId};
			return createNewBattle(BattleEventListener.battleIDCounter++,sideOne,sideTwo);
		}
		
		int inBattleEntity = inBattle == 0x1 ? entityAttacker.entityId : entityAttacked.entityId;
		int newCombatant = inBattle == 0x1 ? entityAttacked.entityId : entityAttacker.entityId;
		
		Integer battleID;
		if((battleID = getBattleID(inBattleEntity)) == null)
			return false;
		
		boolean sideOne = !getBattleSide(battleID, true).contains(inBattleEntity);
		
		return addCombatant(battleID, newCombatant, sideOne);
	}
	
	/**
	 * This method places a combatant into a Battle using entity ID, Battle ID, and a boolean
	 * that determines which side the combatant joins.
	 * @param ID The battle ID.
	 * @param combatant The combatant to add to battle.
	 * @param sideOne True if the combatant is to be added to side One.
	 * @return True if the combatant was added to a battle.
	 * @throws MinecraftException
	 */
	private boolean addCombatant(int ID, int combatant, boolean sideOne)
	{
		if(isInBattle(combatant))
		{
			System.out.println("addCombatant: Combatant already in battle!"); //TODO debug
			return false;
		}
		if(!battles.containsKey(Integer.valueOf(ID)))
		{
			System.out.println("addCombatant: Battle " + ID + " doesn't exist!"); //TODO debug
			return false;
		}
		
		battles.get(Integer.valueOf(ID)).addCombatant(combatant, sideOne);
		this.inBattle.add(Integer.valueOf(combatant));
		System.out.println("addCombatant: entity " + combatant + " has entered battle.");
		return true;
	}
	
	/**
	 * Checks if the entity is currently in battle.
	 * @param entityID The entity that is checked.
	 * @return True if the entity is in battle.
	 */
	public boolean isInBattle(int entityID)
	{
		return inBattle.contains(Integer.valueOf(entityID));
	}
	
	/**
	 * Returns the battle based on battle ID.
	 * @param battleID The id of the battle to find.
	 * @return The Battle if found, null otherwise.
	 */
	public Battle getBattle(int battleID)
	{
		return battles.get(battleID);
	}
	
	/**
	 * Returns the battle based on a combatant's ID.
	 * @param entityID The ID of an entity to search for.
	 * @return The Battle that has the combatant. null otherwise.
	 */
	public Battle getBattleByCombatant(int entityID)
	{
		for(Battle b : battles.values())
		{
			if(b.getSideOne().contains(entityID) || b.getSideTwo().contains(entityID))
			{
				return b;
			}
		}
		return null;
	}
	
	/**
	 * Returns one side of the battle as a Vector<<Integer>> of entity IDs.
	 * @param battleID The battleID of the Battle to return a side from.
	 * @param sideOne Set to true to get side One, false to return side Two
	 * @return A Vector<<Integer>> containing entity IDs of one side of the Battle.
	 */
	public Vector<Integer> getBattleSide(int battleID, boolean sideOne)
	{
		if(battles.get(Integer.valueOf(battleID)) == null)
			return null;
		
		if(sideOne)
			return battles.get(Integer.valueOf(battleID)).getSideOne();
		else
			return battles.get(Integer.valueOf(battleID)).getSideTwo();
	}
	
	/**
	 * Returns the battleID of the Battle that the given entity is in.
	 * @param entityInBattle the Entity ID of the entity used to find the Battle.
	 * @return The Battle that has the given entity.
	 */
	public Integer getBattleID(int entityInBattle)
	{
		Integer id = null;
		for(Battle b : battles.values())
		{
			if(b.getSideOne().contains(entityInBattle) || b.getSideTwo().contains(entityInBattle))
			{
				id = b.getID();
				break;
			}
		}
		return id;
	}
	
	/**
	 * Checks to see if there exists a Battle with this battle ID.
	 * @param battleID The battle ID to check for.
	 * @return True if a Battle exists with this ID.
	 */
	public boolean battleExists(int battleID)
	{
		return battles.containsKey(battleID);
	}
	
//	public void removeBatte(int battleID)
//	{
//		if(!battles.get(battleID).isBattleInProgress())
//			battles.remove(battleID);
//	}
	
	/**
	 * Called by the BattleEventListener on combatant's death, this method removes the
	 * entity that died from a battle if it was in one, and checks to see if the battle
	 * has ended. If it has, removes the entities remaining in battle from the inBattle collection,
	 * removes the Battle from the map of Battles, and notifies all players that were in
	 * the battle that the battle has ended.
	 * @param entity The entity that died.
	 */
	public void combatantDeath(Entity entity)
	{
		if(isInBattle(entity.entityId))
		{
			Battle battle = getBattleByCombatant(entity.entityId);
			battle.removeCombatant(entity.entityId, entity instanceof EntityPlayer);
			inBattle.remove(entity.entityId);

			Entity playerEntity = null;
			if(battle.isBattleInProgress())
			{
				for(Integer player : battle.getListOfPlayers())
				{
					playerEntity = Utility.getEntityByID(player);
					if(playerEntity == null)
						continue;
					PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(true,battle.getSideOne().size(),battle.getSideTwo().size()).makePacket(), (Player)playerEntity);
				}
			}
			else
			{
				for(Integer sideOneEntity : battle.getSideOne())
				{
					inBattle.remove(sideOneEntity);
				}
				for(Integer sideTwoEntity : battle.getSideTwo())
				{
					inBattle.remove(sideTwoEntity);
				}
				for(Integer player : battle.getListOfPlayers())
				{
					playerEntity = Utility.getEntityByID(player);
					if(playerEntity == null)
						continue;
					PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(false,0,0).makePacket(), (Player)playerEntity);
				}
				battles.remove(battle.getID());
				System.out.println("Battle " + battle.getID() + " has been removed."); //TODO debug
			}
		}
	}

//	
//	public void newBattleSize(int ID, int sideOne, int sideTwo)
//	{
//		Integer[] sizeArray = {Integer.valueOf(sideOne), Integer.valueOf(sideTwo)};
//		newBattleSize.put(Integer.valueOf(ID), sizeArray);
//	}
//	
//	public int createNewBattle(int ID, int[] sideOne, int[] sideTwo)
//	{
//		Integer[] sizeArray = newBattleSize.get(Integer.valueOf(ID));
//		if(sizeArray[0] != Integer.valueOf(sideOne.length) || sizeArray[0] != Integer.valueOf(sideTwo.length))
//			return -1;
//		
//		for(int entityID : sideOne)
//			if(inBattle.contains(entityID))
//				return -2;
//		
//		for(int entityID : sideTwo)
//			if(inBattle.contains(entityID))
//				return -2;
//		
//		while(battles.containsKey(battleID))
//			incrementBattleID();
//		
//		Battle newBattle = new Battle(sideOne,sideTwo,battleID);
//		
//		for(int entityID : sideOne)
//			inBattle.add(Integer.valueOf(entityID));
//		for(int entityID : sideTwo)
//			inBattle.add(Integer.valueOf(entityID));
//		
//		battles.put(Integer.valueOf(battleID), newBattle);
//		
//		return incrementBattleID();
//	}
//	
//	public void endBattle(int ID)
//	{
//		if(!battles.containsKey(ID))
//			return;
//
//		//Remove ids from inBattle list	
//		for(int combatant : battles.get(Integer.valueOf(ID)).getSideOne())
//			inBattle.remove(Integer.valueOf(combatant));
//		for(int combatant : battles.get(Integer.valueOf(ID)).getSideTwo())
//			inBattle.remove(Integer.valueOf(combatant));
//		
//		//Remove battle from battles list
//		battles.remove(ID);
//	}
}
