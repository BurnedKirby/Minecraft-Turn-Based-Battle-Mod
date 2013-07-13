package burnedkirby.TurnBasedMinecraft.core.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import burnedkirby.TurnBasedMinecraft.CombatantInfo;
import burnedkirby.TurnBasedMinecraft.ModMain;
import burnedkirby.TurnBasedMinecraft.core.ClientProxy;
import burnedkirby.TurnBasedMinecraft.core.CommonProxy;
import burnedkirby.TurnBasedMinecraft.gui.BattleGui;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

/**
 * Packet sent to player from the server which notifies the player of
 * entering battle and brings up the BattleGUI.
 */
public class InitiateBattlePacket extends CommandPacket {
	
	int battleID;
	CombatantInfo player;
	
	public InitiateBattlePacket(int battleID, CombatantInfo player)
	{
		this.battleID = battleID;
		this.player = player;
	}
	
	public InitiateBattlePacket() {
		player = new CombatantInfo();
	}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeInt(battleID);
		out.writeBoolean(player.isPlayer);
		out.writeInt(player.id);
		out.writeBoolean(player.isSideOne);
		out.writeUTF(player.name);
	}

	@Override
	public void read(ByteArrayDataInput in) {
		battleID = in.readInt();
		player.isPlayer = in.readBoolean();
		player.id = in.readInt();
		player.isSideOne = in.readBoolean();
		player.name = in.readUTF();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if(side.isServer())
		{
			
		}
		else
		{
			ModMain.proxy.newGui(battleID, this.player);
		}
	}

}
