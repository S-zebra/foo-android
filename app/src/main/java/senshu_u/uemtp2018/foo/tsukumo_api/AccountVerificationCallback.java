package senshu_u.uemtp2018.foo.tsukumo_api;

/**
 * Created by s-zebra on 2/11/19.
 */
public interface AccountVerificationCallback {
  void onAccountVerified(String token, boolean valid);
}
