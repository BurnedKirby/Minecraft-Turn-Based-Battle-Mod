package burnedkirby.TurnBasedBattleMod.core.network;

import java.util.logging.Logger;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import burnedkirby.TurnBasedBattleMod.core.network.CommandPacket.ProtocolException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class CommandPacketHandler implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {

		try {
			EntityPlayer entityPlayer = (EntityPlayer)player;
			ByteArrayDataInput in = ByteStreams.newDataInput(packet.data);
			int packetId = in.readUnsignedByte();
			CommandPacket commandPacket = CommandPacket.constructPacket(packetId);
			commandPacket.read(in);
			commandPacket.execute(entityPlayer, entityPlayer.worldObj.isRemote ? Side.CLIENT : Side.SERVER);
			
		} catch (ProtocolException e) {
			if (player instanceof EntityPlayerMP) {
				((EntityPlayerMP) player).playerNetServerHandler.kickPlayerFromServer("Protocol Exception!");
				Logger.getLogger("BKTBBS").warning("Player " + ((EntityPlayer)player).username + " caused a Protocol Exception!");
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException("Unexpected Reflection exception during Packet construction!", e);
		}
	}

}
