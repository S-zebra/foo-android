package senshu_u.uemtp2018.foo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class RelocateActivity extends AppCompatActivity implements OnMapReadyCallback {
  
  public static final int RESULT_CODE = 1;
  public static final String EXTRA_LOCATION = "location";
  private GoogleMap mMap;
  private Marker mMarker;
  private LatLng initialLocation;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_relocate);
    setTitle(R.string.title_activity_relocate);
    
    Toolbar toolbar = findViewById(R.id.relocateToolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
    
    Intent intent = getIntent();
    initialLocation = intent.getParcelableExtra(EXTRA_LOCATION);
    if (initialLocation == null) {
      initialLocation = new LatLng(0, 0);
    }
    
    // Obtain the SupportMapFragment and get notified when the map is ready to be used.
    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
      .findFragmentById(R.id.relocateMap);
    mapFragment.getMapAsync(this);
    
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.relocate_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        finish();
        break;
      case R.id.menuItem_done:
        LatLng pos = mMarker.getPosition();
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_LOCATION, pos);
        setResult(RESULT_CODE, resultIntent);
        finish();
        break;
    }
    return super.onOptionsItemSelected(item);
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
    try {
      mMap.setMyLocationEnabled(true);
    } catch (SecurityException e) {
      Toast.makeText(this, R.string.toast_location_denied, Toast.LENGTH_SHORT).show();
    }
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15f));
    mMarker = mMap.addMarker(new MarkerOptions()
      .position(initialLocation)
      .draggable(true));
    mMarker.showInfoWindow();
    mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
      @Override
      public void onCameraMove() {
        LatLng pos = mMap.getCameraPosition().target;
        mMarker.setPosition(pos);
      }
    });
    mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
      @Override
      public void onCameraIdle() {
        LatLng pos = mMarker.getPosition();
        String addr = GeocoderUtil.getAddress(RelocateActivity.this, pos);
        mMarker.setSnippet(addr);
        mMarker.hideInfoWindow();
        mMarker.setTitle(String.format("%.5f, %.5f", pos.latitude, pos.longitude));
        mMarker.showInfoWindow();
      }
    });
    
  }
  
}
