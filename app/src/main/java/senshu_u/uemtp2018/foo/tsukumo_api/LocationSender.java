package senshu_u.uemtp2018.foo.tsukumo_api;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.lang.ref.WeakReference;

/**
 * Created by s-zebra on 2/11/19.
 */
public class LocationSender extends AsyncTask<LatLng, Void, Boolean> {
  private WeakReference<LocationSendCallback> callbackWR;
  private Post post;
  private String token;
  
  public LocationSender(LocationSendCallback callback, @NonNull Post post, @NonNull String token) {
    this.callbackWR = new WeakReference<>(callback);
    this.post = post;
    this.token = token;
  }
  
  @Override
  protected Boolean doInBackground(LatLng... lngs) {
    LatLng pos = lngs[0];
    try {
      JSONObject obj = new JSONObject();
      obj.put("id", post.getId());
      obj.put("lat", pos.latitude);
      obj.put("lon", pos.longitude);
      Connection conn = Jsoup.connect(TsukumoAPI.LOCATIONS_URL)
        .timeout(10000)
        .header(TsukumoAPI.HEADER_TOKEN, token)
        .requestBody(obj.toString())
        .ignoreContentType(true);
      conn.post();
      Log.d("LocationSender", "Posting...");
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
  @Override
  protected void onPostExecute(Boolean aBoolean) {
    super.onPostExecute(aBoolean);
    Log.d("LocationSender", "PostExecute: " + aBoolean);
    callbackWR.get().onLocationSent(aBoolean);
  }
}
