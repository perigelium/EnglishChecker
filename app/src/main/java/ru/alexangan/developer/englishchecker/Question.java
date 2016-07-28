package ru.alexangan.developer.englishchecker;

import java.util.ArrayList;

/**
 * Created by Administrator on 27.07.16.
 */
public class Question {

    private String question;
    private Integer rightAnswerId;
    private ArrayList<String> bunchOfAnswers;
    private String rightAnswer;

    public Question(String question, ArrayList<String> bunchOfAnswers, Integer rightAnswerId) {

        this.question = question;
        this.rightAnswerId = rightAnswerId;
        this.bunchOfAnswers = bunchOfAnswers;
    }

    public ArrayList<String> getBunchOfAnswers() {

        return bunchOfAnswers;
    }

    public Integer getRightAnswerId() {
        return rightAnswerId;
    }

    public String getQuestion() {
        return question;
    }

    public String getRightAnswer() {

        return bunchOfAnswers.get(rightAnswerId);
    }
}
