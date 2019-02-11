package senshu_u.uemtp2018.foo;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import senshu_u.uemtp2018.foo.tsukumo_api.Post;

/**
 * Created by s-zebra on 2/2/19.
 */
public class ParentPostView extends ConstraintLayout {
  public static final String TAG = ParentPostView.class.getSimpleName();
  private Post post;
  private TextView contentLabel;
  
  public ParentPostView(Context context) {
    super(context);
  }
  
  public ParentPostView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }
  
  public ParentPostView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
  
  public void setPost(Post post) {
    this.post = post;
  }
  
  public View inflate() {
    LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View v = inflater.inflate(R.layout.parent_post_view, null);
    contentLabel = v.findViewById(R.id.parentContentLabel);
    if (post != null) {
      contentLabel.setText(post.getText());
    }
    return v;
  }
}
