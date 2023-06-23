package senshu_u.uemtp2018.foo;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

/**
 * Created by s-zebra on 2/18/19.
 */
public abstract class GeocoderUtil {
  public static String getAddress(Context context, LatLng pos) {
    Geocoder gc = new Geocoder(context);
    try {
      List<Address> addresses = gc.getFromLocation(pos.latitude, pos.longitude, 1);
      if (addresses.isEmpty()) {
        Log.d("RelocateActivity", "Location not found");
        return null;
      } else {
        Address address = addresses.get(0);
        Log.d("RelocateActivity", address.toString());
        String addrLine = address.getAddressLine(0);
        if (addrLine.startsWith("Unnamed Road")) {
          return addrLine.replaceAll("Unnamed Road, ", "");
        } else {
          String[] addressParts = addrLine.split(" ", 2);
          return addressParts[addressParts.length - 1];
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
