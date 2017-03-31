package ua.com.spottertest.spotter2;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.math.BigDecimal;

import ua.com.spottertest.spotter2.core.AdjustmentTask;
import ua.com.spottertest.spotter2.core.ArtilleryType;

/*Activity в котором происходит выбор занятия: задачи на "Дуй в тысячу", пристрелка или теоретические вводные*/

public class SelectionActivity extends AppCompatActivity implements View.OnClickListener{
    Spinner systemSpinner, adjSpinner;
    Button goToTaskButton, customTaskButton, theoryButton, milsTaskButton;
    String userName;
    Toolbar toolbar;
    DataBaseHelper dataBaseHelper;
    AlertDialog.Builder builder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selection);

        /*Получаем из интента позывной аккаунта*/

        dataBaseHelper = new DataBaseHelper(this);

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
        toolbar = (Toolbar)findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.SelActMainText));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.selDeleteAcMenuItem:
                builder = new AlertDialog.Builder(this);
                builder.setTitle("Видалити обліковий запис?").setPositiveButton("Так",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                dataBaseHelper.removeUser(userName);
                                Intent loginActivityIntent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(loginActivityIntent);
                                finish();
                            }
                        });
                builder.setNegativeButton("Ні", null);
                builder.show();
                break;
            case R.id.SelChangeAcMenuItem:
                builder = new AlertDialog.Builder(this);
                builder.setTitle("Змінити обліковий запис?").setPositiveButton("Так",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                Intent loginActivityIntent = new Intent(getApplicationContext(), LoginActivity.class);
                                startActivity(loginActivityIntent);
                                finish();
                            }
                        });
                builder.setNegativeButton("Ні", null);
                builder.show();
                break;
            case R.id.SelStatisticMenuItem:
                User user = dataBaseHelper.getUserForName(userName);
                String message = String.format("Коригувань всього    %d" + "\n" +
                                "Уражень                       %d" + "\n" +
                                "Успішність                   %d" + "\n" +
                                "Середній час               %s", user.getTasks(), user.getSuccessTasks(),
                        user.getPercentageOfSuccess(),
                        getTimeString(user.getAverigeTime()));
                makeDialogWindowMessage(getResources().getString(R.string.adjStatisticMenuItemText) + " " + userName,
                        message);
                user.clear();
                break;
            case R.id.SelQuitMenuItem:
                builder = new AlertDialog.Builder(this);
                builder.setTitle("Вийти з додатку?").setPositiveButton("Так",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                finishAffinity();
                            }
                        });
                builder.setNegativeButton("Ні", null);
                builder.show();
        }
        return super.onOptionsItemSelected(item);
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
                nextActivityIntent = new Intent(this, PreparingAdjustmentActivity.class);
                nextActivityIntent.putExtra("Artillery Description", artilleryTypeDescription);
                int adjustmentTypeVar = 0;
                switch (adjustmentType){
                    case "З далекоміром" :
                        adjustmentTypeVar = AdjustmentTask.RANGE_FINDER_TYPE;
                        break;
                    case "З сопрядженим спостереженням":
                        adjustmentTypeVar = AdjustmentTask.DUAL_OBSERVINGS_TYPE;
                        break;
                }
                nextActivityIntent.putExtra("Adjustment Type Id", adjustmentTypeVar);
                break;
        }

        /*К люому из присвоенных интентов довешиваем позывной юзера и стартуем следующее активити*/

        nextActivityIntent.putExtra("userName", userName);
        startActivity(nextActivityIntent);

    }

    @Override
    public void onBackPressed() {
        Intent loginActivityIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(loginActivityIntent);
        finish();
    }

    private void makeDialogWindowMessage(String title, String message){
        builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setMessage(message).setCancelable(false).setNegativeButton("ОК",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private String getTimeString(long millis){
        int minutes = new BigDecimal(((double) millis)/1000/60).setScale(0, BigDecimal.ROUND_DOWN).intValue();
        int seconds = new BigDecimal(((double)millis)/1000%60).setScale(0,BigDecimal.ROUND_HALF_UP).intValue();

        String result = String.format("%02d:%02d",
                minutes, seconds);
        return result;
    }
}
