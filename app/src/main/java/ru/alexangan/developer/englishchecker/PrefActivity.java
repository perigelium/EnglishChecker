package ru.alexangan.developer.englishchecker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class PrefActivity extends AppCompatActivity {

    final static String appTAG = "eng_chk";
    private TextView mTextValue;
    String[] data = {"5", "10", "15", "20"};
    public final static String QuestInTestName = "ru.alexangan.developer.englishchecker.QuestInTest";
    public static int questInTestCount = 5;
    public static char resCode = 'c';
    Spinner spinner;
    CheckBox chkbox;
    SharedPreferences sPref;
    Boolean avgChecked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pref);

        chkbox = (CheckBox) findViewById(R.id.chkBoxAverage);
        spinner = (Spinner) findViewById(R.id.spQuestCount);


        loadPrefs();

        // адаптер
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        chkbox.setChecked(avgChecked);

        spinner.setAdapter(adapter);
        // заголовок
        spinner.setPrompt("Количество вопросов в тесте");
        // выделяем элемент
        //spinner.setSelection(0);
        spinner.setSelection(questInTestCount/5 - 1);
        // устанавливаем обработчик нажатия
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                //spinner.getSelectedItemPosition()
                // or
                questInTestCount = Integer.valueOf(spinner.getSelectedItem().toString());
            }
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
    }

    public void onClick(View view) {

        Intent answerIntent = new Intent();
        answerIntent.putExtra(QuestInTestName, questInTestCount);
        answerIntent.putExtra(QuestInTestName, chkbox.isChecked());
        answerIntent.putExtra(QuestInTestName, resCode);
        resCode = 'c';

        setResult(RESULT_OK, answerIntent);

        savePrefs();
        this.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            resCode = 'f';
            onClick(null);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putBoolean("avgChecked", chkbox.isChecked());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        resCode = 'c';
        savedInstanceState.getBoolean("avgChecked", avgChecked);
    }

    void savePrefs() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();

        ed.putInt("avgResult", questInTestCount);
        ed.putBoolean("avgChk", chkbox.isChecked());

        ed.commit();
    }

    void loadPrefs() {
        sPref = getPreferences(MODE_PRIVATE);

        questInTestCount = sPref.getInt("avgResult", 0);

        avgChecked = sPref.getBoolean("avgChk", false);
    }
}
