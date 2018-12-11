package senshu_u.uemtp2018.foo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, TsukumoAPIFetchCallback, LocationListener {
  
  private GoogleMap mMap;
  private ClusterManager<Post> mClusterManager;
  private LocationManager mLocationManager;
  private FloatingActionButton fab;
  private static final int LOCATION_REQ_CODE = 1;
  
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
  }
  
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
      grantResults[0] != PackageManager.PERMISSION_GRANTED) {
      Toast.makeText(this, "位置情報が拒否されています", Toast.LENGTH_SHORT).show();
    } else {
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
  }
  
  @Override
  public void onLocationChanged(Location location) {
  
  }
  
  @Override
  public void onStatusChanged(String provider, int status, Bundle extras) {
  
  }
  
  @Override
  public void onProviderEnabled(String provider) {
  
  }
  
  @Override
  public void onProviderDisabled(String provider) {
  
  }
}
