package com.testViewPager;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.viewpagerindicator.TitlePageIndicator.IndicatorStyle;
import com.viewpagerindicator.TitlePageIndicatorEx1;
import com.viewpagerindicator.TitleProvider;


public class TestViewPagerActivity extends FragmentActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        TestPagerAdapter ad = new TestPagerAdapter();  
        ViewPager pager = (ViewPager) findViewById(R.id.pager);  
        pager.setAdapter(ad);  
        
        TitlePageIndicatorEx1 indicator = (TitlePageIndicatorEx1)findViewById(R.id.indicator);
		indicator.setViewPager(pager);
		indicator.setFooterIndicatorStyle(TitlePageIndicatorEx1.IndicatorStyle.Underline);

        mListViews = new ArrayList<View>();  
        LayoutInflater flater = getLayoutInflater();  
        mListViews.add(flater.inflate(R.layout.p1, null));  
        mListViews.add(flater.inflate(R.layout.p2, null)); 
    }
    ArrayList<View> mListViews;
    
    class TestPagerAdapter extends PagerAdapter implements TitleProvider{

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			Log.d("xx", ""+(arg2 == mListViews.get(arg1)));
			((ViewPager)arg0).removeView(mListViews.get(arg1));
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public int getCount() {
//			return 0;
			return mListViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager)arg0).addView(mListViews.get(arg1));
			return mListViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
//			return false;
			return arg0 == arg1;
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}

		@Override
		public String getTitle(int position) {
			return "title"+position;
		}
    	
    }
}