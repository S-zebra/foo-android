package senshu_u.uemtp2018.foo.tsukumo_api;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by s-zebra on 2/11/19.
 */
public class PostFetcher extends AsyncTask<Void, Void, List<Post>> {
  private WeakReference<FetchCallback> apiListenerWeakReference;
  private String query = "";
  private int id;
  
  public PostFetcher(FetchCallback callback) {
    this(-1, callback);
  }
  
  public PostFetcher(int id, FetchCallback callback) {
    this.apiListenerWeakReference = new WeakReference<>(callback);
    this.id = id;
  }
  
  public PostFetcher params(Parameters params) {
    this.query = params.toString();
    return this;
  }
  
  @Override
  protected List<Post> doInBackground(Void... voids) {
    try {
      String url = TsukumoAPI.POSTS_URL + (id > 0 ? "/" + id : query);
      Log.d("TsukumoAPI", url);
      Connection conn = Jsoup.connect(url);
      conn.timeout(100000);
      conn.ignoreContentType(true);
      String res = conn.get().text();
      JSONArray jsonArray = new JSONObject(res).getJSONArray("result");
      List<Post> postList = new ArrayList<>();
      for (int i = 0; i < jsonArray.length(); i++) {
        postList.add(Post.fromJSON(jsonArray.getJSONObject(i)));
      }
      return postList;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
  
  @Override
  protected void onPostExecute(List<Post> posts) {
    apiListenerWeakReference.get().onPostsFetched(posts);
  }
  
  public static class Parameters {
    private HashMap<String, String> params;
    
    public Parameters() {
      params = new HashMap<>();
      //Default Parameters
      params.put("order", "id");
      params.put("desc", "1");
    }
    
    public Parameters limit(int limit) {
      params.put("limit", Integer.toString(limit));
      return this;
    }
    
    public Parameters position(LatLng ne, LatLng sw) {
      if (ne == null || sw == null) {
        Log.e("PostFetcher", "nw or se is null");
        return this;
      }
      //1: ne.lat, sw.lon
      //2: sw.lat, ne.lon
      params.put("lat1", Double.toString(ne.latitude));
      params.put("lon1", Double.toString(sw.longitude));
      params.put("lat2", Double.toString(sw.latitude));
      params.put("lon2", Double.toString(ne.longitude));
      return this;
    }
    
    public Parameters position(LatLng position) {
      if (position == null) {
        Log.e("PostFetcher", "position is null");
        return this;
      }
      params.put("lat", Double.toString(position.latitude));
      params.put("lon", Double.toString(position.longitude));
      return this;
    }
    
    public String toString() {
      if (params.keySet().size() == 0) return "";
      Iterator<String> iterator = params.keySet().iterator();
      StringBuilder sb = new StringBuilder("?");
      while (iterator.hasNext()) {
        String k = iterator.next();
        sb.append(k);
        sb.append('=');
        sb.append(params.get(k));
        if (iterator.hasNext()) sb.append('&');
      }
      Log.d(this.getClass().getSimpleName(), "URI Params: " + sb.toString());
      return sb.toString();
    }
  }
  
  public interface FetchCallback {
    void onPostsFetched(List<Post> posts);
  }
}
