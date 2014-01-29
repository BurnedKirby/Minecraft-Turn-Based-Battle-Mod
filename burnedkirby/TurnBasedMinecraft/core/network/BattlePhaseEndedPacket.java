package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;

/**
 * This packet sent from server to player notifies the player that a turn has
 * ended and the Battle GUI can now return to the main menu.
 *
 */
public class BattlePhaseEndedPacket extends AbstractPacket {

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		// TODO Auto-generated method stub
		
	}

/*	@Override
	public void write(ByteArrayDataOutput out) {
		// TODO Auto-generated method stub

	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException {
		// TODO Auto-generated method stub

	}

	@Override
	public void execute(EntityPlayer player, Side side)
			throws ProtocolException {
		if(side.isServer())
		{
			
		}
		else
		{
//			ModMain.bg.updateTurnEnd(true);
		}
	}*/

}
