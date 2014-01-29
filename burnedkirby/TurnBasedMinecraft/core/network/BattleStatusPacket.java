package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import burnedkirby.TurnBasedMinecraft.ModMain;
import burnedkirby.TurnBasedMinecraft.gui.BattleGui;

/**
 * Packet sent by server to player containing information on whether the battle
 * the player is in exists, and the sizes of the two sides.
 * This packet is sent as a response from the player's BattleQueryPacket.
 */
public class BattleStatusPacket extends AbstractPacket {
	
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
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeBoolean(found);
		buffer.writeBoolean(forceUpdate);
		buffer.writeInt(battleSize);
		buffer.writeBoolean(playerPhase);
		buffer.writeBoolean(turnChoiceReceived);
		buffer.writeShort(timer);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		found = buffer.readBoolean();
		forceUpdate = buffer.readBoolean();
		battleSize = buffer.readInt();
		playerPhase = buffer.readBoolean();
		turnChoiceReceived = buffer.readBoolean();
		timer = buffer.readShort();
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		if(found)
		{
			if(((BattleGui)ModMain.proxy.getGui()) == null && Minecraft.getMinecraft().currentScreen instanceof BattleGui)
			{
				Minecraft.getMinecraft().setIngameFocus();
				return;
			}
			else if(((BattleGui)ModMain.proxy.getGui()) == null)
				return;
			((BattleGui)ModMain.proxy.getGui()).checkBattleInfo(forceUpdate, battleSize, playerPhase, turnChoiceReceived, timer);
		}
		else if(!found && Minecraft.getMinecraft().currentScreen instanceof BattleGui)
		{
			Minecraft.getMinecraft().setIngameFocus();
		}
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
	}

/*	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeBoolean(found);
		out.writeBoolean(forceUpdate);
		out.writeInt(battleSize);
		out.writeBoolean(playerPhase);
		out.writeBoolean(turnChoiceReceived);
		out.writeShort(timer);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException {
		found = in.readBoolean();
		forceUpdate = in.readBoolean();
		battleSize = in.readInt();
		playerPhase = in.readBoolean();
		turnChoiceReceived = in.readBoolean();
		timer = in.readShort();
	}

	@Override
	public void execute(EntityPlayer player, Side side)
			throws ProtocolException {
		if(side.isServer())
		{
			//throw new ProtocolException("Packet can only be received by the player!");
		}
		else
		{
			if(found) //TODO check if null pointer exception can happen
			{
				if(((BattleGui)ModMain.proxy.getGui()) == null && Minecraft.getMinecraft().currentScreen instanceof BattleGui)
				{
					Minecraft.getMinecraft().setIngameFocus();
					return;
				}
				else if(((BattleGui)ModMain.proxy.getGui()) == null)
					return;
				((BattleGui)ModMain.proxy.getGui()).checkBattleInfo(forceUpdate, battleSize, playerPhase, turnChoiceReceived, timer);
			}
			else if(!found && Minecraft.getMinecraft().currentScreen instanceof BattleGui)
			{
				Minecraft.getMinecraft().setIngameFocus();
			}
		}
	}*/

}
