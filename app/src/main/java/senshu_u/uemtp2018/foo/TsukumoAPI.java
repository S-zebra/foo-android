package senshu_u.uemtp2018.foo;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by s-zebra on 12/10/18.
 */
abstract class TsukumoAPI {
  
  static final String SERVER_URL = "https://tsukumokku.herokuapp.com";
  private static final String API_PATH = SERVER_URL + "/api/v1";
  static final String POSTS_URL = API_PATH + "/posts";
  static final String ACCOUNT_VERIFY_URL = API_PATH + "/accounts/available";
  static final String TOKEN_KEY = "Token";
}

class PostFetcher extends AsyncTask<Void, Void, List<Post>> {
  private WeakReference<PostsFetchCallback> apiListenerWeakReference;
  private String query = "";
  
  public PostFetcher(PostsFetchCallback callback) {
    this.apiListenerWeakReference = new WeakReference<>(callback);
  }
  
  //Builderパターン適用してみた
  
  /**
   * 投稿を取得する条件を設定します。<br>
   * <b>設定完了後は、必ず<code>apply()</code>を呼び出す必要があります。</b>
   *
   * @return <code>URIParamBuilder</code>
   */
  public URIParamBuilder params() {
    return new URIParamBuilder(this);
  }
  
  private void setQuery(String query) {
    this.query = query;
  }
  
  @Override
  protected List<Post> doInBackground(Void... voids) {
    try {
      Connection conn = Jsoup.connect(TsukumoAPI.POSTS_URL + query);
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
  
  /**
   * URIパラメーターを作るBuilderクラスです。
   */
  class URIParamBuilder {
    private HashMap<String, String> params;
    private PostFetcher fetcher;
    
    private URIParamBuilder(PostFetcher fetcher) {
      this.fetcher = fetcher;
      params = new HashMap<>();
    }
    
    public URIParamBuilder limit(int limit) {
      params.put("limit", Integer.toString(limit));
      return this;
    }
    
    public URIParamBuilder position(LatLng position) {
      params.put("lat", Double.toString(position.latitude));
      params.put("lon", Double.toString(position.longitude));
      return this;
    }
    
    /**
     * 設定されたパラメータを<code>PostFetcher</code>に適用します。
     */
    public PostFetcher apply() {
      if (params.keySet().size() == 0) return fetcher;
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
      fetcher.setQuery(sb.toString());
      return fetcher;
    }
  }
}

class PostSender extends AsyncTask<Post, Void, Boolean> {
  private WeakReference<PostSendCallback> callback;
  private String token;
  
  public PostSender(PostSendCallback callback, @NonNull String token) {
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
}

class AccountVerifier extends AsyncTask<String, Void, Boolean> {
  
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

/**
 * Foo用の投稿 (構造体)
 */
class Post implements ClusterItem {
  private int ID, parentID;
  private LatLng position;
  private String text;
  
  private Post(int ID, int parentID, double lat, double lon, String text) {
    this.ID = ID;
    this.parentID = parentID;
    position = new LatLng(lat, lon);
    this.text = text;
  }
  
  public Post(double lat, double lon, String text) {
    this(-1, -1, lat, lon, text);
  }
  
  public int getId() {
    return ID;
  }
  
  public int getParentId() {
    return parentID;
  }
  
  public void setPosition(LatLng position) {
    this.position = position;
  }
  
  public String getText() {
    return text;
  }
  
  public void setText(String text) {
    this.text = text;
  }
  
  public static Post fromJSON(JSONObject root) throws JSONException {
    return new Post(root.getInt("id"), root.getInt("parent"), root.getDouble("latitude"), root.getDouble("longitude"), root.getString("text"));
  }
  
  public String toJSONString() {
    try {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("parent", parentID);
      jsonObject.put("lat", position.latitude);
      jsonObject.put("lon", position.longitude);
      jsonObject.put("text", text);
      return jsonObject.toString();
    } catch (JSONException jsone) {
      jsone.printStackTrace();
      return null;
    }
  }
  
  @Override
  public String toString() {
    return "Post #" + getId() + " (@" + position.latitude + ", " + position.longitude + "): " + getText();
  }
  
  @Override
  public LatLng getPosition() {
    return position;
  }
  
  @Override
  public String getTitle() {
    return getText();
  }
  
  @Override
  public String getSnippet() {
    return "";
  }
}

interface PostsFetchCallback {
  void onPostsFetched(List<Post> posts);
}

interface PostSendCallback {
  /**
   * 送信タスク完了時に呼び出される
   *
   * @param succeeded 送信完了時はtrue、そうでなければfalse
   */
  void onSendTaskComplete(boolean succeeded);
}

interface AccountVerificationCallback {
  void onVerificationTaskComplete(boolean isValid);
}
