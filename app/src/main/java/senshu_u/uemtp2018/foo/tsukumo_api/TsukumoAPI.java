package senshu_u.uemtp2018.foo.tsukumo_api;

/**
 * Created by s-zebra on 12/10/18.
 */
public abstract class TsukumoAPI {
  
  public static final String SERVER_URL = "https://tsukumokku.herokuapp.com";
  private static final String API_PATH = SERVER_URL + "/api/v1";
  static final String POSTS_URL = API_PATH + "/posts";
  static final String ACCOUNT_VERIFY_URL = API_PATH + "/accounts/available";
  public static final String TOKEN_KEY = "Token";
}
