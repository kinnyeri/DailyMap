package com.example.dailymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Account extends AppCompatActivity {

    TextView name,logout,dgManager,makeDiaryGroup_btn;
    Spinner spinner;
    private FirebaseAuth mAuth;
    FirebaseFirestore db;
    //DiaryGroup 정보 유지
    String curDG;
    ImageView listSubmit;
    boolean changed = false;
    @Override
    protected void onStart() {
        super.onStart();
        //DiaryGroup 정보 유지
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        name=findViewById(R.id.nameText);
        logout=findViewById(R.id.googleSignOut);
        dgManager=findViewById(R.id.diaryGroupManager);
        spinner=findViewById(R.id.spinner);
        listSubmit=findViewById(R.id.listSubmit);
        makeDiaryGroup_btn=findViewById(R.id.makeDiaryGroup_btn);

        db = FirebaseFirestore.getInstance();

        //DiaryGroup 정보 유지
        curDG = getIntent().getStringExtra("curDG"); //main 받아온거 사용

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(signInAccount!=null){
            name.setText(signInAccount.getDisplayName());
        }
        String uid = FirebaseAuth.getInstance().getUid();

        db.collection("UserList").document(uid)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    String list[];
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        String tmp = document.getData().get("diaryGroupList").toString();
                        tmp = tmp.replace("[","").replace("]","");
                        list =tmp.split(",");
                        for(int i=1;i<list.length;i++){
                            list[i]=list[i].replaceFirst(" ","");
                        }
                    }else{
                        Toast.makeText(Account.this,"no docs",Toast.LENGTH_LONG).show();
                    }
                } else{
                    Toast.makeText(Account.this,"no tasks",Toast.LENGTH_LONG).show();

                }
                ArrayAdapter spinAdapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_spinner_dropdown_item,list);
                spinner.setAdapter(spinAdapter);
                int pos = spinAdapter.getPosition(curDG);
                spinner.setSelection(pos);
            }
        });
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selected = adapterView.getItemAtPosition(i).toString();
                curDG = selected;
                //curDG= curDG.replaceFirst(" ","");
                //startActivity(intent);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        logout.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(Account.this,"SignOut Start",Toast.LENGTH_SHORT).show();
                FirebaseAuth.getInstance().signOut(); //Firebase logout
                Intent intent = new Intent(getApplicationContext(),SignIn.class);
                startActivity(intent);
            }
        });
        dgManager.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Account.this,DiaryGroupManager.class);
                intent.putExtra("curDG",curDG);
                System.out.println("dgmanager++++++++++++++++++++++++++++++++++"+curDG);
                startActivity(intent);
            }
        });
        listSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Account.this,Main.class);
                intent.putExtra("curDG",curDG);
                startActivity(intent);
                finish();
            }
        });
        makeDiaryGroup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Account.this,AddDiaryGroup.class);
                intent.putExtra("curDG",curDG);
                startActivity(intent);
            }
        });
        Toast.makeText(Account.this,"현재 : "+curDG,Toast.LENGTH_LONG).show();

    }
}