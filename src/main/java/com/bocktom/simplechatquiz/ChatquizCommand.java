package com.bocktom.simplechatquiz;

import com.bocktom.simplechatquiz.serialization.Config;
import com.bocktom.simplechatquiz.serialization.MSG;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatquizCommand implements CommandExecutor {

	private final ChatquizPlugin plugin;

	public ChatquizCommand(ChatquizPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if(!(sender instanceof Player player))
			return true;

		if(!player.hasPermission("chatquiz.admin")) {
			player.sendMessage(MSG.get("admin.no_permission"));
			return false;
		}

		if(args.length < 1) {
			player.sendMessage(MSG.get("admin.incorrect_usage"));
			return true;
		}

		switch (args[0]) {
			case "start" -> {

				if (args.length != 2) {
					player.sendMessage(MSG.get("admin.incorrect_usage"));
					return true;
				}

				int amountOfQuestions = Integer.parseInt(args[1]);
				plugin.tryStartQuiz(player, amountOfQuestions);
			}
			case "stop" -> plugin.tryStopQuiz(player);
			case "reload" -> {
				Config.reload();
				player.sendMessage(MSG.get("admin.config_reloaded"));
			}
		}

		return true;
	}
}
