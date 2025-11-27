package com.bocktom.simplechatquiz;

import com.bocktom.simplechatquiz.serialization.Config;
import com.bocktom.simplechatquiz.serialization.MSG;
import com.bocktom.simplechatquiz.serialization.Question;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class Quiz {

	// References
	private ChatquizPlugin plugin;
	private Player starter;

	// General
	private int warmupTime;
	private int timePerQuestion;
	private int delayPerQuestion;
	private List<Question> questions;

	private Rewards winningRewards;
	private Rewards correctAnswerRewards;
	private Rewards firstAnswerRewards;
	private Rewards secondAnswerRewards;
	private Rewards thirdAnswerRewards;

	// Current Question
	private int currentTaskId;
	private int currentQuestionIndex = -1;
	private String currentQuestion = "";
	private long currentQuestionStartTime;
	private int currentAnswerCount = 0;

	public List<String> currentAnswers = new ArrayList<>();
	private List<UUID> currentlyAnswered = new ArrayList<>();

	// Tracking
	private Map<UUID, Integer> answeredCorrectly = new HashMap<>();
	private int highscore;

	public Quiz(ChatquizPlugin plugin, Player starter, int amountOfQuestions) {
		this.starter = starter;
		this.plugin = plugin;

		winningRewards = Rewards.fromConfig("winner");
		correctAnswerRewards = Rewards.fromConfig("correct_answer");
		firstAnswerRewards = Rewards.fromConfig("first_correct_answer");
		secondAnswerRewards = Rewards.fromConfig("second_correct_answer");
		thirdAnswerRewards = Rewards.fromConfig("third_correct_answer");

		warmupTime = Config.settings.get.getInt("timer.warmup");
		timePerQuestion = Config.settings.get.getInt("timer.question");
		delayPerQuestion = Config.settings.get.getInt("timer.delay");

		questions = plugin.loadRandomQuestions(amountOfQuestions);
	}

	public void start() {
		broadcast(MSG.get("global.start"));
		currentQuestionIndex = 0;

		currentTaskId = plugin.scheduler.runTaskLater(plugin, this::askNextQuestion, 20L * warmupTime).getTaskId();
	}

	public void stop(boolean silent) {
		if(plugin.scheduler.isCurrentlyRunning(currentTaskId) || plugin.scheduler.isQueued(currentTaskId))
			plugin.scheduler.cancelTask(currentTaskId);

		currentQuestionIndex = -1;
		currentAnswers.clear();

		if(!silent)
			broadcast(MSG.get("global.cancel"));

		plugin.releaseQuiz();
	}

	public boolean isRunning() {
		return currentQuestionIndex >= 0;
	}

	private void broadcast(String text) {
		Bukkit.broadcast(Component.text(replaceVars(text)));
	}

	private String replaceVars(String text) {
		if(text.contains("%questions.amount%")) {
			text = text.replace("%questions.amount%", String.valueOf(questions.size()));
		}
		if(text.contains("%questions.delay.start%")) {
			text = text.replace("%questions.delay.start%", String.valueOf(warmupTime));
		}
		if(text.contains("%questions.time%")) {
			text = text.replace("%questions.time%", String.valueOf(timePerQuestion));
		}
		if(text.contains("%question%")) {
			text = text.replace("%question%", currentQuestion);
		}
		if(text.contains("%reward.winner%")) {
			text = text.replace("%reward.winner%", winningRewards.getName());
		}
		if(text.contains("%reward.correct.answer%")) {
			text = text.replace("%reward.correct.answer%", correctAnswerRewards.getName());
		}
		if(text.contains("%winner.name%")) {
			text = text.replace("%winner.name%", getWinnerNames());
		}
		if(text.contains("%winner.answers%")) {
			text = text.replace("%winner.answers%", String.valueOf(highscore));
		}
		return text.replace("&", "§"); // Color variables
	}

	private String replacePlayerVars(String text, Player player) {
		text = replaceVars(text);

		if(text.contains("%answers.correct%")) {
			int correctAnswers = answeredCorrectly.getOrDefault(player.getUniqueId(), 0);
			text = text.replace("%answers.correct%", String.valueOf(correctAnswers));
		}
		return text;
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

			int winnerCount = (int) answeredCorrectly.values().stream().filter(i -> i == highscore).count();
			if(winnerCount == 0)
				broadcast(MSG.get("global.end.none"));
			else if(winnerCount == 1)
				broadcast(MSG.get("global.end.single"));
			else
				broadcast(MSG.get("global.end.multiple"));

			sendPlayerStats();
			return;
		}

		// Set variables
		var question = questions.get(currentQuestionIndex++);
		currentlyAnswered.clear();
		currentAnswers.clear();
		currentAnswerCount = 0;

		if(StringUtil.isNullOrEmpty(question.question)) {
			plugin.getLogger().warning("Question at index " + currentQuestionIndex + " has no question text");
			askNextQuestion();
			return;
		}
		currentQuestion = question.question;

		if(question.answers == null || question.answers.isEmpty()) {
			plugin.getLogger().warning("Question " + currentQuestion + " has no answers");
			askNextQuestion();
			return;
		}
		if(question.answers.stream().anyMatch(StringUtil::isNullOrEmpty)) {
			plugin.getLogger().warning("Question " + currentQuestion + " has an empty or null answer");
			askNextQuestion();
			return;
		}

		currentAnswers.addAll(question.answers
				.stream()
				.map(answer -> answer.toLowerCase(Locale.ROOT))
				.collect(Collectors.toSet()));
		currentQuestionStartTime = System.currentTimeMillis();

		broadcast(MSG.get("global.next_question"));

		// Run timer
		currentTaskId = plugin.scheduler.runTaskLater(plugin, () -> {

			broadcast(MSG.get("global.time_over"));

			// Delay before next question
			currentTaskId = plugin.scheduler.runTaskLater(plugin, this::askNextQuestion, 20L * delayPerQuestion).getTaskId();

		}, 20L * timePerQuestion).getTaskId();
	}

	private void sendPlayerStats() {
		List<UUID> winners = answeredCorrectly.entrySet()
				.stream()
				.filter(entry -> entry.getValue() == highscore)
				.map(entry -> Bukkit.getPlayer(entry.getKey()))
				.filter(Objects::nonNull)
				.map(Player::getUniqueId).toList();

		for (Player player : Bukkit.getOnlinePlayers()) {
			UUID uuid = player.getUniqueId();
			if(winners.contains(uuid)) {
				// Skip since winners are announced globally
				continue;
			}
			player.sendMessage(replacePlayerVars(MSG.get("player.end.looser"), player));
		}

		winners.forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if(player != null) {
				player.sendMessage(replacePlayerVars(MSG.get("player.end.winner"), player));
				winningRewards.give(player);
			}
		});
	}

	/**
	 * Reward player with reward-item in the correct amount if they haven't answered yet
	 */
	public void reward(Player player) {
		UUID id = player.getUniqueId();
		if(currentlyAnswered.contains(id))
			return;

		currentlyAnswered.add(id);
		int total = answeredCorrectly.getOrDefault(id, 0) + 1;
		answeredCorrectly.put(id, total);

		// track highscore for faster lookup
		if(highscore < total)
			highscore = total;

		currentAnswerCount++;
		if(currentAnswerCount == 1 && !firstAnswerRewards.isEmpty()) {
			firstAnswerRewards.give(player);
		} else if(currentAnswerCount == 2 && !secondAnswerRewards.isEmpty()) {
			secondAnswerRewards.give(player);
		} else if(currentAnswerCount == 3 && !thirdAnswerRewards.isEmpty()) {
			thirdAnswerRewards.give(player);
		} else {
			correctAnswerRewards.give(player);
		}

		player.sendMessage(replacePlayerVars(MSG.get("player.correct_answer"), player));
	}
}
