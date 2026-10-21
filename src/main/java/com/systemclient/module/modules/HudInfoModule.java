package com.systemclient.module.modules;

import com.systemclient.gui.ClickGUIScreen;
import com.systemclient.module.Module;
import com.systemclient.module.ModuleCategory;
import com.systemclient.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.font.TextRenderer;

/**
 * HUD Info: draws a small overlay with mod name/version, FPS, player
 * position and the current ClickGUI key while enabled. Rendering runs from
 * the HudRenderCallback registered in {@code SystemClient}.
 */
public class HudInfoModule extends Module {
	public static HudInfoModule INSTANCE;

	private static final int LINE_HEIGHT = 11;

	public HudInfoModule() {
		super("HUD Info", "Overlays mod info: FPS, position and ClickGUI keybind.",
				ModuleCategory.RENDER);
		INSTANCE = this;
	}

	public void renderHud(DrawContext context, RenderTickCounter tickCounter) {
		if (!isEnabled()) {
			return;
		}
		MinecraftClient client = MinecraftClient.getInstance();
		TextRenderer renderer = client.textRenderer;

		int y = 4;
		context.drawText(renderer, "SystemClient v1.0.0", 4, y, ClickGUIScreen.ACCENT, true);
		y += LINE_HEIGHT;
		context.drawText(renderer, "FPS: " + client.getCurrentFps(), 4, y, ClickGUIScreen.TEXT, true);
		y += LINE_HEIGHT;
		if (client.player != null) {
			context.drawText(renderer,
					"XYZ: " + client.player.getBlockX() + " / " + client.player.getBlockY()
							+ " / " + client.player.getBlockZ(),
					4, y, ClickGUIScreen.TEXT, true);
			y += LINE_HEIGHT;
		}
		context.drawText(renderer, "GUI: " + ModuleManager.INSTANCE.getGuiKeyName(),
				4, y, ClickGUIScreen.TEXT, true);
	}
}
