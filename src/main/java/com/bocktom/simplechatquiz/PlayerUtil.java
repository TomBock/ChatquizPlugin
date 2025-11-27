package com.bocktom.simplechatquiz;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;

public class PlayerUtil {

	/**
	 * Give or drop an item to a player
	 * Caution: Must run on main thread
	 */
	public static void give(Player player, ItemStack... items) {
		PlayerInventory inventory = player.getInventory();

		HashMap<Integer, ItemStack> result = inventory.addItem(items);

		// Drop in front
		for (Integer i : result.keySet()) {
			player.getWorld().dropItemNaturally(player.getLocation(), result.get(i));
		}
	}
}
