package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import burnedkirby.TurnBasedMinecraft.CombatantInfo;
import burnedkirby.TurnBasedMinecraft.CombatantInfo.Type;
import burnedkirby.TurnBasedMinecraft.ModMain;
import burnedkirby.TurnBasedMinecraft.gui.BattleGui;

/**
 * This packet is sent from server to player with information on a combatant that is
 * in the player's current battle.
 * This is sent as a response from the player's BattleQueryPacket.
 *
 */
public class BattleCombatantPacket implements IMessage {

	CombatantInfo combatant;
	
	public BattleCombatantPacket(CombatantInfo combatant)
	{
		this.combatant = combatant;
	}
	
	public BattleCombatantPacket() {
		combatant = new CombatantInfo();
	}

	@Override
	public void fromBytes(ByteBuf buf) {
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
		buf.writeBoolean(combatant.isPlayer);
		buf.writeInt(combatant.id);
		buf.writeBoolean(combatant.isSideOne);
		ByteBufUtils.writeUTF8String(buf, combatant.name);
		buf.writeBoolean(combatant.ready);
		buf.writeInt(combatant.type.ordinal());
		buf.writeInt(combatant.target);
	}
	
	public static class Handler implements IMessageHandler<BattleCombatantPacket, IMessage>
	{
		@Override
		public IMessage onMessage(BattleCombatantPacket message,
				MessageContext ctx) {
			if(ModMain.proxy.getGui() != null)
				((BattleGui)ModMain.proxy.getGui()).receiveCombatant(message.combatant);
			return null;
		}
	}
}
