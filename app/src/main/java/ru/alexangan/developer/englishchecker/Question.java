package ru.alexangan.developer.englishchecker;

import java.util.ArrayList;

/**
 * Created by Administrator on 27.07.16.
 */
public class Question {

    private String question;
    private Integer rightAnswerId, rightAnswerSn;
    private ArrayList<String> bunchOfAnswers;

    public Question(String question, ArrayList<String> bunchOfAnswers, Integer rightAnswerId, Integer rightAnswerSn) {

        this.question = question;
        this.rightAnswerId = rightAnswerId;
        this.rightAnswerSn = rightAnswerSn;
        this.bunchOfAnswers = bunchOfAnswers;
    }

    public ArrayList<String> getBunchOfAnswers() {

        return bunchOfAnswers;
    }

    public Integer getRightAnswerID() {
        return rightAnswerId;
    }

    public String getQuestion() {
        return question;
    }

    public String getRightAnswer() {

        return bunchOfAnswers.get(rightAnswerId);
    }

    public Integer getRightAnswerSN() {
        return rightAnswerSn;
    }
}
