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
	private Set<Battle> battles;
	

	protected static int battleIDCounter = 0; //TODO maybe split this per world
	
	protected static Random random;
	
	public BattleSystemServer()
	{
		battles = new TreeSet<Battle>();
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
	private boolean createNewBattle(int ID, int[] sideOne, int[] sideTwo)
	{
		return false;
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
		
		
		
		switch(inBattle)
		{
		case 0x0:
			if(!(entityAttacker instanceof EntityPlayer) && !(entityAttacked instanceof EntityPlayer))
				return false;
			
			Stack<CombatantInfo> combatants = new Stack<CombatantInfo>();
			combatants.push(new CombatantInfo(entityAttacker instanceof EntityPlayer, entityAttacker.entityId, true, false, Type.DO_NOTHING, entityAttacker.getAttackTarget() != null ? entityAttacker.getAttackTarget().entityId : 0));
			combatants.push(new CombatantInfo(entityAttacked instanceof EntityPlayer, entityAttacked.entityId, false, false, Type.DO_NOTHING, entityAttacked.getAttackTarget() != null ? entityAttacked.getAttackTarget().entityId : 0));
			battles.add(new Battle(battleIDCounter++, combatants));
			return true;
		case 0x1:
		case 0x2:
			EntityLiving newCombatant = (inBattle == 0x1 ? entityAttacked : entityAttacker);
			EntityLiving inBattleCombatant = (inBattle == 0x1 ? entityAttacker : entityAttacked);
			
			Battle battleToJoin;
			boolean isSideOne = !((battleToJoin = findBattleByCombatant(new CombatantInfo(inBattleCombatant.entityId))).getCombatant(new CombatantInfo(inBattleCombatant.entityId)).isSideOne);
			
			battleToJoin.addCombatant(new CombatantInfo(newCombatant instanceof EntityPlayer, newCombatant.entityId, isSideOne, false, Type.DO_NOTHING, newCombatant.getAttackTarget() != null ? newCombatant.getAttackTarget().entityId : 0));
			
			return true;
		case 0x3:
			return true; //TODO
		default:
			return true;
		}
	}
	
	/**
	 * Checks if the entity is currently in battle.
	 * @param entityID The entity that is checked.
	 * @return True if the entity is in battle.
	 */
	private boolean isInBattle(int entityID)
	{
		for(Battle b : battles)
		{
			if(b.isInBattle(entityID))
				return true;
		}
		return false;
	}
	
	private Battle findBattleByCombatant(CombatantInfo combatant)
	{
		for(Battle b : battles)
		{
			if(b.isInBattle(combatant))
				return b;
		}
		return null;
	}
}
