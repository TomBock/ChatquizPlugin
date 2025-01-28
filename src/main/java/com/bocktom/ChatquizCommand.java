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

		if(!player.hasPermission("chatquiz.start")) {
			player.sendMessage("Du hast keine Berechtigung für diesen Befehl");
			return false;
		}

		if(args.length < 1) {
			player.sendMessage("Dieser Befehl braucht mindestens 1 Parameter: /chatquiz start/stop");
			return true;
		}

		if(args[0].equals("start")) {

			if(args.length != 2) {
				player.sendMessage("Dieser Befehl braucht 2 Parameter: /chatquiz start <amount of questions>");
				return true;
			}

			int amountOfQuestions = Integer.parseInt(args[1]);

			plugin.tryStartQuiz(player, amountOfQuestions);

		} else if(args[0].equals("stop")) {

			plugin.tryStopQuiz(player);

		}



		return true;
	}
}
