package burnedkirby.TurnBasedMinecraft.core;

/**
 * Utility class for commonly used methods.
 *
 */
public class Utility {
	static public float PITCH_OFFSET = 5.0f;
	
	static public void log(String log)
	{
		System.out.println("[BK_TBM] " + log);
	}
	
	static public float yawDirection(double posX, double posZ, double targetX, double targetZ)
	{
		double radians = Math.atan2(targetZ - posZ, targetX - posX);
		radians = (radians - Math.PI / 2.0);
		if(radians < 0.0)
		{
			radians += Math.PI * 2.0;
		}
		return (float)(radians * 180.0 / Math.PI);
	}
	
	static public float pitchDirection(double posX, double posY, double posZ,
			double targetX, double targetY, double targetZ)
	{
		double xDiff = targetX - posX;
		double yDiff = targetY - posY;
		double zDiff = targetZ - posZ;
		double distance = Math.sqrt(xDiff*xDiff + zDiff*zDiff);
		if(Math.abs(yDiff) < 0.1)
		{
			return 0;
		}
		else
		{
			return (float)(-Math.atan(yDiff / distance) * 180 / Math.PI);
		}
	}
}
