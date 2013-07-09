package burnedkirby.TurnBasedBattleMod.core.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import burnedkirby.TurnBasedBattleMod.ModMain;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * Packet sent by server to player containing information on whether the battle
 * the player is in exists, and the sizes of the two sides.
 * This packet is sent as a response from the player's BattleQueryPacket.
 */
public class BattleStatusPacket extends CommandPacket {
	
	boolean found;
	boolean forceUpdate;
	int sideOneSize;
	int sideTwoSize;
	
	public BattleStatusPacket(boolean found, boolean forceUpdate, int sideOneSize, int sideTwoSize)
	{
		this.found = found;
		this.forceUpdate = forceUpdate;
		this.sideOneSize = sideOneSize;
		this.sideTwoSize = sideTwoSize;
	}
	
	public BattleStatusPacket()
	{}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeBoolean(found);
		out.writeBoolean(forceUpdate);
		out.writeInt(sideOneSize);
		out.writeInt(sideTwoSize);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException {
		found = in.readBoolean();
		forceUpdate = in.readBoolean();
		sideOneSize = in.readInt();
		sideTwoSize = in.readInt();
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
			if(found)// && ModMain.bg != null) //TODO check if null pointer exception can happen
			{
				ModMain.bg.checkBattleSize(forceUpdate, sideOneSize, sideTwoSize);
			}
			else// if(!found)
			{
				Minecraft.getMinecraft().setIngameFocus();
			}
		}
	}

}
