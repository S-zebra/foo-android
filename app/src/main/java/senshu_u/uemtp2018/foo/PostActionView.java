package senshu_u.uemtp2018.foo;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import senshu_u.uemtp2018.foo.tsukumo_api.Post;
import senshu_u.uemtp2018.foo.tsukumo_api.PostFetcher;
import senshu_u.uemtp2018.foo.tsukumo_api.PostsFetchCallback;

/**
 * Created by s-zebra on 2/2/19.
 */
public class PostActionView extends ConstraintLayout implements PostsFetchCallback {
  public static final String TAG = PostActionView.class.getSimpleName();
  
  private Post post;
  private TextView contentLabel;
  private AppCompatImageButton replyButton, keepButton;
  
  private LinearLayout postsList;
  
  public PostActionView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
  
  public PostActionView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
  
  public PostActionView(Context context) {
    super(context);
  }
  
  public void setPost(Post post) {
    this.post = post;
    Log.d(TAG, post + ", " + this.post);
  }
  
  View inflate() {
    View v = inflate(getContext(), R.layout.post_action_fragment, null);
    contentLabel = v.findViewById(R.id.postContent);
    if (post == null) return v;
    contentLabel.setText(post.getText());
    
    replyButton = v.findViewById(R.id.replyButton);
    replyButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent npIntent = new Intent(getContext(), NewPostActivity.class);
        npIntent.putExtra(NewPostActivity.PARENT_ID, post.getId());
        npIntent.putExtra(NewPostActivity.PARENT_TEXT, post.getText());
        getContext().startActivity(npIntent);
      }
    });
    
    keepButton = v.findViewById(R.id.keepButton);
    if (post.getParentId() > 0) {
      postsList = v.findViewById(R.id.postsList);
      fetchParentPosts();
      LayoutParams p = (LayoutParams) findViewById(R.id.mainPostCell).getLayoutParams();
      p.setMargins(0, 1, 0, 0);
    }
    return v;
  }
  
  private void fetchParentPosts() {
    new PostFetcher(post.getParentId(), this).execute();
  }
  
  @Override
  public void onPostsFetched(List<Post> posts) {
    Collections.reverse(posts);
    for (Post post : posts) {
      ParentPostView ppv = new ParentPostView(getContext());
      ppv.setPost(post);
      postsList.addView(ppv.inflate(), 0);
      Log.d(TAG, "Added new view: " + post.toString());
    }
  }
  
}