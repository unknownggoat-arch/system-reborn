package com.systemclient.gui;

import com.systemclient.module.ModuleCategory;

/**
 * Geometry of one ClickGUI panel. The panel's position is shared with
 * ConfigManager, so dragging a panel persists across restarts.
 */
public final class Panel {
	public static final int WIDTH = 112;
	public static final int HEADER_HEIGHT = 18;
	public static final int ROW_HEIGHT = 18;
	public static final int SETTING_HEIGHT = 15;
	public static final int BIND_HEIGHT = 15;

	private final ModuleCategory category;
	private int x;
	private int y;

	public Panel(ModuleCategory category, int x, int y) {
		this.category = category;
		this.x = x;
		this.y = y;
	}

	public ModuleCategory getCategory() {
		return category;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
}
