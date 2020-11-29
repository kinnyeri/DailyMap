package com.example.dailymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.SingleLineTransformationMethod;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignIn extends AppCompatActivity {
    //Auth
    private GoogleSignInClient mGoogleSignInClient;
    private final static int RC_SIGN_IN = 123; //any number
    private FirebaseAuth mAuth;

    //CFS
    private FirebaseFirestore db;

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user!=null){
            Toast.makeText(SignIn.this,"user init",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getApplicationContext(),Main.class);
            startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        //CFS
        db = FirebaseFirestore.getInstance(); //Init Firestore

        mAuth=FirebaseAuth.getInstance(); //init Auth
        createRequest(); //init GoogleAuth
        findViewById(R.id.googleSignIn).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                //sign in 버튼 클릭 시
                signIn();
            }
        });

    }
   private void createRequest(){
        //Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        //Build a GoogleSignInClient with the options specified by gso
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);
    }
    private void signIn() { //클릭하면 trigger됨
        Intent signInIntent = mGoogleSignInClient.getSignInIntent(); //어떤 계정을 선택했는지 반환할 것
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) { //
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account); //.getIdToken
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this,e.getMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) { //user 정보가 넘어오면 실행
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(SignIn.this, "success", Toast.LENGTH_SHORT).show();

                            //Database에 저장
                            String key = user.getDisplayName()+"'s diary"; //다이어리 이름으로 document 아이디 설정
                            addNewUser(new User(user.getDisplayName(),user.getEmail()),key,user.getUid());

                            //UI 업데이트
                            Intent intent = new Intent(getApplicationContext(),Main.class);
                            intent.putExtra("curDG",key);
                            startActivity(intent);
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user
                            Toast.makeText(SignIn.this, "sorry auth failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void addNewUser(User tmp,String key,String uid){
        //FST
        tmp.diaryGroupList.add(key);
        db.collection("UserList").document(uid)
                .set(tmp)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        Toast.makeText(SignIn.this,"USER SUCC",Toast.LENGTH_SHORT).show();
                    }
                });
        db.collection("DiaryGroupList").document(tmp.name+"'s diary")
                .set(new DiaryGroup(tmp.email))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void v) {
                        Toast.makeText(SignIn.this,"DG SUCC",Toast.LENGTH_SHORT).show();
                    }
                });
    }
}