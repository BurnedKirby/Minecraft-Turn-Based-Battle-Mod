package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import burnedkirby.TurnBasedMinecraft.ModMain;

/**
 * Packet sent by player in battle to server that is a query for battle status.
 *
 */
public class BattleQueryPacket implements IMessage {
	
	int battleID;
	short type;
	
	public BattleQueryPacket(int battleID, short type)
	{
		this.battleID = battleID;
		this.type = type;
	}
	
	public BattleQueryPacket() {}

	@Override
	public void fromBytes(ByteBuf buf) {
		battleID = buf.readInt();
		type = buf.readShort();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(battleID);
		buf.writeShort(type);
	}
	
	public static class Handler implements IMessageHandler<BattleQueryPacket, IMessage>
	{
		@Override
		public IMessage onMessage(BattleQueryPacket message, MessageContext ctx) {
//			final BattleQueryPacket mes = message;
//			final MessageContext context = ctx;
//			IThreadListener mainThread = (WorldServer) ctx.getServerHandler().player.world;
//			mainThread.addScheduledTask(new Runnable() {
//				@Override
//				public void run() {
					ModMain.bss.manageQuery(message.battleID, message.type, ctx.getServerHandler().playerEntity);
//				}
//			});
			return null;
		}
	}
}
