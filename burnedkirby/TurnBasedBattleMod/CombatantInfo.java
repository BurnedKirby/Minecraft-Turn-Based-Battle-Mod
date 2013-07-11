package burnedkirby.TurnBasedBattleMod;

import java.io.Serializable;
import java.util.Comparator;

public class CombatantInfo{
	
	public enum Type {
		DO_NOTHING, ATTACK, FLEE
	}
	
	public boolean isPlayer;
	public int id;
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
	
	public CombatantInfo(boolean isPlayer, int id, boolean isSideOne, String name, boolean ready, Type type, int target)
	{
		this.isPlayer = isPlayer;
		this.id = id;
		this.isSideOne = isSideOne;
		this.name = name;
		this.ready = ready;
		this.type = type;
		this.target = target;
	}
	
	@Override
	public boolean equals(Object obj) {
		return (obj instanceof CombatantInfo ? id == ((CombatantInfo)obj).id : false);
	}
}
