package burnedkirby.TurnBasedMinecraft;

import net.minecraft.entity.EntityLivingBase;

public class CombatantInfo implements Comparable{
	
	public enum Type {
		DO_NOTHING, ATTACK, FLEE, CHANGE_ITEM
	}
	
	public boolean isPlayer;
	public int id;
	public EntityLivingBase entityReference;
	public boolean isSideOne;
	
	public String name;

	public boolean ready;
	
	/**
	 * Status type:
	 * ** 0: player turn finished **
	 * ** 1: player queued attack **
	 * ** 2: player queued flee **
	 */
	public Type type;
	
	public int target;
	
	public float health;
	
	
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
	
	public void removeEntityReference()
	{
		entityReference = null;
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
