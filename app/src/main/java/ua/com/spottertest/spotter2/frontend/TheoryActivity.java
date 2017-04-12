package ua.com.spottertest.spotter2.frontend;

import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ua.com.spottertest.spotter2.R;
import ua.com.spottertest.spotter2.core.adjustment.AdjustmentTask;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_theory);
        toolbar = (Toolbar)findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
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

    private String getChapterFromAssets(String filename){
        byte[] buffer = null;
        InputStream is;
        try {
            is = getAssets().open(filename);
            int size = is.available();
            buffer = new byte[size];
            is.read(buffer);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String str_data = new String(buffer);
        return str_data;
    }
}
