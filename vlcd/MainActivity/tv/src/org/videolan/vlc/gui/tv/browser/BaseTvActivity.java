/*
 * *************************************************************************
 *  BaseTvActivity.java
 * **************************************************************************
 *  Copyright © 2015 VLC authors and VideoLAN
 *  Author: Geoffrey Métais
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *  ***************************************************************************
 */

package org.videolan.vlc.gui.tv.browser;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.KeyEvent;

import org.videolan.vlc.MediaLibrary;
import org.videolan.vlc.VLCApplication;
import org.videolan.vlc.gui.PlaybackServiceActivity;
import org.videolan.vlc.gui.tv.SearchActivity;
import org.videolan.vlc.util.WeakHandler;

public abstract class BaseTvActivity extends PlaybackServiceActivity {
    protected MediaLibrary mMediaLibrary;
    protected SharedPreferences mSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMediaLibrary = MediaLibrary.getInstance();
        mSettings = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //Handle network connection state
        IntentFilter networkfilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);

        IntentFilter storageFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        storageFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        storageFilter.addDataScheme("file");

        registerReceiver(mExternalDevicesReceiver, networkfilter);
        registerReceiver(mExternalDevicesReceiver, storageFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mExternalDevicesReceiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH){
            startActivity(new Intent(this, SearchActivity.class));
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected abstract void refresh();
    protected abstract void updateList();

    protected final BroadcastReceiver mExternalDevicesReceiver = new BroadcastReceiver() {
        boolean connected = true;
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mMediaLibrary.isWorking())
                return;
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
                final NetworkInfo networkInfo = ((ConnectivityManager) VLCApplication.getAppContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
                if (networkInfo == null || networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    if (networkInfo == null){
                        if (connected)
                            connected = false;
                        else
                            return; //block consecutive calls when disconnected
                    } else
                        connected = true;
                    updateList();
                }

            } else if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_MOUNTED)) {
                mStorageHandlerHandler.sendEmptyMessage(ACTION_MEDIA_MOUNTED);
            } else if (action.equalsIgnoreCase(Intent.ACTION_MEDIA_UNMOUNTED)) {
                mStorageHandlerHandler.sendEmptyMessageDelayed(ACTION_MEDIA_UNMOUNTED, 100); //Delay to cancel it in case of MOUNT
            }
        }
    };

    Handler mStorageHandlerHandler = new FileBrowserFragmentHandler(this);

    protected static final int ACTION_MEDIA_MOUNTED = 1337;
    protected static final int ACTION_MEDIA_UNMOUNTED = 1338;

    private static class FileBrowserFragmentHandler extends WeakHandler<BaseTvActivity> {

        public FileBrowserFragmentHandler(BaseTvActivity owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what){
                case ACTION_MEDIA_MOUNTED:
                    removeMessages(ACTION_MEDIA_UNMOUNTED);
                case ACTION_MEDIA_UNMOUNTED:
                    getOwner().refresh();
                    break;
            }
        }
    }
}
