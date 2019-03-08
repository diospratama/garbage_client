package com.example.hades.garbage;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;

public class wallet extends AppCompatActivity {


    private TextView mSaldo;
    private TextView mPoint;
    private Button mTukar;
    private String userID;
    private android.app.AlertDialog mInternetDialog;
    private int saldo_perpoint,point,hasilPoint,hasilSaldo,hasilSaldo1,sisaPoint1;
    private  boolean bantu=false;
    private android.app.AlertDialog Dialog;
    @BindView(R.id.toolbar) Toolbar mToolbar;


    private FirebaseAuth mAuth;
    private DatabaseReference mDriverDatabase,mdriverCost,dataRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        mToolbar=(Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.title_wallet);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mSaldo=(TextView) findViewById(R.id.Saldo);
        mPoint=(TextView) findViewById(R.id.Point);
        mTukar=(Button) findViewById(R.id.Tukar);

        mDriverDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child("Customers").child(userID);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                setContentView(R.layout.activity_home_member);
                finish();

            }
        });

        mTukar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDriverDatabase.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()&& dataSnapshot.getChildrenCount()>0){
                            Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                            int bantu1=0;

                            if(map.get("point")!=null){
                                int getPoint=Integer.parseInt(map.get("point").toString());

                                if(getPoint==0&&bantu1==0){
                                    messageDialog("Point Anda 0");

                                }
                                if(getPoint<point&&getPoint!=0&&bantu1==0){
                                    messageDialog("Point Anda kurang dari: "+point);
                                }

                                 if(bantu==false){
                                     //hasilSaldo= berisi point saat ini di database
                                     bantu1=1;
                                     if(getPoint%point==0&&getPoint!=0&&getPoint>=point){
                                         sisaPoint1=getPoint%point;
                                         int Point1=getPoint/point;
                                         hasilPoint=Point1*saldo_perpoint;
                                         hasilSaldo1=hasilSaldo+hasilPoint;
                                         //Toast.makeText(wallet.this,"MOD==0: "+hasilSaldo1,Toast.LENGTH_SHORT).show();

                                     }
                                     if(getPoint%point!=0&&getPoint!=0&&getPoint>=point) {
                                         sisaPoint1 = getPoint % point;
                                         int point_ditukar = getPoint - sisaPoint1;
                                         point_ditukar = point_ditukar / point;
                                         //Toast.makeText(wallet.this,"sisa point: "+point_ditukar,Toast.LENGTH_SHORT).show();
                                         hasilPoint = point_ditukar * saldo_perpoint;//
                                         hasilSaldo1 = hasilSaldo + hasilPoint;
                                         //Toast.makeText(wallet.this,"MOD!=0: "+hasilSaldo1,Toast.LENGTH_SHORT).show();
                                     }


                                 }



                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                getChangePoint();
            }
        });



        setValuePointandSaldo();
        setSaldo();


    }






    private void setSaldo(){


        mDriverDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("saldo")!=null){
                        mSaldo.setText("Rp. "+map.get("saldo").toString());
                        hasilSaldo=Integer.parseInt(map.get("saldo").toString());
                    }
                    if(map.get("point")!=null){
                        mPoint.setText(map.get("point").toString()+"pt");

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void setValuePointandSaldo(){
         mdriverCost= FirebaseDatabase.getInstance().getReference().child("Cost").child("Customers");
         mdriverCost.addValueEventListener(new ValueEventListener() {
             @Override
             public void onDataChange(DataSnapshot dataSnapshot) {
                 if(dataSnapshot.exists()&&dataSnapshot.getChildrenCount()>0){
                     Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                     if(map.get("point_customers")!=null){
                            point= Integer.parseInt(map.get("point_customers").toString());
                     }
                     if(map.get("cost_customers")!=null){
                         saldo_perpoint= Integer.parseInt(map.get("cost_customers").toString());
                     }

                 }

             }

             @Override
             public void onCancelled(DatabaseError databaseError) {

             }
         });

    }

    private void messageDialog(String kata){
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(wallet.this);
        builder.setTitle("Wallet");
        builder.setMessage(kata);
        mInternetDialog = builder.create();
        if (!wallet.this.isFinishing()){

            mInternetDialog.show();
        }

    }


    private void getChangePoint(){

        Map getData=new HashMap();
        if(sisaPoint1!=0||hasilSaldo1!=0){
            if(sisaPoint1!=0){
                getData.put("saldo",hasilSaldo1);
                getData.put("point",sisaPoint1);
                mDriverDatabase.updateChildren(getData);
                getData.clear();
                bantu=true;
            }
            if(sisaPoint1==0){
                getData.put("saldo",hasilSaldo1);
                getData.put("point",sisaPoint1);
                mDriverDatabase.updateChildren(getData);
                getData.clear();
                bantu=true;

            }

        }

    }
}
