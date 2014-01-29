package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.UnsupportedEncodingException;

import net.minecraft.entity.player.EntityPlayer;
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
public class BattleCombatantPacket extends AbstractPacket {

	CombatantInfo combatant;
	
	public BattleCombatantPacket(CombatantInfo combatant)
	{
		this.combatant = combatant;
	}
	
	public BattleCombatantPacket() {
		combatant = new CombatantInfo();
	}
/*	
	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeBoolean(combatant.isPlayer);
		out.writeInt(combatant.id);
		out.writeBoolean(combatant.isSideOne);
		out.writeUTF(combatant.name);
		out.writeBoolean(combatant.ready);
		out.writeInt(combatant.type.ordinal());
		out.writeInt(combatant.target);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException {
		combatant.isPlayer = in.readBoolean();
		combatant.id = in.readInt();
		combatant.isSideOne = in.readBoolean();
		combatant.name = in.readUTF();
		combatant.ready = in.readBoolean();
		combatant.type = Type.values()[in.readInt()];
		combatant.target = in.readInt();
	}

	@Override
	public void execute(EntityPlayer player, Side side)
			throws ProtocolException {
		if(side.isServer())
		{
			
		}
		else
		{
			if(ModMain.proxy.getGui() != null)
				((BattleGui)ModMain.proxy.getGui()).receiveCombatant(combatant);
		}
	}*/

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
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
		if(ModMain.proxy.getGui() != null)
			((BattleGui)ModMain.proxy.getGui()).receiveCombatant(combatant);
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		
	}

}
