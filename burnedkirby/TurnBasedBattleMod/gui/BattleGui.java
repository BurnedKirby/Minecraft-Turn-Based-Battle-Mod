package burnedkirby.TurnBasedBattleMod.gui;

import java.util.LinkedList;
import java.util.List;

import burnedkirby.TurnBasedBattleMod.CombatantInfo;
import burnedkirby.TurnBasedBattleMod.CombatantInfo.Type;
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
	private int serverBattleSize;
	private boolean updatingCombatants;
	
	private int bgColor = 0x000000;
	private String info[] = new String[2];
	
	private boolean combatantButton = false;
	private boolean combatantButtonPopulated = false;
	
	private boolean turnChoiceSent;
	
	private int currentMenu;
	
	public BattleGui(int battleID, CombatantInfo player)
	{
		this.battleID = battleID;
		this.player = player;
		player.ready = true;
		
		combatants = new LinkedList<CombatantInfo>();
	}
	
	/**
	 * Called automatically when the client brings up this GUIScreen.
	 */
	@Override
	public void initGui() {
		ModMain.bg = this;
		info[0] = "";
		info[1] = "";
		getMenu(-2);
		updatingCombatants = false;
		PacketDispatcher.sendPacketToServer(new BattleQueryPacket(battleID,(short) 0).makePacket());
		turnChoiceSent = false;
	}
	
	public void checkBattleInfo(boolean forceUpdate, int battleSize, boolean playerPhase, boolean turnChoiceReceived)
	{
		serverBattleSize = battleSize;
		if((!updatingCombatants && combatants.size() != battleSize) || forceUpdate)
		{
			combatants.clear();
			updatingCombatants = true;
			PacketDispatcher.sendPacketToServer(new BattleQueryPacket(battleID,(short) 1).makePacket());
		}
		update(playerPhase, turnChoiceReceived);
	}
	
	public void receiveCombatant(CombatantInfo combatant)
	{
		if(updatingCombatants)
		{
			combatants.add(combatant);
			if(combatants.size() == serverBattleSize)
			{
				updatingCombatants = false;
				PacketDispatcher.sendPacketToServer(new BattleQueryPacket(battleID,(short) 0).makePacket());
			}
		}
	}
	
//	public void updateTurnEnd(boolean serverTurnEnded)
//	{
//		if(serverTurnEnded)
//			turnChoiceSent = false;
//	}
	
	public void update(boolean playerPhase, boolean turnChoiceReceived)
	{
		System.out.println("Update called, turnSentBool is" + turnChoiceSent);
		if(playerPhase && !updatingCombatants)
		{
			if(turnChoiceReceived && !turnChoiceSent)
			{
				turnChoiceSent = true;
				getMenu(-2);
			}
			else if(!turnChoiceReceived && (turnChoiceSent || currentMenu == -2))
			{
				turnChoiceSent = false;
				getMenu(0);
			}
			else if(currentMenu == -2 && !turnChoiceSent)
				getMenu(0);
		}
		else
			getMenu(-2);
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
		int x, y1 = height/4, y2 = height/4;
		for(CombatantInfo combatant : combatants)
		{
			if(combatant.isSideOne)
			{
				y1 += 10;
				x = width/8;
				drawCombatant(combatant,x,y1,0xFFFFFFFF);
			}
			else
			{
				y2 += 10;
				x = width * 7 / 8;
				drawCombatant(combatant,x,y2,0xFFFFFFFF);
			}
		}
		
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
	private void drawCombatant(CombatantInfo combatant, int x, int y, int color)
	{
		int nameLength = Minecraft.getMinecraft().fontRenderer.getStringWidth(combatant.name);
		if(combatantButton)
		{
			if(!combatantButtonPopulated)
			{
				buttonList.add(new IDSelectionButton(5, combatant.id, x - nameLength/2, y, nameLength + 2, 10, combatant.name));
			}
		}
		else
		{
			Minecraft.getMinecraft().fontRenderer.drawString(combatant.name, x - nameLength/2, y, color);
		}
	}

	/**
	 * Displays a menu on the screen.
	 * @param menu The type of menu to display.
	 */
	public void getMenu(int menu) {
		buttonList.clear();
		info[0] = "";
		info[1] = "";
		currentMenu = menu;
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
			player.type = Type.FLEE;
			player.target = player.id;
			PacketDispatcher.sendPacketToServer(new BattleCommandPacket(battleID, player).makePacket());
			turnChoiceSent = true;
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
			player.target = ((IDSelectionButton)button).entityID;
			player.type = Type.ATTACK;
			PacketDispatcher.sendPacketToServer(new BattleCommandPacket(battleID, player).makePacket());
			turnChoiceSent = true;
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
