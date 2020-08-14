package com.example.hellu.Adapter;

import android.content.Context;

import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.hellu.Fragment.WaitForAcceptChatFragment;
import com.example.hellu.Fragment.ChatFragment;
import com.example.hellu.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
    private final Context mContext;

    public SectionsPagerAdapter(Context context,FragmentManager fm) {
        super(fm);
        mContext=context;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0: return ChatFragment.getInstance();
            case 1: return WaitForAcceptChatFragment.getInstance();
            default: return null;
        }
    }
    @Override
    public int getCount() {
        // Show 3 total pages.
        return 2;
    }
}