package ru.alexangan.developer.englishchecker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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
    static int turnDelay = 3500;

    ArrayList <Button> btnArray;
    List<Question> questList;

    static int rightAnswerID;
    static String rightAnswer;
    static int curNewBtnId;
    static int rightAnswersCount;
    static int questPassedCount;
    static final private int turnsCountResId = 1;
    static Boolean avgResultEnabled = true;
    static float avgResult = 0;
    static char resCode = 'c';
    MyTask mt;

    int wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Intent questionIntent = new Intent(MainActivity.this, PrefActivity.class);
        startActivityForResult(questionIntent, turnsCountResId);

        curNewBtnId = 1;
        questPassedCount = 0;
        rightAnswersCount = 0;

        llAnswers = (LinearLayout) findViewById(R.id.llAnswers);
        questView = (TextView) findViewById(R.id.questView);
        btnExit = (Button) findViewById(R.id.btnExit);

        btnExit.setOnClickListener(this);

        context = getBaseContext();

        questList = new ArrayList<>();

        mt = new MyTask();
        mt.execute();

        getTaskResult();
        //prepare_arrays(context);

        btnArray = new ArrayList<>();
        nextTurn();
    }

    private void prepare_arrays(Context context, List<Question> questList)
    {
        String questText = readRawTextFile(context, getResources().getIdentifier(questFilename, "raw", "ru.alexangan.developer.englishchecker"));
        String answersText = readRawTextFile(context, getResources().getIdentifier(answersFilename, "raw", "ru.alexangan.developer.englishchecker"));

        if(answersText == null || questText == null)
        {
            return;
        }

        ArrayList <String> answersBlocks = new ArrayList<>();
        ArrayList <ArrayList> bunchOfAnswers = new ArrayList<>();
        ArrayList <String> questions = new ArrayList<>();
        ArrayList <String> rightAnswers = new ArrayList<>();
        ArrayList<Integer> rightAnswerIDs = new ArrayList<>();

        Pattern patternQuestion = Pattern.compile("(^[A-Z].+$)", Pattern.MULTILINE); // single english line

        Matcher matcher = patternQuestion.matcher(questText);

        while (matcher.find())
        {
            questions.add(matcher.group(0)); // english lines only
        }

        String [] answersBlocksDirty = patternQuestion.split(questText); // except english lines

        for(String curBlk : answersBlocksDirty)
        {
            if(curBlk.length() < minWordLength) continue;

            answersBlocks.add(curBlk);
        }

        String [] rightAnswersDrty = answersText.split(System.getProperty("line.separator"));

        for(String answrStr : rightAnswersDrty)
        {
            if(answrStr.length() < minWordLength) continue;

            rightAnswers.add(answrStr); // right answers in native consequence
        }

        for (int i = 0; i < questions.size(); i++)
        {
            String [] curAnswersPrep = answersBlocks.get(i).split(System.getProperty("line.separator"));
            ArrayList<String> curAnswers = new ArrayList<>();
            rightAnswerID = -1;

            // line breaks exclusion
            for (String prepStr : curAnswersPrep)
            {

                if (prepStr.length() < minWordLength) continue;

                curAnswers.add(prepStr.trim());
            }

            bunchOfAnswers.add(i, curAnswers);

            for(int k = 0; k < curAnswers.size(); k++)
            {
                if(rightAnswerID != -1) break;

                for(String answIter : rightAnswers)
                {
                    if(curAnswers.get(k).trim().equals(answIter.trim()))
                    {
                        rightAnswerIDs.add(k);
                        break;
                    }
                }
            }
        }

        if(questions.size() != bunchOfAnswers.size() || questions.size() != rightAnswerIDs.size())
        {
            return; // questions, set of answers and right answers IDs arrays must be the same size
        }

        for (int i = 0; i < questions.size(); i++) {

            questList.add(new Question(questions.get(i), bunchOfAnswers.get(i), rightAnswerIDs.get(i)));
            Collections.shuffle(questList);
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
        if (v.getId() == R.id.btnExit)
        {
            finish();
        }

        if (v.getId() != R.id.btnExit)
        {

            questList.get(questPassedCount);


            Log.d(appTAG, "questions passed " + String.valueOf(questPassedCount));

            questPassedCount++;

            if(v.getId() == rightAnswerID + 1) {
                Log.d(appTAG, "clicked button with id " + String.valueOf(rightAnswerID));

                //LinearLayout.LayoutParams lParams1;
                //lParams1 = (LinearLayout.LayoutParams) v.getLayoutParams();

                Toast.makeText(this, "Правильно !", Toast.LENGTH_SHORT).show();
                turnDelay = 2500;
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
                turnDelay = 4000;
            }

            if(questPassedCount % turnsCount == 0)
            {
                Log.d(appTAG, "Your score is: " + String.valueOf(rightAnswersCount));


                 avgResult = avgResult!=0.0 ? (avgResult + rightAnswersCount)/2  :  rightAnswersCount;

                // Execute some code after 4 seconds have passed
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showDialog(DIALOG_CONTINUE);
                    }
                }, turnDelay);
            }
        }

        // Execute some code after 4 seconds have passed
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                nextTurn();
            }
        }, turnDelay);

    }

    protected void nextTurn()
    {
        llAnswers.removeAllViews();
        btnArray.clear();
        rightAnswerID = -1;
        curNewBtnId = 1;
        rightAnswer = null;

        Question curQuest = questList.get(questPassedCount);
        rightAnswerID = curQuest.getRightAnswerId();
        rightAnswer = curQuest.getRightAnswer();

        questView.setText(curQuest.getQuestion());

        for (int i = 0; i < curQuest.getBunchOfAnswers().size(); i++) {

            // Создание LayoutParams c шириной и высотой по содержимому
            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                    wrapContent, wrapContent);

            // создаем Button, пишем текст и добавляем в LinearLayout
            Button btnNew = new Button(this);
            btnNew.setId(curNewBtnId++);
            final int id_ = btnNew.getId();

            llAnswers.addView(btnNew, lParams);

            btnArray.add(((Button) findViewById(id_)));

            btnArray.get(i).setText(curQuest.getBunchOfAnswers().get(i));

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
                    " из " + String.valueOf(turnsCount) + "\n\n" + "средняя оценка: " + avgResult + "\n\n Еще раз ?";

            ((AlertDialog)dialog).setMessage(alertText);
        }
    }

    DialogInterface.OnClickListener myClickListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                // положительная кнопка
                case Dialog.BUTTON_POSITIVE:
                    rightAnswersCount = 0;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == turnsCountResId) {
            if (resultCode == RESULT_OK) {
                turnsCount = data.getIntExtra(PrefActivity.QuestInTestName, 5);
                avgResultEnabled = data.getBooleanExtra(PrefActivity.QuestInTestName, true);
                resCode = data.getCharExtra(PrefActivity.QuestInTestName, 'c');

                if(resCode == 'f')
                {
                    mt.cancel(true);
                    finish();
                }

                Log.d(appTAG, "checkbox is checked " + avgResultEnabled);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);  // create menu on base of menu_main.xml
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);

    }

    public void onSettingsMenuClick(MenuItem item) {
        Intent questionIntent = new Intent(MainActivity.this, PrefActivity.class);
        startActivityForResult(questionIntent, turnsCountResId);

    }


    class MyTask extends AsyncTask<Void, Void, List<Question>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<Question> doInBackground(Void... params) {
            try {
                prepare_arrays(context, questList);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return questList;
        }

        @Override
        protected void onPostExecute(List<Question> result) {
            super.onPostExecute(result);
        }
    }

    private void getTaskResult() {
        if (mt == null) return;
        try {
            questList = mt.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putFloat("avgResult", avgResult);
        state.putBoolean("avgChk", avgResultEnabled);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        savedInstanceState.getFloat("avgResult", avgResult);
        savedInstanceState.getBoolean("avgChk", avgResultEnabled);
    }
}