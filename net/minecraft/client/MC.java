package net.minecraft.client;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ChatComponentText;

import static net.minecraft.client.Minecraft.theMinecraft;

public final class MC {

	private MC() {throw null;}

	public static FontRenderer getFontRenderer() {
		return theMinecraft.fontRendererObj;
	}

	public static TextureManager getTextureManager() {
		return theMinecraft.getTextureManager();
	}

	public static RenderItem getRenderItem() {
		return theMinecraft.getRenderItem();
	}

	public static EntityPlayerSP getPlayer() {
		return theMinecraft.thePlayer;
	}

	public static Minecraft i() {
		return Minecraft.getMinecraft();
	}

	public static void chat(String s) {
		MC.i().ingameGUI.getChatGUI().printChatMessage(new ChatComponentText(s));
	}

}