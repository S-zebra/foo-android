package senshu_u.uemtp2018.foo;

import android.content.Context;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatImageButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by s-zebra on 2/2/19.
 */
public class PostActionFragment extends ConstraintLayout {
  public static final String TAG = PostActionFragment.class.getSimpleName();
  private Post post;
  private TextView contentLabel;
  private AppCompatImageButton replyButton, keepButton;
  
  public PostActionFragment(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
  
  public PostActionFragment(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
  
  public PostActionFragment(Context context) {
    super(context);
  }
  
  public void setPost(Post post) {
    this.post = post;
    Log.d(TAG, post + ", " + this.post);
  }
  
  View inflate() {
    View v = inflate(getContext(), R.layout.post_action_fragment, null);
    contentLabel = v.findViewById(R.id.postContent);
    if (post != null) {
      contentLabel.setText(post.getText());
    }
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
    keepButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        
      }
    });
    return v;
  }
  
  
}
