package com.bocktom.simplechatquiz;

import com.bocktom.simplechatquiz.serialization.Config;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Rewards extends ArrayList<Rewards.Reward> {

	public static Rewards fromConfig(String path) {
		if(!Config.rewards.get.contains(path)) {
			return new Rewards();
		}

		List<Map<?, ?>> section = Config.rewards.get.getMapList(path);
		Rewards rewards = new Rewards();

		for (Map<?, ?> entry : section) {
			String name = (String) entry.get("name");
			String type = (String) entry.get("type");

			if(type.equals("command")) {
				rewards.add(new CommandReward(name, (String) entry.get("command")));
			} else if(type.equals("item")) {
				rewards.add(new ItemReward(name, (ItemStack) entry.get("item")));
			} else {
				throw new IllegalArgumentException("Unknown reward type: " + type);
			}
		}
		return rewards;
	}

	public String getName() {
		return this.stream().map(reward -> reward.name).reduce((a, b) -> a + ", " + b).orElse("No Reward");
	}

	public void give(Player player) {
		for(Reward reward : this) {
			reward.give(player);
		}
	}


	public abstract static class Reward {
		public String name;
		public abstract void give(Player player);
	}

	public static class ItemReward extends Reward {
		public ItemStack item;

		public ItemReward(String name, ItemStack item) {
			this.name = name;
			this.item = item;
		}

		@Override
		public void give(Player player) {
			PlayerUtil.give(player, item);
		}
	}

	public static class CommandReward extends Reward {
		public String command;

		public CommandReward(String name, String command) {
			this.name = name;
			this.command = command;
		}

		@Override
		public void give(Player player) {
			String executedCommand = command.replace("%player%", player.getName());
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), executedCommand);
		}
	}
}
