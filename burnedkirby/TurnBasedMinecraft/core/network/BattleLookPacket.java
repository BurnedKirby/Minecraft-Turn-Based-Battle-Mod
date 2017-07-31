package burnedkirby.TurnBasedMinecraft.core.network;

import burnedkirby.TurnBasedMinecraft.ModMain;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

public class BattleLookPacket implements IMessage {
	
	private int battleID;
	private int playerID;
	private int targetID;
	
	public BattleLookPacket()
	{
	}
	
	public BattleLookPacket(int battleID, int playerID, int targetID)
	{
		this.battleID = battleID;
		this.playerID = playerID;
		this.targetID = targetID;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		battleID = buf.readInt();
		playerID = buf.readInt();
		targetID = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(battleID);
		buf.writeInt(playerID);
		buf.writeInt(targetID);
	}
	
	public static class Handler implements IMessageHandler<BattleLookPacket, IMessage>
	{
		@Override
		public IMessage onMessage(BattleLookPacket message,
				MessageContext ctx) {
					ModMain.bss.managePlayerLookAt(message.battleID, message.playerID, message.targetID);
			return null;
		}
	}
}
