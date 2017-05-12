package org.videolan.vlc.gui;

import org.videolan.vlc.R;
import org.videolan.vlc.gui.browser.FileBrowserFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;

public class MyActivity3 extends ActionBarActivity {

	FileBrowserFragment mFileBrowserFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my2);
		// 设置默认的Fragment
		setDefaultFragment();
	}

	private void setDefaultFragment() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mFileBrowserFragment = new FileBrowserFragment();

		ft.replace(R.id.fragment_placeholder, mFileBrowserFragment);
		ft.addToBackStack(null);
		ft.commit();
	}

}
