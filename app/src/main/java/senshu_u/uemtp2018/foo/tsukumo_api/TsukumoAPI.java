package senshu_u.uemtp2018.foo.tsukumo_api;

import android.app.Application;
import android.support.v4.BuildConfig;

/**
 * Created by s-zebra on 12/10/18.
 */
public abstract class TsukumoAPI {

  public static final String SERVER_URL = "http://192.168.0.7:3000";
  private static final String API_PATH = SERVER_URL + "/api/v1";
  static final String POSTS_URL = API_PATH + "/posts";
  public static final String HEADER_TOKEN = "API_TOKEN";
  static final String ACCOUNT_VERIFY_URL = API_PATH + "/accounts/available";
  public static final String TOKEN_KEY = "Token";
  static final String LOCATIONS_URL = POSTS_URL + "/locations";
}
