package ru.alexangan.developer.englishchecker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;
    LinearLayout llAnswers;
    TextView questView;
    Button btnExit;
    int wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;

    final static String appTAG = "eng_chk";
    final static String questFilename = "questions";
    final static String answersFilename = "answers";
    final static int minWordLength = 4;
    final static int DIALOG_CONTINUE = 1;
    static int turnsCount = 5;

    static int turnDelay = 3500;

    ArrayList <Button> btnArray;
    List<Question> questList;

    static int rightAnswerID = 1;
    static String rightAnswer;
    static int curNewBtnId = 1;
    static int rightAnswersCount = 0;
    static int questPassedCount = 0;
    static Boolean avgResultEnabled = true;
    static float avgResult = 0;
    MyTask mt;
    Intent curIntent;
    SharedPreferences sPref;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        llAnswers = (LinearLayout) findViewById(R.id.llAnswers);
        questView = (TextView) findViewById(R.id.questView);
        btnExit = (Button) findViewById(R.id.btnExit);

        btnExit.setOnClickListener(this);

        context = MainActivity.this;

        questList = new ArrayList<>();

        mt = new MyTask();
        mt.execute();

        getTaskResult();
        //prepare_arrays(context);

        btnArray = new ArrayList<>();
        nextTurn();
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveResults();

        Log.d(appTAG, "MainActivity: onPause()");
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadResults();

        curIntent = getIntent();
        loadCurPrefs();

        Log.d(appTAG, "MainActivity: onResume()");
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

    public void onExitClick(View view)
    {
            saveResults();

            exit();
    }

    public void onDialogYesClick()
    {

    }

    public void onDialogNoClick()
    {
        saveResults();

        exit();
    }

    @Override
    public void onClick(View v)
    {
        if (v.getId() != R.id.btnExit) {

            questList.get(questPassedCount);


            Log.d(appTAG, "questions passed " + String.valueOf(questPassedCount));

            questPassedCount++;

            if (v.getId() == rightAnswerID + 1) {
                Log.d(appTAG, "clicked button with id " + String.valueOf(rightAnswerID));

                Toast.makeText(this, "Правильно !", Toast.LENGTH_SHORT).show();
                turnDelay = 2500;

                rightAnswersCount++;
            } else {
                Toast.makeText(this, "Правильный ответ:\n" + rightAnswer, Toast.LENGTH_LONG).show();
                turnDelay = 4000;
            }

            if (questPassedCount % turnsCount == 0) {
                Log.d(appTAG, "Your score is: " + String.valueOf(rightAnswersCount));


                avgResult = avgResult != 0 ? (avgResult + rightAnswersCount) / 2 : rightAnswersCount;

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showResultsDialog();
                        //showDialog(DIALOG_CONTINUE);
                    }
                }, turnDelay);
            }

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    nextTurn();
                }
            }, turnDelay);
        }
    }

    public void showResultsDialog()
    {
        Bundle messageArgs = new Bundle();
        String about_prog_title = getResources().getString(R.string.results_dialog_title);

        String alertText = "Ваша оценка: " + String.valueOf(rightAnswersCount) +
                " из " + String.valueOf(turnsCount) + "\n\n" + "средняя оценка: " + avgResult + "\n\n Еще раз ?";

        messageArgs.putString(AlertDialogFragment.TITLE_ID, about_prog_title);
        messageArgs.putString(AlertDialogFragment.MESSAGE_ID, alertText);
        messageArgs.putBoolean(AlertDialogFragment.Enable_Yes_Btn, true);
        messageArgs.putBoolean(AlertDialogFragment.Enable_No_Btn, true);

        FragmentManager manager = getSupportFragmentManager();
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.setArguments(messageArgs);
        dialog.show(manager, "dialog");

        rightAnswersCount = 0;
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

            LinearLayout.LayoutParams lParams = new LinearLayout.LayoutParams(
                    wrapContent, wrapContent);

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

            adb.setTitle(R.string.exit);
            adb.setMessage(R.string.defaul_alert_text);
            adb.setIcon(android.R.drawable.ic_dialog_info);

            adb.setPositiveButton(R.string.btnYes, myClickListener);
            adb.setNegativeButton(R.string.btnNo, myClickListener);

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

                case Dialog.BUTTON_POSITIVE:
                    rightAnswersCount = 0;
                    break;
                case Dialog.BUTTON_NEGATIVE:
                    finish();
                    break;
                //case Dialog.BUTTON_NEUTRAL:
                  //  break;
            }
        }
    };

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

        finish();
    }

    private void loadResults() {

        sPref = getPreferences(MODE_PRIVATE);

        avgResult = sPref.getFloat("avtResult", 0);

        Log.d(appTAG, "loadResults");
    }

    private void saveResults()
    {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();

        ed.putFloat("avgResult", avgResult);

        Log.d(appTAG, "saveResults");

        ed.commit();
    }

    private void loadCurPrefs()
    {
        turnsCount = curIntent.getIntExtra("turnsCount", 5);
        avgResultEnabled = curIntent.getBooleanExtra("avgChecked", true);

        Log.d(appTAG,"loadCurPrefs-turnsCount= " + turnsCount);
    }



    class MyTask extends AsyncTask<Void, Void, List<Question>>
    {

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
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        savedInstanceState.getFloat("avgResult", avgResult);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            exit();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void exit()
    {
        finish();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}