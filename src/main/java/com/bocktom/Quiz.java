package com.bocktom;

import com.bocktom.serialization.Question;
import com.bocktom.serialization.RewardReference;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.*;
import java.util.stream.Collectors;

public class Quiz {

	// References
	private ChatquizPlugin plugin;
	private BukkitScheduler scheduler;
	private Player starter;

	// General
	private int secondsPerQuestion;
	private List<Question> questions;
	private Map<String, ItemStack> allRewards = new HashMap<>();


	// Current Question
	private int currentTaskId;
	private int currentQuestionIndex = -1;
	private ItemStack[] currentRewards;

	public List<String> currentAnswers = new ArrayList<>();


	public Quiz(ChatquizPlugin plugin, Player starter, int amountOfQuestions, int secondsPerQuestion) {
		this.starter = starter;
		this.plugin = plugin;
		scheduler = Bukkit.getScheduler();

		this.secondsPerQuestion = secondsPerQuestion;
		allRewards = plugin.loadAllRewards();
		questions = plugin.loadRandomQuestions(amountOfQuestions);
	}

	public void start() {
		broadcast("Ein Serverquiz startet, Viel Erfolg!");

		currentQuestionIndex = 0;
		askNextQuestion();
	}

	public void stop(boolean silent) {
		if(scheduler.isCurrentlyRunning(currentTaskId))
			scheduler.cancelTask(currentTaskId);

		currentQuestionIndex = -1;
		currentRewards = null;
		currentAnswers.clear();
		allRewards.clear();

		if(!silent)
			broadcast("Das Quiz wurde gestoppt");
	}

	public boolean isRunning() {
		return currentQuestionIndex >= 0;
	}

	private void broadcast(String text) {
		Bukkit.broadcast(Component.text(ChatquizPlugin.PREFIX + text));
	}

	private void askNextQuestion() {
		if(currentQuestionIndex >= questions.size()) {
			// Exit
			stop(true);
			broadcast("Das Quiz ist vorbei, danke fürs Spielen!");
			return;
		}

		// Set variables
		var question = questions.get(currentQuestionIndex++);
		currentAnswers.clear();
		currentAnswers.addAll(question.answers
				.stream()
				.map(answer -> answer.toLowerCase(Locale.ROOT))
				.collect(Collectors.toSet()));
		currentRewards = getRewards(question.rewards);

		broadcast("Nächste Frage: §e" + question.question);
		broadcast("Antworten sind: '" + currentAnswers.stream().collect(Collectors.joining("', '")) + "'");

		// Run timer
		currentTaskId = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
			int timeLeft = secondsPerQuestion;

			@Override
			public void run() {
				if(timeLeft <= 0) {

					// Cancel current task and continue with next question
					scheduler.cancelTask(currentTaskId);
					broadcast("Zeit ist durch!");
					askNextQuestion();
					return;
				}

				if(timeLeft == 1) {
					broadcast("Noch §e" + timeLeft + "§6 Sekunde...");
				} else if(timeLeft <= 3 || timeLeft == 10) {
					broadcast("Noch §e" + timeLeft + "§6 Sekunden...");
				}
				timeLeft--;
			}

		}, 0L, 20L);
	}

	private ItemStack[] getRewards(List<RewardReference> rewards) {
		ItemStack[] items = new ItemStack[rewards.size()];

		for (int i = 0; i < rewards.size(); i++) {
			RewardReference rewardRef = rewards.get(i);
			ItemStack reward = allRewards.get(rewardRef.name);

			if(reward == null) {
				plugin.log.warning("Could not find a reward by the name of '" + rewardRef.name + "'. Skipping");
				continue;
			}

			var item = new ItemStack(reward);
			item.setAmount(rewardRef.amount);
			items[i] = item;
		}
		return items;
	}

	public void reward(Player player) {
		PlayerUtil.give(player, currentRewards);
		broadcast("Deine Antwort war korrekt. Du hast die Belohnung "
				+ Arrays.stream(currentRewards).map(i -> PlainTextComponentSerializer.plainText().serialize(i.displayName())).collect(Collectors.joining(", "))
				+ " erhalten!");
	}
}
