package com.systemclient.module.modules;

import com.systemclient.module.Module;
import com.systemclient.module.ModuleCategory;
import com.systemclient.module.settings.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;

/**
 * Auto Refill: watches the hotbar slots in the user-selected range
 * ({@code Min slot} .. {@code Max slot}, both 0-8 and freely configurable)
 * and swaps in a fresh stack of the same item from the main inventory once a
 * hotbar stack drops to the configured count.
 *
 * <p>It uses the vanilla slot-click packet ({@code SlotActionType.SWAP} through
 * the normal interaction manager), which is the exact packet the client sends
 * when you click in your own inventory - so it respects plain vanilla
 * server-side inventory handling, with a configurable tick delay between swaps.
 */
public class AutoRefillModule extends Module {
	private final NumberSetting minSlot = new NumberSetting("Min slot (0-8)", 0, 0, 8);
	private final NumberSetting maxSlot = new NumberSetting("Max slot (0-8)", 8, 0, 8);
	private final NumberSetting threshold = new NumberSetting("Refill at count", 8, 1, 64);
	private final NumberSetting delay = new NumberSetting("Delay (ticks)", 10, 2, 40);

	private int cooldown;

	public AutoRefillModule() {
		super("Auto Refill",
				"Keeps hotbar slots Min..Max (0-8, first slot = 0) stocked from spare stacks in your inventory.",
				ModuleCategory.INVENTORY);
		addSettings(minSlot, maxSlot, threshold, delay);
	}

	@Override
	public void onTick(MinecraftClient client) {
		if (client.player == null || client.currentScreen != null) {
			return;
		}
		if (!(client.player.currentScreenHandler instanceof PlayerScreenHandler)) {
			return;
		}
		if (cooldown > 0) {
			cooldown--;
			return;
		}

		int low = Math.min(minSlot.getValue(), maxSlot.getValue());
		int high = Math.max(minSlot.getValue(), maxSlot.getValue());
		int limit = threshold.getValue();

		for (int hotbar = low; hotbar <= high; hotbar++) {
			ItemStack current = client.player.getInventory().getStack(hotbar);
			if (current.isEmpty() || current.getCount() > limit) {
				continue;
			}

			// PlayerScreenHandler main inventory occupies handler slots 9-35.
			int best = -1;
			for (int slot = 9; slot <= 35; slot++) {
				ItemStack spare = client.player.getInventory().getStack(slot);
				if (!spare.isEmpty() && spare.isOf(current.getItem()) && spare.getCount() > current.getCount()) {
					best = slot;
				}
			}
			if (best == -1) {
				continue;
			}

			// SWAP exchanges the source slot with hotbar slot number `button` (0-8).
			client.interactionManager.clickSlot(
					client.player.currentScreenHandler.syncId,
					best,
					hotbar,
					SlotActionType.SWAP,
					client.player);
			cooldown = delay.getValue();
			return;
		}
	}
}
