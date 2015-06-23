package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import burnedkirby.TurnBasedMinecraft.CombatantInfo;
import burnedkirby.TurnBasedMinecraft.ModMain;

/**
 * Packet sent to player from the server which notifies the player of
 * entering battle and brings up the BattleGUI.
 */
public class InitiateBattlePacket implements IMessage {
	
	int battleID;
	CombatantInfo player;
	boolean silly;
	
	public InitiateBattlePacket(int battleID, CombatantInfo player, boolean silly)
	{
		this.battleID = battleID;
		this.player = player;
		this.silly = silly;
	}
	
	public InitiateBattlePacket() {
		player = new CombatantInfo();
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		battleID = buf.readInt();
		player.isPlayer = buf.readBoolean();
		player.id = buf.readInt();
		player.isSideOne = buf.readBoolean();
		silly = buf.readBoolean();
		player.name = ByteBufUtils.readUTF8String(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(battleID);
		buf.writeBoolean(player.isPlayer);
		buf.writeInt(player.id);
		buf.writeBoolean(player.isSideOne);
		buf.writeBoolean(silly);
		ByteBufUtils.writeUTF8String(buf, player.name);
	}

	public static class Handler implements IMessageHandler<InitiateBattlePacket, IMessage>
	{
		@Override
		public IMessage onMessage(InitiateBattlePacket message,
				MessageContext ctx) {
			ModMain.proxy.newGui(message.battleID, message.player, message.silly);
			return null;
		}
	}
}
