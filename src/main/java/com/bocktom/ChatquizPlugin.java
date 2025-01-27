package com.bocktom;

import com.bocktom.serialization.Question;
import com.bocktom.serialization.RewardReference;
import com.google.common.base.Charsets;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Logger;

public class ChatquizPlugin extends JavaPlugin {

	public static final String PREFIX = "§7[§5Chatquiz§7] §6";

	public BukkitScheduler scheduler;
	public FileConfiguration config;
	public Logger log;
	public Quiz quiz;


	@Override
	public void onEnable() {
		log = getLogger();
		config = getConfig();
		scheduler = Bukkit.getScheduler();

		this.getCommand("chatquiz").setExecutor(new ChatquizCommand(this));

		getServer().getPluginManager().registerEvents(new AsyncChatListener(this), this);
	}

	@Override
	public void onDisable() {
		if(quiz.isRunning())
			quiz.stop(true);
	}

	public void tryStartQuiz(Player starter, int amount, int timePerQuestion) {

		if(quiz != null) {
			starter.sendMessage(PREFIX + "Aktuelles Quiz wurde abgebrochen. Ein neues wird gestartet.");
			quiz.stop(true);
		}

		quiz = new Quiz(this, starter, amount, timePerQuestion);
		quiz.start();
	}

	public void tryStopQuiz(Player player) {
		if(!isQuizRunning()) {
			player.sendMessage(PREFIX + "Es läuft gerade kein Quiz");
			return;
		}

		quiz.stop(false);
		quiz = null;
	}

	public boolean isQuizRunning() {
		return quiz != null && quiz.isRunning();
	}

	public Map<String, ItemStack> loadAllRewards() {
		FileConfiguration rewardsConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(getResource("rewards.yml")), Charsets.UTF_8));
		List<Map<String, ItemStack>> rewardsList = (List<Map<String, ItemStack>>) rewardsConfig.getList("rewards");

		Map<String, ItemStack> rewards = new HashMap<>();
		for (Map<String, ItemStack> reward : rewardsList) {
			rewards.putAll(reward);
		}
		return rewards;
	}

	public List<Question> loadRandomQuestions(int amount) {
		var yml = new Yaml(new Constructor(Question.QuestionConfig.class, new LoaderOptions()));
		Question.QuestionConfig config = yml.load(getResource("questions.yml"));
		List<Question> questions = config.questions;

		Collections.shuffle(questions);
		return questions.subList(0, Math.min(amount, questions.size()));
	}

	public void giveReward(Player player) {
		Bukkit.getScheduler().runTask(this, () -> {
			quiz.reward(player);
		});
	}
}
