package com.systemclient;

import com.systemclient.module.ModuleManager;
import com.systemclient.module.modules.HudInfoModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point. Boots the module manager (which loads the config),
 * hooks per-tick keybind polling, and registers the HUD callback.
 */
public class SystemClient implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("SystemClient");

	@Override
	public void onInitializeClient() {
		ModuleManager.INSTANCE.init();

		ClientTickEvents.END_CLIENT_TICK.register(ModuleManager.INSTANCE::tick);
		HudRenderCallback.EVENT.register((context, tickCounter) -> HudInfoModule.INSTANCE.renderHud(context, tickCounter));

		LOGGER.info("SystemClient ready: {} modules registered.", ModuleManager.INSTANCE.getModules().size());
	}
}
