package burnedkirby.TurnBasedMinecraft.core.network;

import net.minecraft.entity.player.EntityPlayer;

import burnedkirby.TurnBasedMinecraft.ModMain;
import burnedkirby.TurnBasedMinecraft.gui.BattleGui;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class CombatantHealthPacket extends CommandPacket {
	
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
	}

}
