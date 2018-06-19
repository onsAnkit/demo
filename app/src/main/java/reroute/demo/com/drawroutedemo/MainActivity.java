package reroute.demo.com.drawroutedemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Step;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, DirectionCallback {

    private GoogleMap mGoogleMap;
    private ArrayList<LatLng> mList;
    private static final String TAG = MainActivity.class.getSimpleName();
    private List<Step> stepsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    /**
     * method used to initialization
     */
    private void init() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mList = new ArrayList<>();
        mList.add(new LatLng(28.541585, 77.399984));
        mList.add(new LatLng(28.541281, 77.401061));
        mList.add(new LatLng(28.540809, 77.401016));
        mList.add(new LatLng(28.539321, 77.401228));
        mList.add(new LatLng(28.535552, 77.401788));
        mList.add(new LatLng(28.533557, 77.392748));
        mList.add(new LatLng(28.534524, 77.398987));
        mList.add(new LatLng(28.534230, 77.391576));
        mList.add(new LatLng(28.535429, 77.390952));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.style_json));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
        if (new GPSTracker(MainActivity.this).canGetLocation()) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(new GPSTracker(this).latitude, new GPSTracker(this).longitude), 14));
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(new GPSTracker(this).latitude, new GPSTracker(this).longitude)).title("My Location"));
            requestDirection(new GPSTracker(this).latitude, new GPSTracker(this).longitude, 28.5355, 77.3910);
        } else {
            new GPSTracker(MainActivity.this).showSettingsAlert();
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void requestDirection(double sourceLatitude, double sourceLongitude, double destinationLatitude, double destinationLogitude) {

        GoogleDirection.withServerKey(getString(R.string.google_map_key))
                .from(new LatLng(sourceLatitude, sourceLongitude))
                .to(new LatLng(destinationLatitude, destinationLogitude))
                .transportMode(TransportMode.DRIVING)
                .execute(this);
    }

    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
        if (direction.isOK()) {
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(new GPSTracker(this).latitude, new GPSTracker(this).longitude)));
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(28.5355, 77.3910)));

            ArrayList<LatLng> directionPositionList = direction.getRouteList().get(0).getLegList().get(0).getDirectionPoint();
            mGoogleMap.addPolyline(DirectionConverter.createPolyline(this, directionPositionList, 5, Color.rgb(39, 175, 250)));

            stepsList = direction.getRouteList().get(0).getLegList().get(0).getStepList();
          /*  distance.setText(direction.getRouteList().get(0).getLegList().get(0).getDistance().getText());
            totalDistance = direction.getRouteList().get(0).getLegList().get(0).getDistance().getText();
            totalDuration = direction.getRouteList().get(0).getLegList().get(0).getDuration().getText();
            totalDurationValue = direction.getRouteList().get(0).getLegList().get(0).getDuration().getValue();*/
            Log.d("Steps:- ", direction.getRouteList().get(0).getLegList().get(0).getStepList().size() + "");

        }
    }

    @Override
    public void onDirectionFailure(Throwable t) {
        Log.d(TAG, "error");
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            CameraPosition currentPlace = new CameraPosition.Builder(mGoogleMap.getCameraPosition())
                    .target(new LatLng(location.getLatitude(), location.getLongitude())).bearing(location.getBearing())
                    .zoom(17).build();
            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(currentPlace));

            LatLng mLatLng = new LatLng(location.getLatitude(), location.getLongitude());

            /*for (int i = 0; i < stepsList.size(); i++) {
            }*/

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

}
