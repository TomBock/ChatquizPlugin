package com.bocktom;

import com.bocktom.serialization.Question;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.*;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class ChatquizPlugin extends JavaPlugin {

	public BukkitScheduler scheduler;
	public FileConfiguration config;
	public Logger log;
	public Quiz quiz;


	@Override
	public void onEnable() {
		log = getLogger();
		config = getConfig();
		scheduler = Bukkit.getScheduler();

		PluginCommand cmd = this.getCommand("chatquiz");
		cmd.setExecutor(new ChatquizCommand(this));
		cmd.setTabCompleter(new ChatquizTabCompleter());

		getServer().getPluginManager().registerEvents(new AsyncChatListener(this), this);
	}

	@Override
	public void onDisable() {
		if(isQuizRunning())
			quiz.stop(true);
	}

	public void tryStartQuiz(Player starter, int amountOfQuestions) {

		if(quiz != null) {
			starter.sendMessage("Aktuelles Quiz wurde abgebrochen. Ein neues wird gestartet.");
			quiz.stop(true);
		}

		quiz = new Quiz(this, starter, amountOfQuestions);
		quiz.start();
	}

	public void tryStopQuiz(Player player) {
		if(!isQuizRunning()) {
			player.sendMessage("Es läuft gerade kein Quiz");
			return;
		}

		quiz.stop(false);
		quiz = null;
	}

	public boolean isQuizRunning() {
		return quiz != null && quiz.isRunning();
	}


	public List<Question> loadRandomQuestions(int amount) {
		Yaml yml = new Yaml(new Constructor(Question.QuestionConfig.class, new LoaderOptions()));
		File file = new File(getDataFolder() + File.separator + "questions.yml");

		Question.QuestionConfig config = null;

		try (InputStream stream = new FileInputStream(file)) {

			config = yml.load(stream);

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		List<Question> questions = config.questions;

		Collections.shuffle(questions);
		return questions.subList(0, Math.min(amount, questions.size()));
	}

	public void giveReward(Player player) {
		Bukkit.getScheduler().runTask(this, () -> {
			quiz.reward(player);
		});
	}

	public void releaseQuiz() {
		quiz = null;
	}
}
