package ua.com.spottertest.spotter2;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import java.math.BigDecimal;

import ua.com.spottertest.spotter2.core.AdjustmentTask;
import ua.com.spottertest.spotter2.core.ArtilleryType;

/*Корневое активити для фрагментов с задачами по подготовке стрельбы.
 * Вся логика описана в фрагментах разных типов*/

public class PreparingAdjustmentActivity extends AppCompatActivity {

    /*Переменная БД*/

    private DataBaseHelper dataBaseHelper;



    private String userName;

    /*Идентификатор разновидности пристрелики*/

    private int adjustmentTypeId;

    /*Тип артиллерии в виде enum*/

    private ArtilleryType artilleryType;

    Toolbar toolbar;

    String taskTitle;
    String artilleryTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preparing_adjustment);
        dataBaseHelper = new DataBaseHelper(this);

        Intent intent = getIntent();

        userName = intent.getStringExtra("userName");

        /*Получаем тип артиллерии по String'овому описанию, полученому из Интента*/

        artilleryType = ArtilleryType.getTypeForDescription(intent.getStringExtra("Artillery Description"));



        /*Получаем идентификатор пристрелки из Интента, если не получаем, присваиваем 0.
        * При этом идентификаторы начинаются с 1*/

        adjustmentTypeId = intent.getIntExtra("Adjustment Type Id", 0);

        /*Получаем менеджер и транзакцию, для запуска фрагмента*/

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        toolbar = (Toolbar)findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        artilleryTitle = artilleryType.getTypeDescription();

        /*Переход к целевому фрагменту через свич*/

        switch (adjustmentTypeId){
            case AdjustmentTask.RANGE_FINDER_TYPE:
                RangeFinderPrepareFragment rangeFinderPrepareFragment = new RangeFinderPrepareFragment();

                /*R.id.frag_container - специально вложеный в макет Активити фрейм
                * В нем и запускается фрагмент*/

                fragmentTransaction.add(R.id.frag_container, rangeFinderPrepareFragment);
                fragmentTransaction.commit();

                /*Передаем в фрагмент тип пристрелки и артиллерии, инициируем переменную названия пристрелки*/

                rangeFinderPrepareFragment.setAdjustmentTask(adjustmentTypeId, artilleryType);
                taskTitle = "Пристрілка з далекоміром";
                break;
            case AdjustmentTask.DUAL_OBSERVINGS_TYPE:
                DualObservingPrepareFragment dualObservingPrepareFragment = new DualObservingPrepareFragment();

                /*R.id.frag_container - специально вложеный в макет Активити фрейм
                * В нем и запускается фрагмент*/

                fragmentTransaction.add(R.id.frag_container, dualObservingPrepareFragment);
                fragmentTransaction.commit();

                /*Передаем в фрагмент тип пристрелки и артиллерии, инициируем переменную названия пристрелки*/

                dualObservingPrepareFragment.setAdjustmentTask(adjustmentTypeId, artilleryType);
                taskTitle = "Пристрілка з сопрядженим спостереженням";
                break;

        }

        /*Выводим описание стрельбы и артсистемы на Actionbar*/

        getSupportActionBar().setTitle(taskTitle + "  " + artilleryTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.adj_activity_menu, menu);
        return true;
    }

    /*Метод определяющий реакцию на выбор строк из списка в меню*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.adjStatisticMenuItem:
                /*При выборе "Статистика занятий"
                * получаем статистику из БД по имени
                * Формируем отчет
                * Выводим его в диалоговое окно*/
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
                /*При выборе кнопки "Выйти"
                * Создаем диалоговое окно,
                * Переспрашиваем
                * Выходим*/
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Вийти з додатку?").setPositiveButton("Так",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                finishAffinity();
                            }
                        });
                builder.setNegativeButton("Ні", null);
                builder.show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*Метод для получения строки с временем выполнения корректировки из милисекунд*/

    private String getTimeString(long millis){
        int minutes = new BigDecimal(((double) millis)/1000/60).setScale(0, BigDecimal.ROUND_DOWN).intValue();
        int seconds = new BigDecimal(((double)millis)/1000%60).setScale(0,BigDecimal.ROUND_HALF_UP).intValue();

        String result = String.format("%02d:%02d",
                minutes, seconds);
        return result;
    }

    /*Метод для получения диалогового окна без возможности выбора*/

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
}
