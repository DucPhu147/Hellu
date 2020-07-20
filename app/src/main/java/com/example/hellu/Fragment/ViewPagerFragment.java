package com.example.hellu.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.example.hellu.Adapter.SectionsPagerAdapter;
import com.example.hellu.R;
import com.google.android.material.tabs.TabLayout;

public class ViewPagerFragment extends Fragment {

    private static final ViewPagerFragment ourInstance = new ViewPagerFragment();
    private int[] tabIcons = {
            R.drawable.ic_baseline_person_24,
            R.drawable.ic_baseline_group_24
    };
    public static ViewPagerFragment getInstance() {
        return ourInstance;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_viewpager,container,false);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getContext(), getChildFragmentManager());
        ViewPager viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = view.findViewById(R.id.tab_layout);
        tabs.setupWithViewPager(viewPager);
        tabs.getTabAt(0).setIcon(tabIcons[0]);
        tabs.getTabAt(1).setIcon(tabIcons[1]);


        tabs.getTabAt(0).setText("Trò chuyện");
        tabs.getTabAt(1).setText("Nhóm");
        tabs.getTabAt(0).getIcon().setTint(getResources().getColor(R.color.colorPrimary));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tab.getIcon().setTint(getResources().getColor(R.color.colorPrimary));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                tab.getIcon().setTint(getResources().getColor(R.color.colorBlackTransparent));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        return view;
    }

}
