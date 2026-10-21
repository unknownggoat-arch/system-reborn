package com.systemclient.module.modules;

import com.systemclient.module.Module;
import com.systemclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;

/**
 * Toggle Sprint: sprints automatically while you are moving forward, without
 * needing to hold the sprint key. Implemented with the vanilla sprint
 * key's pressed state only - it changes nothing about how you interact
 * with blocks, entities or the server.
 */
public class ToggleSprintModule extends Module {
	public ToggleSprintModule() {
		super("Toggle Sprint",
				"Sprints whenever you move forward, without holding the sprint key.",
				ModuleCategory.MOVEMENT);
	}

	@Override
	public void onTick(MinecraftClient client) {
		if (client.player == null || client.currentScreen != null) {
			return;
		}
		client.options.sprintKey.setPressed(client.options.forwardKey.isPressed());
	}

	@Override
	protected void onDisable() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			client.options.sprintKey.setPressed(client.options.forwardKey.isPressed());
		}
	}
}
