package com.genius.demo;

import java.util.List;

import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;

public class ViewPageAdapter extends PagerAdapter{

	private final static String 	TAG = "MyPagerAdapter";
	
	List<View> mViewList;
	
	public ViewPageAdapter(List<View> viewList)
	{
		mViewList = viewList;
	}
	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if (mViewList != null)
		{
			return mViewList.size();
		}
		
		return 0;
	}
	
	@Override
	public Object instantiateItem(View view, int index) {
		// TODO Auto-generated method stub
		
		((ViewPager) view).addView(mViewList.get(index), 0);
	
		return mViewList.get(index);
	}
	
	@Override
	public void destroyItem(View view, int position, Object arg2) {
		// TODO Auto-generated method stub
		
		((ViewPager) view).removeView(mViewList.get(position));
	}

	@Override
	public void finishUpdate(View arg0) {
		// TODO Auto-generated method stub

	}





	@Override
	public boolean isViewFromObject(View view, Object obj) {
		// TODO Auto-generated method stub
		
		return (view == obj);
		
	}

	@Override
	public void restoreState(Parcelable arg0, ClassLoader arg1) {
		// TODO Auto-generated method stub
				
	}

	@Override
	public Parcelable saveState() {
		// TODO Auto-generated method stub
			
		return null;
	}

	@Override
	public void startUpdate(View arg0) {
		// TODO Auto-generated method stub
	//	Log.i(TAG, "startUpdate");
		
		
	}

}
