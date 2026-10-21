package com.systemclient.module;

import com.systemclient.module.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class of every module.
 *
 * <p>Keybinds are our own persisted GLFW-key bindings instead of vanilla
 * registered {@code KeyBinding}s, so they can be reassigned and saved freely
 * from the ClickGUI without re-registration or double-binding quirks.
 * {@code tickKeybind} is invoked once per client tick by {@link ModuleManager}
 * and toggles the module on key press (edge detected), so it never fires
 * multiple times while a key is held.
 */
public abstract class Module {
	private final String name;
	private final String description;
	private final ModuleCategory category;
	private final List<Setting> settings = new ArrayList<>();

	private boolean enabled;
	private int keyBind = GLFW.GLFW_KEY_UNKNOWN;
	private boolean keyWasDown;

	/** ClickGUI-only state: whether the module's settings rows are visible. Not persisted. */
	private boolean settingsExpanded;

	protected Module(String name, String description, ModuleCategory category) {
		this.name = name;
		this.description = description;
		this.category = category;
	}

	protected final void addSettings(Setting... settings) {
		for (Setting setting : settings) {
			this.settings.add(setting);
		}
	}

	// ---------------------------------------------------------------- lifecycle

	/** Called once per client tick while the module is enabled. */
	public void onTick(MinecraftClient client) {
	}

	protected void onEnable() {
	}

	protected void onDisable() {
	}

	public void toggle() {
		setEnabled(!enabled);
	}

	public final void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return;
		}
		this.enabled = enabled;
		if (enabled) {
			onEnable();
		} else {
			onDisable();
		}
	}

	// ---------------------------------------------------------------- keybinds

	public int getKeyBind() {
		return keyBind;
	}

	public void setKeyBind(int keyCode) {
		keyBind = keyCode;
	}

	public String getKeyBindName() {
		return keyName(keyBind);
	}

	public static String keyName(int keyCode) {
		if (keyCode == GLFW.GLFW_KEY_UNKNOWN || keyCode == 0) {
			return "None";
		}
		return InputUtil.Type.KEYSYM.createFromCode(keyCode).getLocalizedText().getString();
	}

	/** Edge-detected toggle from the module keybind; called by ModuleManager every tick. */
	void tickKeybind(long windowHandle) {
		if (keyBind == GLFW.GLFW_KEY_UNKNOWN || keyBind == 0) {
			keyWasDown = false;
			return;
		}
		boolean down = GLFW.glfwGetKey(windowHandle, keyBind) == GLFW.GLFW_PRESS;
		if (down && !keyWasDown) {
			toggle();
		}
		keyWasDown = down;
	}

	// ---------------------------------------------------------------- accessors

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public ModuleCategory getCategory() {
		return category;
	}

	public List<Setting> getSettings() {
		return settings;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean isExpanded() {
		return settingsExpanded;
	}

	public void setExpanded(boolean expanded) {
		settingsExpanded = expanded;
	}
}
