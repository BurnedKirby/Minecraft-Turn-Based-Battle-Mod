package burnedkirby.TurnBasedBattleMod.core.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.packet.Packet;


import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;

public abstract class CommandPacket {
	public static final String CHANNEL = "BKTBBS-Command";
	private static final BiMap<Integer, Class<? extends CommandPacket>> idMap;
	
	/**
	 * Must add new Packet class here when created.
	 */
	static {
		ImmutableBiMap.Builder<Integer, Class<? extends CommandPacket>> builder = ImmutableBiMap.builder();
		
		builder.put(Integer.valueOf(0), BattleCommandPacket.class);
		builder.put(Integer.valueOf(1), InitiateBattlePacket.class);
		builder.put(Integer.valueOf(2), BattleQueryPacket.class);
		builder.put(Integer.valueOf(3), BattleStatusPacket.class);
		builder.put(Integer.valueOf(4), BattleCombatantPacket.class);
		builder.put(Integer.valueOf(5), BattlePhaseEndedPacket.class);
		builder.put(Integer.valueOf(6), BattleMessagePacket.class);
		builder.put(Integer.valueOf(7), CombatantHealthRatioPacket.class);
		
		idMap = builder.build();
	}
	
	public static CommandPacket constructPacket(int packetId) throws ProtocolException, ReflectiveOperationException {
		Class<? extends CommandPacket> theClass = idMap.get(Integer.valueOf(packetId));
		if (theClass == null) {
			throw new ProtocolException("Unknown Packet Id!");
		}
		else {
			return theClass.newInstance();
		}
	}
	
	public static class ProtocolException extends Exception {
		
		public ProtocolException() {
			
		}
		
		public ProtocolException(String message, Throwable cause) {
			super(message, cause);
		}
		
		public ProtocolException(String message) {
			super(message);
		}
		
		public ProtocolException(Throwable cause) {
			super(cause);
		}
	}
	
	public final int getPacketId() {
		if (idMap.inverse().containsKey(getClass())) {
			return idMap.inverse().get(getClass()).intValue();
		}
		else {
			throw new RuntimeException("Packet " + getClass().getSimpleName() + " is missing a mapping!");
		}
	}
	
	public final Packet makePacket() {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeByte(getPacketId());
		write(out);
		return PacketDispatcher.getPacket(CHANNEL, out.toByteArray());
	}
	
	public abstract void write(ByteArrayDataOutput out);
	
	public abstract void read(ByteArrayDataInput in) throws ProtocolException;
	
	public abstract void execute(EntityPlayer player, Side side) throws ProtocolException;
}
