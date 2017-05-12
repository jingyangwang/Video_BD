package org.videolan.vlc.gui;

import org.videolan.vlc.R;
import org.videolan.vlc.gui.browser.FileBrowserFragment;
import org.videolan.vlc.gui.browser.FilePickerFragment;
import org.videolan.vlc.util.AndroidDevices;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;

public class MyPageFragment1 extends Fragment implements OnClickListener{
	private Button btn;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//Log.d("DEBUG", "onCreateView");
		View root = inflater.inflate(R.layout.my2, container, false);
		FragmentTransaction trans = getFragmentManager().beginTransaction();
		trans.add(R.id.fragment_placeholder, new FileBrowserFragment());

		trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
		trans.addToBackStack(null);
		btn=(Button) root.findViewById(R.id.back_btn);
		btn.setOnClickListener(this);
		btn.setVisibility(View.GONE);
		//if( AndroidDevices.EXTERNAL_PUBLIC_DIRECTORY.equals(other)){}
//		if(getActivity().getSupportFragmentManager().getBackStackEntryCount() > 2){
//			btn.setVisibility(View.GONE);
//		}
//		else{
//			btn.setVisibility(View.VISIBLE);
//		}
		
//		btn.setOnClickListener(listener);

		trans.commit();

		// getSupportFragmentManager().popBackStack();
		return root;
	}
//	OnClickListener listener=new OnClickListener() {
//
//		@Override
//		public void onClick(View v) {
//			// TODO Auto-generated method stub
//			if (getFragmentManager().getBackStackEntryCount() > 0) {
//				getActivity().onBackPressed();
//			} 
//			else {
//				if(getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder) != null){
//					//((FilePickerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder)).browseUp();
//					getActivity().finish();
//				}
//			}
//		};};
	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.back_btn){
			if(getActivity() instanceof OnButtonClickListener){
				((OnButtonClickListener)getActivity()).onBackClick(v);
			}
		}
		
	}
//	@Override
//	public void onResume() {
//		// TODO Auto-generated method stub
//		if(getActivity().getSupportFragmentManager().getBackStackEntryCount() > 3){
//			btn.setVisibility(View.GONE);
//		}
//		else{
//			btn.setVisibility(View.VISIBLE);
//		}
//		super.onResume();
//	}
}
