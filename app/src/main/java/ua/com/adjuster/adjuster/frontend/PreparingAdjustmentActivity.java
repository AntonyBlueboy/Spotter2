package ua.com.adjuster.adjuster.frontend;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import java.math.BigDecimal;

import ua.com.adjuster.adjuster.R;
import ua.com.adjuster.adjuster.core.adjustment.AdjustmentTask;
import ua.com.adjuster.adjuster.core.adjustment.ArtilleryType;
import ua.com.adjuster.adjuster.core.database.DataBaseHelper;

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
        getSupportActionBar().setLogo(R.drawable.ic_action_name);

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
                taskTitle = "Пристрілка з спряженими спостереженнями";
                break;
            case AdjustmentTask.WORLD_SIDES_TYPE:
                WorldSidesPrepareFragment worldSidesPrepareFragment = new WorldSidesPrepareFragment();

                /*R.id.frag_container - специально вложеный в макет Активити фрейм
                * В нем и запускается фрагмент*/

                fragmentTransaction.add(R.id.frag_container, worldSidesPrepareFragment);
                fragmentTransaction.commit();

                /*Передаем в фрагмент тип пристрелки и артиллерии, инициируем переменную названия пристрелки*/

                worldSidesPrepareFragment.setAdjustmentTask(adjustmentTypeId, artilleryType);
                taskTitle = "Пристрілка за сторонами світу";
                break;
        }

        /*Выводим описание стрельбы и артсистемы на Actionbar*/
        getSupportActionBar().setLogo(R.drawable.ic_action_name);
        getSupportActionBar().setTitle(taskTitle + "  " + artilleryTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.adj_activity_menu, menu);

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

    /*Метод определяющий реакцию на выбор строк из списка в меню*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.adjStatisticMenuItem:
                /*При выборе "Статистика занятий"
                * получаем статистику из БД по имени
                * Формируем отчет
                * Выводим его в диалоговое окно*/
                String message = dataBaseHelper.getUserStatsForName(userName);
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_stats_black_24dp);
                makeDialogWindowMessage(getResources().getString(R.string.adjStatisticMenuItemText) + " " + userName,
                        message, drawable);
                break;
            case R.id.adjGoTheoryMenuItem:
                final Intent theoryActIntent = new Intent(this,  TheoryActivity.class);
                theoryActIntent.putExtra("taskId", adjustmentTypeId);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Перейти до теорії?").setPositiveButton("Так",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(theoryActIntent);
                            }
                        });
                builder.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_warning_black_24dp));
                builder.setNegativeButton("Ні", null);
                builder.show();
                break;
            case R.id.adjQuitMenuItem:
                /*При выборе кнопки "Выйти"
                * Создаем диалоговое окно,
                * Переспрашиваем
                * Выходим*/
                AlertDialog.Builder tempBuilder = new AlertDialog.Builder(this);
                tempBuilder.setTitle("Вийти з додатку?").setPositiveButton("Так",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
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

    /*Метод для получения строки с временем выполнения корректировки из милисекунд*/

    private String getTimeString(long millis){
        int minutes = new BigDecimal(((double) millis)/1000/60).setScale(0, BigDecimal.ROUND_DOWN).intValue();
        int seconds = new BigDecimal(((double)millis)/1000%60).setScale(0,BigDecimal.ROUND_HALF_UP).intValue();

        String result = String.format("%02d:%02d",
                minutes, seconds);
        return result;
    }

    /*Метод для получения диалогового окна без возможности выбора*/

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


}
