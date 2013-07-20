package burnedkirby.TurnBasedMinecraft.gui;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import burnedkirby.TurnBasedMinecraft.CombatantInfo;
import burnedkirby.TurnBasedMinecraft.ModMain;
import burnedkirby.TurnBasedMinecraft.CombatantInfo.Type;
import burnedkirby.TurnBasedMinecraft.core.ClientProxy;
import burnedkirby.TurnBasedMinecraft.core.network.BattleCommandPacket;
import burnedkirby.TurnBasedMinecraft.core.network.BattleQueryPacket;

import cpw.mods.fml.common.network.PacketDispatcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.scoreboard.ScorePlayerTeam;

/**
 * Player/Client side GUIScreen that also manages the player side of the Battle.
 */
public class BattleGui extends GuiScreen {
	
	private int battleID;
	private CombatantInfo player;
	
	private Map<Integer,CombatantInfo> combatants;
	private int serverBattleSize;
	private boolean updatingCombatants;
	
	private int bgColor = 0x000000;
	private String info[] = new String[2];
	
	private boolean combatantButton = false;
	private boolean combatantButtonPopulated = false;
	private boolean counterMenu = false;
	
	private boolean turnChoiceSent;
	
	private int currentMenu;
	
	private int updateTick;
	private final int updateWaitTime = 400;
	
	private final int nameHeightInterval = 14;
	
	private short timer;
	
	public BattleGui(int battleID, CombatantInfo player)
	{
		this.battleID = battleID;
		this.player = player;
		player.ready = true;
		
		combatants = new TreeMap<Integer,CombatantInfo>();
		updateTick = updateWaitTime;
	}
	
	/**
	 * Called automatically when the client brings up this GUIScreen.
	 */
	@Override
	public void initGui() {
		ModMain.proxy.setGui(this);
		info[0] = "";
		info[1] = "";
		getMenu(-2);
		updatingCombatants = false;
		PacketDispatcher.sendPacketToServer(new BattleQueryPacket(battleID,(short) 0).makePacket());
		turnChoiceSent = false;
		ModMain.proxy.playBattleMusic();
	}
	
	public void checkBattleInfo(boolean forceUpdate, int battleSize, boolean playerPhase, boolean turnChoiceReceived, short timer)
	{
		this.timer = timer;
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
			combatants.put(combatant.id, combatant);
			if(combatants.size() == serverBattleSize)
			{
				updatingCombatants = false;
				PacketDispatcher.sendPacketToServer(new BattleQueryPacket(battleID,(short) 0).makePacket());
			}
		}
	}
	
	public void receiveCombatantHealthInfo(int entityID, float health)
	{
		if(!updatingCombatants)
		{
			CombatantInfo combatant = combatants.get(entityID);
			if(combatant != null)
				combatant.setHealth(health);
		}
	}
	
//	public void updateTurnEnd(boolean serverTurnEnded)
//	{
//		if(serverTurnEnded)
//			turnChoiceSent = false;
//	}
	
	public void update(boolean playerPhase, boolean turnChoiceReceived)
	{
		updateTick = updateWaitTime;
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
		drawRect(0, 0, width, height*5/6, 0xa0000000 | bgColor);
		//drawRect(0, height*6/10, width, height*5/6, 0x70000000 | bgColor);
		
		drawCombatants();

		super.drawScreen(par1, par2, par3);
		
		if(info[0] != "")
			Minecraft.getMinecraft().fontRenderer.drawString(info[0], width/2 - Minecraft.getMinecraft().fontRenderer.getStringWidth(info[0])/2, height - 90, 0xffffffff);
		if(info[1] != "")
			Minecraft.getMinecraft().fontRenderer.drawString(info[1], width/2 - Minecraft.getMinecraft().fontRenderer.getStringWidth(info[1])/2, height - 80, 0xffffffff);
		
		if(!turnChoiceSent)
		{
			String formatString = "";
			if(timer > 14)
				formatString = "\u00A7a";
			else if(timer > 6)
				formatString = "\u00A7e";
			else
				formatString = "\u00A7c";
			
			String timerString = "Time left: " + formatString + (timer / 2);
			
			Minecraft.getMinecraft().fontRenderer.drawString(timerString, width/2 - Minecraft.getMinecraft().fontRenderer.getStringWidth(timerString)/2, 10, 0xffffffff);
		}
		
		updateTick--;
		if(updateTick==0)
		{
			PacketDispatcher.sendPacketToServer(new BattleQueryPacket(battleID,(short) 0).makePacket());
			updateTick = updateWaitTime;
		}
	}
	
	/**
	 * Draws the combatant information on the GUIScreen.
	 */
	public void drawCombatants()
	{
		int x, y1 = height/5, y2 = height/5;
		for(CombatantInfo combatant : combatants.values())
		{
			if(combatant.isSideOne)
			{
				y1 += nameHeightInterval;
				x = width/8;
				drawCombatant(combatant,x,y1,0xFFFFFFFF);
			}
			else
			{
				y2 += nameHeightInterval;
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
		String name = ScorePlayerTeam.formatPlayerName(Minecraft.getMinecraft().theWorld.getScoreboard().getPlayersTeam(combatant.name), combatant.name);
		if(combatantButton)
		{
			if(!combatantButtonPopulated)
			{
				buttonList.add(new IDSelectionButton(counterMenu ? 8 : 5, combatant.id, x - nameLength/2, y, nameLength + 2, 8, name));
			}
		}
		else
		{
			Minecraft.getMinecraft().fontRenderer.drawString(name, x - nameLength/2, y, color);
		}
		
		//Draw Health
		if(combatant.health > 200)
		{
			drawRect(x - nameLength/2 + 16, y + 10, x - nameLength/2 + 19, y + 11, 0xFF0000FF);
			drawRect(x - nameLength/2 + 12, y + 10, x - nameLength/2 + 15, y + 11, 0xFF00FFFF);
			drawRect(x - nameLength/2 + 8, y + 10, x - nameLength/2 + 11, y + 11, 0xFF00FF00);
			drawRect(x - nameLength/2 + 4, y + 10, x - nameLength/2 + 7, y + 11, 0xFFFFFF00);
			drawRect(x - nameLength/2, y + 10, x - nameLength/2 + 3, y + 11, 0xFFFF0000);
			drawRect(x - nameLength/2, y + 9, x - nameLength/2 + (int)((combatant.health - 200.0f) / 100.0f * (float)nameLength), y + 10, 0xFFFFFFFF);
		}
		else if(combatant.health > 100)
		{
			drawRect(x - nameLength/2 + 12, y + 10, x - nameLength/2 + 15, y + 11, 0xFF00FFFF);
			drawRect(x - nameLength/2 + 8, y + 10, x - nameLength/2 + 11, y + 11, 0xFF00FF00);
			drawRect(x - nameLength/2 + 4, y + 10, x - nameLength/2 + 7, y + 11, 0xFFFFFF00);
			drawRect(x - nameLength/2, y + 10, x - nameLength/2 + 3, y + 11, 0xFFFF0000);
			drawRect(x - nameLength/2, y + 9, x - nameLength/2 + (int)((combatant.health - 100.0f) / 100.0f * (float)nameLength), y + 10, 0xFF0000FF);
		}
		else if (combatant.health > 50)
		{
			drawRect(x - nameLength/2 + 8, y + 10, x - nameLength/2 + 11, y + 11, 0xFF00FF00);
			drawRect(x - nameLength/2 + 4, y + 10, x - nameLength/2 + 7, y + 11, 0xFFFFFF00);
			drawRect(x - nameLength/2, y + 10, x - nameLength/2 + 3, y + 11, 0xFFFF0000);
			drawRect(x - nameLength/2, y + 9, x - nameLength/2 + (int)((combatant.health - 50.0f) / 50.0f * (float)nameLength), y + 10, 0xFF00FFFF);
		}
		else if (combatant.health > 20)
		{
			drawRect(x - nameLength/2 + 4, y + 10, x - nameLength/2 + 7, y + 11, 0xFFFFFF00);
			drawRect(x - nameLength/2, y + 10, x - nameLength/2 + 3, y + 11, 0xFFFF0000);
			drawRect(x - nameLength/2, y + 9, x - nameLength/2 + (int)((combatant.health - 20.0f) / 30.0f * (float)nameLength), y + 10, 0xFF00FF00);
		}
		else if (combatant.health > 10)
		{
			drawRect(x - nameLength/2, y + 10, x - nameLength/2 + 3, y + 11, 0xFFFF0000);
			drawRect(x - nameLength/2, y + 9, x - nameLength/2 + (int)((combatant.health - 10.0f) / 10.0f * (float)nameLength), y + 10, 0xFFFFFF00);
		}
		else
		{
			drawRect(x - nameLength/2, y + 9, x - nameLength/2 + (int)(combatant.health / 10.0f * (float)nameLength), y + 10, 0xFFFF0000);
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
			buttonList.add(new GuiButton(1, width*2/6 - 40, height - 40, 80, 20, "Fight"));
			buttonList.add(new GuiButton(2, width*4/6 - 40, height - 40, 80, 20, "Flee"));
			break;
		case 1: //Fight menu
			info[0] = "What will you do?";
			buttonList.add(new GuiButton(3, width/6 - 40, height - 72, 80, 20, "Attack"));
			//controlList.add(new GuiButton(5, width*2/5 - 40, height - 72, 80, 20, "Use Item"));
			buttonList.add(new GuiButton(7, width*2/5 - 40, height - 72, 80, 20, "Dodge/Counter"));
			buttonList.add(new GuiButton(4, width*3/5 - 40, height - 72, 80, 20, "Change Weapon"));
			buttonList.add(new GuiButton(0, width*5/6 - 40, height - 72, 80, 20, "Cancel"));
			break;
		case 2: //Flee status
			info[0] = "You attempt to flee!";
			info[1] = "Waiting for server...";
			break;
		case 3: //Attack Selection (Handled by actionPerformed method)
			info[0] = "Pick a target!";
			break;
		case 4: //Change weapon menu
			info[0] = "Pick your weapon!";
			for(short i=0; i < 9; i++)
			{
				buttonList.add(new ItemSelectionButton(6, width/2 - 88 + i * 20, height - 19, 16, 16, "", i));
			}
			buttonList.add(new GuiButton(0, width/2 - 40, height - 40, 80, 20, "Cancel"));
			break;
		case 5: //Attack Phase (Handled by actionPerformed method)
			info[0] = "You attack!";
			info[1] = "Waiting for server...";
			break;
		case 6: //Weapon Changed
			info[0] = "You switched weapons!";
			info[1] = "Waiting for server...";
			break;
		case 7: //Dodge/Counter menu
			info[0] = "Select your expected attacker!";
			break;
		case 8:
			info[0] = "You prepare to counter that combatant!";
			info[1] = "Waiting for server...";
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
		combatantButton = false;
		
		switch(button.id)
		{
		case 2: //flee
			player.type = Type.FLEE;
			player.target = player.id;
			PacketDispatcher.sendPacketToServer(new BattleCommandPacket(battleID, player).makePacket());
			turnChoiceSent = true;
			break;
		case 3: //attack menu
			combatantButton = true;
			combatantButtonPopulated = false;
			counterMenu = false;
			break;
		case 5: //attack
			player.target = ((IDSelectionButton)button).entityID;
			player.type = Type.ATTACK;
			PacketDispatcher.sendPacketToServer(new BattleCommandPacket(battleID, player).makePacket());
			turnChoiceSent = true;
			break;
		case 6: //change weapon
			short itemStackID = ((ItemSelectionButton)button).getItemStackID();
			Minecraft.getMinecraft().thePlayer.inventory.currentItem = itemStackID;
			
			player.type = Type.CHANGE_WEAPON;
			player.target = player.id;
			PacketDispatcher.sendPacketToServer(new BattleCommandPacket(battleID, player).makePacket());
			turnChoiceSent = true;
			break;
		case 7: //dodge/counter menu
			combatantButton = true;
			combatantButtonPopulated = false;
			counterMenu = true;
			break;
		case 8: //dodge/counter
			player.target = ((IDSelectionButton)button).entityID;
			player.type = Type.DODGE_COUNTER;
			PacketDispatcher.sendPacketToServer(new BattleCommandPacket(battleID, player).makePacket());
			turnChoiceSent = true;
			break;
		default: break;
		}
		
		getMenu(button.id);
	}
	
	/**
	 * Called when the GUI is closed, this method removes the reference to itself to remove the
	 * cached information on combatants.
	 */
	@Override
	public void onGuiClosed() {
		ModMain.proxy.setGui(null);
		ModMain.proxy.stopBattleMusic();
	}
	
	/**
	 * Determines if the GUIScreen pauses the game in singleplayer.
	 */
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	@Override
	protected void keyTyped(char par1, int par2) {
	}
}
