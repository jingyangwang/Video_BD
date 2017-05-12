/**
 * 
 */
package org.videolan.vlc.gui;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

/**
 * @author wjy
 *
 */
public class MyFixedSpeedScroller extends Scroller {
	 private int mDuration = 1500;        
	public MyFixedSpeedScroller(Context context, Interpolator interpolator) {
		super(context, interpolator);
		// TODO Auto-generated constructor stub
	}

	public MyFixedSpeedScroller(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	 @Override      
	    public void startScroll(int startX, int startY, int dx, int dy, int duration) {      
	        // Ignore received duration, use fixed one instead           
	        super.startScroll(startX, startY, dx, dy, mDuration);       
	        }        
	    @Override       
	    public void startScroll(int startX, int startY, int dx, int dy) {          
	            // Ignore received duration, use fixed one instead          
	            super.startScroll(startX, startY, dx, dy, mDuration);      
	            }   
	    public void setmDuration(int time){  
	        mDuration = time;  
	    }     
	    public int getmDuration(){  
	        return mDuration;  
	    }     
}
