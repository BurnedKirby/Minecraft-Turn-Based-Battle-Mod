package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import burnedkirby.TurnBasedMinecraft.ModMain;
import burnedkirby.TurnBasedMinecraft.gui.BattleGui;

/**
 * Packet sent by server to player containing information on whether the battle
 * the player is in exists, and the sizes of the two sides.
 * This packet is sent as a response from the player's BattleQueryPacket.
 */
public class BattleStatusPacket implements IMessage {
	
	boolean found;
	boolean forceUpdate;
	int battleSize;
	boolean playerPhase;
	boolean turnChoiceReceived;
	short timer;
	
	public BattleStatusPacket(boolean found)
	{
		this.found = found;
		forceUpdate = false;
		battleSize = 0;
		playerPhase = false;
		turnChoiceReceived = false;
		timer = 0;
	}
	
	public BattleStatusPacket(boolean found, boolean forceUpdate, int battleSize, boolean playerPhase, boolean turnChoiceReceived, short timer)
	{
		this.found = found;
		this.forceUpdate = forceUpdate;
		this.battleSize = battleSize;
		this.playerPhase = playerPhase;
		this.turnChoiceReceived = turnChoiceReceived;
		this.timer = timer;
	}
	
	public BattleStatusPacket()
	{}

	@Override
	public void fromBytes(ByteBuf buf) {
		found = buf.readBoolean();
		forceUpdate = buf.readBoolean();
		battleSize = buf.readInt();
		playerPhase = buf.readBoolean();
		turnChoiceReceived = buf.readBoolean();
		timer = buf.readShort();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(found);
		buf.writeBoolean(forceUpdate);
		buf.writeInt(battleSize);
		buf.writeBoolean(playerPhase);
		buf.writeBoolean(turnChoiceReceived);
		buf.writeShort(timer);
	}

	public static class Handler implements IMessageHandler<BattleStatusPacket, IMessage>
	{
		@Override
		public IMessage onMessage(BattleStatusPacket message, MessageContext ctx) {

			if(message.found)
			{
				if(((BattleGui)ModMain.proxy.getGui()) == null && Minecraft.getMinecraft().currentScreen instanceof BattleGui)
				{
					Minecraft.getMinecraft().setIngameFocus();
					return null;
				}
				else if(((BattleGui)ModMain.proxy.getGui()) == null)
					return null;
				((BattleGui)ModMain.proxy.getGui()).checkBattleInfo(message.forceUpdate, message.battleSize, message.playerPhase, message.turnChoiceReceived, message.timer);
			}
			else if(!message.found && Minecraft.getMinecraft().currentScreen instanceof BattleGui)
			{
				Minecraft.getMinecraft().setIngameFocus();
			}
			return null;
		}
	}
}
