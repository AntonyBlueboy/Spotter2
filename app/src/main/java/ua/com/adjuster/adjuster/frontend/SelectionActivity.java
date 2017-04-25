package ua.com.adjuster.adjuster.frontend;

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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.math.BigDecimal;

import ua.com.adjuster.adjuster.R;
import ua.com.adjuster.adjuster.core.adjustment.AdjustmentTask;
import ua.com.adjuster.adjuster.core.adjustment.ArtilleryType;
import ua.com.adjuster.adjuster.core.database.DataBaseHelper;

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
        customTaskButton.setOnClickListener(this);
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
        getSupportActionBar().setLogo(R.drawable.ic_action_name);
        getSupportActionBar().setTitle(getResources().getString(R.string.SelActMainText));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_activity_menu, menu);

        SpannableStringBuilder builder;
        MenuItem SelStatisticMenuItem = menu.findItem(R.id.SelStatisticMenuItem);
        builder = new SpannableStringBuilder("  " + SelStatisticMenuItem.getTitle());
        // replace " " with icon
        builder.setSpan(new ImageSpan(this, R.drawable.ic_equalizer_black_24dp), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        SelStatisticMenuItem.setTitle(builder);

        MenuItem SelChangeAcMenuItem = menu.findItem(R.id.SelChangeAcMenuItem);
        builder = new SpannableStringBuilder("  " + SelChangeAcMenuItem.getTitle());
        // replace " " with icon
        builder.setSpan(new ImageSpan(this, R.drawable.ic_change_acc_24dp), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        SelChangeAcMenuItem.setTitle(builder);

        MenuItem selDeleteAcMenuItem = menu.findItem(R.id.selDeleteAcMenuItem);
        builder = new SpannableStringBuilder("  " + selDeleteAcMenuItem.getTitle());
        // replace " " with icon
        builder.setSpan(new ImageSpan(this, R.drawable.ic_delete_black_24dp), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        selDeleteAcMenuItem.setTitle(builder);

        MenuItem SelQuitMenuItem = menu.findItem(R.id.SelQuitMenuItem);
        builder = new SpannableStringBuilder("  " + SelQuitMenuItem.getTitle());
        // replace "*" with icon
        builder.setSpan(new ImageSpan(this, R.drawable.ic_exit_24dp), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        SelQuitMenuItem.setTitle(builder);

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
                builder.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_warning_black_24dp));
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
                builder.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_warning_black_24dp));
                builder.setNegativeButton("Ні", null);
                builder.show();
                break;
            case R.id.SelStatisticMenuItem:
                String message = dataBaseHelper.getUserStatsForName(userName);
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_stats_black_24dp);
                makeDialogWindowMessage(getResources().getString(R.string.adjStatisticMenuItemText) + " " + userName,
                        message, drawable);
                break;
            case R.id.SelQuitMenuItem:
                builder = new AlertDialog.Builder(this);
                builder.setTitle("Вийти з додатку?").setPositiveButton("Так",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                finishAffinity();
                            }
                        });
                builder.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_warning_black_24dp));
                builder.setNegativeButton("Ні", null);
                builder.show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        Intent nextActivityIntent = null;
        String artilleryTypeDescription;
        String adjustmentType;
        int adjustmentTypeVar = 0;

        switch (view.getId()){
            case R.id.selTheoryButton :

                /*При нажатии кнопки selTheoryButton создаем интент на актвити с вводными и присваиваем nextActivityIntent*/

                nextActivityIntent = new Intent(this, TheoryActivity.class);
                nextActivityIntent.putExtra("userName", userName);
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

                artilleryTypeDescription = systemSpinner.getSelectedItem().toString();
                adjustmentType = adjSpinner.getSelectedItem().toString();
                nextActivityIntent = new Intent(this, PreparingAdjustmentActivity.class);
                nextActivityIntent.putExtra("Artillery Description", artilleryTypeDescription);
                switch (adjustmentType){
                    case "З далекоміром" :
                        adjustmentTypeVar = AdjustmentTask.RANGE_FINDER_TYPE;
                        break;
                    case "З спряженими спостереженнями":
                        adjustmentTypeVar = AdjustmentTask.DUAL_OBSERVINGS_TYPE;
                        break;
                    case "За сторонами світу":
                        adjustmentTypeVar = AdjustmentTask.WORLD_SIDES_TYPE;
                        break;
                }
                nextActivityIntent.putExtra("Adjustment Type Id", adjustmentTypeVar);
                break;
            case R.id.selCustomTaskButton:
                /*При нажатии кнопки selGoToTaskButton извлекаем опистание типа арты
                  и типа пристрелки из соответствующих спиннеров, создаем интент на актвити с подготовкой задач
                 на пристрелку, и присваиваем nextActivityIntent. Добавляем в экстра описание типа арты и идентификатор пристрелки*/
                /*
                artilleryTypeDescription = systemSpinner.getSelectedItem().toString();
                adjustmentType = adjSpinner.getSelectedItem().toString();
                nextActivityIntent = new Intent(this, CreateAdjustmentActivity.class);
                nextActivityIntent.putExtra("Artillery Description", artilleryTypeDescription);
                int adjustmentTypeVar2 = 0;
                switch (adjustmentType){
                    case "З далекоміром" :
                        adjustmentTypeVar2 = AdjustmentTask.RANGE_FINDER_TYPE;
                        break;
                    case "З спряженими спостереженнями":
                        adjustmentTypeVar2 = AdjustmentTask.DUAL_OBSERVINGS_TYPE;
                        break;
                    case "За сторонами світу":
                        adjustmentTypeVar2 = AdjustmentTask.WORLD_SIDES_TYPE;
                        break;
                }
                nextActivityIntent.putExtra("Adjustment Type Id", adjustmentTypeVar2);
                break;
                */
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

    private String getTimeString(long millis){
        int minutes = new BigDecimal(((double) millis)/1000/60).setScale(0, BigDecimal.ROUND_DOWN).intValue();
        int seconds = new BigDecimal(((double)millis)/1000%60).setScale(0,BigDecimal.ROUND_HALF_UP).intValue();

        String result = String.format("%02d:%02d",
                minutes, seconds);
        return result;
    }

    private void makeToastMessage(String message){
        Toast.makeText(this, message,
                Toast.LENGTH_SHORT).show();
    }
}
