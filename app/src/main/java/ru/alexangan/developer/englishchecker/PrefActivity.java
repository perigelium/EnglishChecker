package ru.alexangan.developer.englishchecker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class PrefActivity extends AppCompatActivity {

    public final static String appTAG = "eng_chk";
    //public final static String CurValues = "ru.alexangan.developer.englishchecker.CurValues";
    static final private int ConfResId = 1;
    final static int DIALOG_CONTINUE = 1;

    static Boolean avgResultEnabled = true;
    static int turnsCount = 5;
    private TextView mTextValue;
    String[] data = {"5", "10", "15", "20"};

    static float avgResult = 0;
    static Spinner spinner;
    static CheckBox chkboxAvgMark, chkboxNewQuestOnly;
    static SharedPreferences sPref;
    static Boolean avgChecked = true;
    static Boolean newQuestOnlyChecked = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pref);

        chkboxAvgMark = (CheckBox) findViewById(R.id.chkBoxAverage);
        chkboxNewQuestOnly = (CheckBox) findViewById(R.id.chkboxNewQuestOnly);
        spinner = (Spinner) findViewById(R.id.spQuestCount);

        loadPrefs();

        // адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        chkboxAvgMark.setChecked(avgChecked);
        chkboxNewQuestOnly.setChecked(newQuestOnlyChecked);


        spinner.setAdapter(adapter);
        // заголовок
        spinner.setPrompt("Количество вопросов в тесте");

        spinner.setSelection(turnsCount/5 - 1);
        // устанавливаем обработчик нажатия
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                //spinner.getSelectedItemPosition()
                // or
                turnsCount = Integer.valueOf(spinner.getSelectedItem().toString());

                Log.d(appTAG,"onSelect-turnsCount= " + turnsCount);
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        savePrefs();
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadPrefs();
    }

    public void onExitClick(View view) {

        if (view.getId() == R.id.btnExit) {
            exit();
        }
    }

    public void onAboutClick(View view)
    {
        Bundle messageArgs = new Bundle();
        String about_prog_title = getResources().getString(R.string.about_prog_title);
        String about_prog_text = getResources().getString(R.string.about_prog_text);
        messageArgs.putString(AlertDialogFragment.TITLE_ID, about_prog_title);
        messageArgs.putString(AlertDialogFragment.MESSAGE_ID, about_prog_text);
        messageArgs.putBoolean(AlertDialogFragment.Enable_Neutral_Btn, true);

        FragmentManager manager = getSupportFragmentManager();
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.setArguments(messageArgs);
        dialog.show(manager, "dialog");
    }

    public void onStartTestClick(View view) {

        if (view.getId() == R.id.btnStartTest)
        {
            Intent mainActivityIntent = new Intent(PrefActivity.this, MainActivity.class);

            mainActivityIntent.putExtra("turnsCount", turnsCount);
            mainActivityIntent.putExtra("avgChecked", chkboxAvgMark.isChecked());

            Log.d(appTAG, "onClick-turnsCount= " + turnsCount);

            startActivityForResult(mainActivityIntent, ConfResId);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        state.putBoolean("avgChecked", chkboxAvgMark.isChecked());
        state.putBoolean("newQuestOnlyChecked", chkboxNewQuestOnly.isChecked());
        state.putInt("turnsCount", turnsCount);

        Log.d(appTAG,"onSaveInstanceState-turnsCount= " + turnsCount);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        savedInstanceState.getBoolean("avgChecked", avgChecked);
        savedInstanceState.getBoolean("newQuestOnlyChecked", newQuestOnlyChecked);
        savedInstanceState.getInt("turnsCount", turnsCount);

        Log.d("eng_chk","onRestoreInstanceState-turnsCount= " + turnsCount);
    }

    void savePrefs() {

        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();

        ed.putInt("turnsCount", turnsCount);
        ed.putBoolean("avgChecked", chkboxAvgMark.isChecked());
        ed.putBoolean("newQuestOnlyChecked", chkboxNewQuestOnly.isChecked());

        ed.commit();
    }

    void loadPrefs() {

        sPref = getPreferences(MODE_PRIVATE);

        turnsCount = sPref.getInt("turnsCount", 5);
        avgChecked = sPref.getBoolean("avgChecked", true);
        newQuestOnlyChecked = sPref.getBoolean("newQuestOnlyChecked", true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ConfResId) {
            if (resultCode == RESULT_OK) {

                avgResult = data.getFloatExtra("avgResult", 0);

                Log.d(appTAG, "onActivityResult-avgResult= " + avgResult);
            }
        }
    }

    public void exit()
    {
        savePrefs();

        finish();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {

            exit();

            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
