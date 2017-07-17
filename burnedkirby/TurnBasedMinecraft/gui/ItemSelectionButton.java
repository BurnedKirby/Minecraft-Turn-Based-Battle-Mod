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
	public void drawButton(Minecraft par1Minecraft, int mouseX, int mouseY, float partialTicks) {
		if(this.visible)
		{
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			if(this.hovered) //If mouse is hovering over button
			{
				drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0x80ffffff);
			}
		}
	}
	
	public short getItemStackID()
	{
		return itemStackID;
	}
}
