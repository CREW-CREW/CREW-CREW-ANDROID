package com.example.crewcrew_demo;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.baoyz.swipemenulistview.SwipeMenu;
import com.baoyz.swipemenulistview.SwipeMenuAdapter;
import com.baoyz.swipemenulistview.SwipeMenuCreator;
import com.baoyz.swipemenulistview.SwipeMenuItem;
import com.baoyz.swipemenulistview.SwipeMenuListView;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

class RunningCrew{
    String name;
    String level;
    String memberNumber;
    String time;
    String currentMemberNumber;
    ArrayList<LatLng> path = new ArrayList<LatLng>();
    LatLng pathStart;
    LatLng pathEnd;
}

class DatabaseRunningCrew{
    String level;
    String number;
    ArrayList<Double> latitude = new ArrayList<Double>();
    ArrayList<Double> longitude = new ArrayList<Double>();
    String time;
}

public class RunningCrewInfoList extends AppCompatActivity
        implements OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback{
    private GoogleMap mMap;
    private Marker currentMarker = null;

    private static final String TAG = "googlemap_example";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초

    private LatLng startLatLng = new LatLng(0, 0);        //polyline 시작점
    private LatLng endLatLng = new LatLng(0, 0);

    private boolean initialPoint = true;

    // onRequestPermissionsResult에서 수신된 결과에서 ActivityCompat.requestPermissions를 사용한 퍼미션 요청을 구별하기 위해 사용됩니다.
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    boolean needRequest = false;

    // 앱을 실행하기 위해 필요한 퍼미션을 정의합니다.
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};  // 외부 저장소

    Location mCurrentLocatiion;
    LatLng currentPosition;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;
    private Location location;

    private View mLayout;
    public Button contactButton;
    public Button joinButton;
    public Button makeCrewBtn;
    public Button refreshBtn;

    private String addedCrewName;
    private String addedCrewLevel;
    private String addedCrewNum;
    private String addedCrewTime;
    private String addedCrewMinutes;
    private String addedCrewSeconds;

    public static ListViewAdapter adapter;
    public static ArrayList<RunningCrew> crewArrayList = new ArrayList<RunningCrew>();
    ArrayList<LatLng> temp = new ArrayList<LatLng>();
    LatLng runningPathStart;
    LatLng runningPathEnd;

    ArrayList<Polyline> polylines = new ArrayList<Polyline>();

    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();

    public static ArrayList<RunningCrew> dbCrewList = new ArrayList<RunningCrew>();
    Button crewButton;
    Boolean initialize = true;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_running, menu) ;

        return true ;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_addcrew :
                Intent intent = new Intent(getApplicationContext(), MakeCrew.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return true ;
            case R.id.action_chat:
                intent = new Intent(getApplicationContext(), StartChatActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default :
                return super.onOptionsItemSelected(item) ;
        }
    }

    public static int dp2px(Context ctx, float dp) {
        final float scale = ctx.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.runningcrew_info);

        ActionBar ab = getSupportActionBar();
        ab.setTitle("러닝 크루 정보");

        mLayout = findViewById(R.id.layout_main);

        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);

        LocationSettingsRequest.Builder builder =
                new LocationSettingsRequest.Builder();

        builder.addLocationRequest(locationRequest);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        //리스트 생성

        final SwipeMenuListView listview ;
        adapter = new ListViewAdapter() ;
        listview = findViewById(R.id.listView);
        listview.setAdapter(adapter);

        databaseReference.child("RunningCrew").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                RunningCrew dbCrew = new RunningCrew();
                dbCrew.name = dataSnapshot.getKey();
                ArrayList<Double> latitude = new ArrayList<Double>();
                ArrayList<Double> longitude = new ArrayList<Double>();
                //Toast.makeText(RunningCrewInfoList.this, "child: "+dataSnapshot.getKey()+"", Toast.LENGTH_SHORT).show();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    //dbCrewList.add(dataSnapshot1.getValue(RunningCrew.class));
                    //Toast.makeText(RunningCrewInfoList.this, "datasnapshot1: " + dataSnapshot1.getKey() + "", Toast.LENGTH_SHORT).show();
                    if(dataSnapshot1.getKey().equals("latitude")){
                        for(DataSnapshot dataSnapshot2 : dataSnapshot1.getChildren()) {
                            //Toast.makeText(RunningCrewInfoList.this, "path: " + dataSnapshot2.getValue() + "", Toast.LENGTH_SHORT).show();
                            latitude.add((double) dataSnapshot2.getValue());
                        }
                    }else if(dataSnapshot1.getKey().equals("level")){
                        dbCrew.level = dataSnapshot1.getValue().toString();
                    }else if(dataSnapshot1.getKey().equals("longitude")){
                        for(DataSnapshot dataSnapshot2 : dataSnapshot1.getChildren()) {
                            //Toast.makeText(RunningCrewInfoList.this, "path: " + dataSnapshot2.getValue() + "", Toast.LENGTH_SHORT).show();
                            longitude.add((double) dataSnapshot2.getValue());
                        }
                    }else if(dataSnapshot1.getKey().equals("number")){
                        dbCrew.memberNumber = dataSnapshot1.getValue().toString();
                    }else if(dataSnapshot1.getKey().equals("time")){
                        dbCrew.time = dataSnapshot1.getValue().toString();
                    }else if(dataSnapshot1.getKey().equals("curNum")){
                        dbCrew.currentMemberNumber = dataSnapshot1.getValue().toString();
                    }
                }

                for(int i=0;i<latitude.size();i++){
                    LatLng tmp = new LatLng(latitude.get(i), longitude.get(i));
                    dbCrew.path.add(tmp);

                }

                dbCrewList.add(dbCrew);
                adapter.addItem(dbCrew.name, dbCrew.level+"분/km", "/"+dbCrew.memberNumber+"명", "운동시간: "+dbCrew.time+"분", dbCrew.currentMemberNumber+"명");
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        SwipeMenuCreator creator = new SwipeMenuCreator() {

            @Override
            public void create(SwipeMenu menu) {
                // create "open" item
                SwipeMenuItem openItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                openItem.setBackground(new ColorDrawable(Color.rgb(0xC9, 0xC9,
                        0xCE)));
                // set item width
                openItem.setWidth(dp2px(RunningCrewInfoList.this ,90));
                // set item title
                openItem.setTitle("Contact");
                // set item title fontsize
                openItem.setTitleSize(18);
                // set item title font color
                openItem.setTitleColor(Color.WHITE);
                // add to menu
                menu.addMenuItem(openItem);

                // create "delete" item
                SwipeMenuItem deleteItem = new SwipeMenuItem(
                        getApplicationContext());
                // set item background
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
                        0x3F, 0x25)));
                deleteItem.setTitle("Join");
                deleteItem.setTitleSize(18);
                deleteItem.setTitleColor(Color.WHITE);
                // set item width
                deleteItem.setWidth(dp2px(RunningCrewInfoList.this,90));
                // set a icon
                // add to menu
                menu.addMenuItem(deleteItem);
            }
        };

// set creator
        listview.setMenuCreator(creator);
        listview.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(int position, SwipeMenu menu, int index) {
                ListViewItem item = (ListViewItem) listview.getItemAtPosition(position);
                switch(index) {
                    case 0:
                        Intent intent = new Intent(getApplicationContext(), StartChatActivity.class);
                        intent.putExtra("CrewName", item.getCrewName());
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        break;
                    case 1:
                        int a = Integer.parseInt(item.getCrewCurNum().substring(0,1));
                        a=a+1;
                        databaseReference.child("RunningCrew").child(item.getCrewName()).child("curNum").setValue(a+"");
                        adapter.notifyDataSetChanged();
                        break;
                }
                return false;
            }
        });

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private View oldSelection = null;

            public void clearSelection(){
                if(oldSelection != null){
                    oldSelection.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    for(Polyline line : polylines){
                        line.remove();
                    }
                    polylines.clear();
                }
            }
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                clearSelection();
                oldSelection = v;
                v.setBackgroundColor(getResources().getColor(android.R.color.holo_purple));
                ListViewItem item = (ListViewItem) parent.getItemAtPosition(position);

                for(int i=0; i<dbCrewList.size();i++){
                    if(dbCrewList.get(i).name.equals(item.getCrewName())){
                        int j;
                        for(j=0;j<dbCrewList.get(i).path.size()-1;j++){
                            PolylineOptions options = new PolylineOptions().add(dbCrewList.get(i).path.get(j)).add(dbCrewList.get(i).path.get(j+1)).width(15).color(Color.GREEN).geodesic(true);
                            polylines.add(mMap.addPolyline(options));
                        }
                        break;
                    }
                }
            }
        }) ;

        if(getIntent().hasExtra("runningName")) {
            addedCrewName = getIntent().getStringExtra("runningName");
            addedCrewLevel = getIntent().getStringExtra("runningLevel");
            addedCrewNum = getIntent().getStringExtra("runningNum");
            addedCrewMinutes = getIntent().getStringExtra("runningMinutes");
            addedCrewSeconds = getIntent().getStringExtra("runningSeconds");
            temp = getIntent().getParcelableArrayListExtra("runningPath");
            runningPathStart = getIntent().getParcelableExtra("runningPathStart");
            runningPathEnd = getIntent().getParcelableExtra("runningPathEnd");
           // adapter.addItem(addedCrewName, addedCrewLevel+"분/km", "0명/" + addedCrewNum + "명", addedCrewMinutes + "분" + addedCrewSeconds + "초");

            RunningCrew tempCrew = new RunningCrew();
            tempCrew.name = addedCrewName;
            tempCrew.level = addedCrewLevel+"분/km";
            tempCrew.memberNumber = "0명/" + addedCrewNum + "명";
            tempCrew.time = addedCrewMinutes + "분" + addedCrewSeconds+"초";
            tempCrew.path = temp;
            tempCrew.path.add(runningPathEnd);
            tempCrew.pathStart = runningPathStart;
            tempCrew.pathEnd = runningPathEnd;

            databaseReference.child("RunningCrew").child(addedCrewName).child("level").setValue(addedCrewLevel);
            databaseReference.child("RunningCrew").child(addedCrewName).child("number").setValue(addedCrewNum);
            databaseReference.child("RunningCrew").child(addedCrewName).child("time").setValue(addedCrewMinutes);
            databaseReference.child("RunningCrew").child(addedCrewName).child("curNum").setValue("0");

            for(int i=0;i<temp.size();i++) {
                databaseReference.child("RunningCrew").child(addedCrewName).child("latitude").push().setValue(tempCrew.path.get(i).latitude);
                databaseReference.child("RunningCrew").child(addedCrewName).child("longitude").push().setValue(tempCrew.path.get(i).longitude);
            }
            adapter.notifyDataSetChanged();

            //databaseReference.child("RunningCrew").child(tempCrew.name).push().setValue(tempCrew);
            //crewArrayList.add(tempCrew);
            //Toast.makeText(RunningCrewInfoList.this, crewArrayList.size()+"", Toast.LENGTH_SHORT).show();
        }

      /*  //Toast.makeText(RunningCrewInfoList.this, "child: "+dbCrewList.size()+"", Toast.LENGTH_SHORT).show();
        for(int i=0;i<dbCrewList.size();i++){
            RunningCrew temp = dbCrewList.get(i);
            adapter.addItem(temp.name, temp.level, temp.memberNumber, temp.time);
        }*/
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        Log.d(TAG, "onMapReady :");
        mMap = googleMap;
        //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setDefaultLocation();

        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            // 2. 이미 퍼미션을 가지고 있다면
            // ( 안드로이드 6.0 이하 버전은 런타임 퍼미션이 필요없기 때문에 이미 허용된 걸로 인식합니다.)
            startLocationUpdates(); // 3. 위치 업데이트 시작
        }else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.
            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])) {
                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.",
                        Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                        ActivityCompat.requestPermissions( RunningCrewInfoList.this, REQUIRED_PERMISSIONS,
                                PERMISSIONS_REQUEST_CODE);
                    }
                }).show();

            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions( this, REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                Log.d( TAG, "onMapClick :");
            }
        });
    }

    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);

            List<Location> locationList = locationResult.getLocations();

            if (locationList.size() > 0) {
                location = locationList.get(locationList.size() - 1);

                currentPosition
                        = new LatLng(location.getLatitude(), location.getLongitude());

                if (initialPoint) {
                    startLatLng = currentPosition;
                    endLatLng = currentPosition;
                    initialPoint = false;
                }else{
                    startLatLng = endLatLng;
                    endLatLng = currentPosition;
                }

                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                        + " 경도:" + String.valueOf(location.getLongitude());

                Log.d(TAG, "onLocationResult : " + markerSnippet);
                //현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);

                mCurrentLocatiion = location;
            }
        }
    };

    private void startLocationUpdates() {

        if (!checkLocationServicesStatus()) {

            Log.d(TAG, "startLocationUpdates : call showDialogForLocationServiceSetting");
            showDialogForLocationServiceSetting();
        }else {

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION);


            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED ||
                    hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED   ) {

                Log.d(TAG, "startLocationUpdates : 퍼미션 안가지고 있음");
                return;
            }

            Log.d(TAG, "startLocationUpdates : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

            if (checkPermission())
                mMap.setMyLocationEnabled(true);

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (checkPermission()) {
            Log.d(TAG, "onStart : call mFusedLocationClient.requestLocationUpdates");
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
            if (mMap!=null)
                mMap.setMyLocationEnabled(true);
        }
    }


    @Override
    protected void onStop() {

        super.onStop();

        if (mFusedLocationClient != null) {

            Log.d(TAG, "onStop : call stopLocationUpdates");
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }
        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
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
    }


    public void setDefaultLocation() {
        //디폴트 위치, Seoul
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        String markerTitle = "위치정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 요부 확인하세요";

        if (currentMarker != null) currentMarker.remove();

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        currentMarker = mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);

    }

    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    private boolean checkPermission() {

        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED   ) {
            return true;
        }

        return false;
    }

    /*
     * ActivityCompat.requestPermissions를 사용한 퍼미션 요청의 결과를 리턴받는 메소드입니다.
     */
    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grandResults) {

        if ( permsRequestCode == PERMISSIONS_REQUEST_CODE && grandResults.length == REQUIRED_PERMISSIONS.length) {

            // 요청 코드가 PERMISSIONS_REQUEST_CODE 이고, 요청한 퍼미션 개수만큼 수신되었다면

            boolean check_result = true;

            // 모든 퍼미션을 허용했는지 체크합니다.

            for (int result : grandResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    check_result = false;
                    break;
                }
            }

            if ( check_result ) {

                // 퍼미션을 허용했다면 위치 업데이트를 시작합니다.
                startLocationUpdates();
            }
            else {
                // 거부한 퍼미션이 있다면 앱을 사용할 수 없는 이유를 설명해주고 앱을 종료합니다.2 가지 경우가 있습니다.

                if (ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSIONS[1])) {


                    // 사용자가 거부만 선택한 경우에는 앱을 다시 실행하여 허용을 선택하면 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 앱을 다시 실행하여 퍼미션을 허용해주세요. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }else {
                    // "다시 묻지 않음"을 사용자가 체크하고 거부를 선택한 경우에는 설정(앱 정보)에서 퍼미션을 허용해야 앱을 사용할 수 있습니다.
                    Snackbar.make(mLayout, "퍼미션이 거부되었습니다. 설정(앱 정보)에서 퍼미션을 허용해야 합니다. ",
                            Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {

                            finish();
                        }
                    }).show();
                }
            }
        }
    }

    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RunningCrewInfoList.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d(TAG, "onActivityResult : GPS 활성화 되있음");
                        needRequest = true;
                        return;
                    }
                }
                break;
        }
    }
}