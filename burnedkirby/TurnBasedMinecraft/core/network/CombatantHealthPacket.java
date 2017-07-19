package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
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
//			final CombatantHealthPacket mes = message;
//			IThreadListener mainThread = Minecraft.getMinecraft();
//			mainThread.addScheduledTask(new Runnable() {
//				@Override
//				public void run() {
					((BattleGui)ModMain.proxy.getGui()).receiveCombatantHealthInfo(message.entityID, message.health);
//				}
//			});
			return null;
		}
	}
}
