package com.example.hades.garbage;


import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity  {



    ProgressDialog pd;
    @BindView(R.id.edtPass) EditText pass;
    @BindView(R.id.edtUser) EditText user;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;
    private boolean loggedIn;
    boolean network_enabled = false;
    private android.app.AlertDialog mInternetDialog;
    private LocationManager service;
    private Boolean helpAuth=false;
    private String user_id;


    private BroadcastReceiver mNetworkDetectReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkInternetConnection();

        }
    };


    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void goToDashboard() {
        helpAuth=true;
        Intent intent = new Intent(this, HomeMember.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @OnClick({R.id.btn_Login,R.id.btn_Register})
    public void onClick(Button button){

        if(network_enabled==false) {
            showNoInternetDialog();
        }
        else{
            switch (button.getId()){
                case R.id.btn_Login:
                    final String user1 = user.getText().toString().trim();
                    final String pass1 = pass.getText().toString().trim();
                    login(user1,pass1);

//                    loginMysql(user1,pass1);
                    break;
                case R.id.btn_Register:
                    Intent intent = new Intent(MainActivity.this,Register.class);
                    startActivity(intent);
                    break;


            }

        }

    }

    private void login(final String username,final String password){
        if(TextUtils.isEmpty(username)){
            Toast.makeText(MainActivity.this, "username belum diisi", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(password)){
            Toast.makeText(MainActivity.this, "password belum diisi", Toast.LENGTH_SHORT).show();
        }
        else{
            showProgress();
            mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            hideProgress();
                            if (task.isSuccessful()) {
                                final String user_id1= mAuth.getCurrentUser().getUid();

                                DatabaseReference Driver_db= FirebaseDatabase.getInstance().getReference().child("Users").child("Driver").child(user_id1);
                                Driver_db.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            if(user_id1.equals(dataSnapshot.getKey())){
                                                showMessageBox("Akun anda tidak terdaftar");
                                                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                                firebaseAuth.signOut();
                                            }

                                        }
//
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                                DatabaseReference Admin_db= FirebaseDatabase.getInstance().getReference().child("Users").child("Admin").child(user_id1);
                                Admin_db.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            if(user_id1.equals(dataSnapshot.getKey())){
                                                showMessageBox("Akun anda tidak terdaftar");
                                                FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                                                firebaseAuth.signOut();
                                            }

                                        }
//
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                                DatabaseReference Customer_db= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id1);
                                Customer_db.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()){
                                            if(user_id1.equals(dataSnapshot.getKey())){
                                                goToDashboard();
                                            }
                                        }

                                    }
                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });

                            } else {
                                //  login failed
                                showMessageBox("Login gagal. username atau password anda tidak cocok");
                            }
                        }
                    });

        }
    }


    @OnClick({R.id.txt_phone_login})
    public void onClick(){
        Intent intent = new Intent(MainActivity.this,phone_login.class);
        startActivity(intent);
    }


    @OnClick({R.id.txt_reset})
    public void onClick(TextView mText_reset){
        final String user1 = user.getText().toString().trim();

        if(user1.equals("")){
            showMessageBox("Mohon isikan alamat email anda");
        }
        else{
            FirebaseAuth.getInstance().sendPasswordResetEmail(user1).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        showMessageBox("mohon cek inbox email anda untuk reset password");
                    }
                    else{
                        showMessageBox("email tidak terdaftar ");
                    }
                }
            });

        }

    }






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        service = (LocationManager) getSystemService(LOCATION_SERVICE);
        registerReceiver(mNetworkDetectReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        initFirebase();
        ButterKnife.bind(this);
        loggedIn=isLoggedIn();
        if(loggedIn==true){goToDashboard();}

    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mNetworkDetectReceiver);
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }
    public Boolean isLoggedIn() {
        if (mAuth.getCurrentUser() != null) {

            //user logged in
            return true;

        }
        else{return false;}

    }


    private void showProgress() {
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Loading..");
       // dialog.show();
        user.setEnabled(false);
        pass.setEnabled(false);
    }

    private void showMessageBox(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Login");
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialogBuilder.show();
    }

    private void hideProgress() {
        ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        dialog.hide();
        user.setEnabled(true);
        pass.setEnabled(true);
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
        LinearLayout layout = new LinearLayout(this);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
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

        if (!MainActivity.this.isFinishing()){
            mInternetDialog.setView(layout);
            mInternetDialog.show();


        }
    }
}
