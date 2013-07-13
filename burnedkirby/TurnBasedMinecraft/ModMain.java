/**
 * 
 */

package burnedkirby.TurnBasedMinecraft;

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
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.common.network.NetworkMod.SidedPacketHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@NetworkMod(clientSideRequired=true,serverSideRequired=true,
channels = {CommandPacket.CHANNEL}, packetHandler = CommandPacketHandler.class)


@Mod(modid="mod_BurnedKirbyTurnBasedMinecraft",name="BurnedKirby's Turn-Based Minecraft",version="0.2.0")
public class ModMain {
	@Instance("BurnedKirbyTurnBasedBattleSystem")
	public static ModMain instance = new ModMain();
	
	@SidedProxy(clientSide = "burnedkirby.TurnBasedMinecraft.core.ClientProxy", serverSide = "burnedkirby.TurnBasedMinecraft.core.CommonProxy")
	public static CommonProxy proxy;
	
	public static BattleSystemServer bss = new BattleSystemServer();
	
	@Init
	public void initialize(FMLInitializationEvent event){
		MinecraftForge.EVENT_BUS.register(new BattleEventListener());
	}
	
}
