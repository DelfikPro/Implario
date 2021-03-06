package net.minecraft.client.gui.element;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.MCFontRenderer;

public class RunnableTab implements ITab {

	private final GuiButton button;
	private final Runnable runnable;
	private boolean enabled = false;

	public RunnableTab(String name, Runnable runnable, int id, int x, int y) {
		MCFontRenderer fr = Minecraft.getMinecraft().fontRenderer;
		this.button = new GuiButton(id, x, y, fr.getStringWidth(name) + 12, 18, name);
		this.runnable = runnable;
	}

	@Override
	public void focus() {
		if (enabled) return;
		runnable.run();
		enabled = true;
	}

	@Override
	public void unfocus() {
		enabled = false;
	}

	@Override
	public GuiButton getButton() {
		return button;
	}

}
