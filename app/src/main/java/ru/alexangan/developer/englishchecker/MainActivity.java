package ru.alexangan.developer.englishchecker;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

    Context context;

    LinearLayout llAnswers;
    TextView questView;
    Button btnExit;

    final static String appTAG = "eng_chk";
    final static String questFilename = "questions";
    final static String answersFilename = "answers";
    final static int minWordLength = 4;
    static int turnsCount = 5;
    final int DIALOG_CONTINUE = 1;

    String rightAnswer;
    String curQuestion;

    ArrayList <Button> btnArray;
    static ArrayList <String> questions, answersBlocks, rightAnswers;
    static String [] rightAnswersDrty;

    static int curNewBtnId;
    static int blockIterator;
    static int rightAnswerId;
    static int rightAnswersCount;
    static int questPassedCount;

    int wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        curNewBtnId = 1;
        blockIterator = 0;
        questPassedCount = 0;
        rightAnswersCount = 0;

        llAnswers = (LinearLayout) findViewById(R.id.llAnswers);
        questView = (TextView) findViewById(R.id.questView);
        btnExit = (Button) findViewById(R.id.btnExit);

        btnExit.setOnClickListener(this);

        btnArray = new ArrayList<>();
        questions = new ArrayList<>();
        answersBlocks = new ArrayList<>();
        rightAnswers = new ArrayList<>();

        context = getBaseContext();

        prepare_arrays(context);

        nextTurn();
    }

    private void prepare_arrays(Context context)
    {
        String questText = readRawTextFile(context, getResources().getIdentifier(questFilename, "raw", "ru.alexangan.developer.englishchecker"));
        String answersText = readRawTextFile(context, getResources().getIdentifier(answersFilename, "raw", "ru.alexangan.developer.englishchecker"));

        if(answersText == null || questText == null)
        {
            return;
        }

        Pattern offeredAnswers = Pattern.compile("(^[A-Z].+$)", Pattern.MULTILINE); // single english line

        Matcher matcher = offeredAnswers.matcher(questText);

        while (matcher.find())
        {
            questions.add(matcher.group(0));
        }

        String [] answersBlocksDirty = offeredAnswers.split(questText); // except english lines

        for(String curBlk : answersBlocksDirty)
        {
            if(curBlk.length() < minWordLength) continue;

            answersBlocks.add(curBlk);
        }

        rightAnswersDrty = answersText.split(System.getProperty("line.separator"));

        for(String answrStr : rightAnswersDrty)
        {
            if(answrStr.length() < minWordLength) continue;

            rightAnswers.add(answrStr);
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


    private ArrayList<String> prepare_quest_data(ArrayList<String> curAnswers)
    {
        String [] curAnswersPrep = answersBlocks.get(blockIterator).split(System.getProperty("line.separator"));

        for (String prepStr : curAnswersPrep) {

            if (prepStr.length() < minWordLength) continue;

            curAnswers.add(prepStr.trim());
        }

        for(int i = 0; i < curAnswers.size(); i++)
        {
            if(rightAnswerId != -1) continue;

            for(String answIter : rightAnswers)
            {
                if(curAnswers.get(i).trim().equals(answIter.trim()))
                {
                    rightAnswerId = i+1;
                    rightAnswer = answIter.trim();
                    break;
                }
            }
        }

        curQuestion  = questions.get(blockIterator);

        blockIterator++;

        return curAnswers;
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() == R.id.btnExit)
        {
            finish();
        }

        if (v.getId() != R.id.btnExit)
        {
            questPassedCount++;

            Log.d(appTAG, "questions passed " + String.valueOf(questPassedCount));

            if(v.getId() == rightAnswerId) {
                Log.d(appTAG, "clicked button with id " + String.valueOf(rightAnswerId));

                //LinearLayout.LayoutParams lParams1;
                //lParams1 = (LinearLayout.LayoutParams) v.getLayoutParams();

                Toast.makeText(this, "Правильно !", Toast.LENGTH_LONG).show();

/*
                Button btn = (Button) findViewById(v.getId());
                btn.setBackgroundColor(Color.rgb(0, 99, 0));
                btn.requestLayout();
*/
                rightAnswersCount++;
            }
            else
            {
                Toast.makeText(this, "Правильный ответ:\n" + rightAnswer, Toast.LENGTH_LONG).show();
            }

            if(questPassedCount % turnsCount == 0)
            {
                Log.d(appTAG, "Your score is: " + String.valueOf(rightAnswersCount));
                showDialog(DIALOG_CONTINUE);

                questPassedCount = 0;
                rightAnswersCount = 0;
            }
        }

        // Execute some code after 4 seconds have passed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                nextTurn();
            }
        }, 4000);

    }

    protected void nextTurn()
    {
        llAnswers.removeAllViews();
        btnArray.clear();
        rightAnswer = null;
        curQuestion = null;
        rightAnswerId = -1;
        curNewBtnId = 1;
        ArrayList <String> curAnswers = new ArrayList<>();

        prepare_quest_data(curAnswers);

        if (curQuestion.isEmpty() || curAnswers.size() == 0 || rightAnswerId == -1)
        {
            return;
        }

        questView.setText(curQuestion);



        for (int i = 0; i < curAnswers.size(); i++) {

            if (i > 4)
                return;

            // Создание LayoutParams c шириной и высотой по содержимому
            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                    wrapContent, wrapContent);

            // создаем Button, пишем текст и добавляем в LinearLayout
            Button btnNew = new Button(this);
            btnNew.setId(curNewBtnId++);
            final int id_ = btnNew.getId();

            llAnswers.addView(btnNew, lParams);

            btnArray.add(((Button) findViewById(id_)));

            btnArray.get(i).setText(curAnswers.get(i));

            btnArray.get(i).setOnClickListener(this);
        }
    }

    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_CONTINUE) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            // заголовок
            adb.setTitle(R.string.exit);
            // сообщение
            adb.setMessage("alertText");
            // иконка
            adb.setIcon(android.R.drawable.ic_dialog_info);
            // кнопка положительного ответа
            adb.setPositiveButton(R.string.yes, myClickListener);
            // кнопка отрицательного ответа
            adb.setNegativeButton(R.string.no, myClickListener);
            // кнопка нейтрального ответа
            //adb.setNeutralButton(R.string.cancel, myClickListener);
            // создаем диалог
            return adb.create();
        }
        return super.onCreateDialog(id);
    }

    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);

        if (id == DIALOG_CONTINUE) {
            String alertText = "Ваша оценка: " + String.valueOf(rightAnswersCount) +
                    " из " + String.valueOf(questPassedCount) + "\n\n Продолжить тест ?";

            ((AlertDialog)dialog).setMessage(alertText);
        }
    }

    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                // положительная кнопка
                case Dialog.BUTTON_POSITIVE:
                    break;
                // негатитвная кнопка
                case Dialog.BUTTON_NEGATIVE:
                    finish();
                    break;
                // нейтральная кнопка
                //case Dialog.BUTTON_NEUTRAL:
                  //  break;
            }
        }
    };
}