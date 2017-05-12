package org.videolan.vlc.gui;

import org.videolan.vlc.R;
import org.videolan.vlc.gui.browser.FileBrowserFragment;
import org.videolan.vlc.gui.video.VideoGridFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;

public class MyActivity1 extends ActionBarActivity {

	VideoGridFragment mFileBrowserFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my1);
		// 设置默认的Fragment
		setDefaultFragment();
	}

	private void setDefaultFragment() {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mFileBrowserFragment = new VideoGridFragment();

		ft.replace(R.id.fragment_placeholder1, mFileBrowserFragment);
		ft.addToBackStack(null);
		ft.commit();
	}

}
