package ua.com.spottertest.spotter2;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by Rudolf on 22.02.2017.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    /*здесь вполне можно переписать с помощью Map<String, Fragment>*/
    ArrayList<Fragment> fragments = new ArrayList<>();
    ArrayList<String> tabTitles = new ArrayList<>();

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    /*После обучения рефакторить название метода и аргументов, мы добавляем ОДИН фрагмент*/
    public void addFragments(Fragment fragments, String titles){
        this.fragments.add(fragments);
        this.tabTitles.add(titles);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles.get(position);
    }
}
