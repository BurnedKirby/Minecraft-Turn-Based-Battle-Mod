package burnedkirby.TurnBasedBattleMod.core.network;

import net.minecraft.entity.player.EntityPlayer;

import burnedkirby.TurnBasedBattleMod.ModMain;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * This packet sent from server to player notifies the player that a turn has
 * ended and the Battle GUI can now return to the main menu.
 *
 */
public class BattlePhaseEndedPacket extends CommandPacket {

	@Override
	public void write(ByteArrayDataOutput out) {
		// TODO Auto-generated method stub

	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException {
		// TODO Auto-generated method stub

	}

	@Override
	public void execute(EntityPlayer player, Side side)
			throws ProtocolException {
		if(side.isServer())
		{
			
		}
		else
		{
//			ModMain.bg.updateTurnEnd(true);
		}
	}

}
