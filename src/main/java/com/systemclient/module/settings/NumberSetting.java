package com.systemclient.module.settings;

/**
 * Integer slider setting. Values are always clamped into [min, max],
 * so dragging the ClickGUI slider can never push them out of range.
 */
public final class NumberSetting extends Setting {
	private final int min;
	private final int max;
	private int value;

	public NumberSetting(String name, int value, int min, int max) {
		super(name);
		this.min = min;
		this.max = max;
		setValue(value);
	}

	public int getValue() {
		return value;
	}

	public int getMin() {
		return min;
	}

	public int getMax() {
		return max;
	}

	public void setValue(int value) {
		this.value = Math.max(min, Math.min(max, value));
	}
}
