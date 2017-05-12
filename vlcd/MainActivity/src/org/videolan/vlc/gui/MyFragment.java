/*****************************************************************************
 * MainActivity.java
 *****************************************************************************
 * Copyright © 2011-2014 VLC authors and VideoLAN
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/

package org.videolan.vlc.gui;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.Matrix;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LocalActivityManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.videolan.libvlc.util.AndroidUtil;
import org.videolan.vlc.Config;
import org.videolan.vlc.MediaDatabase;
import org.videolan.vlc.MediaLibrary;
import org.videolan.vlc.PlaybackService;
import org.videolan.vlc.R;
import org.videolan.vlc.VLCApplication;
import org.videolan.vlc.gui.SidebarAdapter.SidebarEntry;
import org.videolan.vlc.gui.audio.AudioBrowserFragment;
import org.videolan.vlc.gui.browser.BaseBrowserFragment;
import org.videolan.vlc.gui.browser.FileBrowserFragment;
import org.videolan.vlc.gui.browser.FilePickerFragment;
import org.videolan.vlc.gui.browser.MediaBrowserFragment;
import org.videolan.vlc.gui.browser.NetworkBrowserFragment;
import org.videolan.vlc.gui.network.MRLPanelFragment;
import org.videolan.vlc.gui.video.VideoGridFragment;
import org.videolan.vlc.gui.video.VideoListAdapter;
import org.videolan.vlc.gui.video.VideoPlayerActivity;
import org.videolan.vlc.interfaces.IRefreshable;
import org.videolan.vlc.interfaces.ISortable;
import org.videolan.vlc.util.Util;
import org.videolan.vlc.util.VLCInstance;
import org.videolan.vlc.util.WeakHandler;
import org.videolan.vlc.widget.HackyDrawerLayout;


public class MyFragment extends AudioPlayerContainerActivity implements OnItemClickListener, SearchSuggestionsAdapter.SuggestionDisplay, FilterQueryProvider,OnButtonClickListener {
	public final static String TAG = "VLC/MainActivity";
	private static final String PREF_FIRST_RUN = "first_run";
	private static final int ACTIVITY_RESULT_PREFERENCES = 1;
	private static final int ACTIVITY_SHOW_INFOLAYOUT = 2;
	private static final int ACTIVITY_SHOW_PROGRESSBAR = 3;
	private static final int ACTIVITY_HIDE_PROGRESSBAR = 4;
	private static final int ACTIVITY_SHOW_TEXTINFO = 5;

	MediaLibrary mMediaLibrary;

	private SidebarAdapter mSidebarAdapter;
	private HackyDrawerLayout mDrawerLayout;
	//    private ListView mListView;
	private ActionBarDrawerToggle mDrawerToggle;

	private View mInfoLayout;
	private ProgressBar mInfoProgress;
	private TextView mInfoText;
	private String mCurrentFragment;

private Button btn;
	private int mVersionNumber = -1;
	private boolean mFirstRun = false;
	private boolean mScanNeeded = false;

	private Handler mHandler = new MainActivityHandler(this);
	private int mFocusedPrior = 0;
	private int mActionBarIconId = -1;
	Menu mMenu;
	private SearchView mSearchView;

	private Context mContext=MyFragment.this;
	private ViewPager pager;
	private static final int PAGER_NUM = 2;//2个页面  
	private int mCurrentViewID = 0;         //当前页面  
	private int mMyDuration = 100; 
	private List<String>titleList;
	//private PagerTabStrip tab;
	private List<Fragment>fragList;
	Fragment fragment1;
	Fragment fragment2;
	protected static final String ACTION_SHOW_PROGRESSBAR = "org.videolan.vlc.gui.ShowProgressBar";
	protected static final String ACTION_HIDE_PROGRESSBAR = "org.videolan.vlc.gui.HideProgressBar";
	protected static final String ACTION_SHOW_TEXTINFO = "org.videolan.vlc.gui.ShowTextInfo";
	public static final String ACTION_SHOW_PLAYER = "org.videolan.vlc.gui.ShowPlayer";
	private ImageView cursor;
	int offset,one,two;
	private TextView libray,file1;
private ImageView image1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		if (!VLCInstance.testCompatibleCPU(this)) {
			finish();
			return;
		}
		/* Enable the indeterminate progress feature */
		supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		/* Get the current version from package */
		mVersionNumber = Config.VERSION_CODE;

		/* Check if it's the first run */
		mFirstRun = mSettings.getInt(PREF_FIRST_RUN, -1) != mVersionNumber;
		if (mFirstRun) {
			Editor editor = mSettings.edit();
			editor.putInt(PREF_FIRST_RUN, mVersionNumber);
			Util.commitPreferences(editor);
		}

		mMediaLibrary = MediaLibrary.getInstance();
		if (mMediaLibrary.getMediaItems().isEmpty()) {
			if (mSettings.getBoolean(PreferencesActivity.AUTO_RESCAN, true))
				mMediaLibrary.scanMediaItems();
			else
				mMediaLibrary.loadMedaItems();
		}

		/*** Start initializing the UI ***/

		setContentView(R.layout.my_fragment);
//		image1=(ImageView) findViewById(R.id.image);
//		AlphaAnimation animation = new AlphaAnimation(1.0,0.);   
//		animation.setDuration(5000); //设置持续时间5秒  
//		image1.startAnimation(animation);
		

		mDrawerLayout = (HackyDrawerLayout) findViewById(R.id.root_container);
		//        mListView = (ListView)findViewById(R.id.sidelist);
		//        mListView.setFooterDividersEnabled(true);
		//        mSidebarAdapter = new SidebarAdapter(this);
		//        mListView.setAdapter(mSidebarAdapter);

		initAudioPlayerContainerActivity();
		//mSlidingPane.setActivated(false);


		if (savedInstanceState != null){
			mCurrentFragment = savedInstanceState.getString("current");
		//	if (mCurrentFragment != null)
				//mSidebarAdapter.setCurrentFragment(mCurrentFragment);
		}

		/* Initialize UI variables */
		mInfoLayout = findViewById(R.id.info_layout);
		mInfoProgress = (ProgressBar) findViewById(R.id.info_progress);
		mInfoText = (TextView) findViewById(R.id.info_text);

		/* Set up the action bar */
		prepareActionBar();

		/* Set up the sidebar click listener
		 * no need to invalidate menu for now */
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close){
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				if (getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder) instanceof MediaBrowserFragment)
					((MediaBrowserFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder)).setReadyToDisplay(true);
			}
		};

		// Set the drawer toggle as the DrawerListener
	//	mDrawerLayout.setDrawerListener(mDrawerToggle);
		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

		/* Reload the latest preferences */
		reloadPreferences();


		/*--------------------------------------------- */
		titleList=new ArrayList<String>();
		titleList.add("Libray");
		titleList.add("File");

		initIndView();
		fragment1=new VideoGridFragment();

		//		Fragment fragment2 = getSupportFragmentManager()
		//                .findFragmentById(R.id.fragment_placeholder);

		//		fragment2=new FileBrowserFragment();
		fragment2=new MyPageFragment1();
		btn=(Button) findViewById(R.id.back_btn);
		pager=(ViewPager) findViewById(R.id.pager);
		pager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub
				Matrix matrix = new Matrix();
				switch (arg0) {
				case 0:
					matrix.postTranslate(offset + (one - offset) * arg1, 0);
					break;
				case 1:
					matrix.postTranslate(one + (two - one) * arg1, 0);
					break;
				default:
					break;
				}
				cursor.setImageMatrix(matrix);
			}
			@Override
			public void onPageSelected(int currentID) {
				if(currentID == 0){
					libray.setTextColor(Color.BLUE);
					file1.setTextColor(Color.BLACK);
				}else{
					libray.setTextColor(Color.BLACK);
					file1.setTextColor(Color.BLUE);
				}
				mCurrentViewID = currentID;  
			}
		});
		fragList=new ArrayList<Fragment>();

		fragList.add(fragment1);
		fragList.add(fragment2);
		//		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		////	ft.replace(R.id.fragment_placeholder, new FileBrowserFragment(), mCurrentFragment);
		//		ft.replace(R.id.fragment_placeholder, new FileBrowserFragment());
		//		ft.commit();
		libray=(TextView) findViewById(R.id.libray);
		file1=(TextView) findViewById(R.id.file1);
		file1.setOnClickListener(listener);
		libray.setOnClickListener(listener);
		MyFragmentPagerAdapter adapter2=new MyFragmentPagerAdapter(getSupportFragmentManager(), fragList, titleList);

		pager.setAdapter(adapter2);
		/*--------------------------------------------- */
		
		}

		OnClickListener listener=new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				switch (arg0.getId()) {

				case R.id.libray:
					if(mCurrentViewID != 0){  
						mCurrentViewID--;  
						pager.setCurrentItem(mCurrentViewID, true);  
					}  
					break;
				case R.id.file1:
					if(mCurrentViewID != PAGER_NUM-1){  
						mCurrentViewID++;  
						pager.setCurrentItem(mCurrentViewID, true);  
					}  

					break;
				}
			}
		};


		@Override
		protected void onPostCreate(Bundle savedInstanceState) {
			super.onPostCreate(savedInstanceState);
			// Sync the toggle state after onRestoreInstanceState has occurred.
			mDrawerToggle.syncState();
		}

		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		private void prepareActionBar() {
			//        mActionBar.setDisplayHomeAsUpEnabled(true);
			//        mActionBar.setHomeButtonEnabled(true);
			// 给左上角图标的左边加上一个返回的图标
			mActionBar.setDisplayHomeAsUpEnabled(false);
			// 左上角的图标是否可以点击
			mActionBar.setHomeButtonEnabled(false);
			// 使左上角图标是否显示
			mActionBar.setDisplayShowHomeEnabled(false); 


		}

		@Override
		protected void onResume() {
			super.onResume();


//			/* Load media items from database and storage */
			if (mScanNeeded)
				mMediaLibrary.scanMediaItems();
//			if (mSlidingPane.getState() == mSlidingPane.STATE_CLOSED)
//				mActionBar.hide();
		}

		@Override
		protected void onResumeFragments() {
			super.onResumeFragments();
			//btn.setVisibility(View.INVISIBLE);

			// Figure out if currently-loaded fragment is a top-level fragment.
			//        Fragment current = getSupportFragmentManager()
			//                .findFragmentById(R.id.fragment_placeholder);
			//        boolean found = (current == null) || SidebarAdapter.sidebarFragments.contains(current.getTag());

			/**
			 * Restore the last view.
			 *
			 * Replace:
			 * - null fragments (freshly opened Activity)
			 * - Wrong fragment open AND currently displayed fragment is a top-level fragment
			 *
			 * Do not replace:
			 * - Non-sidebar fragments.
			 * It will try to remove() the currently displayed fragment
			 * (i.e. tracks) and replace it with a blank screen. (stuck menu bug)
			 */
		}

		/**
		 * Stop audio player and save opened tab
		 */
		@Override
		protected void onPause() {
			super.onPause();
			/* Check for an ongoing scan that needs to be resumed during onResume */
			mScanNeeded = mMediaLibrary.isWorking();
			/* Stop scanning for files */
			mMediaLibrary.stop();
			/* Save the tab status in pref */
			SharedPreferences.Editor editor = mSettings.edit();
			editor.putString("fragment", mCurrentFragment);
			Util.commitPreferences(editor);

			mFocusedPrior = 0;
		}

		protected void onSaveInstanceState(Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putString("current", mCurrentFragment);
		}

		@Override
		protected void onRestart() {
			super.onRestart();
			/* Reload the latest preferences */
			reloadPreferences();
		}

		@Override
		public void onBackPressed() {
			/* Close the menu first */
			//        if(mDrawerLayout.isDrawerOpen(mListView)) {
			//            if (mFocusedPrior != 0)
			//                requestFocusOnSearch();
			//            mDrawerLayout.closeDrawer(mListView);
			//            return;
			//        }

			// Slide down the audio player if it is shown entirely.
			if (slideDownAudioPlayer())
				return;

			if (mCurrentFragment!= null) {
				// If it's the directory view, a "backpressed" action shows a parent.
				if (mCurrentFragment.equals(SidebarEntry.ID_NETWORK) || mCurrentFragment.equals(SidebarEntry.ID_DIRECTORIES)){
					BaseBrowserFragment browserFragment = (BaseBrowserFragment) getSupportFragmentManager()
							.findFragmentById(R.id.fragment_placeholder);
					if (browserFragment != null) {
						browserFragment.goBack();
						return;
					}
				}
			}
			finish();
		}

		private Fragment getFragment(String id)
		{
			Fragment frag = getSupportFragmentManager().findFragmentByTag(id);
			if (frag != null)
				return frag;
			return mSidebarAdapter.fetchFragment(id);
		}

		private static void ShowFragment(FragmentActivity activity, String tag, Fragment fragment, String previous) {
			if (fragment == null) {
				Log.e(TAG, "Cannot show a null fragment, ShowFragment("+tag+") aborted.");
				return;
			}

			FragmentManager fm = activity.getSupportFragmentManager();

			//abort if fragment is already the current one
			Fragment current = fm.findFragmentById(R.id.fragment_placeholder);
			if(current != null && current.getTag().equals(tag))
				return;

			//try to pop back if the fragment is already on the backstack
			if (fm.popBackStackImmediate(tag, 0))
				return;

			//fragment is not there yet, spawn a new one
			FragmentTransaction ft = fm.beginTransaction();
			ft.setCustomAnimations(R.anim.anim_enter_right, R.anim.anim_leave_left, R.anim.anim_enter_left, R.anim.anim_leave_right);
			ft.replace(R.id.fragment_placeholder, fragment, tag);
			ft.addToBackStack(previous);
			ft.commit();
		}

		/**
		 * Show a secondary fragment.
		 */
		public void showSecondaryFragment(String fragmentTag) {
			showSecondaryFragment(fragmentTag, null);
		}

		public void showSecondaryFragment(String fragmentTag, String param) {
			Intent i = new Intent(this, SecondaryActivity.class);
			i.putExtra("fragment", fragmentTag);
			if (param != null)
				i.putExtra("param", param);
			startActivity(i);
			// Slide down the audio player if needed.
			slideDownAudioPlayer();
		}





		/** Create menu from XML
		 */
		@TargetApi(Build.VERSION_CODES.FROYO)
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			mMenu = menu;
			/* Note: on Android 3.0+ with an action bar this method
			 * is called while the view is created. This can happen
			 * any time after onCreate.
			 */
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.media_library, menu);

			if (AndroidUtil.isFroyoOrLater()) {
				SearchManager searchManager =
						(SearchManager) VLCApplication.getAppContext().getSystemService(Context.SEARCH_SERVICE);
				mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.ml_menu_search));
				mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
				mSearchView.setQueryHint(getString(R.string.search_hint));
				SearchSuggestionsAdapter searchSuggestionsAdapter = new SearchSuggestionsAdapter(this, null);
				searchSuggestionsAdapter.setFilterQueryProvider(this);
				 //  mSearchView.setIconifiedByDefault(false);//(设置搜索图标在编辑框外，ture时在框内)。
				//mSearchView.setSearchViewBackground(mSearchView);
			//setSearchViewBackground(mSearchView);
				mSearchView.setSuggestionsAdapter(searchSuggestionsAdapter);
			} else
				menu.findItem(R.id.ml_menu_search).setVisible(false);
			return super.onCreateOptionsMenu(menu);
		}

		
		
//		 public void setSearchViewBackground(SearchView searchView) {
//			   try {
//			    Class<?> argClass = searchView.getClass();
//			    // 指定某个私有属性
//			    Field ownField = argClass.getDeclaredField("mSearchPlate"); // 注意mSearchPlate的背景是stateListDrawable(不同状态不同的图片)
//			                   // 所以不能用BitmapDrawable
//			    // setAccessible 它是用来设置是否有权限访问反射类中的私有属性的，只有设置为true时才可以访问，默认为false
//			    ownField.setAccessible(true);
//			    View mView = (View) ownField.get(searchView);
//			    mView.setBackgroundDrawable(getResources().getDrawable(
//			      R.drawable.jpg));
//
//			    // 指定某个私有属性
//			    Field mQueryTextView = argClass.getDeclaredField("mQueryTextView");
//			    mQueryTextView.setAccessible(true);
//			    Class<?> mTextViewClass = mQueryTextView.get(searchView).getClass()
//			      .getSuperclass().getSuperclass().getSuperclass();
//
//			    // mCursorDrawableRes光标图片Id的属性
//			    // 这个属性是TextView的属性，所以要用mQueryTextView（SearchAutoComplete）的父类（AutoCompleteTextView）的父
//			    // 类( EditText）的父类(TextView)
//			    Field mCursorDrawableRes = mTextViewClass
//			      .getDeclaredField("mCursorDrawableRes");
//
//			    // setAccessible 它是用来设置是否有权限访问反射类中的私有属性的，只有设置为true时才可以访问，默认为false
//			    mCursorDrawableRes.setAccessible(true);
//			    mCursorDrawableRes.set(mQueryTextView.get(searchView),
//			      R.drawable.ic_audiodelay_normal);// 注意第一个参数持有这个属性(mQueryTextView)的对象(mSearchView)
//			              // 光标必须是一张图片不能是颜色，因为光标有两张图片，一张是第一次获得焦点的时候的闪烁的图片，一张是后边有内容时候的图片，如果用颜色填充的话，就会失去闪烁的那张图片，颜色填充的会缩短文字和光标的距离（某些字母会背光标覆盖一部分）。
//			   } catch (Exception e) {
//			    // TODO Auto-generated catch block
//			    e.printStackTrace();
//			   }
//			  }
		
		
		
		@Override
		public boolean onPrepareOptionsMenu (Menu menu) {
			super.onPrepareOptionsMenu(menu);
			if (menu == null)
				return false;
			Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);
			MenuItem item;
			// Disable the sort option if we can't use it on the current fragment.
			if (current == null || !(current instanceof ISortable)) {
				item = menu.findItem(R.id.ml_menu_sortby);
				if (item == null)
					return false;
				item.setEnabled(false);
				item.setVisible(false);
			} else {
				ISortable sortable = (ISortable) current;
				item = menu.findItem(R.id.ml_menu_sortby);
				if (item == null)
					return false;
				item.setEnabled(true);
				item.setVisible(true);
				item = menu.findItem(R.id.ml_menu_sortby_name);
				if (sortable.sortDirection(VideoListAdapter.SORT_BY_TITLE) == 1)
					item.setTitle(R.string.sortby_name_desc);
				//item.setTitle(R.string.sortby_name_desc);
				else
					item.setTitle(R.string.sortby_name);
				item = menu.findItem(R.id.ml_menu_sortby_length);
				if (sortable.sortDirection(VideoListAdapter.SORT_BY_LENGTH) == 1)
					item.setTitle(R.string.sortby_length_desc);
				else
					item.setTitle(R.string.sortby_length);
				item = menu.findItem(R.id.ml_menu_sortby_date);
				if (sortable.sortDirection(VideoListAdapter.SORT_BY_DATE) == 1)
					item.setTitle(R.string.sortby_date_desc);
				else
					item.setTitle(R.string.sortby_date);
			}

			boolean networkSave = current instanceof NetworkBrowserFragment && !((NetworkBrowserFragment)current).isRootDirectory();
			if (networkSave) {
				item = menu.findItem(R.id.ml_menu_save);
				item.setVisible(true);
				String mrl = ((BaseBrowserFragment)current).mMrl;
				item.setIcon(MediaDatabase.getInstance().networkFavExists(Uri.parse(mrl)) ?
						R.drawable.ic_menu_bookmark_w :
							R.drawable.ic_menu_bookmark_outline_w);
			} else
				menu.findItem(R.id.ml_menu_save).setVisible(false);
			if (current instanceof MRLPanelFragment)
				menu.findItem(R.id.ml_menu_clean).setVisible(!((MRLPanelFragment) current).isEmpty());
			boolean showLast = current instanceof AudioBrowserFragment || (current instanceof VideoGridFragment && mSettings.getString(PreferencesActivity.VIDEO_LAST, null) != null);
			menu.findItem(R.id.ml_menu_last_playlist).setVisible(showLast);
			return true;
		}

		/**
		 * Handle onClick form menu buttons
		 */
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {

			// Current fragment loaded
			Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);

			// Handle item selection
			switch (item.getItemId()) {
			case R.id.ml_menu_sortby_name:
			case R.id.ml_menu_sortby_length:
			case R.id.ml_menu_sortby_date:
				if (current == null)
					break;
				if (current instanceof ISortable) {
					int sortBy = VideoListAdapter.SORT_BY_TITLE;
					if (item.getItemId() == R.id.ml_menu_sortby_length)
						sortBy = VideoListAdapter.SORT_BY_LENGTH;
					else if(item.getItemId() == R.id.ml_menu_sortby_date)
						sortBy = VideoListAdapter.SORT_BY_DATE;
					((ISortable) current).sortBy(sortBy);
					supportInvalidateOptionsMenu();
				}
				break;
			case R.id.ml_menu_equalizer:
				showSecondaryFragment(SecondaryActivity.EQUALIZER);
				break;
			case R.id.ml_menu_preferences:
				//            	showSecondaryFragment(PreferencesActivity.EQUALIZER);
				startActivity(new Intent(this, PreferencesActivity.class));

				break;
				// Refresh
			case R.id.ml_menu_refresh:
				if (!mMediaLibrary.isWorking()) {
					if(current != null && current instanceof IRefreshable)
						((IRefreshable) current).refresh();
					else
						mMediaLibrary.scanMediaItems(true);
				}
				break;
				// Restore last playlist
			case R.id.ml_menu_last_playlist:
				if (current instanceof AudioBrowserFragment) {
					Intent i = new Intent(PlaybackService.ACTION_REMOTE_LAST_PLAYLIST);
					sendBroadcast(i);
				} else if (current instanceof VideoGridFragment) {
					final Uri uri = Uri.parse(mSettings.getString(PreferencesActivity.VIDEO_LAST, null));
					if (uri != null)
						VideoPlayerActivity.start(this, uri);
				}
				break;
			case android.R.id.home:
				// Slide down the audio player.
				if (slideDownAudioPlayer())
					break;
				//                /* Toggle the sidebar */
				//                if (mDrawerToggle.onOptionsItemSelected(item)) {
				//                    return true;
				//                }
				break;
			case R.id.ml_menu_clean:
				if (getFragment(mCurrentFragment) instanceof MRLPanelFragment)
					((MRLPanelFragment)getFragment(mCurrentFragment)).clearHistory();
				break;
			case R.id.ml_menu_save:
				if (current == null)
					break;
				((NetworkBrowserFragment)current).toggleFavorite();
				item.setIcon(R.drawable.ic_menu_bookmark_w);
				break;
			}
			//        mDrawerLayout.closeDrawer(mListView);
			return super.onOptionsItemSelected(item);
		}

		@Override
		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
			super.onActivityResult(requestCode, resultCode, data);
			if (requestCode == ACTIVITY_RESULT_PREFERENCES) {
				if (resultCode == PreferencesActivity.RESULT_RESCAN)
					mMediaLibrary.scanMediaItems(true);
				else if (resultCode == PreferencesActivity.RESULT_RESTART) {
					Intent intent = getIntent();
					finish();
					startActivity(intent);
				}
			}
		}

		public void setMenuFocusDown(boolean idIsEmpty, int id) {
			if (mMenu == null)
				return;
			//Save menu items ids for focus control
			final int[] menu_controls = new int[mMenu.size()+1];
			for (int i = 0 ; i < mMenu.size() ; i++){
				menu_controls[i] = mMenu.getItem(i).getItemId();
			}
			menu_controls[mMenu.size()] = mActionBarIconId;
			/*menu_controls = new int[]{R.id.ml_menu_search,
            R.id.ml_menu_open_mrl, R.id.ml_menu_sortby,
            R.id.ml_menu_last_playlist, R.id.ml_menu_refresh,
            mActionBarIconId};*/
			int pane = mSlidingPane.getState();
			for(int r : menu_controls) {
				View v = findViewById(r);
				if (v != null) {
					if (!idIsEmpty)
						v.setNextFocusDownId(id);
					else {
						if (pane ==  mSlidingPane.STATE_CLOSED) {
							v.setNextFocusDownId(R.id.play_pause);
						} else if (pane == mSlidingPane.STATE_OPENED) {
							v.setNextFocusDownId(R.id.header_play_pause);
						} else if (pane ==
								mSlidingPane.STATE_OPENED_ENTIRELY) {
							v.setNextFocusDownId(r);
						}
					}
				}
			}
		}

		public void setSearchAsFocusDown(boolean idIsEmpty, View parentView, int id) {
			View playPause = findViewById(R.id.header_play_pause);

			if (!idIsEmpty) {
				View list;
				int pane = mSlidingPane.getState();

				if (parentView == null)
					list = findViewById(id);
				else
					list = parentView.findViewById(id);

				if (list != null) {
					if (pane == mSlidingPane.STATE_OPENED_ENTIRELY) {
						list.setNextFocusDownId(id);
					} else if (pane == mSlidingPane.STATE_OPENED) {
						list.setNextFocusDownId(R.id.header_play_pause);
						playPause.setNextFocusUpId(id);
					}
				}
			} else {
				playPause.setNextFocusUpId(R.id.ml_menu_search);
			}
		}

		// Note. onKeyDown will not occur while moving within a list
		
		
		private boolean key_back() {
			// TODO Auto-generated method stub
			if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
				btn=(Button) findViewById(R.id.back_btn);
				btn.setVisibility(View.VISIBLE);
				super.onBackPressed();
				//return false;
			} else {
				if(getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder) != null){
					//((FilePickerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder)).browseUp();
					btn.setVisibility(View.GONE);
					finish();
				//return false;
				}
			}
			return false;
		}
		@Override
		public boolean onKeyDown(int keyCode, KeyEvent event) {
			//Filter for LG devices, see https://code.google.com/p/android/issues/detail?id=78154
			btn=(Button) findViewById(R.id.back_btn);
				if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
					//btn=(Button) findViewById(R.id.back_btn);
					btn.setVisibility(View.VISIBLE);
					super.onBackPressed();
					return false;
				} else {
					if(getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder) != null){
						//((FilePickerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder)).browseUp();
						btn.setVisibility(View.GONE);
						finish();
					return false;
					}
				}
			//key_back() ;
			
				
			if ((keyCode == KeyEvent.KEYCODE_MENU) &&
					(Build.VERSION.SDK_INT <= 16) &&
					(Build.MANUFACTURER.compareTo("LGE") == 0)) {
				return true;
			}
			if (mFocusedPrior == 0)
				setMenuFocusDown(true, 0);
			if (getCurrentFocus() != null)
				mFocusedPrior = getCurrentFocus().getId();
			return super.onKeyDown(keyCode, event);
		}

		// Note. onKeyDown will not occur while moving within a list
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public boolean onKeyUp(int keyCode, KeyEvent event) {
			//Filter for LG devices, see https://code.google.com/p/android/issues/detail?id=78154
			if ((keyCode == KeyEvent.KEYCODE_MENU) &&
					(Build.VERSION.SDK_INT <= 16) &&
					(Build.MANUFACTURER.compareTo("LGE") == 0)) {
				openOptionsMenu();
				return true;
			}
			View v = getCurrentFocus();
			if (v == null)
				return super.onKeyUp(keyCode, event);
			if ((mActionBarIconId == -1) &&
					(v.getId() == -1)  &&
					(v.getNextFocusDownId() == -1) &&
					(v.getNextFocusUpId() == -1) &&
					(v.getNextFocusLeftId() == -1) &&
					(v.getNextFocusRightId() == -1)) {
				mActionBarIconId = Util.generateViewId();
				v.setId(mActionBarIconId);
				v.setNextFocusUpId(mActionBarIconId);
				v.setNextFocusDownId(mActionBarIconId);
				v.setNextFocusLeftId(mActionBarIconId);
				v.setNextFocusRightId(R.id.ml_menu_search);
				if (AndroidUtil.isHoneycombOrLater())
					v.setNextFocusForwardId(mActionBarIconId);
				if (findViewById(R.id.ml_menu_search) != null)
					findViewById(R.id.ml_menu_search).setNextFocusLeftId(mActionBarIconId);
			}
			return super.onKeyUp(keyCode, event);
		}

		private void reloadPreferences() {
			mCurrentFragment = mSettings.getString("fragment", "video");
		}

		@Override
		public Cursor runQuery(CharSequence constraint) {
			return MediaDatabase.getInstance().queryMedia(constraint.toString());
		}

		private static class MainActivityHandler extends WeakHandler<MyFragment> {
			public MainActivityHandler(MyFragment owner) {
				super(owner);
			}

			@Override
			public void handleMessage(Message msg) {
				MyFragment ma = getOwner();
				if(ma == null) return;

				switch (msg.what) {
				case ACTIVITY_SHOW_INFOLAYOUT:
					ma.mInfoLayout.setVisibility(View.VISIBLE);
					break;
				case ACTIVITY_SHOW_PROGRESSBAR:
					ma.setSupportProgressBarIndeterminateVisibility(true);
					ma.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					break;
				case ACTIVITY_HIDE_PROGRESSBAR:
					ma.setSupportProgressBarIndeterminateVisibility(false);
					ma.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					break;
				case ACTIVITY_SHOW_TEXTINFO:
					String info = (String) msg.obj;
					int max = msg.arg1;
					int progress = msg.arg2;
					ma.mInfoText.setText(info);
					ma.mInfoProgress.setMax(max);
					ma.mInfoProgress.setProgress(progress);

					if (info == null) {
						/* Cancel any upcoming visibility change */
						removeMessages(ACTIVITY_SHOW_INFOLAYOUT);
						ma.mInfoLayout.setVisibility(View.GONE);
					}
					else {
						/* Slightly delay the appearance of the progress bar to avoid unnecessary flickering */
						if (!hasMessages(ACTIVITY_SHOW_INFOLAYOUT)) {
							Message m = new Message();
							m.what = ACTIVITY_SHOW_INFOLAYOUT;
							sendMessageDelayed(m, 300);
						}
					}
					break;
				}
			}
		}

		public void hideKeyboard(){
			((InputMethodManager) VLCApplication.getAppContext().getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
					getWindow().getDecorView().getRootView().getWindowToken(), 0);
		}

		public void showProgressBar() {
			mHandler.obtainMessage(ACTIVITY_SHOW_PROGRESSBAR).sendToTarget();
		}

		public void hideProgressBar() {
			mHandler.obtainMessage(ACTIVITY_HIDE_PROGRESSBAR).sendToTarget();
		}

		public void sendTextInfo(String info, int progress, int max) {
			mHandler.obtainMessage(ACTIVITY_SHOW_TEXTINFO, max, progress, info).sendToTarget();
		}

		public void clearTextInfo() {
			mHandler.obtainMessage(ACTIVITY_SHOW_TEXTINFO, 0, 100, null).sendToTarget();
		}

		protected void onPanelClosedUiSet() {
			mDrawerLayout.setDrawerLockMode(HackyDrawerLayout.LOCK_MODE_LOCKED_CLOSED);
		}

		protected void onPanelOpenedEntirelyUiSet() {
			mDrawerLayout.setDrawerLockMode(HackyDrawerLayout.LOCK_MODE_UNLOCKED);
		}

		protected void onPanelOpenedUiSet() {
			mDrawerLayout.setDrawerLockMode(HackyDrawerLayout.LOCK_MODE_UNLOCKED);
			removeTipViewIfDisplayed();
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			//        SidebarAdapter.SidebarEntry entry = (SidebarEntry) mListView.getItemAtPosition(position);
			//        Fragment current = getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder);
			//
			//        if(current == null || (entry != null && current.getTag().equals(entry.id))) { /* Already selected */
			//            if (mFocusedPrior != 0)
			//                requestFocusOnSearch();
			//            mDrawerLayout.closeDrawer(mListView);
			//            return;
			//        }
			//
			//        // This should not happen
			//        if(entry == null || entry.id == null)
			//            return;
			//
			//        if (entry.type == SidebarEntry.TYPE_FRAGMENT) {
			//
			//                /* Slide down the audio player */
			//            slideDownAudioPlayer();
			//
			//                /* Switch the fragment */
			//            Fragment fragment = getFragment(entry.id);
			//            if (fragment instanceof MediaBrowserFragment)
			//                ((MediaBrowserFragment)fragment).setReadyToDisplay(false);
			//            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			//            ft.replace(R.id.fragment_placeholder, fragment, entry.id);
			//            ft.addToBackStack(mCurrentFragment);
			//            ft.commit();
			//            mCurrentFragment = entry.id;
			//            mSidebarAdapter.setCurrentFragment(mCurrentFragment);
			//
			//            if (mFocusedPrior != 0)
			//                requestFocusOnSearch();
			//        } else if (entry.type == SidebarEntry.TYPE_SECONDARY_FRAGMENT)
			//            showSecondaryFragment(SecondaryActivity.ABOUT);
			//        else if (entry.attributeID == R.attr.ic_menu_preferences)
			//            startActivityForResult(new Intent(this, PreferencesActivity.class), ACTIVITY_RESULT_PREFERENCES);
			//        mDrawerLayout.closeDrawer(mListView);
		}

		private void requestFocusOnSearch() {
			View search = findViewById(R.id.ml_menu_search);
			if (search != null)
				search.requestFocus();
		}


		/**
		 * 初始化动画
		 */
		private void initIndView() {
			cursor = (ImageView) findViewById(R.id.cursor);
			int bmpW = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.indicator_blue_small).getWidth();// 获取图片宽度
			DisplayMetrics dm = new DisplayMetrics();
			((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(dm);
			int screenW = dm.widthPixels;// 获取分辨率宽度
			offset = (screenW / 2 - bmpW) / 2;// 计算偏移量
			one = offset + screenW / 2;
			two = offset + (screenW / 2) * 2;
			Matrix matrix = new Matrix();
			matrix.postTranslate(offset, 0);
			cursor.setImageMatrix(matrix);// 设置动画初始位置
		}

		@Override
		public void onBackClick(View view) {
			//btn=(Button) findViewById(R.id.back_btn);
			if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
				btn.setVisibility(View.VISIBLE);
				super.onBackPressed();
				

			} else {
				btn.setVisibility(View.GONE);
//				if(getSupportFragmentManager().findFragmentById(R.id.fragment_placeholder) != null){
////					finish();
//					btn=(Button) findViewById(R.id.back_btn);
//					btn.setVisibility(View.GONE);
//				}
			}
			
		}

	}
