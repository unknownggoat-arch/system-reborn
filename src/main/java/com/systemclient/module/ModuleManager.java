package com.systemclient.module;

import com.systemclient.config.ConfigManager;
import com.systemclient.gui.ClickGUIScreen;
import com.systemclient.module.modules.AutoRefillModule;
import com.systemclient.module.modules.HudInfoModule;
import com.systemclient.module.modules.ToggleSprintModule;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns all modules, polls the GUI keybind and module keybinds once per tick,
 * and persists everything through ConfigManager.
 */
public final class ModuleManager {
	public static final ModuleManager INSTANCE = new ModuleManager();
	private static final int DEFAULT_GUI_KEY = GLFW.GLFW_KEY_RIGHT_SHIFT;

	private final List<Module> modules = new ArrayList<>();
	private int guiKey = DEFAULT_GUI_KEY;
	private boolean guiKeyWasDown;

	private ModuleManager() {
	}

	/** Creates the module list and restores saved configuration. Call from the client initializer. */
	public void init() {
		modules.clear();
		register(new AutoRefillModule());
		register(new ToggleSprintModule());
		register(new HudInfoModule());
		ConfigManager.load();
	}

	private void register(Module module) {
		modules.add(module);
	}

	public List<Module> getModules() {
		return modules;
	}

	public List<Module> getByCategory(ModuleCategory category) {
		List<Module> out = new ArrayList<>();
		for (Module module : modules) {
			if (module.getCategory() == category) {
				out.add(module);
			}
		}
		return out;
	}

	public Module getByName(String name) {
		for (Module module : modules) {
			if (module.getName().equals(name)) {
				return module;
			}
		}
		return null;
	}

	// ---------------------------------------------------------------- ClickGUI keybind

	public int getGuiKey() {
		return guiKey;
	}

	/** Sets the GUI key and saves the config. Not used while loading. */
	public void setGuiKey(int keyCode) {
		applyGuiKey(keyCode);
		ConfigManager.save();
	}

	/** Sets the GUI key without saving; used by ConfigManager while loading. */
	public void applyGuiKey(int keyCode) {
		guiKey = (keyCode == GLFW.GLFW_KEY_UNKNOWN || keyCode == 0) ? GLFW.GLFW_KEY_UNKNOWN : keyCode;
	}

	public String getGuiKeyName() {
		return Module.keyName(guiKey);
	}

	// ---------------------------------------------------------------- ticking

	public void tick(MinecraftClient client) {
		if (client.currentScreen != null) {
			return;
		}
		long windowHandle = client.getWindow().getHandle();

		if (guiKey != GLFW.GLFW_KEY_UNKNOWN) {
			boolean down = GLFW.glfwGetKey(windowHandle, guiKey) == GLFW.GLFW_PRESS;
			if (down && !guiKeyWasDown) {
				openGui(client);
			}
			guiKeyWasDown = down;
		}

		for (Module module : modules) {
			module.tickKeybind(windowHandle);
		}
	}

	public void openGui(MinecraftClient client) {
		client.setScreen(new ClickGUIScreen());
	}
}
