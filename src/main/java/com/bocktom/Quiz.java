package com.bocktom;

import com.bocktom.serialization.Messages;
import com.bocktom.serialization.Question;
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
	private int warmupTime;
	private int timePerQuestion;
	private int delayPerQuestion;
	private List<Question> questions;
	private ItemStack reward;
	private Map<Integer, Integer> timeBasedRewards = new HashMap<>();
	private Messages messages;

	// Current Question
	private int currentTaskId;
	private int currentQuestionIndex = -1;
	private String currentQuestion = "";
	private long currentQuestionStartTime;
	public List<String> currentAnswers = new ArrayList<>();
	private List<UUID> currentlyAnswered = new ArrayList<>();

	// Tracking
	private Map<UUID, Integer> answeredCorrectly = new HashMap<>();
	private int highscore;

	public Quiz(ChatquizPlugin plugin, Player starter, int amountOfQuestions) {
		this.starter = starter;
		this.plugin = plugin;
		scheduler = Bukkit.getScheduler();

		reward = plugin.config.getItemStack("reward-item");
		warmupTime = plugin.config.getInt("quiz-warmup");
		timePerQuestion = plugin.config.getInt("question-timer");
		delayPerQuestion = plugin.config.getInt("question-delay");
		messages = new Messages(plugin.config);

		var rewards = (List<Map<Integer, Integer>>) plugin.config.get("time-based-rewards");
		rewards.forEach(map -> timeBasedRewards.putAll(map));

		questions = plugin.loadRandomQuestions(amountOfQuestions);
	}

	public void start() {
		broadcast(messages.global.start);
		currentQuestionIndex = 0;

		currentTaskId = Bukkit.getScheduler().runTaskLater(plugin, this::askNextQuestion, 20L * warmupTime).getTaskId();
	}

	public void stop(boolean silent) {
		if(scheduler.isCurrentlyRunning(currentTaskId) || scheduler.isQueued(currentTaskId))
			scheduler.cancelTask(currentTaskId);

		currentQuestionIndex = -1;
		currentAnswers.clear();

		if(!silent)
			broadcast(messages.global.cancel);

		plugin.releaseQuiz();
	}

	public boolean isRunning() {
		return currentQuestionIndex >= 0;
	}

	private void broadcast(String text) {
		Bukkit.broadcast(Component.text(replaceVars(text)));
	}

	private String replaceVars(String text) {
		if(text.contains("{questions.amount}")) {
			text = text.replace("{questions.amount}", String.valueOf(questions.size()));
		}
		if(text.contains("{questions.time}")) {
			text = text.replace("{questions.time}", String.valueOf(timePerQuestion));
		}
		if(text.contains("{question}")) {
			text = text.replace("{question}", currentQuestion);
		}
		if(text.contains("{reward}")) {
			text = text.replace("{reward}", PlainTextComponentSerializer.plainText().serialize(reward.displayName()));
		}
		if(text.contains("{winner.name}")) {
			text = text.replace("{winner.name}", getWinnerNames());
		}
		if(text.contains("{winner.answers}")) {
			text = text.replace("{winner.answers}", String.valueOf(highscore));
		}
		return text.replace("&", "§"); // Color variables
	}

	private String getWinnerNames() {
		return answeredCorrectly.entrySet()
				.stream()
				.filter(entry -> entry.getValue() == highscore)
				.map(entry -> Bukkit.getPlayer(entry.getKey()))
				.filter(Objects::nonNull)
				.map(Player::getName)
				.collect(Collectors.joining(", "));

	}

	private void askNextQuestion() {
		if(currentQuestionIndex >= questions.size() || currentQuestionIndex < 0) {
			// Exit
			stop(true);
			if(highscore == 0)
				broadcast(messages.global.end.none);
			else if(highscore == 1)
				broadcast(messages.global.end.single);
			else
				broadcast(messages.global.end.multiple);
			return;
		}

		// Set variables
		var question = questions.get(currentQuestionIndex++);
		currentlyAnswered.clear();
		currentAnswers.clear();
		currentQuestion = question.question;
		currentAnswers.addAll(question.answers
				.stream()
				.map(answer -> answer.toLowerCase(Locale.ROOT))
				.collect(Collectors.toSet()));
		currentQuestionStartTime = System.currentTimeMillis();

		broadcast(messages.global.nextQuestion);

		// Run timer
		currentTaskId = scheduler.runTaskLater(plugin, () -> {

			broadcast(messages.global.timeOver);

			// Delay before next question
			currentTaskId = scheduler.runTaskLater(plugin, this::askNextQuestion, 20L * delayPerQuestion).getTaskId();

		}, 20L * timePerQuestion).getTaskId();
	}

	/**
	 * Reward player with reward-item in the correct amount if they haven't answered yet
	 */
	public void reward(Player player) {
		UUID id = player.getUniqueId();
		if(currentlyAnswered.contains(id))
			return;

		var secondsSinceAsked = (System.currentTimeMillis() -  currentQuestionStartTime) / 1000;
		var amount = getRewardAmount(secondsSinceAsked);

		if(amount == 0)
			return;

		currentlyAnswered.add(id);
		int total = answeredCorrectly.getOrDefault(id, 0) + 1;
		answeredCorrectly.put(id, total);

		// track highscore for faster lookup
		if(highscore < total)
			highscore = total;

		var item = new ItemStack(reward);
		item.setAmount(amount);
		PlayerUtil.give(player, item);

		player.sendMessage(replaceVars(messages.player.success));
	}

	private int getRewardAmount(long secondsSinceAsked) {
		for(int seconds : timeBasedRewards.keySet().stream().sorted(Integer::compareTo).toList()) {
			if(secondsSinceAsked <= seconds)
				return timeBasedRewards.get(seconds);
		}
		return 0;
	}
}
