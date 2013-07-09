/**
 * 
 */

package burnedkirby.TurnBasedBattleMod;

import burnedkirby.TurnBasedBattleMod.core.CommandPacketHandler;
import burnedkirby.TurnBasedBattleMod.core.CommandPacket;
import burnedkirby.TurnBasedBattleMod.core.CommonProxy;
import burnedkirby.TurnBasedBattleMod.gui.BattleGui;
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
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
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


@Mod(modid="BurnedKirbyTurnBasedBattleSystem",name="BurnedKirby's Turn-Based Battle System",version="0.1")
public class ModMain {
	@Instance("BurnedKirbyTurnBasedBattleSystem")
	public static ModMain instance = new ModMain();
	
	@SidedProxy(clientSide = "burnedkirby.TurnBasedBattleMod.core.ClientProxy", serverSide = "burnedkirby.TurnBasedBattleMod.core.CommonProxy")
	public static CommonProxy proxy;
	
	public static BattleSystemServer bss = new BattleSystemServer();
	
	//Unique GUI per client. (null version may exist server side)
	public static BattleGui bg = null;

	@Init
	public void initialize(FMLInitializationEvent event){
		MinecraftForge.EVENT_BUS.register(new BattleEventListener());
	}
	
}
