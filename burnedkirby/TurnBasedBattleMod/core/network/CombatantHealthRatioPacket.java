package burnedkirby.TurnBasedBattleMod.core.network;

import net.minecraft.entity.player.EntityPlayer;

import burnedkirby.TurnBasedBattleMod.ModMain;
import burnedkirby.TurnBasedBattleMod.gui.BattleGui;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class CombatantHealthRatioPacket extends CommandPacket {
	
	int entityID;
	short healthRatio;
	
	public CombatantHealthRatioPacket() {
	}
	
	public CombatantHealthRatioPacket(int entityID, short healthRatio)
	{
		this.entityID = entityID;
		this.healthRatio = healthRatio;
	}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeInt(entityID);
		out.writeShort(healthRatio);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException {
		entityID = in.readInt();
		healthRatio = in.readShort();
	}

	@Override
	public void execute(EntityPlayer player, Side side)
			throws ProtocolException {
		if(side.isServer())
		{
		}
		else
		{
			((BattleGui)ModMain.proxy.getGui()).receiveCombatantHealthInfo(entityID, healthRatio);
		}
	}

}
