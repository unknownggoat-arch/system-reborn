package com.systemclient.module.settings;

/**
 * Base class for module settings. Settings know only their display name;
 * concrete subclasses own the value and the GUI presentation.
 * Values are persisted by {@code com.systemclient.config.ConfigManager}.
 */
public abstract class Setting {
	private final String name;

	protected Setting(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
