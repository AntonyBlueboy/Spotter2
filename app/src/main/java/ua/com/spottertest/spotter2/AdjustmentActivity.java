package ua.com.spottertest.spotter2;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ua.com.spottertest.spotter2.core.AdjustmentTask;
import ua.com.spottertest.spotter2.core.ArtilleryMilsUtil;
import ua.com.spottertest.spotter2.core.Correction;
import ua.com.spottertest.spotter2.core.NotMilsFormatException;
import ua.com.spottertest.spotter2.core.RangefinderAdjustmentTask;

/*Активити в котором происходят все пристрелки*/


public class AdjustmentActivity extends AppCompatActivity implements View.OnClickListener{

    /*Переменная инкапсулирующая текущие результаты для передачи в БД*/

    User currentUser;

    /*Переменные необходимые для вывода статистики тут, в отличии от User не очищаются при отображении глобальной статы*/

    private int tasks = 0;
    private int succesfulTasks = 0;
    private double percent = 0.0;
    private long summTime = 0;

    DataBaseHelper dataBaseHelper;

    Toolbar toolbar;

    /*Текстовые поисания разрыва и статы*/

    private TextView adjBurstDescrTV, adjCoefsTV, adjStatTV;

    /*Секундомер*/

    private Chronometer chronometer;

    /*Группы радиокнопок для определения лево/право дальше/ближе корректировок*/

    private RadioGroup adjAngCorrRG, adjDistCorrRG;
    private RadioButton adjLeftRB,adjRightRB, adjMoreRB, adjLessRB;

    /*Флажки для случаев без корректур*/

    private CheckBox adjAngNoCorrCB, adjDistNoCorrCB;

    /*Поля для ввода корректур*/

    private EditText adjAngCorrET, adjDistCorrET;

    /*Кнопки получения / обсчета разрыва*/

    private Button adjCorrBut, adjBurstBut;

    /*Переключатели для динамического изменения условий наблюдения и корректировки*/

    private SwitchCompat adjDistSwitch, adjScaleSwitch;



    /*Переменная пристрелки, индекса пристрелки и логина*/

    private AdjustmentTask task;
    private int taskId;
    private String userName;

    /*дП на 100*/

    private int valueOfScale;

    /*Флажки
    * используеться ли дП на 100*/

    private boolean isScaleUsed;

    /*Используется ли целеуказание в виде дальности по разрыву*/

    private boolean isDistanceUsed = false;

    /*Началась ли стрельба*/

    private boolean isStarted = false;

    /*Текущее описание разрыва в двух видах - отклонении по дальности и дальности разрыва*/

    private String[] currentBurstDescriptions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjustment);


        dataBaseHelper = new DataBaseHelper(this);
        Intent currentIntent = getIntent();
        task = currentIntent.getParcelableExtra(RangefinderAdjustmentTask.class.getCanonicalName());
        taskId = currentIntent.getIntExtra("taskId", 0);
        userName = currentIntent.getStringExtra("userName");
        valueOfScale = task.getValueOfScale();
        isScaleUsed = valueOfScale != 0;
        currentUser = new User(userName);

        /*Вызываем метод инициации элементов интерфейса*/

        initiateGUI();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.adj_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.adjStatisticMenuItem:
                dataBaseHelper.refreshUserStats(currentUser);
                User user = dataBaseHelper.getUserForName(userName);
                String message = String.format("Коригувань всього   %d" + "\n" +
                                               "Уражень                      %d" + "\n" +
                                               "Успішність                  %d" + "\n" +
                                               "Середній час              %s", user.getTasks(), user.getSuccessTasks(),
                                                                        user.getPercentageOfSuccess(),
                                                                        getTimeString(user.getAverigeTime()));
                makeDialogWindowMessage(getResources().getString(R.string.adjStatisticMenuItemText) + " " + userName,
                        message);
                user.clear();
                break;
            case R.id.adjQuitMenuItem:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Вийти з додатку?").setPositiveButton("Так",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                dataBaseHelper.refreshUserStats(currentUser);
                                finishAffinity();
                            }
                        });
                builder.setNegativeButton("Ні", null);
                builder.show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*Метод инициирования интерфейса*/

    private void initiateGUI(){
        adjBurstDescrTV = (TextView) findViewById(R.id.adjBurstDescrTV);
        adjCoefsTV = (TextView) findViewById(R.id.adjCoefsTV);
        adjStatTV = (TextView) findViewById(R.id.adjStatTV);
        chronometer = (Chronometer) findViewById(R.id.adjChron);
        adjAngCorrRG = (RadioGroup) findViewById(R.id.adjAngCorrRG);
        adjDistCorrRG = (RadioGroup) findViewById(R.id.adjDistCorrRG);
        adjLeftRB = (RadioButton) findViewById(R.id.adjLeftRB);
        adjRightRB = (RadioButton) findViewById(R.id.adjRightRB);
        adjMoreRB = (RadioButton) findViewById(R.id.adjMoreRB);
        adjLessRB = (RadioButton) findViewById(R.id.adjLessRB);
        adjAngNoCorrCB = (CheckBox) findViewById(R.id.adjAngNoCorrCB);
        adjAngNoCorrCB.setOnClickListener(this);
        adjDistNoCorrCB = (CheckBox) findViewById(R.id.adjDistNoCorrCB);
        adjDistNoCorrCB.setOnClickListener(this);
        adjAngCorrET = (EditText) findViewById(R.id.adjAngCorrET);
        adjDistCorrET = (EditText) findViewById(R.id.adjDistCorrET);
        adjCorrBut = (Button) findViewById(R.id.adjCorrBut);
        adjCorrBut.setOnClickListener(this);
        adjBurstBut = (Button) findViewById(R.id.adjBurstBut);
        adjBurstBut.setOnClickListener(this);
        adjDistSwitch = (SwitchCompat) findViewById(R.id.adjDistSwitch);
        adjDistSwitch.setOnClickListener(this);
        adjScaleSwitch = (SwitchCompat) findViewById(R.id.adjScaleSwitch);
        adjScaleSwitch.setOnClickListener(this);
        toolbar = (Toolbar)findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        adjCoefsTV.setText(task.getCoefsDescription());
        getSupportActionBar().setTitle(task.getAdjustmentTitle() + "  " + task.getArtylleryTypeName());

        adjScaleSwitch.setChecked(isScaleUsed);
        if (isScaleUsed) adjDistCorrET.setHint(getResources().getString(R.string.adjScaleCorrETHintText));
        else adjDistCorrET.setHint(getResources().getString(R.string.adjMetersCorrETHintText));
        adjScaleSwitch.setEnabled(isScaleUsed);
    }


    /*Метод перехвата кликов*/

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            /*Если клик на переключателе дистанция/отклонение
            * то задаем флаг
            * если бой начался, меняем описание разрыва*/

            case R.id.adjDistSwitch:
                isDistanceUsed = adjDistSwitch.isChecked();
                if (isStarted) printBurstDescription();
                break;

            /*Клик на переключателе для дП на 100
            * задаем флаг
            * присваиваем соответствующему полю ввода нужный хинт*/

            case R.id.adjScaleSwitch:
                    isScaleUsed = adjScaleSwitch.isChecked();
                    if (isScaleUsed) adjDistCorrET.setHint(getResources().getString(R.string.adjScaleCorrETHintText));
                    else adjDistCorrET.setHint(getResources().getString(R.string.adjMetersCorrETHintText));
                break;

            /*Клик на флажке "без корректур"
            * выставляем группу радиокнопок на значение по умолчанию
            * отключаем/включаем соответствующие радиогруппы*/

            case R.id.adjAngNoCorrCB:
                boolean isAngleWithoutCorr = adjAngNoCorrCB.isChecked();
                adjLeftRB.setChecked(true);
                adjLeftRB.setEnabled(!isAngleWithoutCorr);
                adjRightRB.setEnabled(!isAngleWithoutCorr);
                adjAngCorrET.setEnabled(!isAngleWithoutCorr);
                break;
            case R.id.adjDistNoCorrCB:
                boolean isDistWithoutCorr = adjDistNoCorrCB.isChecked();
                adjLessRB.setChecked(true);
                adjMoreRB.setEnabled(!isDistWithoutCorr);
                adjLessRB.setEnabled(!isDistWithoutCorr);
                adjDistCorrET.setEnabled(!isDistWithoutCorr);
                break;

            /*При клике на кнопках разрыва и проверки вызываем соответствующие методы*/

            case R.id.adjBurstBut:
                onBurstButtonClicked();
                break;
            case R.id.adjCorrBut:
                onCorrectionButtonClicked();
                break;
        }
    }

    /*Метод для вывода в поле описания разрыва в одной из полученых форм
    * [0] для отклонения
    * [1] для дистанции*/

    private void printBurstDescription(){
        if (!isDistanceUsed) adjBurstDescrTV.setText(currentBurstDescriptions[0]);
        else adjBurstDescrTV.setText(currentBurstDescriptions[1]);
    }

    /*Метод для вызова разрыва
    * Обнуляем секундомер, спускаем его
    * возвращаем поля и флажки в состояния по умолчанию
    * выводим описание разрыва
    * выставляем флаг isStarted в true*/

    private void onBurstButtonClicked(){

        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
        adjLeftRB.setChecked(true);
        adjLeftRB.setEnabled(true);
        adjLessRB.setChecked(true);
        adjLessRB.setEnabled(true);
        adjRightRB.setEnabled(true);
        adjMoreRB.setEnabled(true);
        adjAngNoCorrCB.setChecked(false);
        adjDistNoCorrCB.setChecked(false);
        adjAngCorrET.setText("");
        adjAngCorrET.setEnabled(true);
        adjDistCorrET.setText("");
        adjDistCorrET.setEnabled(true);
        adjBurstBut.setEnabled(false);
        adjCorrBut.setEnabled(true);

        currentBurstDescriptions = task.getBurstDescription();
        printBurstDescription();

        isStarted = true;

    }

    /*метод проверки введенных корректур*
    Остановить секундомер и взять отсчет
    Собрать сообщение о результатах и вывести в диалоговом окне
    при ошибке - вывести тост
    Заблочить/разблочить нужные кнопки
     */

    private void onCorrectionButtonClicked(){
        try {

            /*Для проверки передаем в обьект пристрелки корректуру из полей и флажок использования дП на 100*/

            StringBuilder resulMessage = new StringBuilder(task.checkCorrection(getUserCorrection(), isScaleUsed));
            chronometer.stop();
            long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
            summTime += elapsedMillis;
            resulMessage.append("\nЧас - " + getTimeString(elapsedMillis));
            String title = "Ціль не вражено";
            if(task.isLastCorrectionSuccessful()){
                title = "Ціль вражено";
            }
            makeDialogWindowMessage(title, resulMessage.toString());

            adjBurstBut.setEnabled(true);
            adjCorrBut.setEnabled(false);

            onTaskDecided(task.isLastCorrectionSuccessful());

        }
        catch (NotMilsFormatException e){
            makeToastMessage(getResources().getString(R.string.milsExMessage));
                    }
        catch (NumberFormatException e){
            makeToastMessage(getResources().getString(R.string.numExMessage));
        }

    }



    private Correction getUserCorrection() throws NotMilsFormatException, NumberFormatException{
        Correction userCorrection;
        boolean isTotheLeft, isLower;
        int angleCorrection, distanceCorrection, scaleCorrection;

        if (adjAngNoCorrCB.isChecked() & adjDistNoCorrCB.isChecked()) userCorrection = null;
        else {
            /*углемер левее или нет*/

            isTotheLeft = adjAngCorrRG.getCheckedRadioButtonId() == R.id.adjLeftRB;

            /*угломер изменить на количество тысячных*/

            if (adjAngNoCorrCB.isChecked()) angleCorrection = 0;
            else angleCorrection = ArtilleryMilsUtil.convertToIntFormat(adjAngCorrET.getText().toString());

            /*дальность меньше или нет*/

            isLower = adjDistCorrRG.getCheckedRadioButtonId() == R.id.adjLessRB;

            /*Дальность изменить на количество метров*/

            if (adjDistNoCorrCB.isChecked()) distanceCorrection = 0;
            else distanceCorrection = Integer.parseInt(adjDistCorrET.getText().toString());

            /*Дальность изменить на количество делений прицела*/

            scaleCorrection = new BigDecimal((double)distanceCorrection / 100 * valueOfScale)
                    .setScale(0, RoundingMode.HALF_UP).intValue();

            userCorrection = new Correction(isLower, distanceCorrection,scaleCorrection, isTotheLeft, angleCorrection);
        }
        return userCorrection;
    }

    private void makeToastMessage(String message){
        Toast.makeText(this, message,
                Toast.LENGTH_SHORT).show();
    }

    private void makeDialogWindowMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message).setCancelable(false).setNegativeButton("До стрільби",
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /*Метод для получения строки с временем выполнения корректировки из милисекунд*/

    private String getTimeString(long millis){
        int minutes = new BigDecimal(((double) millis)/1000/60).setScale(0, BigDecimal.ROUND_DOWN).intValue();
        int seconds = new BigDecimal(((double)millis)/1000%60).setScale(0,BigDecimal.ROUND_HALF_UP).intValue();

        String result = String.format("%02d:%02d",
                minutes, seconds);
        return result;
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Залишити пристрілку?").setPositiveButton("Так",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
                        dataBaseHelper.refreshUserStats(currentUser);
                        finish();
                    }
                });
        builder.setNegativeButton("Ні", null);
        builder.show();
    }

    public void onTaskDecided(boolean isSuccessful){
        tasks++;
        percent = (double) succesfulTasks / tasks * 100;
        String avTime = getTimeString(summTime/tasks);

        adjStatTV.setText(String.format("%d з %d (%.1f%%)\nСер. час - %s", succesfulTasks, tasks, percent, avTime));
        if (percent < 50) adjStatTV.setBackgroundColor(Color.RED);
        else if (percent > 80) adjStatTV.setBackgroundColor(Color.GREEN);
        else adjStatTV.setBackgroundColor(Color.YELLOW);

        if (isSuccessful){
            succesfulTasks++;
            currentUser.incrementSuccessTasks(summTime/tasks);
        }
        else currentUser.incrementUnsuccessTasks(summTime/tasks);
    }


}
