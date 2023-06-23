package senshu_u.uemtp2018.foo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import senshu_u.uemtp2018.foo.tsukumo_api.Post;
import senshu_u.uemtp2018.foo.tsukumo_api.PostFetcher;

/**
 * Created by s-zebra on 2/19/19.
 */
public class PostActionDialogFragment extends DialogFragment implements PostFetcher.FetchCallback {
  public static final String TAG = PostActionDialogFragment.class.getSimpleName();
  
  private Post post;
  private MapsActivity mapsActivity;
  private View inflatedView;
  private LinearLayout postsList;
  
  public Post getPost() {
    return post;
  }
  
  public void setPost(Post post) {
    this.post = post;
  }
  
  public void setMapsActivity(MapsActivity mapsActivity) {
    this.mapsActivity = mapsActivity;
  }
  
  @NonNull
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    
    inflatedView = new PostActionView(getContext()).inflate();
    Log.d("MapsActivity", String.valueOf(post));
    builder.setView(inflatedView);
    
    inflatedView.findViewById(R.id.keepButton).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mapsActivity.keepPost(post);
        dismiss();
      }
    });
    inflatedView.findViewById(R.id.replyButton).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent npIntent = new Intent(getContext(), NewPostActivity.class);
        npIntent.putExtra(NewPostActivity.PARENT_ID, post.getId());
        npIntent.putExtra(NewPostActivity.PARENT_TEXT, post.getText());
        getContext().startActivity(npIntent);
      }
    });
    TextView content = inflatedView.findViewById(R.id.postContent);
    content.setText(post.getText());
    
    Log.d(TAG, "post.parentID: " + post.getParentId());
    if (post.getParentId() > 0) {
      postsList = inflatedView.findViewById(R.id.postsList);
      fetchParent(post.getParentId());
      LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) inflatedView.findViewById(R.id.mainPostCell).getLayoutParams();
      p.setMargins(0, 1, 0, 0);
    }
    
    return builder.create();
  }
  
  private void fetchParent(int id) {
    Log.d(TAG, "Fetching post for id " + id);
    new PostFetcher(id, this).execute();
  }
  
  @Override
  public void onPostsFetched(List<Post> posts) {
    if (posts == null) {
      Log.d(TAG, "posts = null");
      return;
    }
    Post p = posts.get(0);
    ParentPostView ppv = new ParentPostView(getContext());
    ppv.setPost(p);
    postsList.addView(ppv.inflate(), 0);
    Log.d(TAG, "Added new view: " + post.toString());
    if (p.getParentId() > 0) {
      fetchParent(p.getParentId());
    }
  }
  
  public static class PostActionView extends ConstraintLayout {
    
    public PostActionView(Context context, AttributeSet attrs, int defStyleAttr) {
      super(context, attrs, defStyleAttr);
    }
    
    public PostActionView(Context context, AttributeSet attrs) {
      super(context, attrs);
    }
    
    public PostActionView(Context context) {
      super(context);
    }
    
    View inflate() {
      return inflate(getContext(), R.layout.post_action_fragment, null);
    }
    
  }
}