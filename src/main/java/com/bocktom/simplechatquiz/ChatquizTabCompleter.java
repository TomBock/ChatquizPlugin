package com.bocktom.simplechatquiz;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ChatquizTabCompleter implements TabCompleter {

	@Override
	public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String @NotNull [] args) {

		if(args.length == 1) {
			return List.of("start", "stop", "reload");
		} else if(args.length == 2 && args[0].equals("start")) {
			return List.of("5", "10", "15", "20");
		}
		return List.of();
	}
}
