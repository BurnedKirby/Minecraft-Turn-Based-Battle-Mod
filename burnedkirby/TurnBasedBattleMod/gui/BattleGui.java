package burnedkirby.TurnBasedBattleMod.gui;

import java.util.List;

import burnedkirby.TurnBasedBattleMod.CombatantInfo;
import burnedkirby.TurnBasedBattleMod.ModMain;
import burnedkirby.TurnBasedBattleMod.core.network.BattleCommandPacket;
import burnedkirby.TurnBasedBattleMod.core.network.BattleQueryPacket;

import cpw.mods.fml.common.network.PacketDispatcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

/**
 * Player/Client side GUIScreen that also manages the player side of the Battle.
 */
public class BattleGui extends GuiScreen {
	
	private int battleID;
	private CombatantInfo player;
	
	private List<CombatantInfo> combatants;
	
	private int bgColor = 0x000000;
	private String info[] = new String[2];
	
	private boolean combatantButton = false;
	private boolean combatantButtonPopulated = false;

	private int command = 0;
	
	public BattleGui(int battleID, CombatantInfo player)
	{
		this.battleID = battleID;
		this.player = player;
	}
	
	/**
	 * Called automatically when the client brings up this GUIScreen.
	 */
	@Override
	public void initGui() {
		info[0] = "";
		info[1] = "";
		getMenu(-2);
		PacketDispatcher.sendPacketToServer(new BattleQueryPacket(battleID,(short) 0).makePacket());
	}
	
	/**
	 * Method that draws the elements of the GUI.
	 */
	@Override
	public void drawScreen(int par1, int par2, float par3) {
		drawRect(0, 0, width, height, 0xa0000000 | bgColor);
		drawRect(0, height*6/10, width, height, 0x70000000 | bgColor);
		
		drawCombatants();

		super.drawScreen(par1, par2, par3);
		
		if(info[0] != "")
			Minecraft.getMinecraft().fontRenderer.drawString(info[0], width/2 - Minecraft.getMinecraft().fontRenderer.getStringWidth(info[0])/2, height - 90, 0xffffffff);
		if(info[1] != "")
			Minecraft.getMinecraft().fontRenderer.drawString(info[1], width/2 - Minecraft.getMinecraft().fontRenderer.getStringWidth(info[1])/2, height - 80, 0xffffffff);
		
	}
	
	/**
	 * Draws the combatant information on the GUIScreen.
	 */
	public void drawCombatants()
	{
//		synchronized(sideOne)
//		{
//			for(int i=0; i<sideOne.size(); i++)
//			{
//				drawCombatant(sideOne.get(i),true,width/8,height/4+10*i,0xffffffff);
//			}
//		}
//		synchronized(sideTwo)
//		{
//			for(int i=0; i<sideTwo.size(); i++)
//			{
//				drawCombatant(sideTwo.get(i),false,width*7/8,height/4+10*i,0xffffffff);
//			}
//		}
		if(!combatantButtonPopulated)
			combatantButtonPopulated = true;
	}
	
	/**
	 * Draws a combatant on the GUIScreen based on the given parameters.
	 * @param id entityID of the combatant.
	 * @param sideOne True if the combatant is on side One.
	 * @param x X coordinate of the drawn information.
	 * @param y Y coordinate of the drawn information.
	 * @param color The color of the drawn information (when applicable).
	 */
	private void drawCombatant(int id, boolean sideOne, int x, int y, int color)
	{
		int nameLength = 0;
//		if(combatantButton)
//		{
//			if(!combatantButtonPopulated)
//			{
//				if(sideOne)
//				{
//					nameLength = Minecraft.getMinecraft().fontRenderer.getStringWidth(sideOneInfo.get(id).name);
//					buttonList.add(new IDSelectionButton(5, id, x - nameLength/2, y, nameLength + 2, 10, sideOneInfo.get(id).name));
//				}
//				else
//				{
//					nameLength = Minecraft.getMinecraft().fontRenderer.getStringWidth(sideTwoInfo.get(id).name);
//					buttonList.add(new IDSelectionButton(5, id, x - nameLength/2, y, nameLength + 2, 10, sideTwoInfo.get(id).name));
//				}
//			}
//		}
//		else
//		{
//			if(sideOne)
//			{
//				nameLength = Minecraft.getMinecraft().fontRenderer.getStringWidth(sideOneInfo.get(id).name);
//				Minecraft.getMinecraft().fontRenderer.drawString(sideOneInfo.get(id).name, x - nameLength/2, y, color);
//			}
//			else
//			{
//				nameLength = Minecraft.getMinecraft().fontRenderer.getStringWidth(sideTwoInfo.get(id).name);
//				Minecraft.getMinecraft().fontRenderer.drawString(sideTwoInfo.get(id).name, x - nameLength/2, y, color);
//			}
//		}
	}

	/**
	 * Displays a menu on the screen.
	 * @param menu The type of menu to display.
	 */
	public void getMenu(int menu) {
		buttonList.clear();
		info[0] = "";
		info[1] = "";
		switch(menu)
		{
		case -2: //Waiting on server
			info[0] = "Waiting for server...";
			break;
		case -1: //Empty menu
			break;
		case 0: //Main menu
			info[0] = "What will you do?";
			buttonList.add(new GuiButton(1, width/6 - 40, height - 40, 80, 20, "Fight"));
			buttonList.add(new GuiButton(2, width*5/6 - 40, height - 40, 80, 20, "Flee"));
			break;
		case 1: //Fight menu
			info[0] = "What will you do?";
			buttonList.add(new GuiButton(3, width/6 - 40, height - 40, 80, 20, "Attack"));
			//controlList.add(new GuiButton(5, width*2/5 - 40, height - 72, 80, 20, "Use Item"));
			buttonList.add(new GuiButton(4, width*3/5 - 40, height - 72, 80, 20, "Change Weapon"));
			buttonList.add(new GuiButton(0, width*5/6 - 40, height - 40, 80, 20, "Cancel"));
			break;
		case 2: //Flee status
			info[0] = "You attempt to flee!";
			info[1] = "Waiting for server...";
			break;
		case 3: //Attack Selection (Handled by actionPerformed method)
			info[0] = "Pick a target!";
			break;
		case 4: //Change weapon menu
			break;
		case 5: //Attack Phase (Handled by actionPerformed method)
			info[0] = "You attack!";
			info[1] = "Waiting for server...";
			break;
		default:
			break;
		}
	}
	
	/**
	 * This method is called automatically when a button is pressed.
	 * Calls getMenu() with the button ID.
	 */
	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id == 2) //Flee
		{
			command = 2;
			PacketDispatcher.sendPacketToServer(new BattleCommandPacket(battleID,0,command).makePacket());
//			waitingPhase = true;
		}
		
		if(button.id == 3) //Show attack menu
		{
			combatantButton = true;
			combatantButtonPopulated = false;
		}
		else
			combatantButton = false;
		
		if(button.id == 5) //Attack phase
		{
			command = 1;
//			attackTarget = ((IDSelectionButton)button).entityID;
//			PacketDispatcher.sendPacketToServer(new BattleCommandPacket(battleID, attackTarget, command).makePacket());
//			waitingPhase = true;
		}
		
		getMenu(button.id);
	}
	
	/**
	 * Called when the GUI is closed, this method removes the reference to itself to remove the
	 * cached information on combatants.
	 */
	@Override
	public void onGuiClosed() {
		ModMain.bg = null;
	}
	
	/**
	 * Determines if the GUIScreen pauses the game in singleplayer.
	 */
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
