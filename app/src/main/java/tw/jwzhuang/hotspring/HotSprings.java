package tw.jwzhuang.hotspring;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.List;

import tw.jwzhuang.hotspring.db.Area;
import tw.jwzhuang.hotspring.db.HotSpring;
import tw.jwzhuang.hotspring.db.HotSpringsDB;

public class HotSprings extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private Marker myMarker;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private final int REQUEST_LOCATION = 1;
    private boolean mResolvingError;
    private List<Marker> hotSpringMarkers = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotsprints);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getMapFragment();
        mapFragment.getMapAsync(this);
        setupGoogleApiClient();
        setupLocationRequest();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        stopLocationUpdates();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
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
        if (checkLocationPermission()){
            return;
        }
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    //<editor-fold desc="GoogleApiClient.ConnectionCallbacks">
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (checkLocationPermission()) {
            return;
        }
        getLastLocation();
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    //</editor-fold>

    //<editor-fold desc="OnConnectionFailedListener">
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (mResolvingError) {
            // Already attempting to resolve an error.
            return;
        }
        mResolvingError = true;
        Snackbar.make(getMapFragment().getView(), "Cannot connect gps", Snackbar.LENGTH_LONG)
                .show();

    }
    //</editor-fold>

    //<editor-fold desc="LocationListener">
    @Override
    public void onLocationChanged(Location location) {
        updateUILocation(location);
    }
    //</editor-fold>

    //<editor-fold desc="GoogleMap.OnCameraMoveStartedListener">
    @Override
    public void onCameraMoveStarted(int i) {
        removeHotSpringsMarkers();
    }
    //</editor-fold>

    //<editor-fold desc="GoogleMap.OnCameraIdleListener">
    @Override
    public void onCameraIdle() {
        addHotSpringsMarkers();
    }
    //</editor-fold>

    //<editor-fold desc="GoogleMap.OnMarkerClickListener">
    @Override
    public boolean onMarkerClick(Marker marker) {
        if (marker == myMarker){
            return false;
        }
        HotSpring hotSpring = (HotSpring) marker.getTag();
        String uriStr = String.format("geo:%f,%f?q=%s",hotSpring.getLatitude(), hotSpring.getLongitude(), hotSpring.getName());
        Uri gmmIntentUri = Uri.parse(uriStr);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
        return true;
    }
    //</editor-fold>

    //<editor-fold desc="Request Permissions Result">
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION){
            if(grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    //</editor-fold>

    //<editor-fold desc="Private Method">
    private boolean checkLocationPermission(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(getMapFragment().getView(), "Need Permission", Snackbar.LENGTH_INDEFINITE)
                        .setAction("OK", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                requestLocationPermissions();
                            }
                        }).show();
            } else {
                requestLocationPermissions();
            }
            return true;
        }
        return false;
    }

    private Fragment getMapFragment(){
        return getSupportFragmentManager().findFragmentById(R.id.map);
    }

    private void requestLocationPermissions(){
        ActivityCompat.requestPermissions(
                this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_LOCATION);
    }

    private void getLastLocation() {
        if (checkLocationPermission()) {
            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        updateUILocation(mLastLocation);
    }

    private void updateUILocation(Location location){
        if (location != null) {
            double lat = location.getLatitude();
            double lng = location.getLongitude();

            LatLng loc = new LatLng(lat, lng);
            if (myMarker != null){
                myMarker.setPosition(loc);
                return;
            }
            myMarker = mMap.addMarker(new MarkerOptions().position(loc).title("我的位置"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 12.0f));
        }
    }

    private void setupGoogleApiClient(){

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }
    //</editor-fold>

    //<editor-fold desc="Private Method Update Location">
    private void setupLocationRequest(){
        if (checkLocationPermission()){
            return;
        }
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mLocationRequest.setInterval(5 * 1000);
        mLocationRequest.setFastestInterval(1 * 1000);
    }

    private void startLocationUpdates() {
        if (checkLocationPermission()) {
            return;
        }
        if (mGoogleApiClient ==null || !mGoogleApiClient.isConnected()) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        if (mGoogleApiClient ==null || !mGoogleApiClient.isConnected()) {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }
    //</editor-fold>

    private LatLngBounds getVisableRegionLatLngBounds(){
        VisibleRegion vRegion = mMap.getProjection().getVisibleRegion();
        return vRegion.latLngBounds;
    }

    private void addHotSpringsMarkers(){
        LatLngBounds latLngBounds = getVisableRegionLatLngBounds();
        double topLatitude = latLngBounds.northeast.latitude;
        double bottomLatitude = latLngBounds.southwest.latitude;
        double leftLongitude = latLngBounds.southwest.longitude;
        double rightLongitude = latLngBounds.northeast.longitude;
        HotSpring[] hotSprings = HotSpringsDB.getInstance(getApplicationContext()).listHostSprints(topLatitude, bottomLatitude, leftLongitude, rightLongitude);
        for (HotSpring hotSpring: hotSprings){
            MarkerOptions markerOptions = genHotSpringMarkerOptions(hotSpring.getName(), hotSpring.getLatitude(), hotSpring.getLongitude());
            Marker hotSpringMarker = mMap.addMarker(markerOptions);
            hotSpringMarker.setTag(hotSpring);
            hotSpringMarkers.add(hotSpringMarker);
        }
    }

    private void removeHotSpringsMarkers(){
        for (Marker marker : hotSpringMarkers){
            marker.setTag(null);
            marker.remove();
        }
        hotSpringMarkers.clear();
    }

    private MarkerOptions genHotSpringMarkerOptions(String title, double lat, double lng){
        LatLng loc = new LatLng(lat, lng);
        IconGenerator tc = new IconGenerator(this);
        Bitmap bmp = tc.makeIcon(title);
        return new MarkerOptions().position(loc).icon(BitmapDescriptorFactory.fromBitmap(bmp));
    }
}
