package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.text.TextComponentString;
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
			final BattleMessagePacket mes = message;
			//final MessageContext context = ctx;
			IThreadListener mainThread = Minecraft.getMinecraft();
			mainThread.addScheduledTask(new Runnable() {
				@Override
				public void run() {
					Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(mes.message));
				}
			});
			return null;
		}
	}
}
