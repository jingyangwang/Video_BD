package org.videolan.vlc.gui;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

public class MyFragmentPagerAdapter extends FragmentPagerAdapter{

	private List<Fragment>fragList;
	private List<String>titleList;
	
	static final int NUM_ITEMS = 2;
    private final FragmentManager mFragmentManager;
    private Fragment mFragmentAtPos0;
	
	
	public MyFragmentPagerAdapter(FragmentManager fm,List<Fragment> fragList,List<String>titleList) {
		super(fm);
		// TODO Auto-generated constructor stub
		mFragmentManager = fm;
		this.fragList=fragList;
		this.titleList=titleList;
	}

	@Override
	public Fragment getItem(int position) {
		return fragList.get(position);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return fragList.size();
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		// TODO Auto-generated method stub
		return titleList.get(position);
	}
	

}
