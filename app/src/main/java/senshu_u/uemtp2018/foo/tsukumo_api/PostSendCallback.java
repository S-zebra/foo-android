package senshu_u.uemtp2018.foo.tsukumo_api;

/**
 * Created by s-zebra on 2/11/19.
 */

public interface PostSendCallback {
  /**
   * 送信タスク完了時に呼び出される
   *
   * @param succeeded 送信完了時はtrue、そうでなければfalse
   */
  void onSendTaskComplete(boolean succeeded);
}