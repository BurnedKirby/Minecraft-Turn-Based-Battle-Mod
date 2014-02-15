package burnedkirby.TurnBasedMinecraft.core.network;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Special thanks to Sirgingalot for his netty tutorial
 * from which most of this network code for 1.7.2 and onwards
 * is from.
 *
 */

public abstract class AbstractPacket {

	public abstract void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer);
	
	public abstract void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer);
	
	public abstract void handleClientSide(EntityPlayer player);
	
	public abstract void handleServerSide(EntityPlayer player);
	
	protected void encodeUTF(String msg, ByteBuf buffer) throws UnsupportedEncodingException
	{
		buffer.writeInt(msg.length());
		buffer.writeBytes(msg.getBytes("UTF-8"));
	}
	
	protected String decodeUTF(ByteBuf buffer) throws UnsupportedEncodingException
	{
		int length = buffer.readInt();
		byte[] data = new byte[length];
		buffer.readBytes(length).readBytes(data);
		return new String(data, "UTF-8");
	}
}
