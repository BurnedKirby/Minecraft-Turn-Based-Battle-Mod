package burnedkirby.TurnBasedBattleMod;

public class PlayerStatus {
	
	public boolean ready;
	
	/**
	 * Status type:
	 * ** 0: player turn finished **
	 * ** 1: player queued attack **
	 * ** 2: player queued flee **
	 */
	public int type;
	
	public int target;
	
	public PlayerStatus()
	{
		ready = false;
		type = 0;
		target = 0;
	}
	
	public PlayerStatus(boolean ready, int type, int target)
	{
		this.ready = ready;
		this.type = type;
		this.target = target;
	}
}
