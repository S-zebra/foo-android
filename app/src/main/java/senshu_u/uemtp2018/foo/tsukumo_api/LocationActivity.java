package senshu_u.uemtp2018.foo.tsukumo_api;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import senshu_u.uemtp2018.foo.R;

/**
 * Created by s-zebra on 2/11/19.
 */
@SuppressLint ("MissingPermission")
public abstract class LocationActivity extends AppCompatActivity {
  private static final String TAG = "LocationActivity (Base)";
  private FusedLocationProviderClient locationClient;
  private int permReqCode;
  private LocationRequest locationRequest;
  private LocationCallback locationCallback;
  private boolean startedUpdating = false;
  
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    locationClient = new FusedLocationProviderClient(this);
    
    //ダミー
    locationRequest = LocationRequest.create()
      .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
      .setInterval(3000);
    
    locationCallback = new LocationCallback() {
      @Override
      public void onLocationResult(LocationResult result) {
        super.onLocationResult(result);
        Log.e(TAG, "Location updated, but this callback is dummy!");
      }
      
      @Override
      public void onLocationAvailability(LocationAvailability availability) {
        super.onLocationAvailability(availability);
        Log.e(TAG, "Location availability changed, but this callback is dummy!");
      }
    };
    
    if (!isLocationAvailable()) {
      // API23以降。それ以前だとどうしようもない
      // isLocationEnabledで分岐させる
      permReqCode = (int) (Math.random() * 10);
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, permReqCode);
      return;
    }
//    requestLocationUpdates();
  }
  
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == permReqCode && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
      if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
        Toast.makeText(this, R.string.toast_location_denied, Toast.LENGTH_SHORT).show();
      } else {
        requestLocationUpdates();
      }
    }
  }
  
  @Override
  protected void onResume() {
    super.onResume();
    if (startedUpdating) {
      requestLocationUpdates();
    }
  }
  
  protected void setLocationCallback(LocationCallback locationCallback) {
    locationClient.removeLocationUpdates(this.locationCallback);
    this.locationCallback = locationCallback;
    if (startedUpdating) {
      Log.d("LocationActivity", "Update requested for " + this.locationCallback.toString());
      locationClient.requestLocationUpdates(locationRequest, this.locationCallback, null);
    }
  }
  
  protected void setLocationRequest(LocationRequest locationRequest) {
    locationClient.removeLocationUpdates(locationCallback);
    this.locationRequest = locationRequest;
    if (startedUpdating) {
      locationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }
  }
  
  protected void requestLocationUpdates() {
    Log.d("LocationActivity", "Update requested for " + this.locationCallback.toString());
    this.locationClient.requestLocationUpdates(locationRequest, this.locationCallback, null);
    startedUpdating = true;
  }
  
  protected void removeLocationUpdates() {
    Log.d("LocationActivity", "Update removed for " + this.locationCallback.toString());
    this.locationClient.removeLocationUpdates(this.locationCallback);
    startedUpdating = false;
  }
  
  @Override
  protected void onPause() {
    super.onPause();
    if (startedUpdating) {
      locationClient.removeLocationUpdates(locationCallback);
    }
  }
  
  private boolean isLocationAvailable() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
    boolean gpsEnabled = false, netEnabled = false;
    try {
      gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    } catch (NullPointerException npe) {
      npe.printStackTrace();
      gpsEnabled = false;
    }
    try {
      netEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    } catch (NullPointerException npe) {
      npe.printStackTrace();
      netEnabled = false;
    }
    return gpsEnabled || netEnabled;
  }
  
}
