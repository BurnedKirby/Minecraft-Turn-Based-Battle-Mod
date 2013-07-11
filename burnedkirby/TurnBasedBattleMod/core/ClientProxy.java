package burnedkirby.TurnBasedBattleMod.core;

import net.minecraft.client.Minecraft;
import burnedkirby.TurnBasedBattleMod.CombatantInfo;
import burnedkirby.TurnBasedBattleMod.gui.BattleGui;

public class ClientProxy extends CommonProxy {

	//Unique GUI per client. (null version may exist server side)
	public BattleGui bg = null;

	public ClientProxy()
	{
	}

	public void newGui(int battleID, CombatantInfo player)
	{
		bg = new BattleGui(battleID, player);
		Minecraft.getMinecraft().displayGuiScreen(bg);
	}
	
	public Object getGui()
	{
		return bg;
	}
	
	public void setGui(Object gui)
	{
		bg = (BattleGui) gui;
	}
}
