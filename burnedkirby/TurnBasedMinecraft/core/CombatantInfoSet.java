package burnedkirby.TurnBasedMinecraft.core;

import java.util.TreeSet;

import burnedkirby.TurnBasedMinecraft.CombatantInfo;

public class CombatantInfoSet extends TreeSet<CombatantInfo> {
	
	/**
	 * Checks if this set contains a CombatantInfo by its id.
	 * @param id The id to search for.
	 * @return True if the CombatantInfo was found.
	 */
	public boolean contains(int id) {
		CombatantInfo comparingInfo = new CombatantInfo(id);
		return contains(comparingInfo);
	}
}
