package com.systemclient.module;

/**
 * Categories groups belong to in the ClickGUI. Each panel gets its own accent color.
 */
public enum ModuleCategory {
	MOVEMENT("Movement", 0xFF3D85C6),
	INVENTORY("Inventory", 0xFFE67E22),
	RENDER("Render", 0xFF9B59B6),
	MISC("Misc", 0xFF95A5A6);

	private final String displayName;
	private final int accentColor;

	ModuleCategory(String displayName, int accentColor) {
		this.displayName = displayName;
		this.accentColor = accentColor;
	}

	public String getDisplayName() {
		return displayName;
	}

	/** ARGB color drawn as the ClickGUI panel header of this category. */
	public int getAccentColor() {
		return accentColor;
	}
}
