package ua.com.spottertest.spotter2.frontend;

import android.content.DialogInterface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.math.BigDecimal;

import ua.com.spottertest.spotter2.R;
import ua.com.spottertest.spotter2.core.database.DataBaseHelper;
import ua.com.spottertest.spotter2.core.database.User;
import ua.com.spottertest.spotter2.core.mils.MilsTaskTrainer;

/*Активити, которое содержить три фрагмента с разными задачами в формате листаемых колонок
* Вся логика задач и GUI описана во фрагментах, тут только инициализация и работа с меню туулбара*/

public class MilTaskActivity extends AppCompatActivity {
    Toolbar toolbar;
    TabLayout tabLayout;
    ViewPager viewPager;
    ViewPagerAdapter viewPagerAdapter;

    /*Переменные фрагментов*/

    MilTaskFragment distanceMilTaskFragment;
    MilTaskFragment angleMilTaskFragment;
    MilTaskFragment sizeMilTaskFragment;

    String userName;

    /*Переменная базы данных*/

    DataBaseHelper dataBaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mil_task);

        /*Инициализируем тулбар и назначаем его экшонбаром*/

        toolbar = (Toolbar) findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Вирішення задач з тисячною");

        /*Инициализируем базу данных и Имя юзернейма из интента*/

        dataBaseHelper = new DataBaseHelper(this);
        userName = getIntent().getStringExtra("userName");

        /*Создаем три фрагмента и передаем в них имя пользователя, что нужно для возврата статистики*/

        distanceMilTaskFragment = new MilTaskFragment();
        distanceMilTaskFragment.setUser(userName);
        distanceMilTaskFragment.setTaskId(MilsTaskTrainer.DISTANCE_TASK);

        angleMilTaskFragment = new MilTaskFragment();
        angleMilTaskFragment.setUser(userName);
        angleMilTaskFragment.setTaskId(MilsTaskTrainer.ANGLE_TASK);

        sizeMilTaskFragment = new MilTaskFragment();
        sizeMilTaskFragment.setUser(userName);
        sizeMilTaskFragment.setTaskId(MilsTaskTrainer.SIZE_TASK);

        /*Инициализируем вью и адаптеры*/

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        viewPagerAdapter.addFragments(distanceMilTaskFragment, "Дистанції");
        viewPagerAdapter.addFragments(angleMilTaskFragment, "Кутоміра");
        viewPagerAdapter.addFragments(sizeMilTaskFragment, "Величини");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.adj_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /*При выборе любого пункта меню
        * Cоздаем и присваиваем пустой временный Юзер
        * Если фрагменты были в работе (Задачи решались), извлекаем статистику в формате User*/

        final User tempUser = new User(userName);
        if(distanceMilTaskFragment.isUsed()) {
            tempUser.pourInUserStats(distanceMilTaskFragment.getStats());
            distanceMilTaskFragment.getStats().clear();
        }
        if(angleMilTaskFragment.isUsed()){
            tempUser.pourInUserStats(angleMilTaskFragment.getStats());
            angleMilTaskFragment.getStats().clear();
        }
        if(sizeMilTaskFragment.isUsed()){
            tempUser.pourInUserStats(sizeMilTaskFragment.getStats());
            sizeMilTaskFragment.getStats().clear();
        }



        switch (item.getItemId()){
            case R.id.adjStatisticMenuItem:

                /*Для пункта вызова статистики,
                * обновляем данные юзера в базе
                * извлекаем обновленные данные и выводим в Окно*/
                dataBaseHelper.refreshUserStats(tempUser);
                User user = dataBaseHelper.getUserForName(userName);
                String message = String.format("Коригувань всього   %d" + "\n" +
                                "Уражень                      %d" + "\n" +
                                "Успішність                  %d" + "\n" +
                                "Середній час              %s\n" +
                                "Задач усього             %d\n" +
                                "Вірних відповідей     %d\n" +
                                "Успішність                  %d", user.getTasks(), user.getSuccessTasks(),
                        user.getPercentageOfSuccess(),
                        getTimeString(user.getAverigeTime()),
                        user.getMilTasks(), user.getSuccessMilTasks(),
                        user.getPercentageOfSuccessMilTasks());
                makeDialogWindowMessage(getResources().getString(R.string.adjStatisticMenuItemText) + " " + userName,
                        message);
                break;
            case R.id.adjQuitMenuItem:

                /*Для пунтка "Выйти"
                * Создаем билдер окон, переспрашиваем, если подтверждает обновляем бд юзера и выходим*/

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Вийти з додатку?").setPositiveButton("Так",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                dataBaseHelper.refreshUserStats(tempUser);
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

    /*Метод для простого вывода в диалоговое окно*/

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
