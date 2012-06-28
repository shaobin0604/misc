package cn.yo2.aquarium.example.httpclientasynctask;

import android.net.http.AndroidHttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Web Service Data Access Layer, used to get data from server
 * 
 * @author dev01
 *
 */
public class WebServiceDAL {
    private static final String BASE_URL = "http://strong-day-9896.herokuapp.com";
//    private static final String BASE_URL = "http://192.168.0.105:5000";
    
    
    private volatile boolean mIsAbort = false;
    
    private volatile AndroidHttpClient mHttpClient;
    private volatile HttpGet mHttpGet;
    
    /**
     * Abort the current http request
     */
    public void abortRequest() {
        MyLog.i("abortRequest");
        mIsAbort = true;
        if (mHttpGet != null) {
            mHttpGet.abort();
        }
    }
    
    /**
     * Get a list of {@link Station} which is belong to the specified {@link Type} 
     * 
     * @param typeId the id of {@link Type}
     * @return a list of {@link Station} which is belong to that type or null if error occurs
     */
    public ArrayList<Station> getStationsByTypeId(long typeId) {
        mIsAbort = false;
        final String url = BASE_URL + "/types/" + typeId + "/stations";
        mHttpClient = AndroidHttpClient.newInstance("Generic Android");
        mHttpGet = new HttpGet(url);
        try {
            MyLog.d(">>>>> httpClient.execute(httpGet) url = " + url);
            HttpResponse response = mHttpClient.execute(mHttpGet);
            MyLog.d("<<<<< httpClient.execute(httpGet)");
            
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
            
                MyLog.d(">>>>> response.getEntity()");
                HttpEntity entity = response.getEntity();
                MyLog.d("<<<<< response.getEntity()");
                
                MyLog.d(">>>>> EntityUtils.toString");
                String body = EntityUtils.toString(entity, "UTF-8");
                MyLog.d("<<<<< EntityUtils.toString");
                
                if (mIsAbort) {
                    MyLog.d("get http response body but is abort");
                } else {
                    return jsonToStations(body);
                }
            } else {
                MyLog.e("http response code is " + statusCode);
            }
        } catch (IOException e) {
            mHttpGet.abort();
            MyLog.e("io error", e);
        } finally {
            mHttpClient.close();
        }
        return null;
    }
    
    private ArrayList<Station> jsonToStations(String json) {
        try {
            JSONObject jsonRoot = new JSONObject(json);
            JSONObject jsonResponse = jsonRoot.getJSONObject("response");
            int code = jsonResponse.getInt("code");
            if (code != 0) {
                MyLog.e("json response code is not zero, code = " + code);
            } else {
                ArrayList<Station> stations = new ArrayList<Station>();
                JSONArray jsonStations = jsonRoot.getJSONArray("stations");
                final int length = jsonStations.length();
                
                long id;
                String name;
                String description;
                String streamUrl;
                String streamCodec;
                String streamCodeRate;
                String sinaWeibo;
                boolean isStable;
                String logoUrl;
                String frequency;
                String vtag;
                String keywords;
                long regionId;
                long typeId;
                String regionName;
                String typeName;
                long favoriteCount;
                
                for (int i = 0; i < length; i++) {
                    JSONObject jsonStation = jsonStations.getJSONObject(i);
                    
                    id = jsonStation.getLong("id");
                    name = jsonStation.getString("name");
                    description = jsonStation.getString("description");
                    streamUrl = jsonStation.getString("stream_url");
                    streamCodec = jsonStation.getString("stream_codec");
                    streamCodeRate = jsonStation.getString("stream_code_rate");
                    sinaWeibo = jsonStation.getString("sina_weibo");
                    isStable = jsonStation.getBoolean("is_stable");
                    logoUrl = jsonStation.getString("logo_url");
                    frequency = jsonStation.getString("frequency");
                    vtag = jsonStation.getString("vtag");
                    keywords = jsonStation.getString("keywords");
                    regionId = jsonStation.getLong("region_id");
                    typeId = jsonStation.getLong("type_id");
                    regionName = jsonStation.getString("region_name");
                    typeName = jsonStation.getString("type_name");
                    favoriteCount = jsonStation.getLong("favorite_count");
                    
                    stations.add(new Station(id, name, description, streamUrl, streamCodec, streamCodeRate, sinaWeibo, isStable, logoUrl, frequency, vtag, keywords, regionId, regionName, typeId, typeName, favoriteCount));
                }
                return stations;
            }
        } catch (JSONException e) {
           MyLog.e("json parse error", e);
        }
        return null;
    }

    /**
     * Get a list of {@link Type}
     * 
     * @return list of {@link Type} or null if error occurs
     */
    public ArrayList<Type> getTypes() {
        final String url = BASE_URL + "/types";
        AndroidHttpClient httpClient = AndroidHttpClient.newInstance("Generic Android");
        HttpGet httpGet = new HttpGet(url);
        try {
            HttpResponse response = httpClient.execute(httpGet);
            
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
            
                HttpEntity entity = response.getEntity();
                
                String body = EntityUtils.toString(entity, "UTF-8");
                
                return jsonToTypes(body);
            } else {
                MyLog.e("http response code is " + statusCode);
            }
        } catch (IOException e) {
            httpGet.abort();
            MyLog.e("io error", e);
        } finally {
            httpClient.close();
        }
        return null;
    }
    
    private ArrayList<Type> jsonToTypes(String json) {
        try {
            JSONObject jsonRoot = new JSONObject(json);
            JSONObject jsonResponse = jsonRoot.getJSONObject("response");
            int code = jsonResponse.getInt("code");
            if (code != 0) {
                MyLog.e("json response code is not zero, code = " + code);
            } else {
                ArrayList<Type> types = new ArrayList<Type>();
                JSONArray jsonTypes = jsonRoot.getJSONArray("types");
                final int length = jsonTypes.length();
                long id;
                String name;
                for (int i = 0; i < length; i++) {
                    JSONObject jsonType = jsonTypes.getJSONObject(i);
                    
                    id = jsonType.getLong("id");
                    name = jsonType.getString("name");
                    
                    types.add(new Type(id, name));
                }
                return types;
            }
        } catch (JSONException e) {
           MyLog.e("json parse error", e);
        }
        return null;
    }
}
