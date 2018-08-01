package memoryGame;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.sonya.myapplication.R;

import java.util.ArrayList;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import memoryGame.bl.Score;

public class RecordsActivity extends AppCompatActivity implements OnMapReadyCallback, IConstants {

    private Button              btnTable;
    private Button              btnMap;
    private Button              btnBack;

    //records
    private ArrayList<Score>    scoreList;
    private Database            database;

    //map
    private GoogleMap           map;
    private Marker[]            markers;
    private SupportMapFragment  mapFragment;

    //table
    private RecordTableFragment recordTableFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_records);

        database = new Database(this);
        scoreList = database.getScoreList();

        recordTableFragment = new RecordTableFragment();

        mapFragment = new SupportMapFragment();
        markers = new Marker[10];

        setTableFragment();
        setTableBtn();
        setMapBtn();
        setBackBtn();
    }

    private void setTableFragment(){

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.placeholder, recordTableFragment );
        ft.commit();
        recordTableFragment.showTable(scoreList);
    }

    private void setTableBtn(){
        btnTable = (Button) findViewById(R.id.btn_table);
        btnTable.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setTableFragment();
            }
        });
    }

    private void setMapBtn(){
        btnMap = (Button) findViewById(R.id.btn_map);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.placeholder, mapFragment);
                ft.commit();
                mapFragment.getMapAsync(RecordsActivity.this);
            }
        });
    }

    private void setBackBtn(){
        btnBack = (Button) findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        updateLocationUI();
        updateMarkersOnMap();
    }

    private void updateMarkersOnMap() {
        if (map == null)
            return;
        map.clear();
        for (int i = 0; i < scoreList.size(); i++) {
            String name = scoreList.get(i).getName();
            LatLng location = scoreList.get(i).getLocation();
            String strLocation = scoreList.get(i).getStrLocation();
            int scoreVaue = scoreList.get(i).getValue();
            markers[i] = map.addMarker(new MarkerOptions().title(name + "\n" ).snippet(strLocation + " \nScore:" + scoreVaue).position(location));
            markers[i].setTag(i);
        }
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {

                LinearLayout info = new LinearLayout(RecordsActivity.this);
                info.setOrientation(LinearLayout.VERTICAL);

                TextView title = new TextView(RecordsActivity.this);
                title.setTextColor(Color.BLACK);
                title.setGravity(Gravity.CENTER);
                title.setTypeface(null, Typeface.BOLD);
                title.setText(marker.getTitle());

                TextView snippet = new TextView(RecordsActivity.this);
                snippet.setTextColor(Color.GRAY);
                snippet.setText(marker.getSnippet());

                info.addView(title);
                info.addView(snippet);

                return info;
            }
        });
        if ( scoreList.size() > 0 )
            zoomMap( scoreList.get(0).getLocation() );
    }

    private void zoomMap(LatLng location){
        map.moveCamera(CameraUpdateFactory.newLatLng(location));
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, ZOOM));
    }

    @SuppressLint("MissingPermission")
    private void updateLocationUI() {
        if (map == null)
            return;
        if (checkLocationPermissionAndEnabled()) {
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);
        }
        else {
            map.setMyLocationEnabled(false);
            map.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    private boolean checkLocationPermissionAndEnabled() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        else {
            try {
                gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            } catch (Exception ex) { }
            try {
                network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            } catch (Exception ex) { }

            if (!gps_enabled && !network_enabled)
                return false;
            return true;
        }
    }
}