package burnedkirby.TurnBasedBattleMod.core;

import java.util.Enumeration;
import java.util.Vector;

import net.minecraft.entity.player.EntityPlayer;

import burnedkirby.TurnBasedBattleMod.ModMain;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;

public class BattleQueryPacket extends CommandPacket {
	
	int battleID;
	int type;
	
	public BattleQueryPacket(int battleID, int type)
	{
		this.battleID = battleID;
		this.type = type;
	}
	
	public BattleQueryPacket() {}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeInt(battleID);
		out.writeInt(type);
	}

	@Override
	public void read(ByteArrayDataInput in) {
		battleID = in.readInt();
		type = in.readInt();
	}

	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if(side.isServer())
		{
			if(!ModMain.bss.battleExists(battleID) || !ModMain.bss.getBattle(battleID).isBattleInProgress())
			{
				PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(false,0,0).makePacket(), (Player)player);
				return;
			}
			
			Vector<Integer> sideOne = ModMain.bss.getBattleSide(battleID, true);
//			if(sideOne == null)
//			{
//				PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(false,0,0).makePacket(), (Player)player);
//			}
			Vector<Integer> sideTwo = ModMain.bss.getBattleSide(battleID, false);
			if(type == 0)
			{
				PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(true, sideOne.size(), sideTwo.size()).makePacket(), (Player)player);
			}
			else if(type == 1)
			{
				Enumeration<Integer> list = sideOne.elements();
				String name = "";
				int id;
				while(list.hasMoreElements())
				{
					id = list.nextElement();
					name = Utility.getEntityByID(id).getEntityName();
					PacketDispatcher.sendPacketToPlayer(new BattleCombatantPacket(id, true, name).makePacket(), (Player)player);
				}
			}
			else if(type == 2)
			{
				Enumeration<Integer> list = sideTwo.elements();
				String name = "";
				int id;
				while(list.hasMoreElements())
				{
					id = list.nextElement();
					name = Utility.getEntityByID(id).getEntityName();
					PacketDispatcher.sendPacketToPlayer(new BattleCombatantPacket(id, false, name).makePacket(), (Player)player);
				}
			}
		}
		else
		{
			throw new ProtocolException("Packet can only be received by the server!");
		}
	}

}
