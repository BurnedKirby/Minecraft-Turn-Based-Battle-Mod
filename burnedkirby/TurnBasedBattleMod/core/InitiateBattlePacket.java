package burnedkirby.TurnBasedBattleMod.core;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import burnedkirby.TurnBasedBattleMod.ModMain;
import burnedkirby.TurnBasedBattleMod.gui.BattleGui;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class InitiateBattlePacket extends CommandPacket {
	
	int battleID;
	
	public InitiateBattlePacket(int battleID)
	{
		this.battleID = battleID;
	}
	
	public InitiateBattlePacket() {}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeInt(battleID);
	}

	@Override
	public void read(ByteArrayDataInput in) {
		battleID = in.readInt();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if(side.isServer())
		{
			
		}
		else
		{
			ModMain.bg = new BattleGui(battleID,player.entityId);
			Minecraft.getMinecraft().displayGuiScreen(ModMain.bg);
		}
	}

}
