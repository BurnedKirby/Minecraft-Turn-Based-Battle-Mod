/**
 * 
 */

package burnedkirby.TurnBasedMinecraft;

import java.util.Iterator;

import burnedkirby.TurnBasedMinecraft.core.CommonProxy;
import burnedkirby.TurnBasedMinecraft.core.network.CommandPacket;
import burnedkirby.TurnBasedMinecraft.core.network.CommandPacketHandler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLModContainer;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


@Mod(modid="mod_BurnedKirbyTurnBasedMinecraft",name="BurnedKirby's Turn-Based Minecraft",version="0.3.0")
public class ModMain {
	
	public static String versionNumber;
	
	@Instance("BurnedKirbyTurnBasedBattleSystem")
	public static ModMain instance = new ModMain();
	
	@SidedProxy(clientSide = "burnedkirby.TurnBasedMinecraft.core.ClientProxy", serverSide = "burnedkirby.TurnBasedMinecraft.core.CommonProxy")
	public static CommonProxy proxy;
	
	public static BattleSystemServer bss = new BattleSystemServer();

	public static final String modFolder = "./TurnBasedMinecraft";
	
	public static final String battleSettingsFile = modFolder + "/battleSettings.xml";
	
	@EventHandler
	public void initialize(FMLInitializationEvent event){
		MinecraftForge.EVENT_BUS.register(new BattleEventListener());

		Iterator<ModContainer> iter = Loader.instance().getModList().iterator();
		while(iter.hasNext())
		{
			ModContainer mod = iter.next();
			if(mod.getModId().equals("mod_BurnedKirbyTurnBasedMinecraft"))
			{
				versionNumber = mod.getVersion();
				break;
			}
		}
		
		proxy.initializeSettings();
		proxy.initializeMusicManager();
	}
}
