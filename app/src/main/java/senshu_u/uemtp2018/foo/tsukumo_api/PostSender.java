package senshu_u.uemtp2018.foo.tsukumo_api;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.lang.ref.WeakReference;

/**
 * Created by s-zebra on 2/11/19.
 */
public class PostSender extends AsyncTask<Post, Void, Boolean> {
  private WeakReference<SendCallback> callback;
  private String token;
  
  public PostSender(SendCallback callback, @NonNull String token) {
    this.callback = new WeakReference<>(callback);
    this.token = token;
  }
  
  @Override
  protected Boolean doInBackground(Post... posts) {
    try {
      Connection conn = Jsoup.connect(TsukumoAPI.POSTS_URL)
        .timeout(10000)
        .ignoreContentType(true)
        .header("API_TOKEN", token)
        .requestBody(posts[0].toJSONString());
      conn.post();
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
  
  @Override
  protected void onPostExecute(Boolean aBoolean) {
    callback.get().onSendTaskComplete(aBoolean);
  }
  
  public interface SendCallback {
    /**
     * 送信タスク完了時に呼び出される
     *
     * @param succeeded 送信完了時はtrue、そうでなければfalse
     */
    void onSendTaskComplete(boolean succeeded);
  }
}
