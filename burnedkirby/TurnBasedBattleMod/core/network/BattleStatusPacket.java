package burnedkirby.TurnBasedBattleMod.core.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import burnedkirby.TurnBasedBattleMod.ModMain;
import burnedkirby.TurnBasedBattleMod.core.ClientProxy;
import burnedkirby.TurnBasedBattleMod.gui.BattleGui;

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
	int battleSize;
	boolean playerPhase;
	boolean turnChoiceReceived;
	
	public BattleStatusPacket(boolean found)
	{
		this.found = found;
		forceUpdate = false;
		battleSize = 0;
		playerPhase = false;
		turnChoiceReceived = false;
	}
	
	public BattleStatusPacket(boolean found, boolean forceUpdate, int battleSize, boolean playerPhase, boolean turnChoiceReceived)
	{
		this.found = found;
		this.forceUpdate = forceUpdate;
		this.battleSize = battleSize;
		this.playerPhase = playerPhase;
		this.turnChoiceReceived = turnChoiceReceived;
	}
	
	public BattleStatusPacket()
	{}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeBoolean(found);
		out.writeBoolean(forceUpdate);
		out.writeInt(battleSize);
		out.writeBoolean(playerPhase);
		out.writeBoolean(turnChoiceReceived);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException {
		found = in.readBoolean();
		forceUpdate = in.readBoolean();
		battleSize = in.readInt();
		playerPhase = in.readBoolean();
		turnChoiceReceived = in.readBoolean();
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
			System.out.println("BattleStatusPacket: found is " + found + ", received playerPhase is " + playerPhase + ", turnReceived is " + turnChoiceReceived);
			if(found) //TODO check if null pointer exception can happen
			{
				if(((BattleGui)ModMain.proxy.getGui()) == null && Minecraft.getMinecraft().currentScreen instanceof BattleGui)
				{
					Minecraft.getMinecraft().setIngameFocus();
					return;
				}
				else if(((BattleGui)ModMain.proxy.getGui()) == null)
					return;
				((BattleGui)ModMain.proxy.getGui()).checkBattleInfo(forceUpdate, battleSize, playerPhase, turnChoiceReceived);
			}
			else if(!found && Minecraft.getMinecraft().currentScreen instanceof BattleGui)
			{
				Minecraft.getMinecraft().setIngameFocus();
			}
		}
	}

}
