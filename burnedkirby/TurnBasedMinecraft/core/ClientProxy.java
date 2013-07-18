package burnedkirby.TurnBasedMinecraft.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import net.minecraft.client.Minecraft;
import burnedkirby.TurnBasedMinecraft.CombatantInfo;
import burnedkirby.TurnBasedMinecraft.gui.BattleGui;

public class ClientProxy extends CommonProxy {

	//Unique GUI per client. (null version may exist server side)
	public BattleGui bg = null;
	
	protected BattleMusicManager bmm = null;
	
	public ClientProxy()
	{
	}

	@Override
	public void newGui(int battleID, CombatantInfo player)
	{
		bg = new BattleGui(battleID, player);
		Minecraft.getMinecraft().displayGuiScreen(bg);
	}
	
	@Override
	public Object getGui()
	{
		return bg;
	}
	
	@Override
	public void setGui(Object gui)
	{
		bg = (BattleGui) gui;
	}
	
	@Override
	public void initializeMusicManager()
	{
		if(bmm == null)
			bmm = new BattleMusicManager();
	}
	
	@Override
	public void playBattleMusic() {
		bmm.playRandomBattleMusic();
	}
	
	@Override
	public void stopBattleMusic() {
		bmm.stopBattleMusic();
	}
}
