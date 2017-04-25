package ua.com.adjuster.adjuster.frontend;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import ua.com.adjuster.adjuster.R;
import ua.com.adjuster.adjuster.core.adjustment.AdjustmentTask;
import ua.com.adjuster.adjuster.core.adjustment.ArtilleryType;
import ua.com.adjuster.adjuster.core.database.DataBaseHelper;

public class CreateAdjustmentActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_create_adjustment);

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
                RangerfinderCreatingFragment rangerfinderCreatingFragment = new RangerfinderCreatingFragment();

                /*R.id.frag_container - специально вложеный в макет Активити фрейм
                * В нем и запускается фрагмент*/

                fragmentTransaction.add(R.id.create_frag_container, rangerfinderCreatingFragment);
                fragmentTransaction.commit();

                /*Передаем в фрагмент тип пристрелки и артиллерии, инициируем переменную названия пристрелки*/

                /*rangerfinderCreatingFragment.setAdjustmentTask(adjustmentTypeId, artilleryType);*/
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


}
