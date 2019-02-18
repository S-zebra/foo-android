package senshu_u.uemtp2018.foo.tsukumo_api;

/**
 * Created by s-zebra on 2/11/19.
 */

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Foo用の投稿 (構造体)
 */
public class Post implements ClusterItem {
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
  
  public static Post fromJSON(JSONObject root) throws JSONException {
    return new Post(root.getInt("id"), root.getInt("parent"), root.getDouble("latitude"), root.getDouble("longitude"), root.getString("text"));
  }
  
  /**
   * ローカルに格納されたJSONから解析します。
   *
   * @param root
   * @return
   * @throws JSONException
   */
  public static Post fromLocalJSON(JSONObject root) throws JSONException {
    return new Post(root.getInt("id"), root.getInt("parent"), root.getDouble("lat"), root.getDouble("lon"), root.getString("text"));
  }
  
  public int getId() {
    return ID;
  }
  
  public int getParentId() {
    return parentID;
  }
  
  public void setParentID(int parentID) {
    this.parentID = parentID;
  }
  
  public String getText() {
    return text;
  }
  
  public void setText(String text) {
    this.text = text;
  }
  
  public String toJSONString() {
    try {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put("id", ID);
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
  
  public void setPosition(LatLng position) {
    this.position = position;
  }
  
  @Override
  public String getTitle() {
    return getText();
  }
  
  @Override
  public String getSnippet() {
    return null;
  }
}