package burnedkirby.TurnBasedBattleMod.core;

import java.io.EOFException;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;

import burnedkirby.TurnBasedBattleMod.ModMain;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class BattleCombatantPacket extends CommandPacket {

	int id;
	boolean isSideOne;
	String name;
	
	BattleCombatantPacket(int id, boolean isSideOne, String name)
	{
		this.id = id;
		this.isSideOne = isSideOne;
		this.name = name;
	}
	
	BattleCombatantPacket() {}
	
	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeInt(id);
		out.writeBoolean(isSideOne);
		out.writeUTF(name);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException {
		id = in.readInt();
		isSideOne = in.readBoolean();
		name = in.readUTF();
	}

	@Override
	public void execute(EntityPlayer player, Side side)
			throws ProtocolException {
		if(side.isServer())
		{
			
		}
		else
		{
			ModMain.bg.receiveCombatant(id, isSideOne, name);
		}
	}

}
