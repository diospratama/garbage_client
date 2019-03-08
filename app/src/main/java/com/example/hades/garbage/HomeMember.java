package com.example.hades.garbage;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;


public class HomeMember extends AppCompatActivity implements OnMapReadyCallback,RoutingListener{

    private Toolbar toolbar;
    private NavigationView navigasiView;
    private DrawerLayout drawerLayout;
    private GoogleMap mMap;
    private String userId;
    private Button button_request;
    private FloatingActionButton button_placePicker;
    private LatLng pickupLocation;
    private LatLng placePickerLocation;
    private Boolean requestBol = false;
    private int radius=1,cek_radius=1;
    private Boolean driverFound=false;
    private String driverFoundID,placeName,placeAddress;
    private LinearLayout mDriverInfo;
    private DatabaseReference driveHasEndedRef;
    private ValueEventListener driveHasEndedRefListener;
    private DatabaseReference driverLocationRef,driverCekProfile;
    private ValueEventListener driverLocationRefListener;
    private Marker pickupMarker;
    private TextView mDriverName, mDriverPhone, mCostDriver, mCostSampah;
    private ImageView mDriverProfileImage;
    private RadioGroup mRadioGroup;
    private RadioButton mRadioSampah;
    private String destination,requestService;
    private LatLng destinationLatLng;
    boolean getDriversAroundStarted = false;
    private FusedLocationProviderClient mFusedLocationClient;
    private SupportMapFragment mapFragment;
    private RatingBar mRatingBar;
    private Marker mDriverMarker;
    private static final int[] COLORS = new int[]{R.color.colorPrimary};
    private List<Polyline> polylines;
    private Double distance;
    private double km=1000;
    private int rb_sampah;
    private double biaya_driver;
    private String biaya_sampah;
    public String sampah;
    public String cost_sampah;
    private Boolean bantu_placepicker=false;
    private Boolean bantu=false;
    private android.app.AlertDialog mDialog;

    private static int PLACE_PICKER_REQUEST = 1;




    GoogleApiClient mGmapClientApi;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    GeoQuery geoQuery;
    List<Marker> markers = new ArrayList<Marker>();
    boolean zoom=false;

    boolean network_enabled = false;
    private AlertDialog mInternetDialog;
    private LocationManager service;

    private BroadcastReceiver mNetworkDetectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkInternetConnection();

        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_member);

        //cek koneksi internet
        service = (LocationManager) getSystemService(LOCATION_SERVICE);
        registerReceiver(mNetworkDetectReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        try {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }catch (Exception e){
            Log.d(""+e,""+e);}

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        destinationLatLng = new LatLng(0.0,0.0);
        mDriverInfo = (LinearLayout) findViewById(R.id.driverInfo);
        mDriverProfileImage = (ImageView) findViewById(R.id.driverProfileImage);
        mDriverName = (TextView) findViewById(R.id.driverName);
        mDriverPhone = (TextView) findViewById(R.id.driverPhone);
        mRatingBar = (RatingBar) findViewById(R.id.ratingBar);
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        mRadioGroup.check(R.id.sampah1);
        mRadioGroup.check(R.id.sampah2);
        mRadioGroup.check(R.id.sampah3);

        rb_sampah=mRadioGroup.getCheckedRadioButtonId();
        mRadioSampah = (RadioButton) findViewById(rb_sampah);

        polylines = new ArrayList<>();
        button_request=(Button) findViewById(R.id.btn_call_driver);
        button_placePicker=(FloatingActionButton) findViewById(R.id.place_picker);
        mCostDriver=(TextView) findViewById(R.id.cost_driver);
        mCostSampah=(TextView) findViewById(R.id.cost_sampah);

        toolbar = (Toolbar) findViewById(R.id.toolbar1);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        navigasiView = (NavigationView) findViewById(R.id.navigasi_view);


        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(checkedId==R.id.sampah1){
                    cek_harga_sampah(0);
                }
                if(checkedId==R.id.sampah2){
                    cek_harga_sampah(1);
                }
                if(checkedId==R.id.sampah3){
                    cek_harga_sampah(2);
                }


            }
        });


        button_placePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                placePickerRequest();

            }
        });

        button_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (button_request.getText().equals("Call Driver")) {
                    showDialogSeaarchDriver();

                }
                else{
                    cekProfile();

                }
            }


        });
        navigasiView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                if (menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);
                drawerLayout.closeDrawers();

                switch (menuItem.getItemId()) {
                    case R.id.navigation2:
                        //Toast.makeText(getApplicationContext(), "Profil", Toast.LENGTH_SHORT).show();
                        Intent intent1 = new Intent(HomeMember.this, profil.class);
                        startActivity(intent1);
                        return true;
                    case R.id.navigation3:
                        Intent intent2 = new Intent(HomeMember.this, wallet.class);
                        startActivity(intent2);
                        return true;
                    case R.id.navigation4:
                        Toast.makeText(getApplicationContext(), "About", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.navigation5:
                        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                        firebaseAuth.signOut();
                        Intent intent = new Intent(HomeMember.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        return true;
                    case R.id.navigation6:
                        Intent intent3 = new Intent(HomeMember.this, History.class);
                        startActivity(intent3);
                        return true;

                    default:
                        Toast.makeText(getApplicationContext(), "Kesalahan", Toast.LENGTH_SHORT).show();
                        return true;
                }

            }
        });


        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };
        //Mensetting actionbarToggle untuk drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        //memanggil synstate
        actionBarDrawerToggle.syncState();

//        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
//                getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

//        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override
//            public void onPlaceSelected(Place place) {
//                // TODO: Get info about the selected place.
//                destination = place.getName().toString();
//                destinationLatLng = place.getLatLng();
//            }
//            @Override
//            public void onError(Status status) {
//                // TODO: Handle the error.
//            }
//        });
    }


    public void cek_harga_sampah(final int index){
        DatabaseReference cek_sampah=FirebaseDatabase.getInstance().getReference("Cost").child("Sampah");
        cek_sampah.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("kering")!=null&& index==0){
                        messageDialog("Biaya Sampah","Sampah kering "+map.get("kering").toString());

                    }
                    if(map.get("basah")!=null && index==1){
                        messageDialog("Biaya Sampah","Sampah basah "+map.get("basah").toString());

                    }
                    if(map.get("campuran")!=null && index==2){
                        messageDialog("Biaya Sampah","Sampah campuran "+map.get("campuran").toString());

                    }

                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });



    }




    @Override
    protected void onDestroy() {
        unregisterReceiver(mNetworkDetectReceiver);
        super.onDestroy();
    }


    private void getClosestDriver() {
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("DriverAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);
        geoQuery = geoFire.queryAtLocation(new GeoLocation(pickupLocation.latitude, pickupLocation.longitude), radius);
        geoQuery.removeAllListeners();

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!driverFound && requestBol){
                    DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(key);
                    mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                Map<String, Object> driverMap = (Map<String, Object>) dataSnapshot.getValue();
                                if (driverFound){
                                    return;
                                }
                                Toast.makeText(getApplicationContext(), requestService, Toast.LENGTH_LONG).show();


                               // if(driverMap.get("service").equals(requestService)){
                                driverFound = true;
                                driverFoundID = dataSnapshot.getKey();

                                DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID).child("customerRequest");
                                String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                HashMap map = new HashMap();
                                map.put("customerRideId", customerId);
                                map.put("destination", destination);
                                map.put("destinationLat", destinationLatLng.latitude);
                                map.put("destinationLng", destinationLatLng.longitude);
                                map.put("sampah", sampah);
                                map.put("cost_sampah",cost_sampah);
                                driverRef.updateChildren(map);
                                getDriverLocation();
                                getDriverInfo();
                                getHasRideEnded();
                                button_request.setText("Looking for Driver Location....");
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onKeyExited(String key) {

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {
                if (!driverFound)
                {
                    if(radius>=10){
                        radius=1;
                    }
                   if(cek_radius==11){
                       Toast.makeText(getApplicationContext(), "driver tidak ditemukan pada radius >=10 km", Toast.LENGTH_LONG).show();



                   }
                    radius++;
                    cek_radius++;
                    getClosestDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });


    }
    private void setCostSampah(){
        rb_sampah=mRadioGroup.getCheckedRadioButtonId();
        final RadioButton radioButton = (RadioButton) findViewById(rb_sampah);

        if (radioButton.getText() == null) {
            return;
        }else {
            final DatabaseReference driverRef1=FirebaseDatabase.getInstance().getReference("Cost").child("Sampah");

            driverRef1.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        DatabaseReference driverRef2=FirebaseDatabase.getInstance().getReference().child("customerRequest").child(userId);
                        Map setSampah=new HashMap();
                        if(map.get("kering")!=null){
                            if(radioButton.getText().equals("Sampah kering")){
                                cost_sampah=null;
                                sampah=null;
                                biaya_sampah=(map.get("kering").toString());
                                mCostSampah.setText("Sampah Kering Rp."+biaya_sampah);
                                cost_sampah= String.valueOf((biaya_sampah));
                                sampah="kering";

                                setSampah.put("sampah",sampah);
                                setSampah.put("cost_sampah",cost_sampah);
                                driverRef2.updateChildren(setSampah);

                            }
                            if(radioButton.getText().equals("Sampah basah")){
                                cost_sampah=null;
                                sampah=null;
                                biaya_sampah=(map.get("basah").toString());
                                mCostSampah.setText("Sampah Basah Rp."+biaya_sampah);
                                cost_sampah= String.valueOf((biaya_sampah));
                                sampah="basah";
                                setSampah.put("sampah",sampah);
                                setSampah.put("cost_sampah",cost_sampah);
                                driverRef2.updateChildren(setSampah);
                            }
                            if(radioButton.getText().equals("Sampah campuran")){
                                cost_sampah=null;
                                sampah=null;
                                biaya_sampah=(map.get("campuran").toString());
                                mCostSampah.setText("Sampah Campuran Rp."+biaya_sampah);
                                cost_sampah= String.valueOf((biaya_sampah));
                                sampah="campuran";
                                setSampah.put("sampah",sampah);
                                setSampah.put("cost_sampah",cost_sampah);
                                driverRef2.updateChildren(setSampah);
                            }

                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }


    private void getDriverLocation() {
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("DriverWorking").child(driverFoundID).child("l");
        driverLocationRefListener = driverLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Object value = dataSnapshot.getValue();
                if(value instanceof List) {
                    List<Object> map = (List<Object>) value;
                    // do your magic with values

                    if(dataSnapshot.exists()&& requestBol){
                        double locationLat =0;
                        double locationLng =0;

                        if(map.get(0)!=null){
                            locationLat=Double.parseDouble(map.get(0).toString());
                        }
                        if(map.get(1)!=null){
                            locationLng= Double.parseDouble(map.get(1).toString());
                        }
                        LatLng driverLatLng=new LatLng(locationLat,locationLng);
                        if(mDriverMarker != null){
                            mDriverMarker.remove();
                        }
                        double f1,f2,to1,to2;
                        f1=pickupLocation.latitude;
                        f2=pickupLocation.longitude;

                        to1=driverLatLng.latitude;
                        to2=driverLatLng.longitude;

                        LatLng loc1  = new LatLng(f1,f2);
                        LatLng loc2 = new LatLng(to1,to2);

                        distance = SphericalUtil.computeDistanceBetween(loc1,loc2);
                        if (distance<100){
                            button_request.setText("Driver's Here");
                        }else{
                            DecimalFormat df = new DecimalFormat("#.##");
                            button_request.setText("Driver Found: " + String.valueOf(df.format(distance/km)));
                            final DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Cost").child("Driver");
                            driverRef.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                                        if(map.get("jarak")!=null){
                                            if(distance!=0 && bantu==false){
                                                biaya_driver= Double.valueOf(map.get("jarak").toString()).doubleValue();
                                                DecimalFormat df = new DecimalFormat("#");
                                                mCostDriver.setText("Rp."+String.valueOf(df.format((distance/km)*biaya_driver)));
                                                bantu=true;
                                            }
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        }
                        mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("your driver").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_scooter)));
                        getRouteToMarker(driverLatLng);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getDriverInfo() {
        mDriverInfo.setVisibility(View.VISIBLE);
        DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID);
        mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    if(dataSnapshot.child("name")!=null){
                        mDriverName.setText(""+dataSnapshot.child("name").getValue().toString());
                    }
                    if(dataSnapshot.child("phone")!=null){
                        mDriverPhone.setText(""+dataSnapshot.child("phone").getValue().toString());
                    }
                    if(dataSnapshot.child("profileImageUrl").getValue()!=null){
                        Glide.with(getApplication()).load(dataSnapshot.child("profileImageUrl").getValue().toString()).into(mDriverProfileImage);
                    }



                    int ratingSum = 0;
                    float ratingsTotal = 0;
                    float ratingsAvg = 0;
                    for (DataSnapshot child : dataSnapshot.child("rating").getChildren()){
                        ratingSum = ratingSum + Integer.valueOf(child.getValue().toString());
                        ratingsTotal++;
                    }
                    if(ratingsTotal!= 0){
                        ratingsAvg = ratingSum/ratingsTotal;
                        mRatingBar.setRating(ratingsAvg);//Rating
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getHasRideEnded(){
        bantu=false;
        driveHasEndedRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID).child("customerRequest").child("customerRideId");
        driveHasEndedRefListener = driveHasEndedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                }else{
                    endRide();
                    erasePolylines();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void endRide() {

        requestBol = false;
        geoQuery.removeAllListeners();

        if(driverLocationRefListener!=null){
            driverLocationRef.removeEventListener(driverLocationRefListener);
        }

        if(driveHasEndedRefListener!=null){
            driveHasEndedRef.removeEventListener(driveHasEndedRefListener);
        }


        if (driverFoundID != null){
            DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(driverFoundID).child("customerRequest");
            driverRef.removeValue();
            driverFoundID = null;

        }
        driverFound = false;
        radius = 1;
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(userId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                ref.removeValue();

            }
        });

        if(pickupMarker != null){
            pickupMarker.remove();
        }
        if (mDriverMarker != null){
            mDriverMarker.remove();
        }
        button_request.setText("Call Driver");
        erasePolylines();

        mDriverInfo.setVisibility(View.GONE);
        mDriverName.setText(null);
        mCostDriver.setText(null);
        mDriverPhone.setText(null);
        mCostSampah.setText(null);
        mDriverProfileImage.setImageResource(R.drawable.ic_account_circle_black_24dp);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){

            }else{
                checkLocationPermission();
            }
        }

        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);

    }


    private void placePickerRequest(){

        PlacePicker.IntentBuilder builder =new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(HomeMember.this),PLACE_PICKER_REQUEST);
            if(pickupMarker!=null){
                pickupMarker.remove();
            }


        }catch (GooglePlayServicesNotAvailableException e){
            e.printStackTrace();
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        }




    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == PLACE_PICKER_REQUEST) {
//            if (resultCode == RESULT_OK) {
//
//                LatLng toLatLng = place.getLatLng();
//
//                // Add Marker
//                Marker marker = mMap.addMarker(new MarkerOptions().position(toLatLng)
//                        .title(placeName).snippet(placeAddress)
//                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA)));
//
//                // Move Camera to selected place
//                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(toLatLng,15));
//
//            }
//        }
//    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode, Intent data) {
        if(requestCode==PLACE_PICKER_REQUEST){
            if(resultCode==RESULT_OK){
                Place place=PlacePicker.getPlace(HomeMember.this,data);
                placePickerLocation=place.getLatLng();
                placeName = String.format("Place: %s", place.getName());
                placeAddress =  String.format("Address: %s", place.getAddress());
                Toast.makeText(getApplicationContext(), ""+placeAddress, Toast.LENGTH_LONG).show();
                bantu_placepicker=true;
                zoom=false;
            }

        }

    }


    LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
          for(Location location : locationResult.getLocations()){
            if(getApplicationContext()!=null) {
                if(bantu_placepicker==true){
                    double lat,lng;
                    //Toast.makeText(getApplicationContext(), ""+placePickerLocation.toString(), Toast.LENGTH_LONG).show();
                    lat=placePickerLocation.latitude;
                    location.setLatitude(placePickerLocation.latitude);
                    location.setLongitude(placePickerLocation.longitude);
                    mLastLocation=location;
                    if(zoom==false){
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(placePickerLocation));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                        pickupMarker = mMap.addMarker(new MarkerOptions().position(placePickerLocation).title(placeName).snippet(placeAddress)
                        .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                        zoom=true;
                    }
                    if (!getDriversAroundStarted) {
                        getDriversAround();
                    }


                }

                if(bantu_placepicker==false){
                    mLastLocation = location;
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    if(zoom==false){
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                        zoom=true;
                    }

                    if (!getDriversAroundStarted) {
                        getDriversAround();
                    }

                }

            }
          }
        }

    };

    private void checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                new android.app.AlertDialog.Builder(this)
                        .setTitle("give permission")
                        .setMessage("give permission message")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                ActivityCompat.requestPermissions(HomeMember.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                            }
                        })
                        .create()
                        .show();
            }
            else{
                ActivityCompat.requestPermissions(HomeMember.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:{
                if(grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
                        mMap.setMyLocationEnabled(true);
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void getDriversAround() {
        getDriversAroundStarted = true;
        DatabaseReference driverLocation = FirebaseDatabase.getInstance().getReference().child("DriverAvailable");

        GeoFire geoFire = new GeoFire(driverLocation);
        GeoQuery geoQuery =
        geoFire.queryAtLocation(new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), 999999999);

        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                LatLng driverLocation = new LatLng(location.latitude, location.longitude);
                String user = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

                Marker mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLocation).title("driver").icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_scooter)));
                mDriverMarker.setTag("driver");

                markers.add(mDriverMarker);


            }

            @Override
            public void onKeyExited(String key) {

                        markers.remove(0);

            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                for(Marker markerIt : markers){
                    if(markerIt.getTag().equals(key)){
                        markerIt.setPosition(new LatLng(location.latitude, location.longitude));
                    }
                }
            }

            @Override
            public void onGeoQueryReady() {
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void getRouteToMarker(LatLng pickupLatLng) {
        if (pickupLatLng != null && mLastLocation != null){
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints(new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude()), pickupLatLng)
                    .key("AIzaSyAZpvxhoqh6oSDYlupHMni8N6WSozDQq6M")
                    .build();
            routing.execute();
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {

        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

//            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }
    private void erasePolylines(){
        for(Polyline line : polylines){
            line.remove();
        }
        polylines.clear();
    }
    @Override
    public void onRoutingCancelled() {

    }



    private void checkInternetConnection() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = manager.getActiveNetworkInfo();

        if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
            network_enabled=true;
        } else {
            showNoInternetDialog();
        }
    }

    private void showNoInternetDialog() {

        if (mInternetDialog != null && mInternetDialog.isShowing()) {
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Internet Disabled!");
        builder.setMessage("No active Internet connection found.");
        builder.setPositiveButton("Turn On", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent gpsOptionsIntent = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                startActivityForResult(gpsOptionsIntent, WifiManager.WIFI_STATE_ENABLED);
            }
        }).setNegativeButton("No, Just Exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        mInternetDialog = builder.create();
        mInternetDialog.show();
    }


    private void cekProfile(){
        driverCekProfile=FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId);
        driverCekProfile.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();

                try {
                    if(map.get("name").equals(null)||map.get("name").equals("")||map.get("phone").equals(null)||map.get("phone").equals("")){
                        messageDialog("profil","isi dahulu profile anda di menu profile");
                    }

                    else {

                        if(requestBol){
                            endRide();
                            erasePolylines();
                        }else{
                            if(network_enabled==true){
//                    Toast.makeText(getApplicationContext(), requestService, Toast.LENGTH_LONG).show();
                                requestBol = true;
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("customerRequest");
                                GeoFire geoFire = new GeoFire(ref);
                                geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()), new GeoFire.CompletionListener() {
                                    @Override
                                    public void onComplete(String key, DatabaseError error) {


                                    }
                                });
                                pickupLocation = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pickupLocation, 15));
                                pickupMarker = mMap.addMarker(new MarkerOptions().position(pickupLocation).title("Pickup Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_pickup)));
                                button_request.setText("Getting your Driver....");
                                setCostSampah();
                                getClosestDriver();

                            }else if(network_enabled==false){showNoInternetDialog();}


                        }
                    }

                }catch (Exception e){
                    messageDialog("Profil","isi dahulu profile anda di menu profile");

                }




            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void messageDialog(String title,String kata){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(kata);
        mInternetDialog = builder.create();
        if (!HomeMember.this.isFinishing()){

            mInternetDialog.show();
        }

    }
    private void showDialogSeaarchDriver() {
        LinearLayout layout = new LinearLayout(this);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Notif");
        builder.setMessage("Cari pengambil sampah?");
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cekProfile();
            }
        }).setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
       mDialog = builder.create();
        mDialog.show();

        if (!HomeMember.this.isFinishing()){
           mDialog.setView(layout);
            mDialog.show();


        }
    }



}


