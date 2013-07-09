package burnedkirby.TurnBasedBattleMod.core;

import net.minecraft.entity.player.EntityPlayer;

import burnedkirby.TurnBasedBattleMod.Battle;
import burnedkirby.TurnBasedBattleMod.ModMain;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class BattleCommandPacket extends CommandPacket {
	
	private int battleID;
	private int entityTargetID;
	private int command;
	
	public BattleCommandPacket(int battleID, int entityTargetID, int command)
	{
		this.battleID = battleID;
		this.entityTargetID = entityTargetID;
		this.command = command;
	}
	
	public BattleCommandPacket() {}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeInt(battleID);
		out.writeInt(entityTargetID);
		out.writeInt(command);
	}

	@Override
	public void read(ByteArrayDataInput in) {
		battleID = in.readInt();
		entityTargetID = in.readInt();
		command = in.readInt();
	}

	@Override
	public void execute(EntityPlayer player, Side side) {
		if(side.isServer())
		{
			Battle battle = ModMain.bss.getBattle(battleID);
			battle.updatePlayer(player.entityId, command, entityTargetID);
		}
		else
		{
			
		}
	}

}
