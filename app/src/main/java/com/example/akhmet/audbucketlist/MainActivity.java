package com.example.akhmet.audbucketlist;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Comment;

import java.io.IOException;
import java.io.InputStream;
import java.time.chrono.MinguoChronology;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ListView lv;
    List<eventClass> eventsFirebase;
    SharedPreferences sharedPref;

    private FirebaseDatabase mFirebaseDatabase;


    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mDatabase;
    private String mUserId;


    MyListAdapter adapter;
    List<String> keys;



    //Tag for parsing
    private String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        keys=new ArrayList<String>();
        eventsFirebase=new ArrayList<>();
        lv=findViewById(R.id.listView);
        Date date=new Date(1990,2,3);
       // eventsFirebase.add(new eventClass(date,"new name"));
     //   eventsFirebase.add(new eventClass(new Date(1920,1,5),"randomName"));
        adapter=new MyListAdapter(MainActivity.this,eventsFirebase);
        lv.setAdapter(adapter);

        mFirebaseDatabase=FirebaseDatabase.getInstance();


        mFirebaseAuth=FirebaseAuth.getInstance();
        mFirebaseUser=mFirebaseAuth.getCurrentUser();

        if(mFirebaseUser==null)
        {
            loadLogInView();
        }else {

            mUserId=mFirebaseUser.getUid();

            mDatabase= mFirebaseDatabase.getReference("users").child(mUserId);
            sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences sharedPref1=PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
            final SharedPreferences.Editor editor1=sharedPref1.edit();
            editor1.putString("mUserID",mUserId);
            editor1.commit();
            FloatingActionButton fab;
            fab = findViewById(R.id.BtnFloating);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startActivity(new Intent(MainActivity.this, AddItem.class));
                }
            });

            mDatabase.addChildEventListener(childEventListener);

        }
//        new GetEvents().execute();


     //   itemname[0]=events[0].getName();
       // itemname[1]=events[1].getName();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id=item.getItemId();
        {
            if (id == R.id.action_logout) {
                mFirebaseAuth.signOut();
                loadLogInView();
            }


        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStart() {
        super.onStart();
        lv.setOnItemClickListener(mOnItemClickListener);
    }




    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

            // A new comment has been added, add it to the displayed list
            eventClass event= dataSnapshot.getValue(eventClass.class);
            event.setKey(dataSnapshot.getKey());
            eventsFirebase.add(event);
            Collections.sort(eventsFirebase, new Comparator<eventClass>() {
                public int compare(eventClass o1, eventClass o2) {
                    if (o1.getDueDate() == null || o2.getDueDate() == null)
                        return 0;
                    return o1.getDueDate().compareTo(o2.getDueDate());
                }
            });
            Collections.sort(eventsFirebase, new Comparator<eventClass>() {
                public int compare(eventClass o1, eventClass o2) {
                    if (o1.getCompleted() == null || o2.getCompleted() == null)
                        return 0;
                    return o1.getCompleted().compareTo(o2.getCompleted());
                }
            });
            adapter=new MyListAdapter(MainActivity.this,eventsFirebase);
            lv.setAdapter(adapter);

            // ...
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            for(int i=0;i<eventsFirebase.size();i++)
            {
                if(eventsFirebase.get(i).getKey().equals(dataSnapshot.getKey()))
                {
                    eventClass tempEvent=dataSnapshot.getValue(eventClass.class);
                    tempEvent.setKey(dataSnapshot.getKey());
                    eventsFirebase.set(i,tempEvent);
                    List<eventClass> temp=new ArrayList<>();
                    temp.addAll(eventsFirebase);
                    eventsFirebase.clear();
                    eventsFirebase.addAll(temp);
                    Collections.sort(eventsFirebase, new Comparator<eventClass>() {
                        public int compare(eventClass o1, eventClass o2) {
                            if (o1.getDueDate() == null || o2.getDueDate() == null)
                                return 0;
                            return o1.getDueDate().compareTo(o2.getDueDate());
                        }
                    });

                    Collections.sort(eventsFirebase, new Comparator<eventClass>() {
                        public int compare(eventClass o1, eventClass o2) {
                            if (o1.getCompleted() == null || o2.getCompleted() == null)
                                return 0;
                            return o1.getCompleted().compareTo(o2.getCompleted());
                        }
                    });
                    adapter=new MyListAdapter(MainActivity.this,eventsFirebase);
                    lv.setAdapter(adapter);
                    final SharedPreferences.Editor editor = sharedPref.edit();



                    lv.setOnItemClickListener(mOnItemClickListener);
                    break;
                }
            }

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());

            // A comment has changed, use the key to determine if we are displaying this
            // comment and if so remove it.
            for(int i=0;i<eventsFirebase.size();i++)
            {
                if(eventsFirebase.get(i).getKey().equals(dataSnapshot.getKey()))
                {
                    eventsFirebase.remove(i);
                    adapter=new MyListAdapter(MainActivity.this,eventsFirebase) ;
                    lv.setAdapter(adapter);
                    lv.setOnItemClickListener(mOnItemClickListener);
                    break;
                }
            }

            // ...
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());

            // A comment has changed position, use the key to determine if we are
            // displaying this comment and if so move it.
            Comment movedComment = dataSnapshot.getValue(Comment.class);
            String commentKey = dataSnapshot.getKey();

            // ...
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            Toast.makeText(MainActivity.this, "Failed to load comments.",
                    Toast.LENGTH_SHORT).show();
        }
    };

    public class sortByDates implements Comparator<eventClass>
    {

        @Override
        public int compare(eventClass a, eventClass b) {
            return a.getDueDate().compareTo(b.getDueDate());
        }
    }
    public class sortByChecked implements Comparator<eventClass>
    {

        @Override
        public int compare(eventClass a, eventClass b) {
            return a.getCompleted().compareTo(b.getCompleted());
        }
    }
    private void loadLogInView() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    AdapterView.OnItemClickListener mOnItemClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            final SharedPreferences.Editor editor = sharedPref.edit();
            String key=eventsFirebase.get(i).getKey();
            editor.putString("key", key);
            editor.commit();
            startActivity(new Intent(MainActivity.this, ItemInfoActivity.class));
        }
    };

}
