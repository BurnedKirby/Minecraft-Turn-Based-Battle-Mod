package burnedkirby.TurnBasedBattleMod.core;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import burnedkirby.TurnBasedBattleMod.ModMain;
import burnedkirby.TurnBasedBattleMod.core.CommandPacket.ProtocolException;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class BattleStatusPacket extends CommandPacket {
	
	boolean found;
	int sideOneSize;
	int sideTwoSize;
	
	public BattleStatusPacket(boolean found, int sideOneSize, int sideTwoSize)
	{
		this.found = found;
		this.sideOneSize = sideOneSize;
		this.sideTwoSize = sideTwoSize;
	}
	
	public BattleStatusPacket()
	{}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeBoolean(found);
		out.writeInt(sideOneSize);
		out.writeInt(sideTwoSize);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException {
		found = in.readBoolean();
		sideOneSize = in.readInt();
		sideTwoSize = in.readInt();
	}

	@Override
	public void execute(EntityPlayer player, Side side)
			throws ProtocolException {
		if(side.isServer())
		{
			throw new ProtocolException("Packet can only be received by the player!");
		}
		else
		{
			if(found)// && ModMain.bg != null) //TODO check if null pointer exception can happen
			{
				ModMain.bg.checkBattleSize(sideOneSize, sideTwoSize);
			}
			else// if(!found)
			{
				Minecraft.getMinecraft().setIngameFocus();
			}
		}
	}

}
