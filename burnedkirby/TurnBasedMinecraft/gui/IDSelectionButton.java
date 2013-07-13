package burnedkirby.TurnBasedMinecraft.gui;

import net.minecraft.client.gui.GuiButton;

/**
 * An extended GuiButton that also contains an entity ID.
 */
public class IDSelectionButton extends GuiButton {

	protected int entityID;

	public IDSelectionButton(int buttonID, int entityID, int x, int y, int width, int height,
			String text) {
		super(buttonID, x, y, width, height, text);
		this.entityID = entityID;
	}

}
