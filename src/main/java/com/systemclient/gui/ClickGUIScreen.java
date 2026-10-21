package com.systemclient.gui;

import com.systemclient.config.ConfigManager;
import com.systemclient.module.Module;
import com.systemclient.module.ModuleCategory;
import com.systemclient.module.ModuleManager;
import com.systemclient.module.settings.BooleanSetting;
import com.systemclient.module.settings.NumberSetting;
import com.systemclient.module.settings.Setting;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * ClickGUI: one draggable panel per {@link ModuleCategory}.
 *
 * <p>Interactions:
 * <ul>
 *   <li>Left click a module name toggles it; right click expands/collapses its settings.</li>
 *   <li>Boolean settings toggle on click; slider settings drag with the mouse.</li>
 *   <li>Click the "Bind:" row, then press any key to assign a module keybind
 *       (ESC/Backspace clears it). Binds and settings are saved immediately.</li>
 *   <li>Drag panel headers to re-arrange; positions persist in the config file.</li>
 * </ul>
 */
public class ClickGUIScreen extends Screen {
	public static final int BG = 0x90070710;
	public static final int PANEL_BG = 0xF012121C;
	public static final int TEXT = 0xFFFFFFFF;
	public static final int TEXT_DIM = 0xFFC8C8C8;
	public static final int ACCENT = 0xFF2AD4FF;
	public static final int ON_COLOR = 0xFF6FE06F;
	public static final int WARN = 0xFFFF5555;
	public static final int HOVER = 0x20FFFFFF;
	public static final int ENABLED_BG = 0x4022AA55;
	public static final int SLIDER_TRACK = 0xFF25252F;

	private static final int KIND_MODULE = 0;
	private static final int KIND_BOOLEAN = 1;
	private static final int KIND_SLIDER = 2;
	private static final int KIND_BIND = 3;
	private static final int KIND_GUI_BIND = 4;
	private static final int ROW_GAP = 4;

	private final List<Panel> panels = new ArrayList<>();

	private Panel draggingPanel;
	private double dragOffsetX;
	private double dragOffsetY;

	private Module listeningModule;
	private boolean listeningGuiBind;

	private NumberSetting draggingSlider;
	private int sliderX;
	private int sliderY;
	private int sliderW;
	private int sliderH;

	public ClickGUIScreen() {
		super(Text.literal("SystemClient - ClickGUI"));
	}

	@Override
	protected void init() {
		if (panels.isEmpty()) {
			int x = ROW_GAP;
			for (ModuleCategory category : ModuleCategory.values()) {
				int[] pos = ConfigManager.getPanelPosition(category);
				if (pos[0] < 0 || pos[1] < 0) {
					pos[0] = x;
					pos[1] = ROW_GAP;
					x += Panel.WIDTH + ROW_GAP;
				}
				panels.add(new Panel(category, pos[0], pos[1]));
			}
		}
	}

	// ---------------------------------------------------------------- rows

	/** One interactive row of a panel, with kind, top-CC y coordinate in screen space, and payload. */
	private record Row(int kind, int top, int height, Module module, Setting setting) {
	}

	private List<Row> rowsOf(Panel panel) {
		List<Row> rows = new ArrayList<>();
		int top = panel.getY() + Panel.HEADER_HEIGHT;
		for (Module module : ModuleManager.INSTANCE.getByCategory(panel.getCategory())) {
			rows.add(new Row(KIND_MODULE, top, Panel.ROW_HEIGHT, module, null));
			top += Panel.ROW_HEIGHT;
			if (module.isExpanded()) {
				for (Setting setting : module.getSettings()) {
					if (setting instanceof BooleanSetting) {
						rows.add(new Row(KIND_BOOLEAN, top, Panel.SETTING_HEIGHT, module, setting));
						top += Panel.SETTING_HEIGHT;
					} else if (setting instanceof NumberSetting) {
						rows.add(new Row(KIND_SLIDER, top, Panel.SETTING_HEIGHT, module, setting));
						top += Panel.SETTING_HEIGHT;
					}
				}
				rows.add(new Row(KIND_BIND, top, Panel.BIND_HEIGHT, module, null));
				top += Panel.BIND_HEIGHT;
			}
		}
		if (panel.getCategory() == ModuleCategory.MISC) {
			rows.add(new Row(KIND_GUI_BIND, top, Panel.BIND_HEIGHT, null, null));
		}
		return rows;
	}

	private int panelHeight(Panel panel) {
		int height = Panel.HEADER_HEIGHT;
		for (Row row : rowsOf(panel)) {
			height += row.height();
		}
		return height;
	}

	// ---------------------------------------------------------------- render

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if ((draggingPanel != null || draggingSlider != null) && !isLeftButtonPressed()) {
			if (draggingPanel != null) {
				ConfigManager.save();
			}
			draggingPanel = null;
			draggingSlider = null;
		}

		context.fill(0, 0, width, height, BG);

		for (Panel panel : panels) {
			List<Row> rows = rowsOf(panel);
			int totalHeight = panelHeight(panel);

			context.fill(panel.getX(), panel.getY() + Panel.HEADER_HEIGHT,
					panel.getX() + Panel.WIDTH, panel.getY() + totalHeight, PANEL_BG);
			context.fill(panel.getX(), panel.getY(),
					panel.getX() + Panel.WIDTH, panel.getY() + Panel.HEADER_HEIGHT,
					panel.getCategory().getAccentColor());
			context.drawText(textRenderer, panel.getCategory().getDisplayName(),
					panel.getX() + 6, panel.getY() + Panel.HEADER_HEIGHT / 2 - 4, TEXT, true);

			for (Row row : rows) {
				boolean hovered = mouseX >= panel.getX() && mouseX < panel.getX() + Panel.WIDTH
						&& mouseY >= row.top() && mouseY < row.top() + row.height();
				drawRow(context, panel, row, hovered, mouseX, mouseY);
			}
		}

		String hint = hintFor();
		if (hint != null) {
			context.fill(0, 4, width, 18, 0x90000000);
			context.drawText(textRenderer, hint, width / 2 - textRenderer.getWidth(hint) / 2, 7, WARN, true);
		}
	}

	private void drawRow(DrawContext context, Panel panel, Row row, boolean hovered, int mouseX, int mouseY) {
		int x = panel.getX();
		switch (row.kind()) {
			case KIND_MODULE -> {
				Module module = row.module();
				if (module.isEnabled()) {
					context.fill(x, row.top(), x + Panel.WIDTH, row.top() + row.height(), ENABLED_BG);
				}
				if (hovered) {
					context.fill(x, row.top(), x + Panel.WIDTH, row.top() + row.height(), HOVER);
				}
				context.drawText(textRenderer, module.getName(), x + 6, row.top() + 5,
						module.isEnabled() ? ON_COLOR : TEXT_DIM, true);
				context.drawText(textRenderer, module.isExpanded() ? "-" : "+", x + Panel.WIDTH - 10,
						row.top() + 5, TEXT_DIM, true);
			}
			case KIND_BOOLEAN -> {
				if (hovered) {
					context.fill(x, row.top(), x + Panel.WIDTH, row.top() + row.height(), HOVER);
				}
				BooleanSetting bool = (BooleanSetting) row.setting();
				context.drawText(textRenderer, bool.getName() + ": " + (bool.getValue() ? "ON" : "OFF"),
						x + 12, row.top() + 3, bool.getValue() ? ON_COLOR : TEXT_DIM, true);
			}
			case KIND_SLIDER -> {
				if (hovered) {
					context.fill(x, row.top(), x + Panel.WIDTH, row.top() + row.height(), HOVER);
				}
				NumberSetting setting = (NumberSetting) row.setting();
				context.drawText(textRenderer, setting.getName(), x + 12, row.top() + 2, TEXT_DIM, true);
				int trackX = x + 4;
				int trackY = row.top() + 11;
				int trackW = Panel.WIDTH - 8;
				context.fill(trackX, trackY, trackX + trackW, trackY + 2, SLIDER_TRACK);
				double fraction = (setting.getValue() - setting.getMin())
						/ (double) Math.max(1, setting.getMax() - setting.getMin());
				int filled = (int) Math.round(trackW * fraction);
				context.fill(trackX, trackY, trackX + filled, trackY + 2, ACCENT);
				String valueText = String.valueOf(setting.getValue());
				context.drawText(textRenderer, valueText, x + Panel.WIDTH - 4 - textRenderer.getWidth(valueText),
						row.top() + 2, ACCENT, true);
			}
			case KIND_BIND -> {
				if (hovered) {
					context.fill(x, row.top(), x + Panel.WIDTH, row.top() + row.height(), HOVER);
				}
				boolean listeningHere = listeningModule == row.module();
				context.drawText(textRenderer, "Bind: " + (listeningHere ? "press any key..." : row.module().getKeyBindName()),
						x + 12, row.top() + 3, listeningHere ? WARN : TEXT, true);
			}
			case KIND_GUI_BIND -> {
				if (hovered) {
					context.fill(x, row.top(), x + Panel.WIDTH, row.top() + row.height(), HOVER);
				}
				String label = "ClickGUI key: " + (listeningGuiBind ? "press any key..." : ModuleManager.INSTANCE.getGuiKeyName());
				context.drawText(textRenderer, label, x + 6, row.top() + 3, listeningGuiBind ? WARN : TEXT, true);
			}
			default -> {
			}
		}
	}

	private String hintFor() {
		if (listeningModule != null) {
			return "Press a key to bind '" + listeningModule.getName() + "' - ESC or Backspace clears";
		}
		if (listeningGuiBind) {
			return "Press a key for the ClickGUI toggle - ESC or Backspace clears";
		}
		return null;
	}

	// ---------------------------------------------------------------- input

	@Override
	public boolean mouseClicked(Click click, boolean doubled) {
		double mx = click.x();
		double my = click.y();
		int button = click.button();

		if (button != 0 && button != 1) {
			return super.mouseClicked(click, doubled);
		}

		for (int i = panels.size() - 1; i >= 0; i--) {
			Panel panel = panels.get(i);
			if (mx >= panel.getX() && mx < panel.getX() + Panel.WIDTH
					&& my >= panel.getY() && my < panel.getY() + Panel.HEADER_HEIGHT) {
				if (button == 0) {
					draggingPanel = panel;
					dragOffsetX = mx - panel.getX();
					dragOffsetY = my - panel.getY();
				}
				return true;
			}
		}

		for (int i = panels.size() - 1; i >= 0; i--) {
			Panel panel = panels.get(i);
			if (mx < panel.getX() || mx >= panel.getX() + Panel.WIDTH) {
				continue;
			}
			for (Row row : rowsOf(panel)) {
				if (my < row.top() || my >= row.top() + row.height()) {
					continue;
				}
				switch (row.kind()) {
					case KIND_MODULE -> {
						if (button == 0) {
							row.module().toggle();
							ConfigManager.save();
						} else if (button == 1) {
							row.module().setExpanded(!row.module().isExpanded());
						}
						return true;
					}
					case KIND_BOOLEAN -> {
						if (button == 0) {
							((BooleanSetting) row.setting()).toggle();
							ConfigManager.save();
						}
						return true;
					}
					case KIND_SLIDER -> {
						if (button == 0) {
							draggingSlider = (NumberSetting) row.setting();
							sliderX = panel.getX() + 4;
							sliderY = row.top() + 11;
							sliderW = Panel.WIDTH - 8;
							sliderH = 2;
							applySlider(my);
						}
						return true;
					}
					case KIND_BIND -> {
						if (button == 0) {
							listeningModule = row.module();
							listeningGuiBind = false;
						}
						return true;
					}
					case KIND_GUI_BIND -> {
						if (button == 0) {
							listeningGuiBind = true;
							listeningModule = null;
						}
						return true;
					}
					default -> {
					}
				}
			}
		}

		return super.mouseClicked(click, doubled);
	}

	@Override
	public boolean mouseDragged(Click click, double offsetX, double offsetY) {
		double mx = click.x();
		double my = click.y();
		if (draggingPanel != null) {
			int newX = (int) Math.round(mx - dragOffsetX);
			int newY = (int) Math.round(my - dragOffsetY);
			draggingPanel.setX(Math.max(0, Math.min(width - Panel.WIDTH, newX)));
			draggingPanel.setY(Math.max(0, Math.min(height - Panel.HEADER_HEIGHT, newY)));
			int[] persisted = ConfigManager.getPanelPosition(draggingPanel.getCategory());
			persisted[0] = draggingPanel.getX();
			persisted[1] = draggingPanel.getY();
			return true;
		}
		if (draggingSlider != null) {
			applySlider(my);
			return true;
		}
		return super.mouseDragged(click, offsetX, offsetY);
	}

	private void applySlider(double mouseY) {
		NumberSetting setting = draggingSlider;
		if (setting == null) {
			return;
		}
		double fraction = (mouseY - sliderY) / (double) Math.max(1, sliderW);
		fraction = Math.max(0.0, Math.min(1.0, fraction));
		setting.setValue(setting.getMin() + (int) Math.round(fraction * (setting.getMax() - setting.getMin())));
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		if (listeningModule != null) {
			int code = input.key();
			if (code == GLFW.GLFW_KEY_ESCAPE || code == GLFW.GLFW_KEY_BACKSPACE) {
				listeningModule.setKeyBind(GLFW.GLFW_KEY_UNKNOWN);
			} else {
				listeningModule.setKeyBind(code);
			}
			listeningModule = null;
			ConfigManager.save();
			return true;
		}
		if (listeningGuiBind) {
			int code = input.key();
			if (code == GLFW.GLFW_KEY_ESCAPE || code == GLFW.GLFW_KEY_BACKSPACE) {
				ModuleManager.INSTANCE.applyGuiKey(GLFW.GLFW_KEY_UNKNOWN);
			} else {
				ModuleManager.INSTANCE.applyGuiKey(code);
			}
			listeningGuiBind = false;
			ConfigManager.save();
			return true;
		}
		return super.keyPressed(input);
	}

	private boolean isLeftButtonPressed() {
		long handle = client.getWindow().getHandle();
		return GLFW.glfwGetMouseButton(handle, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		ConfigManager.save();
		super.close();
	}
}
