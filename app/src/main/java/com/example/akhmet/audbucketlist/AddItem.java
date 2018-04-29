package com.example.akhmet.audbucketlist;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

public class AddItem extends FragmentActivity implements OnMapReadyCallback {

    private DatabaseReference db;
    private GoogleMap mGoogleMap;
    private MarkerOptions mMarker;
    private LatLng mLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        SharedPreferences sharedPref1= PreferenceManager.getDefaultSharedPreferences(AddItem.this);

        String mUserID=sharedPref1.getString("mUserID","no key");
        db = FirebaseDatabase.getInstance().getReference("users").child(mUserID);

        final EditText txtName = findViewById(R.id.txtEditEvent);
        final EditText txtDescription = findViewById(R.id.txtEditDescription);
        Button btnAdd = findViewById(R.id.btnAddItem);
        android.support.v7.widget.Toolbar toolbar=findViewById(R.id.toolbar);
        ImageButton btnCancel = toolbar.findViewById(R.id.btnCancel);

        final DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);

        Calendar c=Calendar.getInstance();
        datePicker.setMinDate(c.getTimeInMillis());

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String userId = db.push().getKey();
                Calendar calendar=Calendar.getInstance();
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth() + 1;
                int year = datePicker.getYear();
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,day);
                Date d=calendar.getTime();
                String name=txtName.getText().toString();
                if(name.equals(null) || name.equals(""))
                {
                    Toast.makeText(AddItem.this, "Please, add the name to the event", Toast.LENGTH_SHORT).show();
                }else {
                    String description=txtDescription.getText().toString();
                    if(description.equals(null) || description.equals(""))
                    {
                        description="No description";
                    }
                    try {
                        eventClass event = new eventClass(d, txtName.getText().toString(), description, mLatLng);
                        db.child(userId).setValue(event);
                        finish();
                    }catch (Exception e)
                    {
                        Toast.makeText(AddItem.this, "Please, select the event place on the map", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap=googleMap;
        LatLng dubai=new LatLng(25.2048,55.2708);
        mMarker=new MarkerOptions().position(dubai).title("Dubai");
        mGoogleMap.clear();
        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(dubai));
        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mLatLng=latLng;
                mGoogleMap.clear();
                mMarker=new MarkerOptions().position(latLng).title("Event Place");
                mGoogleMap.addMarker(mMarker);
            }
        });
    }
}
