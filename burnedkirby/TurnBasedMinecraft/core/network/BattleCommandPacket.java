package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import burnedkirby.TurnBasedMinecraft.CombatantInfo;
import burnedkirby.TurnBasedMinecraft.CombatantInfo.Type;
import burnedkirby.TurnBasedMinecraft.ModMain;

/**
 * Packet sent to server from the player that has information
 * on what the player has decided to do on their turn.
 */
public class BattleCommandPacket implements IMessage {
	
	private int battleID;
	private CombatantInfo combatant;
	
	public BattleCommandPacket(int battleID, CombatantInfo combatant)
	{
		this.battleID = battleID;
		this.combatant = combatant;
	}
	
	public BattleCommandPacket() {
		combatant = new CombatantInfo();
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		battleID = buf.readInt();
		combatant.isPlayer = buf.readBoolean();
		combatant.id = buf.readInt();
		combatant.isSideOne = buf.readBoolean();
		combatant.name = ByteBufUtils.readUTF8String(buf);
		combatant.ready = buf.readBoolean();
		combatant.type = Type.values()[buf.readInt()];
		combatant.target = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(battleID);
		buf.writeBoolean(combatant.isPlayer);
		buf.writeInt(combatant.id);
		buf.writeBoolean(combatant.isSideOne);
		ByteBufUtils.writeUTF8String(buf, combatant.name);
		buf.writeBoolean(combatant.ready);
		buf.writeInt(combatant.type.ordinal());
		buf.writeInt(combatant.target);
	}

	public static class Handler implements IMessageHandler<BattleCommandPacket, IMessage>
	{
		@Override
		public IMessage onMessage(BattleCommandPacket message,
				MessageContext ctx) {
			ModMain.bss.managePlayerUpdate(message.battleID, message.combatant);
			return null;
		}
	}
}
