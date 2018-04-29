package com.example.akhmet.audbucketlist;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

public class ItemInfoActivity extends FragmentActivity implements OnMapReadyCallback {

    String inKey,userID;
    private DatabaseReference ref;
    private FirebaseDatabase mFirebaseDatabase;
    private GoogleMap mGoogleMap;
    eventClass event;
    private MarkerOptions mMarker;
    private LatLng mLatLng;
    EditText txtName;
    EditText txtDescription;
    DatePicker datePicker;
    TextView txtLong,txtLat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_info);
        final SharedPreferences sharedPref= PreferenceManager.getDefaultSharedPreferences(this);
        inKey =sharedPref.getString("key","error uploading key");
        userID=sharedPref.getString("mUserID","error uploading key");




        txtName=findViewById(R.id.txtEditEvent);
        txtDescription=findViewById(R.id.txtEditDescription);
        datePicker=findViewById(R.id.datePickerEdit);
        txtLong=findViewById(R.id.txtLogntitude);
        txtLat=findViewById(R.id.txtLat);

     /*   txtName=findViewById(R.id.txtEditEvent);
        txtDescription=findViewById(R.id.txtEditDescription);
        datePicker=findViewById(R.id.datePickerEdit);*/

        Calendar c=Calendar.getInstance();
        datePicker.setMinDate(c.getTimeInMillis());
        android.support.v7.widget.Toolbar toolbar=findViewById(R.id.toolbar);
        ImageButton btnCancel = toolbar.findViewById(R.id.btnCancel);
        Button btnEdit=findViewById(R.id.btnEditItem);
        Button btnDelete=findViewById(R.id.btnDelete);
    //    mFirebaseDatabase=FirebaseDatabase.getInstance();

        mFirebaseDatabase=FirebaseDatabase.getInstance();
        ref=mFirebaseDatabase.getReference("users").child(userID);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapEdit);
        mapFragment.getMapAsync(this);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calendar=Calendar.getInstance();
                int day = datePicker.getDayOfMonth();
                int month = datePicker.getMonth() ;
                int year = datePicker.getYear();
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,day);
                Date d=calendar.getTime();
                String name=txtName.getText().toString();
                if(name.equals(null) || name.equals("") || name.isEmpty())
                {
                    Toast.makeText(ItemInfoActivity.this, "Please, add the name to the event", Toast.LENGTH_SHORT).show();
                }else {
                    String description=txtDescription.getText().toString();
                    if(description.equals(null) || description.equals("") || description.isEmpty())
                    {
                        description="No description";
                    }
                    eventClass event = new eventClass(d, txtName.getText().toString(), description,mLatLng);
                    ref.child(inKey).setValue(event);
                    finish();
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ref.child(inKey).removeValue();
                finish();
            }
        });

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap=googleMap;

        ref.child(inKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                event = dataSnapshot.getValue(eventClass.class);
                event.setKey(inKey);
                txtName.setText(event.getName());
                txtDescription.setText(event.getDescription());
                Date d = event.getDueDate();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(d);
                datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                LatLng dubai = new LatLng(event.getLatitude(), event.getLongtitude());
                mLatLng = dubai;
                txtLong.setText(Double.toString(mLatLng.longitude));
                txtLat.setText(Double.toString(mLatLng.latitude));
                mMarker = new MarkerOptions().position(dubai).title(event.getName());
                mGoogleMap.clear();
                mGoogleMap.addMarker(mMarker);
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(dubai));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        /*addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(inKey.equals(dataSnapshot.getKey())) {
                    event = dataSnapshot.getValue(eventClass.class);
                    event.setKey(inKey);
                    txtName.setText(event.getName());
                    txtDescription.setText(event.getDescription());
                    Date d = event.getDueDate();
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(d);
                    datePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                    LatLng dubai = new LatLng(event.getLatitude(), event.getLongtitude());
                    mLatLng = dubai;
                    mMarker = new MarkerOptions().position(dubai).title(event.getName());
                    mGoogleMap.clear();
                    mGoogleMap.addMarker(mMarker);
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(dubai));
                }
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
        });*/

        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mLatLng=latLng;
                txtLong.setText(Double.toString(mLatLng.longitude));
                txtLat.setText(Double.toString(mLatLng.latitude));
                mGoogleMap.clear();
                mMarker=new MarkerOptions().position(latLng).title("Event Place");
                mGoogleMap.addMarker(mMarker);
            }
        });

    }
}
