package burnedkirby.TurnBasedMinecraft.core.network;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

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
		byte[] data;
		try {
			data = message.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		
		buffer.writeBytes(data);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		byte[] data = null;
		buffer.readBytes(data);
		try {
			message = new String(data, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
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
