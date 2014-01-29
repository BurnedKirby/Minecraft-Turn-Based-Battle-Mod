package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import burnedkirby.TurnBasedMinecraft.ModMain;
import burnedkirby.TurnBasedMinecraft.gui.BattleGui;

public class CombatantHealthPacket extends AbstractPacket {
	
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
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(entityID);
		buffer.writeFloat(health);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		entityID = buffer.readInt();
		health = buffer.readFloat();
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		((BattleGui)ModMain.proxy.getGui()).receiveCombatantHealthInfo(entityID, health);
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
	}

/*	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeInt(entityID);
		out.writeFloat(health);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException {
		entityID = in.readInt();
		health = in.readFloat();
	}

	@Override
	public void execute(EntityPlayer player, Side side)
			throws ProtocolException {
		if(side.isServer())
		{
		}
		else
		{
			((BattleGui)ModMain.proxy.getGui()).receiveCombatantHealthInfo(entityID, health);
		}
	}*/

}
