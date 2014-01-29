package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.UnsupportedEncodingException;

import net.minecraft.entity.player.EntityPlayer;
import burnedkirby.TurnBasedMinecraft.CombatantInfo;
import burnedkirby.TurnBasedMinecraft.CombatantInfo.Type;
import burnedkirby.TurnBasedMinecraft.ModMain;

/**
 * Packet sent to server from the player that has information
 * on what the player has decided to do on their turn.
 */
public class BattleCommandPacket extends AbstractPacket {
	
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
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(battleID);
		buffer.writeBoolean(combatant.isPlayer);
		buffer.writeInt(combatant.id);
		buffer.writeBoolean(combatant.isSideOne);
		try {
			encodeUTF(combatant.name, buffer);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		buffer.writeBoolean(combatant.ready);
		buffer.writeInt(combatant.type.ordinal());
		buffer.writeInt(combatant.target);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		battleID = buffer.readInt();
		combatant.isPlayer = buffer.readBoolean();
		combatant.id = buffer.readInt();
		combatant.isSideOne = buffer.readBoolean();
		try {
			combatant.name = decodeUTF(buffer);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return;
		}
		combatant.ready = buffer.readBoolean();
		combatant.type = Type.values()[buffer.readInt()];
		combatant.target = buffer.readInt();
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		ModMain.bss.managePlayerUpdate(battleID, combatant);
	}

/*	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeInt(battleID);
		out.writeBoolean(combatant.isPlayer);
		out.writeInt(combatant.id);
		out.writeBoolean(combatant.isSideOne);
		out.writeUTF(combatant.name);
		out.writeBoolean(combatant.ready);
		out.writeInt(combatant.type.ordinal());
		out.writeInt(combatant.target);
	}

	@Override
	public void read(ByteArrayDataInput in) {
		battleID = in.readInt();
		combatant.isPlayer = in.readBoolean();
		combatant.id = in.readInt();
		combatant.isSideOne = in.readBoolean();
		combatant.name = in.readUTF();
		combatant.ready = in.readBoolean();
		combatant.type = Type.values()[in.readInt()];
		combatant.target = in.readInt();
	}

	@Override
	public void execute(EntityPlayer player, Side side) {
		if(side.isServer())
		{
			ModMain.bss.managePlayerUpdate(battleID, combatant);
		}
		else
		{
			
		}
	}*/

}
