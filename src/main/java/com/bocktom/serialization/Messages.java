package com.bocktom.serialization;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class Messages {

	public GlobalMessages global;
	public PlayerMessages player;

	public static class GlobalMessages {
		public String start;
		public String end;
		public String cancel;
		public String nextQuestion;
		public String timeOver;
	}

	public static class PlayerMessages {
		public String success;
	}

	public Messages(FileConfiguration config) {
		ConfigurationSection globalSection = config.getConfigurationSection("messages.global");
		ConfigurationSection playerSection = config.getConfigurationSection("messages.player");

		global = new Messages.GlobalMessages();
		global.start = globalSection.getString("start");
		global.end = globalSection.getString("end");
		global.cancel = globalSection.getString("cancel");
		global.nextQuestion = globalSection.getString("next-question");
		global.timeOver = globalSection.getString("time-over");

		player = new Messages.PlayerMessages();
		player.success = playerSection.getString("success");
	}
}
