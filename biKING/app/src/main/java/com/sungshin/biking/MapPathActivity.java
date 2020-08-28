package com.sungshin.biking;
//0501 현재 위치 & 지도에서 클릭한 위치 & 위치 검색 후 리스트에서 선택한 위치 마커, 목적지 설정

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.provider.SettingsSlicesContract.KEY_LOCATION;


public class MapPathActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    static Context mContext;
    private static final String TAG = MapPathActivity.class.getSimpleName();
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private Marker currentMarker = null;
    private LocationRequest locationRequest;
    private Location mCurrentLocatiion;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private final LatLng mDefaultLocation = new LatLng(37.56, 126.97);
    private boolean mLocationPermissionGranted;

    private static final int UPDATE_INTERVAL_MS = 1000 * 60 * 15;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 1000 * 30;

    private Button text_path;
    int AUTOCOMPLETE_REQUEST_CODE = 1;
    private TextView destination;

    private Button search_path;
    private LatLng mOrigin;
    private LatLng mDestination;
    ArrayList<LatLng> mMarkerPoints;

    private Button path_on_game;
    private Button back_to_game;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = MapPathActivity.this;

        if (savedInstanceState != null) {
            mCurrentLocatiion = savedInstanceState.getParcelable(KEY_LOCATION);
            CameraPosition mCameraPosition = savedInstanceState.getParcelable("camera_position");
        }
        setContentView(R.layout.activity_map_path);

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //지도 표시
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mMarkerPoints = new ArrayList<>();

        text_path = (Button) findViewById(R.id.text_path);

        //위치 검색 리스트 표시
        Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        text_path.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
                Intent intent = new Autocomplete.IntentBuilder(
                        AutocompleteActivityMode.OVERLAY, fields)
                        .build(getApplicationContext());

                startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE);


            }
        });

        path_on_game = (Button) findViewById(R.id.path_on_game);
        back_to_game = (Button) findViewById(R.id.back_to_game);

        back_to_game.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        path_on_game.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if( mDestination != null && mOrigin != null){
                    Intent intent = getIntent();

                    Double glatitude = mDestination.latitude;
                    Double glongitude = mDestination.longitude;
                    Double slatitude = mOrigin.latitude;
                    Double slongitude = mOrigin.longitude;
                    intent.putExtra("glatitude",glatitude);
                    intent.putExtra("glongitude",glongitude);
                    intent.putExtra("slatitude",slatitude);
                    intent.putExtra("slongitude",slongitude);
                    setResult(RESULT_OK, intent);
                    finish();

                }else{
                    Toast.makeText(getApplicationContext(), "경로가 지정되지 않았습니다", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    //리스트 결과 화면
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = Autocomplete.getPlaceFromIntent(data);
                Log.i("ok", "Place: " + place.getName() + ", " + place.getId());

                destination = (TextView)findViewById(R.id.destination);
                destination.setText(place.getName());
                destination.setVisibility(View.VISIBLE);

                final LatLng searchDestination = place.getLatLng();
                mMap.addMarker(new MarkerOptions().position(searchDestination).title("목적지"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(searchDestination, 15));

                mMarkerPoints.add(searchDestination);

            } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                Status status = Autocomplete.getStatusFromIntent(data);
                Log.i("error", status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                super.onBackPressed();
            }
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

        setDefaultLocation();
        getLocationPermission();
        updateLocationUI();
        getDeviceLocation();

        //클릭 시 목적지 설정
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                final MarkerOptions marker = new MarkerOptions();
                marker.title("목적지");
                Double latitude = latLng.latitude;
                Double longitude = latLng.longitude;

                LatLng clickDestination = new LatLng(latitude, longitude);
                marker.position(clickDestination);

                mMarkerPoints.add(clickDestination);

                String clickAddress = getCurrentAddress(clickDestination);

                new AlertDialog.Builder(mContext)
                        .setTitle("목적지를 추가합니다")
                        .setMessage(clickAddress + "추가하시겠습니까?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                mMap.addMarker(marker);
                            }})
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                Toast.makeText(mContext, "취소하였습니다. 다시 선택해주세요.", Toast.LENGTH_SHORT).show();
                            }})
                        .show();
            }
        });

        search_path = (Button) findViewById(R.id.search_path);

        search_path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mMarkerPoints.size() >= 2) {
                    mOrigin = mMarkerPoints.get(0);
                    mDestination = mMarkerPoints.get(1);
                    String key = getString(R.string.google_maps_key);
                    MapPath.drawRoute(mOrigin, mDestination, key, mMap, getApplicationContext());
                }

            }
        });

    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mCurrentLocatiion = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void setDefaultLocation() {
        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(mDefaultLocation);
        markerOptions.title("위치 정보를 가져올 수 없습니다.");
        markerOptions.snippet("위치 권한과 GPS 설정을 확인하세요.");
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        currentMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mDefaultLocation, 15);
        mMap.moveCamera(cameraUpdate);
    }

    String getCurrentAddress(LatLng latlng) {
        // 위치 정보 & 지역 -> 주소 문자열
        List<Address> addressList = null;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // 지오 코더 -> 주소 문자열
        try {
            addressList = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1);
        } catch (IOException e) {
            Toast.makeText(this, "네트워크 Error.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return "주소 인식 불가";
        }

        if (addressList.size() < 1) { // 주소 리스트가 비어있는지 비어 있으면
            return "해당 위치의 주소를 불러올 수 없습니다.";
        }

        // 문자열 생성 후 리턴
        Address address = addressList.get(0);
        StringBuilder addressStringBuilder = new StringBuilder();
        for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
            addressStringBuilder.append(address.getAddressLine(i));
            if (i < address.getMaxAddressLineIndex())
                addressStringBuilder.append("\n");
        }

        return addressStringBuilder.toString();
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                Location location = locationList.get(locationList.size() - 1);

                LatLng currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());

                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                Log.d(TAG, "Time :" + CurrentTime() + " onLocationResult : " + markerSnippet);

                setCurrentLocation(location, markerTitle, markerSnippet);
                mCurrentLocatiion = location;
            }
        }
    };

    private String CurrentTime(){
        Date today = new Date();
        SimpleDateFormat date = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss a");
        return time.format(today);
    }

    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if (currentMarker != null) currentMarker.remove();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);

        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);

        mMarkerPoints.add(currentLatLng);
    }

    private void getDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mLocationPermissionGranted) {
            Log.d(TAG, "onStart : requestLocationUpdates");
            mFusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            if (mMap!=null)
                mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mFusedLocationProviderClient != null) {
            Log.d(TAG, "onStop : removeLocationUpdates");
            mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mFusedLocationProviderClient != null) {
            Log.d(TAG, "onDestroy : removeLocationUpdates");
            mFusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable("camera_position", mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mCurrentLocatiion);
            super.onSaveInstanceState(outState);
        }
    }










}

