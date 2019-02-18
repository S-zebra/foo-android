package senshu_u.uemtp2018.foo.tsukumo_api;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by s-zebra on 2/11/19.
 */
public class PostFetcher extends AsyncTask<Void, Void, List<Post>> {
  private WeakReference<PostsFetchCallback> apiListenerWeakReference;
  private String query = "";
  private int id;
  
  public PostFetcher(PostsFetchCallback callback) {
    this(-1, callback);
  }
  
  public PostFetcher(int id, PostsFetchCallback callback) {
    this.apiListenerWeakReference = new WeakReference<>(callback);
    this.id = id;
  }
  
  public PostFetcher params(FetchParams params) {
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
  
}
