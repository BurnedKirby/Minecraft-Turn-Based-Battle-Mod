package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.UnsupportedEncodingException;

import net.minecraft.entity.player.EntityPlayer;
import burnedkirby.TurnBasedMinecraft.CombatantInfo;
import burnedkirby.TurnBasedMinecraft.ModMain;

/**
 * Packet sent to player from the server which notifies the player of
 * entering battle and brings up the BattleGUI.
 */
public class InitiateBattlePacket extends AbstractPacket {
	
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
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(battleID);
		buffer.writeBoolean(player.isPlayer);
		buffer.writeInt(player.id);
		buffer.writeBoolean(player.isSideOne);
		buffer.writeBoolean(silly);
		try {
			encodeUTF(player.name, buffer);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		battleID = buffer.readInt();
		player.isPlayer = buffer.readBoolean();
		player.id = buffer.readInt();
		player.isSideOne = buffer.readBoolean();
		silly = buffer.readBoolean();
		try {
			player.name = decodeUTF(buffer);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
		ModMain.proxy.newGui(battleID, this.player, silly);
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
	}

/*	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeInt(battleID);
		out.writeBoolean(player.isPlayer);
		out.writeInt(player.id);
		out.writeBoolean(player.isSideOne);
		out.writeUTF(player.name);
	}

	@Override
	public void read(ByteArrayDataInput in) {
		battleID = in.readInt();
		player.isPlayer = in.readBoolean();
		player.id = in.readInt();
		player.isSideOne = in.readBoolean();
		player.name = in.readUTF();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if(side.isServer())
		{
			
		}
		else
		{
			ModMain.proxy.newGui(battleID, this.player);
		}
	}*/

}
