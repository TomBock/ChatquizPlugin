package com.bocktom.serialization;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class Messages {

	public GlobalMessages global;
	public PlayerMessages player;

	public static class GlobalMessages {
		public String start;
		public EndMessages end;
		public String cancel;
		public String nextQuestion;
		public String timeOver;

		public static class EndMessages {
			public String none;
			public String single;
			public String multiple;
			public String own;
		}
	}

	public static class PlayerMessages {
		public String success;
	}

	public Messages(FileConfiguration config) {
		ConfigurationSection globalSection = config.getConfigurationSection("messages.global");
		ConfigurationSection playerSection = config.getConfigurationSection("messages.player");

		global = new Messages.GlobalMessages();
		global.start = globalSection.getString("start");

		ConfigurationSection endSection = config.getConfigurationSection("messages.global.end");
		global.end = new GlobalMessages.EndMessages();
		global.end.none = endSection.getString("none");
		global.end.single = endSection.getString("single");
		global.end.multiple = endSection.getString("multiple");
		global.end.own = endSection.getString("own");

		global.cancel = globalSection.getString("cancel");
		global.nextQuestion = globalSection.getString("next-question");
		global.timeOver = globalSection.getString("time-over");

		player = new Messages.PlayerMessages();
		player.success = playerSection.getString("success");
	}
}
