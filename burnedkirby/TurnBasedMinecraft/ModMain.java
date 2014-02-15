/**
 * 
 */

package burnedkirby.TurnBasedMinecraft;

import java.util.Iterator;

import net.minecraftforge.common.MinecraftForge;
import burnedkirby.TurnBasedMinecraft.core.CommonProxy;
import burnedkirby.TurnBasedMinecraft.core.network.PacketPipeline;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;


@Mod(modid=ModMain.modid,name="BurnedKirby's Turn-Based Minecraft",version=ModMain.versionNumber)
public class ModMain {
	
	public static final String modid = "mod_BurnedKirbyTurnBasedMinecraft";
	
	public static final String versionNumber = "0.3.0";
	
	@Instance("BurnedKirbyTurnBasedBattleSystem")
	public static ModMain instance = new ModMain();
	
	@SidedProxy(clientSide = "burnedkirby.TurnBasedMinecraft.core.ClientProxy", serverSide = "burnedkirby.TurnBasedMinecraft.core.CommonProxy")
	public static CommonProxy proxy;
	
	public static BattleSystemServer bss = new BattleSystemServer();

	public static final String modFolder = "./TurnBasedMinecraft";
	
	public static final String battleSettingsFile = modFolder + "/battleSettings.xml";
	
	public static PacketPipeline pp = new PacketPipeline();
	
	@EventHandler
	public void initialize(FMLInitializationEvent event){
		MinecraftForge.EVENT_BUS.register(new BattleEventListener());
		
		proxy.initializeSettings();
		proxy.initializeMusicManager();
		
		pp.initialize();
	}
	
	@EventHandler
	public void initialize(FMLPostInitializationEvent event){
		pp.postInitialize();
	}
}
