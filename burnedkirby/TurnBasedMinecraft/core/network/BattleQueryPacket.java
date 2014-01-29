package burnedkirby.TurnBasedMinecraft.core.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import burnedkirby.TurnBasedMinecraft.ModMain;

/**
 * Packet sent by player in battle to server that is a query for battle status.
 *
 */
public class BattleQueryPacket extends AbstractPacket {
	
	int battleID;
	short type;
	
	public BattleQueryPacket(int battleID, short type)
	{
		this.battleID = battleID;
		this.type = type;
	}
	
	public BattleQueryPacket() {}

	@Override
	public void encodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		buffer.writeInt(battleID);
		buffer.writeShort(type);
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {
		battleID = buffer.readInt();
		type = buffer.readShort();
	}

	@Override
	public void handleClientSide(EntityPlayer player) {
	}

	@Override
	public void handleServerSide(EntityPlayer player) {
		ModMain.bss.manageQuery(battleID, type, player);
	}

/*	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeInt(battleID);
		out.writeShort(type);
	}

	@Override
	public void read(ByteArrayDataInput in) {
		battleID = in.readInt();
		type = in.readShort();
	}*/

	/**
	 * If battle does not exist or has ended, will send a BattleStatusPacket that the battle
	 * has ended.
	 */
/*	@Override
	public void execute(EntityPlayer player, Side side) throws ProtocolException {
		if(side.isServer())
		{
			ModMain.bss.manageQuery(battleID, type, player);
//			if(!ModMain.bss.battleExists(battleID) || !ModMain.bss.getBattle(battleID).isBattleInProgress())
//			{
//				PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(false,false,0,0).makePacket(), (Player)player);
//				return;
//			}
//			
//			Vector<Integer> sideOne = ModMain.bss.getBattleSide(battleID, true);
////			if(sideOne == null)
////			{
////				PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(false,0,0).makePacket(), (Player)player);
////			}
//			Vector<Integer> sideTwo = ModMain.bss.getBattleSide(battleID, false);
//			if(type == 0) //Send sizes to player.
//			{
//				PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(true, false, sideOne.size(), sideTwo.size()).makePacket(), (Player)player);
//			}
//			else if(type == 1) //Send side one information to player.
//			{
//				Enumeration<Integer> list = sideOne.elements();
//				String name = "";
//				int id;
//				while(list.hasMoreElements())
//				{
//					id = list.nextElement();
//					name = Utility.getEntityByID(id).getEntityName();
//					PacketDispatcher.sendPacketToPlayer(new BattleCombatantPacket(id, true, name).makePacket(), (Player)player);
//				}
//			}
//			else if(type == 2) //Send side two information to player.
//			{
//				Enumeration<Integer> list = sideTwo.elements();
//				String name = "";
//				int id;
//				while(list.hasMoreElements())
//				{
//					id = list.nextElement();
//					name = Utility.getEntityByID(id).getEntityName();
//					PacketDispatcher.sendPacketToPlayer(new BattleCombatantPacket(id, false, name).makePacket(), (Player)player);
//				}
//			}
		}
		else
		{
			//throw new ProtocolException("Packet can only be received by the server!");
		}
	}*/

}
