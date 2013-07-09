package burnedkirby.TurnBasedBattleMod.gui;

import java.util.TreeMap;
import java.util.Vector;

import burnedkirby.TurnBasedBattleMod.ModMain;
import burnedkirby.TurnBasedBattleMod.core.BattleCommandPacket;
import burnedkirby.TurnBasedBattleMod.core.BattleQueryPacket;

import cpw.mods.fml.common.network.PacketDispatcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class BattleGui extends GuiScreen {
	
	private int battleID;
	private int playerID;
	
	private Vector<Integer> sideOne;
	private Vector<Integer> sideTwo;
	private boolean updatingListOne;
	private boolean updatingListTwo;
	private int sideOneSize;
	private int sideTwoSize;
	
	private TreeMap<Integer,EntityInfo> sideOneInfo;
	private TreeMap<Integer,EntityInfo> sideTwoInfo;
	
	private int bgColor = 0x000000;
	private String info[] = new String[2];
	
	private int updateTick;
	private final int updateTime = 800;
	
	private boolean combatantButton = false;
	private boolean combatantButtonPopulated = false;
	
	private boolean waitingPhase = false;
	private int command = 0;
	private int attackTarget = 0;
	
	public BattleGui(int battleID, int playerID)
	{
		this.battleID = battleID;
		this.playerID = playerID;
		
		sideOne = new Vector<Integer>();
		sideTwo = new Vector<Integer>();
		updatingListOne = false;
		updatingListTwo = false;
		sideOneSize = 0;
		sideTwoSize = 0;
		
		sideOneInfo = new TreeMap<Integer,EntityInfo>();
		sideTwoInfo = new TreeMap<Integer,EntityInfo>();
		
		updateTick = updateTime;
	}
	
	@Override
	public void initGui() {
		info[0] = "";
		info[1] = "";
		getMenu(-2);
		PacketDispatcher.sendPacketToServer(new BattleQueryPacket(battleID,0).makePacket());
	}
	
	public void checkBattleSize(int one, int two)
	{
		System.out.println("CheckBattleSize called with params (" + one +","+two+")");
		boolean updateStateChanged = false;
		
		if((!updatingListOne && !updatingListTwo) && (one == 0 || two == 0))
		{
			//TODO
			Minecraft.getMinecraft().setIngameFocus();
		}

		synchronized(sideOne)
		{
			if(!updatingListOne && sideOne.size() != one) //TODO reset entity id lists and repopulate lists
			{
				updatingListOne = true;
				updateStateChanged = true;
				sideOneSize = one;
				sideOne.clear();
				sideOneInfo.clear();
				PacketDispatcher.sendPacketToServer(new BattleQueryPacket(battleID,1).makePacket());
			}
		}
		synchronized(sideTwo)
		{
			
			if(!updatingListOne && sideTwo.size() != two)
			{
				updatingListTwo = true;
				updateStateChanged = true;
				sideTwoSize = two;
				sideTwo.clear();
				sideTwoInfo.clear();
				PacketDispatcher.sendPacketToServer(new BattleQueryPacket(battleID,2).makePacket());
			}
		}
		
		if(updateStateChanged)
		{
			updateTick = updateTime;
			getMenu(-2);
		}
	}
	
	public void receiveCombatant(int id, boolean isSideOne, String name)
	{
		boolean updateStateChanged = false;
		
		if(updatingListOne && isSideOne)
		{
			synchronized(sideOne)
			{
				sideOne.add(id);
				sideOneInfo.put(id, new EntityInfo(name));
			}
		}
		else if(updatingListTwo && !isSideOne)
		{
			synchronized(sideTwo)
			{
				sideTwo.add(id);
				sideTwoInfo.put(id, new EntityInfo(name));
			}
		}
		
		synchronized(sideOne)
		{
			if(updatingListOne && sideOne.size() == sideOneSize)
			{
				updatingListOne = false;
				updateStateChanged = true;
			}
		}
		synchronized(sideTwo)
		{
			if(updatingListTwo && sideTwo.size() == sideTwoSize)
			{
				updatingListTwo = false;
				updateStateChanged = true;
			}
		}
		
		if(updateStateChanged)
		{
			if(!updatingListOne && !updatingListTwo)
			{
				short empty = 0x0;
				synchronized(sideOne)
				{
					empty |= sideOne.size() == 0 ? 0x1 : 0x0;
				}
				synchronized(sideTwo)
				{
					empty |= sideTwo.size() == 0 ? 0x2 : 0x0;
				}
				
				if(empty != 0x0)
				{
					updateTick = updateTime;
					PacketDispatcher.sendPacketToServer(new BattleQueryPacket(battleID,0).makePacket());
				}
				else
				{
					getMenu(0);
				}
			}
		}
	}
	
	public void battlePhaseEnded()
	{
		if(waitingPhase)
		{
			waitingPhase = false;
			getMenu(0);
		}
	}
	
	private void update()
	{
		updateTick--;
		if(updateTick == 0)
		{
			updateTick = updateTime;
			PacketDispatcher.sendPacketToServer(new BattleQueryPacket(battleID,0).makePacket());
			
//			if(waitingPhase)
//				PacketDispatcher.sendPacketToServer(new BattleCommandPacket(battleID,attackTarget,command).makePacket());
		}
	}
	
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
		
		update();
	}
	
	public void drawCombatants()
	{
		synchronized(sideOne)
		{
			for(int i=0; i<sideOne.size(); i++)
			{
				drawCombatant(sideOne.get(i),true,width/8,height/4+10*i,0xffffffff);
			}
		}
		synchronized(sideTwo)
		{
			for(int i=0; i<sideTwo.size(); i++)
			{
				drawCombatant(sideTwo.get(i),false,width*7/8,height/4+10*i,0xffffffff);
			}
		}
		if(!combatantButtonPopulated)
			combatantButtonPopulated = true;
	}
	
	private void drawCombatant(int id, boolean sideOne, int x, int y, int color)
	{
		int nameLength = 0;
		if(combatantButton)
		{
			if(!combatantButtonPopulated)
			{
				int width;
				if(sideOne)
				{
					width = Minecraft.getMinecraft().fontRenderer.getStringWidth(sideOneInfo.get(id).name);
					buttonList.add(new IDSelectionButton(5, id, x, y, width + 2, 10, sideOneInfo.get(id).name));
				}
				else
				{
					width = Minecraft.getMinecraft().fontRenderer.getStringWidth(sideTwoInfo.get(id).name);
					buttonList.add(new IDSelectionButton(5, id, x, y, width + 2, 10, sideTwoInfo.get(id).name));
				}
			}
		}
		else
		{
			if(sideOne)
			{
				nameLength = Minecraft.getMinecraft().fontRenderer.getStringWidth(sideOneInfo.get(id).name);
				Minecraft.getMinecraft().fontRenderer.drawString(sideOneInfo.get(id).name, x - nameLength/2, y, color);
			}
			else
			{
				nameLength = Minecraft.getMinecraft().fontRenderer.getStringWidth(sideTwoInfo.get(id).name);
				Minecraft.getMinecraft().fontRenderer.drawString(sideTwoInfo.get(id).name, x - nameLength/2, y, color);
			}
		}
	}

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
	
	@Override
	protected void actionPerformed(GuiButton button) {
		if(button.id == 2) //Flee
		{
			command = 2;
			PacketDispatcher.sendPacketToServer(new BattleCommandPacket(battleID,0,command).makePacket());
			waitingPhase = true;
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
			attackTarget = ((IDSelectionButton)button).entityID;
			PacketDispatcher.sendPacketToServer(new BattleCommandPacket(battleID, attackTarget, command).makePacket());
			waitingPhase = true;
		}
		
		getMenu(button.id);
	}
	
	@Override
	public void onGuiClosed() {
		ModMain.bg = null;
	}
	
	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
}
