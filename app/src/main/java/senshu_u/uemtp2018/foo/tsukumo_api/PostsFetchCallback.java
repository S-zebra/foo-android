package senshu_u.uemtp2018.foo.tsukumo_api;

import java.util.List;

/**
 * Created by s-zebra on 2/11/19.
 */
public interface PostsFetchCallback {
  void onPostsFetched(List<Post> posts);
}
