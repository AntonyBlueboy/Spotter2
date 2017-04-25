package ua.com.adjuster.adjuster.frontend;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
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
import ua.com.adjuster.adjuster.core.database.DataBaseHelper;
import ua.com.adjuster.adjuster.core.database.User;
import ua.com.adjuster.adjuster.core.mils.MilsTaskTrainer;

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
        getSupportActionBar().setLogo(R.drawable.ic_action_name);
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
        viewPagerAdapter.addFragments(distanceMilTaskFragment, "Дистанція");
        viewPagerAdapter.addFragments(angleMilTaskFragment, "Кутомір");
        viewPagerAdapter.addFragments(sizeMilTaskFragment, "Величина");
        viewPager.setAdapter(viewPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mil_activity_menu, menu);

        SpannableStringBuilder builder;
        MenuItem milStatisticMenuItem = menu.findItem(R.id.milStatisticMenuItem);
        builder = new SpannableStringBuilder("  " + milStatisticMenuItem.getTitle());
        // replace " " with icon
        builder.setSpan(new ImageSpan(this, R.drawable.ic_equalizer_black_24dp), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        milStatisticMenuItem.setTitle(builder);

        MenuItem milGoTheoryMenuItem = menu.findItem(R.id.milGoTheoryMenuItem);
        builder = new SpannableStringBuilder("  " + milGoTheoryMenuItem.getTitle());
        // replace " " with icon
        builder.setSpan(new ImageSpan(this, R.drawable.ic_go_theory_24dp), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        milGoTheoryMenuItem.setTitle(builder);

        MenuItem milQuitMenuItem = menu.findItem(R.id.milQuitMenuItem);
        builder = new SpannableStringBuilder("  " + milQuitMenuItem.getTitle());
        // replace " " with icon
        builder.setSpan(new ImageSpan(this, R.drawable.ic_exit_24dp), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        milQuitMenuItem.setTitle(builder);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        /*При выборе любого пункта меню
        * Cоздаем и присваиваем пустой временный Юзер
        * Если фрагменты были в работе (Задачи решались), извлекаем статистику в формате User*/

        final User tempUser = getStatsFromFragments();
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
            case R.id.milStatisticMenuItem:

                /*Для пункта вызова статистики,
                * обновляем данные юзера в базе
                * извлекаем обновленные данные и выводим в Окно*/
                dataBaseHelper.refreshUserStats(tempUser);
                String message = dataBaseHelper.getUserStatsForName(userName);
                Drawable drawable = ContextCompat.getDrawable(this, R.drawable.ic_stats_black_24dp);
                makeDialogWindowMessage(getResources().getString(R.string.adjStatisticMenuItemText) + " " + userName,
                        message, drawable);
                break;
            case R.id.milQuitMenuItem:

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
                builder.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_warning_black_24dp));
                builder.setNegativeButton("Ні", null);
                builder.show();
                break;
            case R.id.milGoTheoryMenuItem:
                final Intent theoryActIntent = new Intent(this,  TheoryActivity.class);
                theoryActIntent.putExtra("taskId", 0);
                AlertDialog.Builder tempBuilder = new AlertDialog.Builder(this);
                tempBuilder.setTitle("Перейти до теорії?").setPositiveButton("Так",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                dataBaseHelper.refreshUserStats(tempUser);
                                startActivity(theoryActIntent);
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

    /*Метод для простого вывода в диалоговое окно*/

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

    @Override
    public void onBackPressed() {
        final User tempUser = getStatsFromFragments();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Залишити заняття?").setPositiveButton("Так",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id) {
                        dataBaseHelper.refreshUserStats(tempUser);
                        finish();
                    }
                });
        builder.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_warning_black_24dp));
        builder.setNegativeButton("Ні", null);
        builder.show();
    }

    private User getStatsFromFragments(){
        User tempUser = new User(userName);
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
        return tempUser;
    }
}
