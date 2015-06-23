package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import burnedkirby.TurnBasedMinecraft.ModMain;
import burnedkirby.TurnBasedMinecraft.gui.BattleGui;

public class CombatantHealthPacket implements IMessage {
	
	int entityID;
	float health;
	
	public CombatantHealthPacket() {
	}
	
	public CombatantHealthPacket(int entityID, float health)
	{
		this.entityID = entityID;
		this.health = health;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		entityID = buf.readInt();
		health = buf.readFloat();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(entityID);
		buf.writeFloat(health);
	}

	public static class Handler implements IMessageHandler<CombatantHealthPacket, IMessage>
	{
		@Override
		public IMessage onMessage(CombatantHealthPacket message,
				MessageContext ctx) {
			((BattleGui)ModMain.proxy.getGui()).receiveCombatantHealthInfo(message.entityID, message.health);
			return null;
		}
	}
}
