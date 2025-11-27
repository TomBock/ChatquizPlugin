package com.bocktom.simplechatquiz.serialization;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.List;

import static org.bukkit.Bukkit.getLogger;

public class MSG {

	public static String get(String path) {
		String msg = Config.msg.get.getString(path);
		if(msg == null) {
			getLogger().severe("MSG missing: <" + path + ">");
			return "§cMSG missing: <" + path + ">";
		}
		return msg.replace("&", "§");
	}
	public static List<String> getList(String path) {
		return Config.msg.get.getStringList(path);
	}

	public static String get(String path, String... replaceVariables) {
		if(replaceVariables.length % 2 != 0) {
			throw new IllegalArgumentException("replaceVariables must be a multiple of 2");
		}
		String message = get(path);

		for(int i = 0; i < replaceVariables.length; i += 2) {
			message = message.replace(replaceVariables[i], replaceVariables[i + 1]);
		}
		return message;
	}

	public static TextComponent get(String path, Component... replaceVariables) {
		if(replaceVariables.length % 2 != 0) {
			throw new IllegalArgumentException("replaceVariables must be a multiple of 2");
		}
		TextComponent message = LegacyComponentSerializer.legacySection().deserialize(get(path));
		for(int i = 0; i < replaceVariables.length; i += 2) {
			TextComponent target = (TextComponent) replaceVariables[i];
			Component replacement = replaceVariables[i + 1];

			TextReplacementConfig config = TextReplacementConfig.builder()
					.matchLiteral(target.content())
					.replacement(replacement)
					.build();

			message = (TextComponent) message.replaceText(config);
		}
		return message;
	}

}
