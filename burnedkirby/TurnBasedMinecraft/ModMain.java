/**
 * 
 */

package burnedkirby.TurnBasedMinecraft;

import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import burnedkirby.TurnBasedMinecraft.core.CommonProxy;
import burnedkirby.TurnBasedMinecraft.core.network.BattleCombatantPacket;
import burnedkirby.TurnBasedMinecraft.core.network.BattleCommandPacket;
import burnedkirby.TurnBasedMinecraft.core.network.BattleMessagePacket;
import burnedkirby.TurnBasedMinecraft.core.network.BattleQueryPacket;
import burnedkirby.TurnBasedMinecraft.core.network.BattleStatusPacket;
import burnedkirby.TurnBasedMinecraft.core.network.CombatantHealthPacket;
import burnedkirby.TurnBasedMinecraft.core.network.InitiateBattlePacket;


@Mod(modid=ModMain.modid,name="BurnedKirby's Turn-Based Minecraft",version=ModMain.versionNumber)
public class ModMain {
	
	public static final String modid = "mod_burnedkirby_turnbasedminecraft";
	
	public static final String versionNumber = "0.4.1_1.7.10_a";
	
	@Instance("BurnedKirbyTurnBasedBattleSystem")
	public static ModMain instance = new ModMain();
	
	@SidedProxy(clientSide = "burnedkirby.TurnBasedMinecraft.core.ClientProxy", serverSide = "burnedkirby.TurnBasedMinecraft.core.CommonProxy")
	public static CommonProxy proxy;
	
	public static BattleSystemServer bss = new BattleSystemServer();

	public static final String modFolder = "./TurnBasedMinecraft";
	
	public static final String battleSettingsFile = modFolder + "/battleSettings.xml";
	
	public static SimpleNetworkWrapper network;
	
	@EventHandler
	public void initialize(FMLInitializationEvent event){
		MinecraftForge.EVENT_BUS.register(new BattleEventListener());
		
		proxy.initializeSettings();
		proxy.initializeMusicManager();
		
		network = NetworkRegistry.INSTANCE.newSimpleChannel("BK_TBM_Channel");

		network.registerMessage(BattleCombatantPacket.Handler.class, BattleCombatantPacket.class, 0, Side.CLIENT);
		network.registerMessage(BattleCommandPacket.Handler.class, BattleCommandPacket.class, 1, Side.SERVER);
		network.registerMessage(BattleMessagePacket.Handler.class, BattleMessagePacket.class, 2, Side.CLIENT);
		network.registerMessage(BattleQueryPacket.Handler.class, BattleQueryPacket.class, 3, Side.SERVER);
		network.registerMessage(BattleStatusPacket.Handler.class, BattleStatusPacket.class, 4, Side.CLIENT);
		network.registerMessage(CombatantHealthPacket.Handler.class, CombatantHealthPacket.class, 5, Side.CLIENT);
		network.registerMessage(InitiateBattlePacket.Handler.class, InitiateBattlePacket.class, 6, Side.CLIENT);
	}
	
	@EventHandler
	public void initialize(FMLPostInitializationEvent event){
	}
	
	@EventHandler
	public void getServerInstance(FMLServerAboutToStartEvent event){
		bss.setServerInstance(event.getServer());
	}
	
	@EventHandler
	public void cleanup(FMLServerStoppingEvent event){
		proxy.cleanup();
	}
}
