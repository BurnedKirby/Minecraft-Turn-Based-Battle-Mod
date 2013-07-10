package burnedkirby.TurnBasedBattleMod;

import java.io.Serializable;

public class CombatantInfo implements Comparable<CombatantInfo>, Serializable {
	
	public enum Type {
		DO_NOTHING, ATTACK, FLEE
	}
	
	public boolean isPlayer;
	public int id;
	public boolean isSideOne;
	
	public String name;

	public transient boolean ready;
	
	/**
	 * Status type:
	 * ** 0: player turn finished **
	 * ** 1: player queued attack **
	 * ** 2: player queued flee **
	 */
	public transient Type type;
	
	public transient int target;
	
	
	public CombatantInfo()
	{
		isPlayer = false;
		id = 0;
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
		isSideOne = true;
		name = "";
		ready = false;
		type = Type.DO_NOTHING;
		target = 0;
	}
	
	public CombatantInfo(boolean isPlayer, int id, boolean isSideOne, boolean ready, Type type, int target)
	{
		this.isPlayer = isPlayer;
		this.id = id;
		this.isSideOne = isSideOne;
		name = "";
		this.ready = ready;
		this.type = type;
		this.target = target;
	}

	@Override
	public int compareTo(CombatantInfo other) {
		return id - other.id;
	}
}
