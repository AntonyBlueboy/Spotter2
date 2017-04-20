package ua.com.spottertest.spotter2.frontend;

import android.content.DialogInterface;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import java.util.ArrayList;

import ua.com.spottertest.spotter2.R;
import ua.com.spottertest.spotter2.core.adjustment.AdjustmentTask;
import ua.com.spottertest.spotter2.core.database.DataBaseHelper;

public class TheoryActivity extends AppCompatActivity implements View.OnClickListener{
    ActionBarDrawerToggle drawerToggle;
    Toolbar toolbar;
    RecyclerView recyclerView;
    DrawerLayout drawerLayout;
    RecyclerView.Adapter adapter;
    RecyclerView.LayoutManager layoutManager;
    ArrayList<String> arrayList = new ArrayList<>();
    WebView webView;
    private static final String main_theory_url = "file:///android_asset/Main_theory.html";
    private static final String mil_theory_url = "file:///android_asset/Mil_theory.html";
    private static final String range_adjust_url = "file:///android_asset/RangerFinder_adjustment.html";
    private static final String timer_adjust_url = "file:///android_asset/Timer_adjustment.html";
    private static final String szr_adjust_url = "file:///android_asset/Szr_adjustment.html";
    private static final String dual_adjust_url = "file:///android_asset/Dual_adjustment.html";
    private static final String ws_adjust_url = "file:///android_asset/WS_adjustment.html";
    DataBaseHelper dataBaseHelper;
    String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theory);

        userName = getIntent().getStringExtra("userName");
        dataBaseHelper = new DataBaseHelper(this);
        toolbar = (Toolbar)findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setLogo(R.drawable.ic_action_name);
        getSupportActionBar().setTitle("Теорія");
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        /*Подозрительное место, возможно здесь задается неразвертываемость ресайклера*/
        recyclerView.setHasFixedSize(true);
        String[] items = getResources().getStringArray(R.array.topics);

        /*Цикл можно заменить на конвертацию*/
        for (String item : items){
            arrayList.add(item);
        }
        adapter = new RecyclerAdapter(arrayList, this);
        recyclerView.setAdapter(adapter);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);

        webView = (WebView) findViewById(R.id.webView);
        String startPage = main_theory_url;
        if (getIntent().hasExtra("taskId")){
            switch (getIntent().getIntExtra("taskId", 0)){
                case 0:
                    startPage = mil_theory_url;
                    break;
                case AdjustmentTask.RANGE_FINDER_TYPE:
                    startPage = range_adjust_url;
                    break;
                case AdjustmentTask.DUAL_OBSERVINGS_TYPE:
                    startPage = dual_adjust_url;
                    break;
                case AdjustmentTask.WORLD_SIDES_TYPE:
                    startPage = ws_adjust_url;
                    break;
            }
        }
        webView.loadUrl(startPage);

     }



    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onClick(View view) {
        if (view instanceof TextView){
            TextView currentPoint = (TextView) view;
            switch (currentPoint.getText().toString()){
                case "Загальні відомості":
                    webView.loadUrl(main_theory_url);
                    drawerLayout.closeDrawers();
                    break;
                case "Теорія тисячної":
                    webView.loadUrl(mil_theory_url);
                    drawerLayout.closeDrawers();
                    break;
                case "Пристрілка з далекоміром":
                    webView.loadUrl(range_adjust_url);
                    drawerLayout.closeDrawers();
                    break;
                case "Пристрілка з секундоміром":
                    webView.loadUrl(timer_adjust_url);
                    drawerLayout.closeDrawers();
                    break;
                case "Пристрілка за СЗР":
                    webView.loadUrl(szr_adjust_url);
                    drawerLayout.closeDrawers();
                    break;
                case "Пристрілка з спряженими спостереженнями":
                    webView.loadUrl(dual_adjust_url);
                    drawerLayout.closeDrawers();
                    break;
                case "Пристрілка за сторонами світу":
                    webView.loadUrl(ws_adjust_url);
                    drawerLayout.closeDrawers();
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.theory_activity_menu, menu);

        SpannableStringBuilder builder;
        MenuItem theoryStatisticMenuItem = menu.findItem(R.id.theoryStatisticMenuItem);
        builder = new SpannableStringBuilder("  " + theoryStatisticMenuItem.getTitle());
        // replace " " with icon
        builder.setSpan(new ImageSpan(this, R.drawable.ic_equalizer_black_24dp), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        theoryStatisticMenuItem.setTitle(builder);

        MenuItem theoryQuitMenuItem = menu.findItem(R.id.theoryQuitMenuItem);
        builder = new SpannableStringBuilder("  " + theoryQuitMenuItem.getTitle());
        // replace " " with icon
        builder.setSpan(new ImageSpan(this, R.drawable.ic_exit_24dp), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        theoryQuitMenuItem.setTitle(builder);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.theoryStatisticMenuItem:
                String message = dataBaseHelper.getUserStatsForName(userName);
                makeDialogWindowMessage(getResources().getString(R.string.adjStatisticMenuItemText) + " " + userName,
                        message);
                break;
            case R.id.theoryQuitMenuItem:
                AlertDialog.Builder tempBuilder = new AlertDialog.Builder(this);
                tempBuilder.setTitle("Вийти з додатку?").setPositiveButton("Так",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id) {
                                finishAffinity();
                            }
                        });
                tempBuilder.setNegativeButton("Ні", null);
                tempBuilder.show();

                break;
        }
        return super.onOptionsItemSelected(item);
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
}
