package senshu_u.uemtp2018.foo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;


public class NewPostActivity extends AppCompatActivity implements PostSendCallback {
  public static final String TAG = NewPostActivity.class.getSimpleName();
  public static final String PARENT_ID = "parentID";
  public static final String PARENT_TEXT = "parentText";
  public static final int RELOCATE_REQ_CODE = 1;
  
  private FusedLocationProviderClient client;
  private TextView locationLabel;
  private final MyLocationCallback locationCallback = new MyLocationCallback();
  private EditText contentEditor;
  private Button relocateButton;
  private LatLng lastLocation;
  private String token;
  private int parentID;
  
  private TextView inReplyToHeader, inReplyToLabel;
  
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_post);
    setTitle(getString(R.string.title_new_post));
    Toolbar toolbar = findViewById(R.id.toolbar);
    locationLabel = findViewById(R.id.myLocationLabel);
    contentEditor = findViewById(R.id.contentEditor);
    setSupportActionBar(toolbar);
    startUpdatingLocation();
  
    token = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE).getString(TsukumoAPI.TOKEN_KEY, null);
    parentID = getIntent().getIntExtra(PARENT_ID, -1);
    inReplyToHeader = findViewById(R.id.inReplyToHeader);
    inReplyToLabel = findViewById(R.id.replyToLabel);
  
    if (parentID > 0) {
      inReplyToHeader.setVisibility(View.VISIBLE);
      inReplyToLabel.setVisibility(View.VISIBLE);
      inReplyToLabel.setText(getIntent().getStringExtra(PARENT_TEXT));
    }
  
    relocateButton = findViewById(R.id.relocateButton);
    relocateButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(NewPostActivity.this, RelocateActivity.class);
        if (lastLocation != null) {
          i.putExtra(RelocateActivity.EXTRA_LOCATION, lastLocation);
        }
        startActivityForResult(i, RELOCATE_REQ_CODE);
      }
    });
  }
  
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.new_post_menu, menu);
    return true;
  }
  
  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Log.d(TAG, String.valueOf(data));
    if (requestCode == RELOCATE_REQ_CODE) {
      if (data == null) return;
      client.removeLocationUpdates(locationCallback);
      lastLocation = data.getParcelableExtra(RelocateActivity.EXTRA_LOCATION);
      updateLocationDisplay();
    }
  }
  
  public void startUpdatingLocation() {
    //位置情報未許可時
    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
      }
      //M以前で位置情報が拒否されている状態
    } else {
      client = LocationServices.getFusedLocationProviderClient(this);
      LocationRequest req = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
        .setInterval(3000);
      client.requestLocationUpdates(req, locationCallback, null);
    }
  }
  
  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
      startUpdatingLocation();
    } else {
      //拒否されたとき
    }
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.send) {
      if (lastLocation == null) return true;
      if (token == null) {
        Toast.makeText(this, R.string.toast_not_logged_in, Toast.LENGTH_SHORT).show();
        return true;
      }
      String text = contentEditor.getText().toString();
      if (text.length() == 0) return true;
      Post newPost = new Post(lastLocation.latitude, lastLocation.longitude, text);
      if (parentID > 0) {
        newPost.setParentID(this.parentID);
      }
      PostSender sender = new PostSender(this, token);
      sender.execute(newPost);
    }
    return true;
  }
  
  public void updateLocationDisplay() {
    Geocoder gc = new Geocoder(NewPostActivity.this);
    StringBuilder sb = new StringBuilder(String.format("%.5f %.5f", lastLocation.latitude, lastLocation.longitude) + "\n");
    try {
      List<Address> addresses = gc.getFromLocation(lastLocation.latitude, lastLocation.longitude, 1);
      Log.d(getClass().getSimpleName(), "Addresses: " + addresses.toString());
      Address firstAddress = addresses.get(0);
      sb.append(firstAddress.getAddressLine(0));
    } catch (IOException e) {
      e.printStackTrace();
    }
    locationLabel.setText(sb.toString());
  }
  
  @Override
  public void onSendTaskComplete(boolean succeeded) {
    if (succeeded) {
      Toast.makeText(this, R.string.toast_post_sent, Toast.LENGTH_SHORT).show();
      finish();
    } else {
      Toast.makeText(this, R.string.toast_post_send_failed, Toast.LENGTH_SHORT).show();
    }
  }
  
  
  class MyLocationCallback extends LocationCallback {
    @Override
    public void onLocationResult(LocationResult result) {
      lastLocation = new LatLng(result.getLastLocation().getLatitude(), result.getLastLocation().getLongitude());
      updateLocationDisplay();
    }
  }
}