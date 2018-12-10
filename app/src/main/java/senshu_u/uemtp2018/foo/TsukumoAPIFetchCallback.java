package senshu_u.uemtp2018.foo;

import java.util.List;

/**
 * Created by s-zebra on 12/10/18.
 */
public interface TsukumoAPIFetchCallback {
  void onPostsFetched(List<Post> posts);
}
