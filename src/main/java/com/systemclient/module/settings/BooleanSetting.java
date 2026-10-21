package com.systemclient.module.settings;

public final class BooleanSetting extends Setting {
	private boolean value;

	public BooleanSetting(String name, boolean value) {
		super(name);
		this.value = value;
	}

	public boolean getValue() {
		return value;
	}

	public void setValue(boolean value) {
		this.value = value;
	}

	public void toggle() {
		value = !value;
	}
}
