package com.example.hades.garbage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.renderscript.Sampler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.hades.garbage.R;
import com.example.hades.garbage.api.Server;
import com.example.hades.garbage.api.api_service;
import com.example.hades.garbage.model.ResponseModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import net.rimoto.intlphoneinput.IntlPhoneInput;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;



public class Register extends  AppCompatActivity {

    @BindView(R.id.toolbar) Toolbar mtoolbar;
    @BindView(R.id.edtEmail) EditText email;
    @BindView(R.id.edtName) EditText name;
    @BindView(R.id.edtPassword) EditText pass;
    @BindView(R.id.edtAlamat) EditText alamat;
    @BindView(R.id.relative_layout_progress_activity_signup)
    RelativeLayout relativeLayoutProgress;
    @BindView(R.id.my_phone_input)IntlPhoneInput phoneInputView;
    private String profileimageurl;
    private String mEmail;
    private String mPhone;
    private String mPassword;
    private String mName;
    private String malamat;

    private DatabaseReference mDriverDatabase;
    private String userID;
    private Uri resultUri;


    Button btn_send;
    FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        initFirebase();
        phoneInputView.setDefault();
        mtoolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(R.string.register);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        mtoolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setContentView(R.layout.activity_main);
                finish();
            }
        });




    }

    private void initFirebase(){ mAuth= FirebaseAuth.getInstance();}

    @OnClick({R.id.btn_Send})
    public void onClick(Button button){
        switch (button.getId()){
            case R.id.btn_Send:
                mEmail = email.getText().toString().trim();
                mPassword = pass.getText().toString().trim();
                mName=name.getText().toString().trim();
                malamat=alamat.getText().toString().trim();
                if(phoneInputView.isValid()) {
                    mPhone = phoneInputView.getNumber();
                }
                //String hp1 = hp.getText().toString().trim();
                register(mEmail,mPhone,mPassword,mName,malamat);
                //registerMysql(email1,hp1,pass1);

                break;

        }


    }

//    private void registerMysql(String email1,String hp1, String pass1){
//        email1=email.getText().toString();
//        hp1=hp.getText().toString();
//        pass1=pass.getText().toString();
//
//        api_service api= Server.getClient().create(api_service.class);
//        Call<ResponseModel> call=api.daftar(email1,hp1,pass1);
//        call.enqueue(new Callback<ResponseModel>() {
//            @Override
//            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
//
//                Log.d("RETRO","response: "+response.body().toString());
//                String kode=response.body().getKode();
//                String pesan = response.body().getPesan();
//                if(kode.equals("1")){
//
//                }
//                else{
//
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseModel> call, Throwable t) {
//
//                Log.d("RETRO","Failure: "+"Gagal kirim pesan");
//
//
//            }
//
//        });
//    }



    private void register(final String Email, final String Hp, String mPass,final String name,final String Alamat){
            if(TextUtils.isEmpty(name)){
                Toast.makeText(Register.this,"name belum diisi",Toast.LENGTH_SHORT).show();
            }
            else if(TextUtils.isEmpty(Email)){
                Toast.makeText(Register.this,"email belum diisi",Toast.LENGTH_SHORT).show();
            }
            else if(TextUtils.isEmpty(Alamat)){
                Toast.makeText(Register.this,"alamat belum diisi",Toast.LENGTH_SHORT).show();
            }
            else if(TextUtils.isEmpty(mPass)){
                Toast.makeText(Register.this,"password belum diisi",Toast.LENGTH_SHORT).show();
            }
            else if(TextUtils.isEmpty(Hp)){
                Toast.makeText(Register.this,"no hp belum diisi",Toast.LENGTH_SHORT).show();
            }

            else{
                showProgress();
                mAuth.createUserWithEmailAndPassword(mEmail,mPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        hideProgress();
                        if(task.isSuccessful()){
                            String user_id= mAuth.getCurrentUser().getUid();
                            Map userinfo=new HashMap();
                            userinfo.put("name",name);
                            userinfo.put("email",Email);
                            userinfo.put("email",Alamat);
                            userinfo.put("phone",Hp);
                            userinfo.put("saldo",0);
                            userinfo.put("point",0);
                            DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
                            current_user_db.setValue(true);
                            current_user_db.updateChildren(userinfo);
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Register.this);
                            alertDialogBuilder.setTitle("Signup");
                            alertDialogBuilder.setMessage("Your account has been registered. Please sign in use your username and password.");

                            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                    finish();
                                }
                            });
                            alertDialogBuilder.show();
                        }
                        else{
                            task.getException().printStackTrace();
                            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "registered has been failed! Please try again.", Snackbar.LENGTH_INDEFINITE);
                            snackbar.setAction("OK", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    snackbar.dismiss();
                                }
                            });
                            snackbar.setActionTextColor(getResources().getColor(R.color.colorAccent));
                            snackbar.show();

                        }
                    }
                });

            }
    }

    private void hideProgress() {
        relativeLayoutProgress.setVisibility(View.GONE);
    }
    private void showProgress() {
        relativeLayoutProgress.setVisibility(View.VISIBLE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebaseAuthListener);
    }



}
