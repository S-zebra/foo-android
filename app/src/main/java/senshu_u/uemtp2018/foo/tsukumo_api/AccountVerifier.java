package senshu_u.uemtp2018.foo.tsukumo_api;

import android.os.AsyncTask;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.lang.ref.WeakReference;

/**
 * Created by s-zebra on 2/11/19.
 */
public class AccountVerifier extends AsyncTask<String, Void, Boolean> {
  
  private WeakReference<AccountVerificationCallback> callback;
  
  public AccountVerifier(AccountVerificationCallback callback) {
    this.callback = new WeakReference<>(callback);
  }
  
  @Override
  protected Boolean doInBackground(String... strings) {
    try {
      Connection conn = Jsoup.connect(TsukumoAPI.ACCOUNT_VERIFY_URL + "?token=" + strings[0]);
      conn.ignoreContentType(true);
      JSONObject obj = new JSONObject(conn.get().text());
      return obj.getBoolean("result");
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
  @Override
  protected void onPostExecute(Boolean aBoolean) {
    callback.get().onVerificationTaskComplete(aBoolean);
  }
  
}

