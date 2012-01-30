package com.genius.demo;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ViewPageActivity extends Activity implements android.view.View.OnClickListener, OnPageChangeListener{

	private ViewPager mViewPager;

	private ViewPageAdapter mPageAdapter;
	
	private List<View> mListViews;
	
	private final static int viewBackground[] = {R.drawable.guide01,
												R.drawable.guide02,
												R.drawable.guide03,
												R.drawable.guide04,
												R.drawable.guide05,};
	
	


	private ImageView[] mImageViews;
	
	private int mViewCount;
	
	private int mCurSel;
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_viewpage_layout);
        
      
        initViewPage();  
       
        init();
    }
	
	private void initViewPage()
	{
		mListViews = new ArrayList<View>();
        LayoutInflater mLayoutInflater = getLayoutInflater();
        
        mViewCount = viewBackground.length;       
        for(int i = 0; i < mViewCount; i++)
        {
        	View view = mLayoutInflater.inflate(R.layout.layout, null);
        	view.setBackgroundResource(viewBackground[i]);
        	mListViews.add(view);
        }
        

        
        mPageAdapter = new ViewPageAdapter(mListViews);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(mPageAdapter);
        mViewPager.setOnPageChangeListener(this);
	}
	
	
    private void init()
    {
    	LinearLayout linearLayout = (LinearLayout) findViewById(R.id.llayout);
    	
    	mImageViews = new ImageView[mViewCount];
    	
    	for(int i = 0; i < mViewCount; i++)
    	{
    		mImageViews[i] = (ImageView) linearLayout.getChildAt(i);
    		mImageViews[i].setEnabled(true);
    		mImageViews[i].setOnClickListener(this);
    		mImageViews[i].setTag(i);
    	}
    	
    	mCurSel = 0;
    	mImageViews[mCurSel].setEnabled(false);
    	
    }
    
    private void setCurView(int pos)
    {
    	if (pos < 0 || pos >= mViewCount)
    	{
    		return ;
    	}

    	mViewPager.setCurrentItem(pos);
    }

    
    private void setCurPoint(int index)
    {
    	if (index < 0 || index > mViewCount - 1 || mCurSel == index)
    	{
    		return ;
    	}
    	
    	
    	mImageViews[mCurSel].setEnabled(true);
    	mImageViews[index].setEnabled(false);
    	
    	mCurSel = index;
    }
    
    
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		int pos = (Integer)v.getTag();
		setCurView(pos);
		setCurPoint(pos);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int arg0) {
		// TODO Auto-generated method stub
		setCurPoint(arg0);
	}

}
