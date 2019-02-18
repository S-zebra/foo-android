package senshu_u.uemtp2018.foo.tsukumo_api;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Iterator;

/**
 * URIパラメーターを格納するクラス
 */
public class FetchParams {
  private HashMap<String, String> params;
  
  public FetchParams() {
    params = new HashMap<>();
    //Default FetchParams
    params.put("order", "id");
    params.put("desc", "1");
  }
  
  public FetchParams limit(int limit) {
    params.put("limit", Integer.toString(limit));
    return this;
  }
  
  public FetchParams position(LatLng ne, LatLng sw) {
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
  
  public FetchParams position(LatLng position) {
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
