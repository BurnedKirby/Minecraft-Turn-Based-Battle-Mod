package burnedkirby.TurnBasedMinecraft.core;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;

/**
 * Utility class for commonly used methods.
 *
 */
public class Utility {
	static public Entity getEntityByID(int id)
	{
		if(FMLCommonHandler.instance().getEffectiveSide().isClient())
			return null;
		
		Entity entity;
		for(int i=0; i<MinecraftServer.getServer().worldServers.length; i++)
		{
			if((entity = MinecraftServer.getServer().worldServers[i].getEntityByID(id)) != null)
				return entity;
		}
		return null;
	}
	
	static public void log(String log)
	{
		System.out.println("[BK_TBM] " + log);
	}
}
