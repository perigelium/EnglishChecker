package ru.alexangan.developer.englishchecker;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class MainActivity extends Activity implements View.OnClickListener {

    LinearLayout llAnswers;
    TextView questView;
    Button btnNextQ;
    static int curNewBtnId;
    ArrayList <Button> btnArray;
    static String appTAG = "englishChecker";

    Context context;
    String resName;
    String rightAnswer;
    String question;
    String [] questionBlocks;
    String [] answers;
    static int blockIterator;
    String block;
    static int rightAnswerId;
    static int rightAnswers;
    static int questionsPassed;

    int wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        curNewBtnId = 1;
        blockIterator = 0;
        rightAnswers = 0;
        questionsPassed = 0;
        llAnswers = (LinearLayout) findViewById(R.id.llAnswers);
        questView = (TextView) findViewById(R.id.questView);
        btnNextQ = (Button) findViewById(R.id.btnNextQuestion);

        btnNextQ.setOnClickListener(this);

        btnArray = new ArrayList();

        context = getBaseContext();
        resName = "questions";

        //читаем текстовый файл из ресурсов по имени
        String text = readRawTextFile(context, getResources().getIdentifier(resName, "raw", "ru.alexangan.developer.englishchecker"));


        questionBlocks = text.split("\\*");
    }

    protected void prepare_question(String block) {

        answers = block.split("\n");
        question = answers[0];
        rightAnswer = null;

        for ( int s=0; s < answers.length; s++) {

            if(answers[s].charAt(0) == '+') {

                answers[s].replace('+',' ');
                rightAnswer = answers[s];
            }
        }
    }

    //читаем текст из raw-ресурсов
    public static String readRawTextFile(Context context, int resId)
    {
        InputStream inputStream = context.getResources().openRawResource(resId);

        InputStreamReader inputReader = new InputStreamReader(inputStream);
        BufferedReader buffReader = new BufferedReader(inputReader);
        String line;
        StringBuilder builder = new StringBuilder();

        try {
            while (( line = buffReader.readLine()) != null) {
                builder.append(line);
                builder.append("\n");
            }
        } catch (IOException e) {
            return null;
        }
        return builder.toString();
    }


    @Override
    public void onClick(View v)
    {
        if (v.getId() != R.id.btnNextQuestion)
        {
            questionsPassed++;
            Log.d(appTAG, "questions passed " + String.valueOf(questionsPassed));

            if(v.getId() == rightAnswerId) {
                Log.d(appTAG, "clicked button with id " + String.valueOf(rightAnswerId));
                //v.setBackgroundColor(Color.rgb(0, 99, 0));
                rightAnswers++;
            }

            if(questionsPassed %5 == 0)
            {

                Log.d(appTAG, "Your score is: " + String.valueOf(rightAnswers));
            }
        }
                llAnswers.removeAllViews();
                btnArray.clear();

                block = "";
                while(block.length() < 3)
                {
                    block  = questionBlocks[blockIterator++];
                }
                if (block.isEmpty())
                    return;

                prepare_question(block);

                questView.setText(question);

                for (int i = 0; i < answers.length-1; i++) {
                    if (i > 4)
                        return;

                    // Создание LayoutParams c шириной и высотой по содержимому
                    LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                            wrapContent, wrapContent);

                    // создаем Button, пишем текст и добавляем в LinearLayout
                    Button btnNew = new Button(this);
                    btnNew.setId(curNewBtnId++);
                    final int id_ = btnNew.getId();
                    btnNew.setText("answer " + String.valueOf(id_));

                    llAnswers.addView(btnNew, lParams);

                    btnArray.add(((Button) findViewById(id_)));

                    if (answers[i + 1].charAt(0) == '+')
                    {
                        rightAnswerId = id_;
                        answers[i + 1].replace('+',' ');
                    }

                    btnArray.get(i).setText(answers[i+1]);

                    btnArray.get(i).setOnClickListener(this);
                }
    }
}