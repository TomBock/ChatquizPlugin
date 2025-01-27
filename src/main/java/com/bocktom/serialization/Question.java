package com.bocktom.serialization;

import java.util.List;

public class Question {

	public String question;
	public List<String> answers;
	public List<RewardReference> rewards;

	public static class QuestionConfig {
		public List<Question> questions;
	}
}
