package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.UnsupportedEncodingException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

public class BattleMessagePacket extends AbstractPacket {
	
	String message;
	
	public BattleMessagePacket() {message = "";}
	
	public BattleMessagePacket(String message)
	{
		this.message = message;
	}

/*	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeUTF(message);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException {
		message = in.readUTF();
	}

	@Override
	public void execute(EntityPlayer player, Side side)
			throws ProtocolException {
		if(side.isServer())
		{}
		else
		{
			Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(message);
		}
	}*/

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		try {
			encodeUTF(message, buffer);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		try {
			message = decodeUTF(buffer);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		player.func_146105_b(new ChatComponentText(message));
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		
	}

}
