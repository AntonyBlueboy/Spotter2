package ua.com.adjuster.adjuster.frontend;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
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

import ua.com.adjuster.adjuster.R;
import ua.com.adjuster.adjuster.core.adjustment.AdjustmentTask;
import ua.com.adjuster.adjuster.core.database.User;
import ua.com.adjuster.adjuster.core.mils.ArtilleryMilsUtil;
import ua.com.adjuster.adjuster.core.adjustment.Correction;
import ua.com.adjuster.adjuster.core.adjustment.DualObservingAdjustmentTask;
import ua.com.adjuster.adjuster.core.mils.NotMilsFormatException;
import ua.com.adjuster.adjuster.core.adjustment.RangefinderAdjustmentTask;
import ua.com.adjuster.adjuster.core.adjustment.WorldSidesAdjustmentTask;
import ua.com.adjuster.adjuster.core.database.DataBaseHelper;

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

    private SwitchCompat adjDistOrAngSwitch, adjScaleSwitch;

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

    private boolean isDistanceOrAnglesUsed = false;

    /*Началась ли стрельба*/

    private boolean isStarted = false;

    /*Текущее описание разрыва в двух видах - отклонении по дальности и дальности разрыва*/

    private String[] currentBurstDescriptions;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adjustment);

        /*Вызываем метод инициации элементов интерфейса*/

        initiateGUI();
        dataBaseHelper = new DataBaseHelper(this);

        /*Если есть сохраненное состояние*/
        if (savedInstanceState != null){
            /*Восстанавливаем переменные*/

            taskId = savedInstanceState.getInt("taskId");
            task = savedInstanceState.getParcelable("task");
            userName = savedInstanceState.getString("userName");
            tasks = savedInstanceState.getInt("tasks");
            succesfulTasks = savedInstanceState.getInt("succesfulTasks");
            percent = savedInstanceState.getDouble("percent");
            summTime = savedInstanceState.getLong("summTime");
            isDistanceOrAnglesUsed = savedInstanceState.getBoolean("isDistanceOrAnglesUsed");
            isStarted = savedInstanceState.getBoolean("isStarted");
            currentBurstDescriptions = savedInstanceState.getStringArray("currentBurstDescriptions");

            /*Восстанавливаем состояние GUI*/

            adjLeftRB.setChecked(savedInstanceState.getBoolean("adjLeftRB.isChecked"));
            adjLessRB.setChecked(savedInstanceState.getBoolean("adjLessRB.isChecked"));
            adjAngNoCorrCB.setChecked(savedInstanceState.getBoolean("adjAngNoCorrCB.isChecked"));
            onClick(adjAngNoCorrCB);
            adjDistNoCorrCB.setChecked(savedInstanceState.getBoolean("adjDistNoCorrCB.isChecked"));
            onClick(adjDistNoCorrCB);
            adjCorrBut.setEnabled(savedInstanceState.getBoolean("adjCorrBut.isEnabled"));
            adjBurstBut.setEnabled(savedInstanceState.getBoolean("adjBurstBut.isEnabled"));

            adjAngCorrET.setText(savedInstanceState.getString("adjAngCorrET.text", ""));
            adjDistCorrET.setText(savedInstanceState.getString("adjDistCorrET.text", ""));

            if(isStarted) printBurstDescription();
            if(tasks > 0) printStats();
        }
        else {
            /*Если сохраненного состояния нет, то все инициируется с нуля*/
            Intent currentIntent = getIntent();
            taskId = currentIntent.getIntExtra("taskId", 0);

            switch (taskId) {
                case AdjustmentTask.RANGE_FINDER_TYPE:
                    task = currentIntent.getParcelableExtra(RangefinderAdjustmentTask.class.getCanonicalName());
                    break;
                case AdjustmentTask.DUAL_OBSERVINGS_TYPE:
                    task = currentIntent.getParcelableExtra(DualObservingAdjustmentTask.class.getCanonicalName());
                    break;
                case AdjustmentTask.WORLD_SIDES_TYPE:
                    task = currentIntent.getParcelableExtra(WorldSidesAdjustmentTask.class.getCanonicalName());
                    break;
            }

            userName = currentIntent.getStringExtra("userName");
        }

        /*Дальнейше выполняется независимо от способа инициации переменных и интерфейса*/

        valueOfScale = task.getValueOfScale();
        isScaleUsed = valueOfScale != 0;
        currentUser = new User(userName);

        if (taskId == AdjustmentTask.WORLD_SIDES_TYPE) {
            adjDistOrAngSwitch.setVisibility(View.INVISIBLE);
            adjDistOrAngSwitch.setEnabled(false);
        }
        else {
            adjDistOrAngSwitch.setOnClickListener(this);
        }
        if (taskId == AdjustmentTask.DUAL_OBSERVINGS_TYPE) adjDistOrAngSwitch.setText("Дирекційні по цілі");

        adjCoefsTV.setText(task.getCoefsDescription());
        getSupportActionBar().setLogo(R.drawable.ic_action_name);
        getSupportActionBar().setTitle(task.getAdjustmentTitle() + "  " + task.getArtylleryTypeName());

        adjScaleSwitch.setChecked(isScaleUsed);
        adjDistOrAngSwitch.setChecked(isDistanceOrAnglesUsed);
        if (isScaleUsed) adjDistCorrET.setHint(getResources().getString(R.string.adjScaleCorrETHintText));
        else adjDistCorrET.setHint(getResources().getString(R.string.adjMetersCorrETHintText));
        adjScaleSwitch.setEnabled(isScaleUsed);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        /*Сохраняем переменные при уничтожении активити (например при повороте экрана (которое происходит
        на некоторых устройствах при выходе из спящего режима))*/

        outState.putParcelable("task", task);
        outState.putInt("taskId", taskId);
        outState.putString("userName", userName);
        outState.putInt("tasks", tasks);
        outState.putInt("succesfulTasks", succesfulTasks);
        outState.putDouble("percent", percent);
        outState.putLong("summTime", summTime);
        outState.putBoolean("isDistanceOrAnglesUsed", isDistanceOrAnglesUsed);
        outState.putBoolean("isStarted", isStarted);
        outState.putStringArray("currentBurstDescriptions", currentBurstDescriptions);

        /*Сохраняем состояние вью*/

        outState.putBoolean("adjLeftRB.isChecked", adjLeftRB.isChecked());
        outState.putBoolean("adjLessRB.isChecked", adjLessRB.isChecked());
        outState.putBoolean("adjAngNoCorrCB.isChecked", adjAngNoCorrCB.isChecked());
        outState.putBoolean("adjDistNoCorrCB.isChecked", adjDistNoCorrCB.isChecked());
        outState.putBoolean("adjCorrBut.isEnabled", adjCorrBut.isEnabled());
        outState.putBoolean("adjBurstBut.isEnabled", adjBurstBut.isEnabled());

        outState.putString("adjAngCorrET.text", adjAngCorrET.getText().toString());
        outState.putString("adjDistCorrET.text", adjDistCorrET.getText().toString());


        dataBaseHelper.refreshUserStats(currentUser);
        super.onSaveInstanceState(outState);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.adj_activity_menu, menu);
        /*При инициации пунктов меню, мы доваляем к строкам в них иконки*/
        SpannableStringBuilder builder;
        MenuItem theoryStatisticMenuItem = menu.findItem(R.id.adjStatisticMenuItem);
        builder = new SpannableStringBuilder("  " + theoryStatisticMenuItem.getTitle());
        // replace " " with icon
        builder.setSpan(new ImageSpan(this, R.drawable.ic_equalizer_black_24dp), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        theoryStatisticMenuItem.setTitle(builder);

        MenuItem adjGoTheoryMenuItem = menu.findItem(R.id.adjGoTheoryMenuItem);
        builder = new SpannableStringBuilder("  " + adjGoTheoryMenuItem.getTitle());
        // replace " " with icon
        builder.setSpan(new ImageSpan(this, R.drawable.ic_go_theory_24dp), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        adjGoTheoryMenuItem.setTitle(builder);

        MenuItem adjQuitMenuItem = menu.findItem(R.id.adjQuitMenuItem);
        builder = new SpannableStringBuilder("  " + adjQuitMenuItem.getTitle());
        // replace " " with icon
        builder.setSpan(new ImageSpan(this, R.drawable.ic_exit_24dp), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        adjQuitMenuItem.setTitle(builder);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.adjStatisticMenuItem:
                dataBaseHelper.refreshUserStats(currentUser);
                String message = dataBaseHelper.getUserStatsForName(userName);
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_stats_black_24dp);
                makeDialogWindowMessage(getResources().getString(R.string.adjStatisticMenuItemText) + " " + userName,
                        message, drawable);
                currentUser.clear();
                break;
            case R.id.adjGoTheoryMenuItem:
                final Intent theoryActIntent = new Intent(this,  TheoryActivity.class);
                theoryActIntent.putExtra("taskId", taskId);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Перейти до теорії?").setPositiveButton("Так",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                dataBaseHelper.refreshUserStats(currentUser);
                                startActivity(theoryActIntent);
                            }
                        });
                builder.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_warning_black_24dp));
                builder.setNegativeButton("Ні", null);
                builder.show();
                break;
            case R.id.adjQuitMenuItem:
                AlertDialog.Builder tempBuilder = new AlertDialog.Builder(this);
                tempBuilder.setTitle("Вийти з додатку?").setPositiveButton("Так",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                dataBaseHelper.refreshUserStats(currentUser);
                                finishAffinity();
                            }
                        });
                tempBuilder.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_warning_black_24dp));
                tempBuilder.setNegativeButton("Ні", null);
                tempBuilder.show();

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
        adjDistOrAngSwitch = (SwitchCompat) findViewById(R.id.adjDistSwitch);
        adjScaleSwitch = (SwitchCompat) findViewById(R.id.adjScaleSwitch);
        adjScaleSwitch.setOnClickListener(this);
        toolbar = (Toolbar)findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

    }


    /*Метод перехвата кликов*/

    @Override
    public void onClick(View view) {
        switch (view.getId()){

            /*Если клик на переключателе дистанция/отклонение
            * то задаем флаг
            * если бой начался, меняем описание разрыва
            * в пристрелке по сторонам света кнопка отсутствует*/

            case R.id.adjDistSwitch:
                isDistanceOrAnglesUsed = adjDistOrAngSwitch.isChecked();
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
        if (!isDistanceOrAnglesUsed) adjBurstDescrTV.setText(currentBurstDescriptions[0]);
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
        adjDistCorrET.setText("");
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

            StringBuilder resultMessage = new StringBuilder(task.checkCorrection(getUserCorrection(), isScaleUsed));
            chronometer.stop();
            long elapsedMillis = SystemClock.elapsedRealtime() - chronometer.getBase();
            summTime += elapsedMillis;
            resultMessage.append("\nЧас - " + getTimeString(elapsedMillis));
            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_loose_black_24dp);
            String title = "Ціль не вражено";
            if(task.isLastCorrectionSuccessful()){
                title = "Ціль вражено";
                drawable = ContextCompat.getDrawable(this, R.drawable.target_destroyed);
            }
            makeDialogWindowMessage(title, resultMessage.toString(), drawable);

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

            if (adjDistNoCorrCB.isChecked()) {
                distanceCorrection = 0;
                scaleCorrection = 0;
            }
            else {
                if(!isScaleUsed) {
                    distanceCorrection = Integer.parseInt(adjDistCorrET.getText().toString());

            /*Дальность изменить на количество делений прицела*/

                    scaleCorrection = new BigDecimal((double) distanceCorrection / 100 * valueOfScale)
                            .setScale(0, RoundingMode.HALF_UP).intValue();
                }
                else {
                    scaleCorrection = Integer.parseInt(adjDistCorrET.getText().toString());
                    distanceCorrection = new BigDecimal((double) scaleCorrection/valueOfScale * 100)
                            .setScale(0, RoundingMode.HALF_UP).intValue();
                }

            }

            userCorrection = new Correction(isLower, distanceCorrection,scaleCorrection, isTotheLeft, angleCorrection);
        }
        return userCorrection;
    }

    private void makeToastMessage(String message){
        Toast.makeText(this, message,
                Toast.LENGTH_SHORT).show();
    }

    private void makeDialogWindowMessage(String title, String message, Drawable drawable){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message).setCancelable(false).setNegativeButton("До стрільби",
                new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.setIcon(drawable);
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
        builder.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_warning_black_24dp));
        builder.setNegativeButton("Ні", null);
        builder.show();
    }

    public void onTaskDecided(boolean isSuccessful){
        tasks++;

        if (isSuccessful){
            succesfulTasks++;
            currentUser.incrementSuccessTasks(summTime/tasks);
        }
        else currentUser.incrementUnsuccessTasks(summTime/tasks);

        percent = (double) succesfulTasks / tasks * 100;
        printStats();

    }

    private void printStats(){
        String avTime = getTimeString(summTime/tasks);

        adjStatTV.setText(String.format("%d з %d (%.1f%%)\nСер. час - %s", succesfulTasks, tasks, percent, avTime));
        if (percent < 50) adjStatTV.setBackgroundColor(Color.RED);
        else if (percent > 80) adjStatTV.setBackgroundColor(Color.GREEN);
        else adjStatTV.setBackgroundColor(Color.YELLOW);
    }


}
