package com.bocktom;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ChatquizCommand implements CommandExecutor {

	private ChatquizPlugin plugin;

	public ChatquizCommand(ChatquizPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {
		if(!(sender instanceof Player player))
			return true;

		if(args.length < 1) {
			player.sendMessage("Dieser Befehl braucht mindestens 1 Parameter: /quizchat start/stop");
			return true;
		}

		if(args[0].equals("start")) {

			if(args.length != 3) {
				player.sendMessage("Dieser Befehl braucht 3 Parameter: /quizchat start <amount of questions> <seconds per question>");
				return true;
			}

			int amount = Integer.parseInt(args[1]);
			int timePerQuestion = Integer.parseInt(args[2]);

			plugin.tryStartQuiz(player, amount, timePerQuestion);

		} else if(args[0].equals("stop")) {

			plugin.tryStopQuiz(player);

		}



		return true;
	}
}
