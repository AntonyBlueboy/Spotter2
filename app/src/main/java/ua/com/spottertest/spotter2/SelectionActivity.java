package ua.com.spottertest.spotter2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import ua.com.spottertest.spotter2.core.AdjustmentTask;
import ua.com.spottertest.spotter2.core.ArtilleryType;

/*Activity в котором происходит выбор занятия: задачи на "Дуй в тысячу", пристрелка или теоретические вводные*/

public class SelectionActivity extends AppCompatActivity implements View.OnClickListener{
    Spinner systemSpinner, adjSpinner;
    Button goToTaskButton, customTaskButton, theoryButton, milsTaskButton;
    String userName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        /*Получаем из интента позывной аккаунта*/

        userName = getIntent().getStringExtra("userName");

        /*Инициализируем все view, всем кнопкам присваиваем OnClickListener, то есть само активити*/

        goToTaskButton = (Button) findViewById(R.id.selGoToTaskButton);
        goToTaskButton.setOnClickListener(this);
        customTaskButton = (Button) findViewById(R.id.selCustomTaskButton);
        //позже
        theoryButton = (Button) findViewById(R.id.selTheoryButton);
        theoryButton.setOnClickListener(this);
        milsTaskButton = (Button) findViewById(R.id.selMilsTaskButton);
        milsTaskButton.setOnClickListener(this);

        /*Создаем и присваиваем спиннер артсистем, а также адаптер связывающий перечень описаний доступных
        артилерийских систем с спиннером артсистем*/

        systemSpinner = (Spinner) findViewById(R.id.selSystemSpinner);
        ArrayAdapter<String> typeSpinAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                ArtilleryType.getDescriptions());
        systemSpinner.setAdapter(typeSpinAdapter);

        /*Создаем и присваиваем спиннер пристрелок, а также адаптер связывающий ресурс массива с
         названиями пристрелок с спиннером типа пристрелки*/

        adjSpinner = (Spinner) findViewById(R.id.selAdjSpinner);
        ArrayAdapter<String> adjSpinAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.adjustmentTasks));

        adjSpinner.setAdapter(adjSpinAdapter);
    }

    @Override
    public void onClick(View view) {
        Intent nextActivityIntent = null;
        switch (view.getId()){
            case R.id.selTheoryButton :

                /*При нажатии кнопки selTheoryButton создаем интент на актвити с вводными и присваиваем nextActivityIntent*/

                nextActivityIntent = new Intent(this, TheoryActivity.class);
                break;
            case R.id.selMilsTaskButton :

                /*При нажатии кнопки selMilsTaskButton создаем интент на актвити с задачами
                 на "дуй в тысячу" и присваиваем nextActivityIntent*/

                nextActivityIntent = new Intent(this, MilTaskActivity.class);

                break;
            case R.id.selGoToTaskButton :

                 /*При нажатии кнопки selGoToTaskButton извлекаем опистание типа арты
                  и типа пристрелки из соответствующих спиннеров, создаем интент на актвити с задачами
                 на пристрелку, и присваиваем nextActivityIntent. Добавляем в экстра описание типа арты и идентификатор пристрелки*/

                String artilleryTypeDescription = systemSpinner.getSelectedItem().toString();
                String adjustmentType = adjSpinner.getSelectedItem().toString();
                switch (adjustmentType){
                    case "З далекоміром" :
                        nextActivityIntent = new Intent(this, PreparingAdjustmentActivity.class);
                        nextActivityIntent.putExtra("Artillery Description", artilleryTypeDescription);
                        nextActivityIntent.putExtra("Adjustment Type Id", AdjustmentTask.RANGEFINDER_TYPE);
                        break;
                }
                break;
        }

        /*К люому из присвоенных интентов довешиваем позывной юзера и стартуем следующее активити*/

        nextActivityIntent.putExtra("userName", userName);
        startActivity(nextActivityIntent);

    }
}
