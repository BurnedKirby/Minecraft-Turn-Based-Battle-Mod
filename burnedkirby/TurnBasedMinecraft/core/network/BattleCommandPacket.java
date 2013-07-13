package burnedkirby.TurnBasedMinecraft.core.network;

import net.minecraft.entity.player.EntityPlayer;

import burnedkirby.TurnBasedMinecraft.Battle;
import burnedkirby.TurnBasedMinecraft.CombatantInfo;
import burnedkirby.TurnBasedMinecraft.ModMain;
import burnedkirby.TurnBasedMinecraft.CombatantInfo.Type;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * Packet sent to server from the player that has information
 * on what the player has decided to do on their turn.
 */
public class BattleCommandPacket extends CommandPacket {
	
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
	}

}
