package com.example.hades.garbage;

import android.content.Intent;
import android.nfc.Tag;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.rimoto.intlphoneinput.IntlPhoneInput;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;

public class phone_login extends AppCompatActivity {
    private DatabaseReference dataReference;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private FirebaseAuth mAuth;
    EditText codeed;
    FloatingActionButton fabbutton;
    String mVerificationId;
    TextView timertext;
    Timer timer;
    ImageView verifiedimg;
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    Boolean mVerified = false;
    private Boolean bantu_setProfile=false;
    int bantu=0;

    private IntlPhoneInput phoneInputView;
    private  String phoneed;








    @BindView(R.id.toolbar) Toolbar mtoolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);
        codeed=(EditText) findViewById(R.id.edtKode);
        fabbutton = (FloatingActionButton) findViewById(R.id.btn_SignIn);
        timertext=(TextView) findViewById (R.id.txt_timer);
        verifiedimg = (ImageView) findViewById(R.id.verifiedsign);
        phoneInputView = (IntlPhoneInput) findViewById(R.id.my_phone_input);
        phoneInputView.setDefault();



        FirebaseApp.initializeApp(this);
        mAuth=FirebaseAuth.getInstance();
        mtoolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mtoolbar);
        getSupportActionBar().setTitle(R.string.signin);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mtoolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                setContentView(R.layout.activity_main);
                finish();
            }
        });
        mCallbacks=new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                Log.d("TAG", "onVerificationCompleted:" + credential);

                signInWithPhoneAuthCredential(credential);
                //linkCredential(credential);



            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                Log.w("TAG", "onVerificationFailed", e);

                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(phone_login.this,"Verification Failed !! Invalied verification Code",Toast.LENGTH_SHORT).show();

                }
                else if (e instanceof FirebaseTooManyRequestsException) {
                    Toast.makeText(phone_login.this,"Verification Failed !! Too many request. Try after some time. ",Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d("TAG", "onCodeSent:" + verificationId);

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId;
                mResendToken = token;
            }
        };
        fabbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fabbutton.getTag().equals(getResources().getString(R.string.tag_send))) {
                    try {
                        if (!phoneInputView.getNumber().toString().trim().isEmpty() && phoneInputView.getNumber().toString().trim().length() >= 10) {
                            if(phoneInputView.isValid()) {
                                phoneed = phoneInputView.getNumber();
                            }
                            startPhoneNumberVerification(phoneed.toString().trim());
                            starttimer();
                            codeed.setVisibility(View.VISIBLE);
                            fabbutton.setImageResource(R.drawable.ic_forward);
                            mVerified = false;
                            fabbutton.setTag(getResources().getString(R.string.tag_verify));
                            bantu=1;

                        }

                    }catch (Exception e){
                        Toast.makeText(phone_login.this,"please enter valid mobile number",Toast.LENGTH_SHORT).show();
                    }

                }

                if(bantu==1){
                    //mSignIn.setText("next");
                    if (!codeed.getText().toString().trim().isEmpty() && !mVerified) {
                        Toast.makeText(phone_login.this,"Please wait...",Toast.LENGTH_SHORT).show();
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, codeed.getText().toString().trim());
                        signInWithPhoneAuthCredential(credential);

                        //linkCredential(credential);

                        bantu=0;
                    }
//                    if(mVerified){
//                        startActivity(new Intent(phone_login.this, HomeMember.class));
//                        bantu=0;
//
//                    }

                }


            }
        });

        timertext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              if (timertext.getText().equals("RESEND CODE")) {
                    resendVerificationCode(phoneed.toString().trim(), mResendToken);
                    mVerified = false;
                    starttimer();
                    codeed.setVisibility(View.VISIBLE);
                    fabbutton.setImageResource(R.drawable.ic_forward);
                    fabbutton.setTag(getResources().getString(R.string.tag_verify));
                    Toast.makeText(phone_login.this,"Resending verification code...",Toast.LENGTH_SHORT).show();

                }
            }
        });



    }


    public void linkCredential(AuthCredential credential) {
        mAuth.getCurrentUser().linkWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("TAG", "linkWithCredential:success");
                            FirebaseUser user = task.getResult().getUser();
                            Toast.makeText(phone_login.this, "Merged", Toast.LENGTH_SHORT).show();
                            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
                            firebaseAuth.signOut();
                            Intent intent = new Intent(phone_login.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            //moveToHome();

                        } else {
                            Log.w("TAG", "linkWithCredential:failure", task.getException());
                            Toast.makeText(phone_login.this, "Failed to merge" + task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }




    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            mVerified = true;
                            timer.cancel();
                            verifiedimg.setVisibility(View.VISIBLE);
                            timertext.setVisibility(View.INVISIBLE);
                            phoneInputView.setEnabled(false);
                            codeed.setVisibility(View.INVISIBLE);
                            Toast.makeText(phone_login.this,"Successfully Verified",Toast.LENGTH_SHORT).show();

                            String user_id= mAuth.getCurrentUser().getUid();
                            if(user_id.equals(null)){
                                Toast.makeText(phone_login.this,"No handphone sudah terdaftar",Toast.LENGTH_SHORT).show();

                            }
                            else {
                                setProfile(phoneed);
                                startActivity(new Intent(phone_login.this, HomeMember.class));
                                finish();
                            }
//                            DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
//                            current_user_db.addValueEventListener(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot dataSnapshot) {
//
//                                        try{
//                                            bantu_setProfile=false;
//                                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
//
//
//
////                                            if(map.get("phone").equals(phoneed)&& bantu_setProfile==true){
////
////
////                                                startActivity(new Intent(phone_login.this, HomeMember.class));
////
////                                            }
//
//
//
//                                        }catch (Exception e){
//                                            Toast.makeText(phone_login.this,""+e,Toast.LENGTH_SHORT).show();
//                                        }
//
//
//                                 }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//
//                                }
//
//                            });




                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                Toast.makeText(phone_login.this,"Invalid OTP ! Please enter correct OTP",Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                });
    }

    private void startPhoneNumberVerification(String phoneNumber) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

    }


    public void starttimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {

            int second = 60;

            @Override
            public void run() {
                if (second <= 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timertext.setText("RESEND CODE");
                            timer.cancel();
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            timertext.setText("00:" + second--);
                        }
                    });
                }

            }
        }, 0, 1000);
    }

    private void resendVerificationCode(String phoneNumber,
                                        PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks,         // OnVerificationStateChangedCallbacks
                token);             // ForceResendingToken from callbacks
    }

    private void setProfile(String phone){

        String user_id= mAuth.getCurrentUser().getUid();
        DatabaseReference current_user_db= FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(user_id);
        Map profil=new HashMap();
        profil.put("phone",phone);
        profil.put("saldo",0);
        profil.put("point",0);

        current_user_db.setValue(true);
        current_user_db.updateChildren(profil);
    }

}

