package burnedkirby.TurnBasedMinecraft;

import net.minecraft.entity.EntityLivingBase;

public class CombatantInfo implements Comparable{
	
	public enum Type {
		DO_NOTHING, ATTACK, FLEE, CHANGE_WEAPON, DODGE_COUNTER
	}
	
	public boolean isPlayer;
	public int id;
	public EntityLivingBase entityReference;
	public boolean isSideOne;
	
	public String name;

	public boolean ready;
	
	public Type type;
	
	public int target;
	
	public float health;
	
	public short turnTickTimer;
	
	/**
	 * All values are percentages.
	 * 
	 * evasionRate: default evasion rate for a combatant.
	 * criticalRate: default critical hit rate for a combatant.
	 * onDodgeEvasionRate: evasion rate when dodging an attack from a combatant that wasn't selected.
	 * onCorrectDodgeEvasionRate: evasion rate when dodging an attack from a combatant that was selected.
	 * counterRateAfterHit: counter-attack rate after being hit by a selected combatant.
	 * counterRateAfterMiss: counter-attack rate after dodging an attack by a selected combatant.
	 * onCorrectDodgeHitBonus: hit bonus that increases the chances of hitting a combatant on the next turn.
	 * onCorrectDodgeCriticalBonus: critical bonus that increases the chances of a critical hit on a combatant on the next turn.
	 */
	
	public static float evasionRate = 0.12f;
	
	public static float criticalRate = 0.08f;
	
	public static float onDodgeEvasionRate = 0.09f;
	
	public static float onCorrectDodgeEvasionRate = 0.35f;
	
	public static float counterRateAfterHit = 0.3f;
	
	public static float counterRateAfterMiss = 0.7f;
	
	public static float onCorrectDodgeHitBonus = 0.1f;
	
	public static float onCorrectDodgeCriticalBonus = 0.1f;
	
	public static int maxParticipantsInBattle = 0;
	
	public float evasionBonus = 0.0f;
	
	public float criticalBonus = 0.0f;
	
	public float hitBonus = 0.0f;
	
	public float counterBonus = 0.0f;
	
	public boolean counterSelectionSuccess = false;
	
	public CombatantInfo()
	{
		isPlayer = false;
		id = 0;
		entityReference = null;
		isSideOne = true;
		name = "";
		ready = false;
		type = Type.DO_NOTHING;
		target = 0;
	}
	
	public CombatantInfo(int id)
	{
		this.id = id;
		
		isPlayer = false;
		entityReference = null;
		isSideOne = true;
		name = "";
		ready = false;
		type = Type.DO_NOTHING;
		target = 0;
	}
	
	public CombatantInfo(boolean isPlayer, int id, EntityLivingBase reference, boolean isSideOne, String name, boolean ready, Type type, int target)
	{
		this.isPlayer = isPlayer;
		this.id = id;
		entityReference = reference;
		this.isSideOne = isSideOne;
		this.name = name;
		this.ready = ready;
		this.type = type;
		this.target = target;
	}
	
	public boolean updateBattleInformation(CombatantInfo newInfo)
	{
		if(id != newInfo.id)
			return false;
		
		ready = newInfo.ready;
		target = newInfo.target;
		type = newInfo.type;
		return true;
	}
	
	public void setHealth(float health)
	{
		this.health = health;
	}
	
	public void setTarget(int target)
	{
		this.target = target;
	}
	
	public void setTurnTickTimer(short tick)
	{
		turnTickTimer = tick;
	}
	
	public void removeEntityReference()
	{
		entityReference = null;
	}
	
	public short decrementTimer()
	{
		return --turnTickTimer;
	}
	
	public void resetBonuses()
	{
		evasionBonus = 0.0f;
		criticalBonus = 0.0f;
		hitBonus = 0.0f;
		counterBonus = 0.0f;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof CombatantInfo ? id == ((CombatantInfo)obj).id : false);
	}

	@Override
	public int compareTo(Object other) {
		return id - ((CombatantInfo)other).id;
	}
}
