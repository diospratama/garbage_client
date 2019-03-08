package com.example.hades.garbage;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;

import com.example.hades.garbage.ReycylerHistory.HistoryAdapter;
import com.example.hades.garbage.ReycylerHistory.HistoryObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;

public class History extends AppCompatActivity {
    private String customerOrDriver, userId;
    public static String profileimageurl;
    private double mDistance,mcostTotal,mCostDriver,mCostSampah;
    @BindView(R.id.toolbar)Toolbar mtoolbar;

    private RecyclerView mHistoryRecyclerView;
    private RecyclerView.Adapter mHistoryAdapter;
    private RecyclerView.LayoutManager mHistoryLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        mtoolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(R.string.history);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mtoolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                setContentView(R.layout.activity_home_member);
                finish();

            }
        });

        mHistoryRecyclerView = (RecyclerView) findViewById(R.id.historyRecyclerView);
        mHistoryRecyclerView.setNestedScrollingEnabled(false);
        mHistoryRecyclerView.setHasFixedSize(true);
        mHistoryLayoutManager = new LinearLayoutManager(History.this);
        mHistoryRecyclerView.setLayoutManager(mHistoryLayoutManager);
        mHistoryAdapter = new HistoryAdapter(getDataSetHistory(), History.this);
        mHistoryRecyclerView.setAdapter(mHistoryAdapter);

        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getUserHistoryIds();
    }

    private ArrayList resultsHistory = new ArrayList<HistoryObject>();
    private ArrayList<HistoryObject> getDataSetHistory() {
        return resultsHistory;
    }

    private String getDate(Long time) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.setTimeInMillis(time*1000);
        String date = DateFormat.format("MM-dd-yyyy hh:mm", cal).toString();
        return date;
    }

    private void getUserHistoryIds() {
        DatabaseReference userHistoryDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userId).child("history");
        userHistoryDatabase.addListenerForSingleValueEvent(new ValueEventListener()  {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    for(DataSnapshot history : dataSnapshot.getChildren()){
                        FetchRideInformation(history.getKey());
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void FetchRideInformation(String rideKey) {
        DatabaseReference historyDatabase = FirebaseDatabase.getInstance().getReference().child("history").child(rideKey);
        historyDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    final String rideId = dataSnapshot.getKey();
                    Long timestamp = 0L;
                    String distance = "";
                    String status_transaksi = "";
                    String costTotal="";
                    String idDriver = null;
                    Double ridePrice = 0.0;
                    String urlCustomer = null;

                    if(dataSnapshot.child("timestamp").getValue() != null){
                        timestamp = Long.valueOf(dataSnapshot.child("timestamp").getValue().toString());
                    }
                    if(dataSnapshot.child("distance").getValue() != null) {
                        mDistance=Double.parseDouble(dataSnapshot.child("distance").getValue().toString());
                        DecimalFormat df = new DecimalFormat("#.##");
                        distance=(df.format(mDistance/1000))+" KM";
                    }

                    if(dataSnapshot.child("cost_sampah").getValue()!=null){
                        mCostSampah=Double.parseDouble(dataSnapshot.child("cost_sampah").getValue().toString());

                        if(dataSnapshot.child("cost_driver").getValue()!=null){
                            mCostDriver=Double.parseDouble(dataSnapshot.child("cost_driver").getValue().toString());
                        }
                        mcostTotal=mCostSampah+mCostDriver;
                        DecimalFormat df = new DecimalFormat("#");
                        costTotal=("Rp. "+df.format(mcostTotal));
                    }
                    if(dataSnapshot.child("transaksi").getValue()!=null){
                        if(dataSnapshot.child("transaksi").getValue().equals("cash")){
                            status_transaksi="Transaksi Cash";
                        }
                        if(dataSnapshot.child("transaksi").getValue().equals("saldo")){
                            status_transaksi="Transaksi Saldo";
                        }
                        if(dataSnapshot.child("transaksi").getValue().equals("0")){
                            status_transaksi="Transaksi Belum dibayar";
                        }

                    }
                    if(dataSnapshot.child("driver").getValue() != null){

                        idDriver=dataSnapshot.child("driver").getValue().toString();
                        getInformationDriver(idDriver);

                    }
                    DatabaseReference userCustomer = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(idDriver);
                    final Long finalTimestamp = timestamp;
                    final String finalDistance = distance;
                    final String finalCostTotal = costTotal;
                    final String finalStatus_transaksi = status_transaksi;
                    userCustomer.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists()){
                                try{
                                    if(dataSnapshot.child("profileImageUrl").getValue() != null){
                                        profileimageurl=dataSnapshot.child("profileImageUrl").getValue().toString();
                                    }
                                }catch (Exception e){}

                            }

                            HistoryObject obj = new HistoryObject(rideId, getDate(finalTimestamp), finalDistance, finalCostTotal, finalStatus_transaksi,profileimageurl);
                            resultsHistory.add(obj);
                            mHistoryAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });





                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void getInformationDriver(final String idDriver) {
        DatabaseReference userCustomer = FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(idDriver);
        userCustomer.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    try{
                        if(dataSnapshot.child("profileImageUrl").getValue() != null){
                            profileimageurl=dataSnapshot.child("profileImageUrl").getValue().toString();


                        }
                    }catch (Exception e){}


                }



            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //Toast.makeText(getApplicationContext(), ""+profileimageurl, Toast.LENGTH_SHORT).show();

    }

}

