package com.bocktom;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Locale;

public class AsyncChatListener implements Listener {

	private ChatquizPlugin plugin;

	public AsyncChatListener(ChatquizPlugin plugin) {
		this.plugin = plugin;
	}


	/**
	 * Using deprecated version to not overwrite the text, but adjust it
	 */
	@EventHandler
	public void onChatEvent(AsyncPlayerChatEvent event) {
		if(!plugin.isQuizRunning()) {
			return;
		}

		String message = event.getMessage().toLowerCase(Locale.ROOT);

		for (String answer : plugin.quiz.currentAnswers) {
			if(message.equals(answer)) {

				event.setMessage(censorMessage(answer, message));
				plugin.giveReward(event.getPlayer());
			}
		}
	}

	private static String censorMessage(String answer, String message) {
		var censoredMessage = new StringBuilder(message);

		// Get the index of the answer (case-insensitive)
		int index = message.toLowerCase(Locale.ROOT).indexOf(answer.toLowerCase(Locale.ROOT));
		while (index >= 0) {
			// Replace all characters in the answer except spaces with '*'
			for (int i = 0; i < answer.length(); i++) {
				if (answer.charAt(i) != ' ') {
					censoredMessage.setCharAt(index + i, '*');
				}
			}

			// Find the next occurrence of the same word
			index = message.toLowerCase(Locale.ROOT).indexOf(answer.toLowerCase(Locale.ROOT), index + answer.length());

		}
		return censoredMessage.toString();
	}

}
