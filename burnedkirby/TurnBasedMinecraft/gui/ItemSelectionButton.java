package burnedkirby.TurnBasedMinecraft.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

public class ItemSelectionButton extends GuiButton {
	
	private short itemStackID;

	public ItemSelectionButton(int buttonID, int x, int y, String text, short itemStackID) {
		super(buttonID, x, y, text);
		this.itemStackID = itemStackID;
	}
	
	public ItemSelectionButton(int buttonID, int x, int y, int width, int height, String text, short itemStackID)
	{
		super(buttonID, x, y, width, height, text);
		this.itemStackID = itemStackID;
	}
	
	@Override
	public void drawButton(Minecraft par1Minecraft, int mouseX, int mouseY) {
		if(this.visible)
		{
			this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
			if(this.field_146123_n) //If mouse is hovering over button
			{
				drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 0x80ffffff);
			}
		}
	}
	
	public short getItemStackID()
	{
		return itemStackID;
	}
}
