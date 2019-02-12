package senshu_u.uemtp2018.foo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, PostsFetchCallback, AccountVerificationCallback {
  
  private GoogleMap mMap;
  private ClusterManager<Post> mClusterManager;
  private FloatingActionButton fab;
  private SharedPreferences sharedPref;
  private Toolbar toolbar;
  private ProgressDialog mProgressDialog;
  private final String LAST_LAT = "LAST_LAT";
  private final String LAST_LON = "LAST_LON";
  private final String LAST_ZOOM = "LAST_ZOOM";
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);
    setTitle(R.string.app_name);
    
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
      .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
  
    toolbar = findViewById(R.id.mapToolBar);
    setSupportActionBar(toolbar);
    
    fab = findViewById(R.id.floatingActionButton);
    fab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent(MapsActivity.this, NewPostActivity.class));
      }
    });
    sharedPref = getSharedPreferences(getPackageName(), MODE_PRIVATE);
    Uri receivedUri = getIntent().getData();
    if (receivedUri != null) {
      String tempToken = receivedUri.getQueryParameter("token");
      new AccountVerifier(this).execute(tempToken);
    } else {
      String savedToken = sharedPref.getString(TsukumoAPI.TOKEN_KEY, null);
      if (savedToken == null) {
        showLoginDialog();
      }
    }
  
    setLocationCallback(locationCallback);
  }
  
  private void showLoginDialog() {
    new AlertDialog.Builder(this)
      .setTitle(R.string.alert_login_title)
      .setMessage(R.string.alert_login_body)
      .setNegativeButton(R.string.later, null)
      .setPositiveButton(R.string.login_now, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          Intent loginIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TsukumoAPI.SERVER_URL));
          startActivity(loginIntent);
        }
      }).show();
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.map_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menuItem_refresh) {
      if (mMap == null) return true;
      fetchPosts();
    }
    return true;
  }
  
  /**
   * Manipulates the map once available.
   * This callback is triggered when the map is ready to be used.
   * This is where we can add markers or lines, add listeners or move the camera. In this case,
   * we just add a marker near Sydney, Australia.
   * If Google Play services is not installed on the device, the user will be prompted to install
   * it inside the SupportMapFragment. This method will only be triggered once the user has
   * installed Google Play services and returned to the app.
   */
  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;
    LatLng lastPos = new LatLng(sharedPref.getFloat(LAST_LAT, 0), sharedPref.getFloat(LAST_LON, 0));
    CameraUpdate camUpdate = CameraUpdateFactory.newLatLngZoom(lastPos, sharedPref.getFloat(LAST_ZOOM, 0));
    mMap.moveCamera(camUpdate);
    mMap.setMyLocationEnabled(true);
    mClusterManager = new ClusterManager<>(this, mMap);
    mClusterManager.setAnimation(false);
    mMap.setOnCameraIdleListener(mClusterManager);
    mMap.setOnMarkerClickListener(mClusterManager);
    mMap.setOnInfoWindowClickListener(mClusterManager);
    mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<Post>() {
      @Override
      public void onClusterItemInfoWindowClick(Post post) {
        PostActionDialogFragment f = new PostActionDialogFragment();
        f.setPost(post);
        f.setMapsActivity(MapsActivity.this);
        f.show(getSupportFragmentManager(), "");
      }
    });
    fetchPosts();
  }
  
  public static class PostActionDialogFragment extends DialogFragment {
    private Post post;
    
    public Post getPost() {
      return post;
    }
    
    public void setPost(Post post) {
      this.post = post;
    }
    
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      PostActionFragment fragment = new PostActionFragment(getContext());
      Log.d("MapsActivity", String.valueOf(post));
      fragment.setPost(post);
      builder.setView(fragment.inflate());
      return builder.create();
    }
  }
  
  @SuppressLint ("ResourceType")
  @Override
  public void onPostsFetched(List<Post> posts) {
    mProgressDialog.dismiss();
    if (posts == null) {
      Toast.makeText(this, R.string.toast_post_fetch_failed, Toast.LENGTH_SHORT).show();
      return;
    }
    mClusterManager.clearItems();
    
    Log.d(getClass().getSimpleName(), posts.toString());
    mClusterManager.addItems(posts);
    
    try {
      getSupportFragmentManager().findFragmentById(R.id.map).getView().findViewById(2).performClick();
    } catch (NullPointerException npe) {
      npe.printStackTrace();
    }
  }
  
  @Override
  public void onAccountVerified(String token, boolean valid) {
    if (valid) {
      Toast.makeText(this, R.string.toast_login_success, Toast.LENGTH_SHORT).show();
      sharedPref.edit()
        .putString(TsukumoAPI.TOKEN_KEY, token)
        .apply();
    } else {
      Toast.makeText(this, R.string.toast_login_failure, Toast.LENGTH_SHORT).show();
    }
  }
  
  @Override
  public void onLocationSent(boolean success) {
    mProgressDialog.dismiss();
    Toast.makeText(MapsActivity.this, success ? R.string.toast_post_put_success : R.string.toast_post_put_failed, Toast.LENGTH_SHORT).show();
    if (success) {
      sharedPref.edit()
        .remove(SP_KEPT_POST)
        .apply();
      putButton.setVisibility(View.GONE);
    }
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    if (mMap == null) return;
    LatLng camPos = mMap.getCameraPosition().target;
    sharedPref.edit()
      .putFloat(LAST_LAT, (float) camPos.latitude)
      .putFloat(LAST_LON, (float) camPos.longitude)
      .putFloat(LAST_ZOOM, mMap.getCameraPosition().zoom)
      .apply();
  }
  
  public static class PostActionDialogFragment extends DialogFragment {
    private Post post;
    private MapsActivity mapsActivity;
    
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
      PostActionView view = new PostActionView(getContext());
      view.setPost(post);
      View inflatedView = view.inflate();
      Log.d("MapsActivity", String.valueOf(post));
      builder.setView(inflatedView);
      inflatedView.findViewById(R.id.keepButton).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mapsActivity.keepPost(post);
          dismiss();
        }
      });
      return builder.create();
    }
    
  }
  
  class MapsLocationCallback extends LocationCallback {
    @Override
    public void onLocationResult(LocationResult result) {
      super.onLocationResult(result);
      Log.d("MapsActivity", "location updated");
      lastLocation = new LatLng(result.getLastLocation().getLatitude(), result.getLastLocation().getLongitude());
    }
    
    @Override
    public void onLocationAvailability(LocationAvailability availability) {
      super.onLocationAvailability(availability);
    }
  }
}
