package senshu_u.uemtp2018.foo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, PostsFetchCallback, AccountVerificationCallback {
  
  private GoogleMap mMap;
  private ClusterManager<Post> mClusterManager;
  private FloatingActionButton fab;
  private final int LOCATION_REQ_CODE = 1;
  private String tempToken;
  private SharedPreferences sharedPref;
  private final String LAST_LAT = "LAST_LAT";
  private final String LAST_LON = "LAST_LON";
  private final String LAST_ZOOM = "LAST_ZOOM";
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);
  
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
      .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
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
      tempToken = receivedUri.getQueryParameter("token");
      new AccountVerifier(this).execute(tempToken);
    } else {
      String savedToken = sharedPref.getString(TsukumoAPI.TOKEN_KEY, null);
      if (savedToken == null) {
        showLoginDialog();
      }
    }
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
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
      grantResults[0] != PackageManager.PERMISSION_GRANTED) {
      Toast.makeText(this, "位置情報が拒否されています", Toast.LENGTH_SHORT).show();
    } else {
      if (mMap != null) {
        mMap.setMyLocationEnabled(true);
      }
//      mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//      mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
    }
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
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQ_CODE);
      } else {
        mMap.setMyLocationEnabled(true);
      }
    } else {
      mMap.setMyLocationEnabled(true);
    }
    
    PostFetcher pf = new PostFetcher(this);
    pf.params()
      .position(new LatLng(35.606120, 139.527240))
      .apply()
      .execute();
  
    mClusterManager = new ClusterManager<>(this, mMap);
    mClusterManager.setAnimation(false);
    mMap.setOnCameraIdleListener(mClusterManager);
    mMap.setOnMarkerClickListener(mClusterManager);
  }
  
  @Override
  public void onPostsFetched(List<Post> posts) {
    if (posts == null) {
      Toast.makeText(this, "投稿を取得できませんでした。", Toast.LENGTH_SHORT).show();
      return;
    }
    Log.d(getClass().getSimpleName(), posts.toString());
    for (Post post : posts) {
      mClusterManager.addItem(post);
    }
    mClusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<Post>() {
      @Override
      public void onClusterItemInfoWindowClick(Post post) {
      
      }
    });
    try {
      getSupportFragmentManager().findFragmentById(R.id.map).getView().findViewById(2).performClick();
    } catch (NullPointerException npe) {
      npe.printStackTrace();
    }
  }
  
  @Override
  public void onVerificationTaskComplete(boolean isValid) {
    if (isValid) {
      Toast.makeText(this, "ログインが完了しました", Toast.LENGTH_SHORT).show();
      SharedPreferences.Editor editor = sharedPref.edit();
      editor.putString(TsukumoAPI.TOKEN_KEY, tempToken);
      editor.apply();
    } else {
      Toast.makeText(this, "ログインできません", Toast.LENGTH_SHORT).show();
    }
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    if (mMap == null) return;
    SharedPreferences.Editor editor = sharedPref.edit();
    LatLng camPos = mMap.getCameraPosition().target;
    editor.putFloat(LAST_LAT, (float) camPos.latitude);
    editor.putFloat(LAST_LON, (float) camPos.longitude);
    editor.putFloat(LAST_ZOOM, mMap.getCameraPosition().zoom);
    editor.apply();
  }
}
