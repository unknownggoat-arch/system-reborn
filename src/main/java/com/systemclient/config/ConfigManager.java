package com.systemclient.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.systemclient.SystemClient;
import com.systemclient.module.Module;
import com.systemclient.module.ModuleCategory;
import com.systemclient.module.ModuleManager;
import com.systemclient.module.settings.BooleanSetting;
import com.systemclient.module.settings.NumberSetting;
import com.systemclient.module.settings.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;

/**
 * Persists module enabled-state, custom keybinds, setting values and
 * ClickGUI panel positions as pretty-printed JSON in {@code config/systemclient.json}.
 * Unknown/removed settings or modules are skipped on load, so config files
 * survive updates without breaking.
 */
public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Map<ModuleCategory, int[]> PANEL_POSITIONS = new EnumMap<>(ModuleCategory.class);

	private ConfigManager() {
	}

	public static Path getConfigFile() {
		return FabricLoader.getInstance().getConfigDir().resolve("systemclient.json");
	}

	/**
	 * Live-editable panel position for a category. Mutate the returned array
	 * directly (the ClickGUI does this while dragging); {@link #save()} writes it.
	 */
	public static int[] getPanelPosition(ModuleCategory category) {
		return PANEL_POSITIONS.computeIfAbsent(category, c -> new int[]{-1, -1});
	}

	// ---------------------------------------------------------------- save

	public static void save() {
		JsonObject root = new JsonObject();

		root.addProperty("guiKey", ModuleManager.INSTANCE.getGuiKey());

		JsonObject positions = new JsonObject();
		for (Map.Entry<ModuleCategory, int[]> entry : PANEL_POSITIONS.entrySet()) {
			JsonObject pos = new JsonObject();
			pos.addProperty("x", entry.getValue()[0]);
			pos.addProperty("y", entry.getValue()[1]);
			positions.add(entry.getKey().name(), pos);
		}
		root.add("panelPositions", positions);

		JsonArray moduleArray = new JsonArray();
		for (Module module : ModuleManager.INSTANCE.getModules()) {
			JsonObject obj = new JsonObject();
			obj.addProperty("name", module.getName());
			obj.addProperty("enabled", module.isEnabled());
			obj.addProperty("key", module.getKeyBind());

			JsonObject settings = new JsonObject();
			for (Setting setting : module.getSettings()) {
				if (setting instanceof BooleanSetting bool) {
					settings.addProperty(setting.getName(), bool.getValue());
				} else if (setting instanceof NumberSetting number) {
					settings.addProperty(setting.getName(), number.getValue());
				}
			}
			obj.add("settings", settings);
			moduleArray.add(obj);
		}
		root.add("modules", moduleArray);

		try {
			Files.writeString(getConfigFile(), GSON.toJson(root));
		} catch (IOException e) {
			SystemClient.LOGGER.error("Failed to save SystemClient config", e);
		}
	}

	// ---------------------------------------------------------------- load

	public static void load() {
		Path file = getConfigFile();
		if (!Files.exists(file)) {
			return;
		}
		try {
			JsonObject root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();

			if (root.has("guiKey")) {
				ModuleManager.INSTANCE.applyGuiKey(root.get("guiKey").getAsInt());
			}

			if (root.has("panelPositions")) {
				JsonObject positions = root.getAsJsonObject("panelPositions");
				for (Map.Entry<String, JsonElement> entry : positions.entrySet()) {
					try {
						ModuleCategory category = ModuleCategory.valueOf(entry.getKey());
						JsonObject pos = entry.getValue().getAsJsonObject();
						int[] target = getPanelPosition(category);
						target[0] = pos.get("x").getAsInt();
						target[1] = pos.get("y").getAsInt();
					} catch (IllegalArgumentException ignored) {
						// unknown category in an old config -> skip
					}
				}
			}

			if (root.has("modules")) {
				for (JsonElement element : root.getAsJsonArray("modules")) {
					JsonObject obj = element.getAsJsonObject();
					Module module = ModuleManager.INSTANCE.getByName(obj.get("name").getAsString());
					if (module == null) {
						continue;
					}
					module.setEnabled(obj.get("enabled").getAsBoolean());
					if (obj.has("key")) {
						module.setKeyBind(obj.get("key").getAsInt());
					}
					if (obj.has("settings")) {
						JsonObject settings = obj.getAsJsonObject("settings");
						for (Setting setting : module.getSettings()) {
							if (!settings.has(setting.getName())) {
								continue;
							}
							JsonElement value = settings.get(setting.getName());
							if (setting instanceof BooleanSetting bool) {
								bool.setValue(value.getAsBoolean());
							} else if (setting instanceof NumberSetting number) {
								number.setValue(value.getAsInt());
							}
						}
					}
				}
			}

			SystemClient.LOGGER.info("Loaded SystemClient config from {}", file.getFileName());
		} catch (Exception e) {
			SystemClient.LOGGER.warn("Could not read SystemClient config, using defaults", e);
		}
	}
}
