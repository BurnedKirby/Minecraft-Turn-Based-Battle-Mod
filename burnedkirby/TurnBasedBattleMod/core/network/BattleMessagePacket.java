package burnedkirby.TurnBasedBattleMod.core.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import cpw.mods.fml.relauncher.Side;

public class BattleMessagePacket extends CommandPacket {
	
	String message;
	
	public BattleMessagePacket() {message = "";}
	
	public BattleMessagePacket(String message)
	{
		this.message = message;
	}

	@Override
	public void write(ByteArrayDataOutput out) {
		out.writeUTF(message);
	}

	@Override
	public void read(ByteArrayDataInput in) throws ProtocolException {
		message = in.readUTF();
	}

	@Override
	public void execute(EntityPlayer player, Side side)
			throws ProtocolException {
		if(side.isServer())
		{}
		else
		{
			Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(message);
		}
	}

}
