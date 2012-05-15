package cn.yo2.aquarium.example.viewpager;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import nl.matshofman.saxrssreader.RssFeed;
import nl.matshofman.saxrssreader.RssItem;
import nl.matshofman.saxrssreader.RssReader;

import org.xml.sax.SAXException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

public class ViewPagerDemoActivity extends Activity implements OnPageChangeListener {
	private static final String TAG = ViewPagerDemoActivity.class.getSimpleName();
	
	private ViewPager mViewPager;
	private TextView mTitle;
	
	private RssFeed mFeed;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setOnPageChangeListener(this);
        
        mTitle = (TextView) findViewById(R.id.title);
        
        new LoadFeedTask().execute("http://www.ifanr.com/feed");
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
		if (mFeed != null) {
			ArrayList<RssItem> items = mFeed.getRssItems();
			if (items != null && items.size() > arg0) {
				mTitle.setText(items.get(arg0).getTitle());
			}
		}
	}



	private class LoadFeedTask extends AsyncTask<String, Void, RssFeed> {
    	private ProgressDialog mProgressDialog;

		@Override
		protected RssFeed doInBackground(String... params) {
			URL url;
			try {
				url = new URL(params[0]);
				return RssReader.read(url);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
    	
		@Override
		protected void onPostExecute(RssFeed result) {
			if (mProgressDialog != null && mProgressDialog.isShowing()) {
				mProgressDialog.dismiss();
			}
			
			if (result != null) {
				mFeed = result;
				mViewPager.setAdapter(new SimpleViewPagerAdapter(ViewPagerDemoActivity.this, result));
				mViewPager.setCurrentItem(0);
			} else {
				Toast.makeText(ViewPagerDemoActivity.this, "failed to load feed", Toast.LENGTH_SHORT).show();
			}
		}
		
		@Override
		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(ViewPagerDemoActivity.this, "Loading...", "Loading feed");
		}
    }
    
    private static class HtmlBuilder {
    	private static final String CSS = "<head><style type=\"text/css\">body {max-width: 100%}\nimg {max-width: 100%; height: auto;}\npre {white-space: pre-wrap;}</style></head>";
        private static final String HTML_START = "<html>";
        private static final String HTML_END = "</html>";
    	private static final String BODY_START = "<body>";
        private static final String BODY_END = "</body>";
        
    	public static final String buildHtml(String content) {
    		StringBuilder builder = new StringBuilder();
    		builder.append(HTML_START);
    		builder.append(CSS);
    		builder.append(BODY_START);
    		builder.append(content);
			builder.append(BODY_END);
			builder.append(HTML_END);
			return builder.toString();
    	}
    }
    
    private static class SimpleViewPagerAdapter extends PagerAdapter {
    	private Queue<WebView> mWebViews;
    	private RssFeed mFeed;
    	private Context mContext;
    	
    	private SimpleViewPagerAdapter(Context context, RssFeed feed) {
    		mContext = context;
    		mWebViews = new LinkedList<WebView>();
    		for (int i = 0; i < 4; i++) {
            	WebView webView = new WebView(context);
            	mWebViews.add(webView);
            }
    		mFeed = feed;
    	}

		@Override
		public int getCount() {
			return mFeed.getRssItems().size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
    	
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			Log.d(TAG, "instantiateItem position = " + position);
			
			RssItem item = mFeed.getRssItems().get(position);
			
			WebView webView = mWebViews.remove();
			
			webView.loadDataWithBaseURL(null, HtmlBuilder.buildHtml(item.getContent()), "text/html", "utf-8", null);
			container.addView(webView);
			return webView;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			Log.d(TAG, "destroyItem position = " + position);
			
			container.removeView((View) object);
			
			mWebViews.add((WebView) object);
		}
    }
}