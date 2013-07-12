package burnedkirby.TurnBasedBattleMod.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class ItemSelectionButton extends GuiButton {
	
	private int itemStackID;

	public ItemSelectionButton(int buttonID, int x, int y, String text, int itemStackID) {
		super(buttonID, x, y, text);
		this.itemStackID = itemStackID;
	}
	
	public ItemSelectionButton(int buttonID, int x, int y, int width, int height, String text, int itemStackID)
	{
		super(buttonID, x, y, width, height, text);
		this.itemStackID = itemStackID;
	}
	
	@Override
	public void drawButton(Minecraft par1Minecraft, int par2, int par3) {
		if(this.drawButton)
		{
			this.field_82253_i = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;
			if(this.field_82253_i) //If mouse is hovering over button
			{
				drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 0x80ffffff);
			}
		}
	}
	
	public int getItemStackID()
	{
		return itemStackID;
	}
}
