package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class BattleMessagePacket implements IMessage {
	
	String message;
	
	public BattleMessagePacket() {message = "";}
	
	public BattleMessagePacket(String message)
	{
		this.message = message;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		message = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, message);
	}

	public static class Handler implements IMessageHandler<BattleMessagePacket, IMessage>
	{
		@Override
		public IMessage onMessage(BattleMessagePacket message,
				MessageContext ctx) {
			ctx.getServerHandler().playerEntity.addChatComponentMessage(new ChatComponentText(message.message));
			return null;
		}
	}
}
