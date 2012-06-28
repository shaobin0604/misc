
package cn.yo2.aquarium.example.httpclientasynctask;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

import java.util.ArrayList;

public class HttpClientAsyncTaskActivity extends Activity implements OnClickListener {

    private ViewFlipper mViewFlipper;
    private ListView mListView;
    private ToggleButton mToggleLoad;
    private TextView mInfo;
    private TextView mListEmpty;

    // private GetTypesTask mGetTypesTask;

    private GetStationsTask mGetStationsTask;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mViewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        mListView = (ListView) findViewById(R.id.list_view);
        mToggleLoad = (ToggleButton) findViewById(R.id.toggle_load);
        mInfo = (TextView) findViewById(R.id.info);
        mListEmpty = (TextView) findViewById(R.id.list_empty);

        mListView.setEmptyView(mListEmpty);

        mToggleLoad.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == mToggleLoad) {
            if (mToggleLoad.isChecked()) {
                startLoad();
            } else {
                cancelLoad();
            }
        }
    }

    private void startLoad() {
        // mGetTypesTask = new GetTypesTask();
        // mGetTypesTask.execute((Void)null);

        mGetStationsTask = new GetStationsTask();
        mGetStationsTask.execute((Void) null);
    }

    private void cancelLoad() {
        if (mGetStationsTask != null && mGetStationsTask.getStatus() != AsyncTask.Status.FINISHED) {
            mGetStationsTask.cancelTask();
        }
    }

    private static final class StationsAdapter extends BaseAdapter {

        private LayoutInflater mInflater;
        private ArrayList<Station> mStations;

        public StationsAdapter(Context context) {
            super();

            mInflater = LayoutInflater.from(context);
            mStations = new ArrayList<Station>(0);
        }

        public StationsAdapter(Context context, ArrayList<Station> stations) {
            super();

            mInflater = LayoutInflater.from(context);
            mStations = stations;
        }

        public void setStations(ArrayList<Station> stations) {
            mStations = stations;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return mStations.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return mStations.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return mStations.get(position).id;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            View v;
            if (convertView == null) {
                v = mInflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            } else {
                v = convertView;
            }
            TextView tv = (TextView) v.findViewById(android.R.id.text1);
            tv.setText(mStations.get(position).name);

            return v;
        }

    }

    private final class GetStationsTask extends AsyncTask<Void, Void, ArrayList<Station>> {

        private static final int CHILD_LIST_VIEW = 0;
        private static final int CHILD_PROGRESS_VIEW = 1;
        private WebServiceDAL mDal;

        public GetStationsTask() {
            super();

            mDal = new WebServiceDAL();
        }

        public void cancelTask() {
            Status status = getStatus();
            MyLog.d("status = " + status);
            
            if (status == Status.RUNNING) {
                mDal.abortRequest();
            }
            cancel(true);
        }

        @Override
        protected void onPreExecute() {
            MyLog.i("onPreExecute");

            mViewFlipper.setDisplayedChild(CHILD_PROGRESS_VIEW);
            mInfo.setText("loading...");
        }

        @Override
        protected ArrayList<Station> doInBackground(Void... params) {
            MyLog.i("doInBackground E");
            ArrayList<Station> stations = mDal.getStationsByTypeId(54);
            MyLog.i("doInBackground X");
            return stations;
        }

        @Override
        protected void onPostExecute(ArrayList<Station> stations) {

            if (stations == null) {
                MyLog.i("onPostExecute error");
                mInfo.setText("onPostExecute error");
                stations = new ArrayList<Station>(0);
            } else {
                MyLog.i("onPostExecute success");
                mInfo.setText("onPostExecute success");
            }
            
            ListAdapter adapter = mListView.getAdapter();
            if (adapter == null) {
                adapter = new StationsAdapter(HttpClientAsyncTaskActivity.this, stations);
                mListView.setAdapter(adapter);
            } else {
                StationsAdapter stationsAdapter = (StationsAdapter) adapter;
                stationsAdapter.setStations(stations);
                stationsAdapter.notifyDataSetChanged();
            }
            mViewFlipper.setDisplayedChild(CHILD_LIST_VIEW);
        }

        @Override
        protected void onCancelled() {
            MyLog.i("onCancelled");
            mInfo.setText("onCancelled");

            ArrayList<Station> stations = new ArrayList<Station>(0);

            ListAdapter adapter = mListView.getAdapter();
            if (adapter == null) {
                adapter = new StationsAdapter(HttpClientAsyncTaskActivity.this, stations);
                mListView.setAdapter(adapter);
            } else {
                StationsAdapter stationsAdapter = (StationsAdapter) adapter;
                stationsAdapter.setStations(stations);
                stationsAdapter.notifyDataSetChanged();
            }

            mViewFlipper.setDisplayedChild(CHILD_LIST_VIEW);
        }

    }
}
