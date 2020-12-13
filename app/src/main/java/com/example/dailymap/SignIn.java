package com.example.dailymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.SingleLineTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
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
    String key;
    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user!=null){
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
                Log.d("DM","sign in btn 누름");
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
                Log.d("DM","onActivityResult");

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
                            Toast.makeText(SignIn.this, "로그인 완료", Toast.LENGTH_SHORT).show();
                            //Database에 저장
                            key = user.getDisplayName()+"'s diary"; //다이어리 이름으로 document 아이디 설정
                            Thread addUser = new Thread("add User"){
                                FirebaseUser user = mAuth.getCurrentUser();
                                @Override
                                public void run() {
                                    super.run();
                                    Log.d("DM","스레드 시작 singin");
                                    addNewUser(new User(user.getDisplayName(),user.getEmail()),key,user.getUid());

                                    Log.d("DM","스레드 시작 singin");

                                }
                            };
                            addUser.start();
                            Thread finishSignIn = new Thread("finish Sign in"){
                                @Override
                                public void run() {
                                    super.run();
                                    Log.d("DM","스레드 시작 finish");

                                    Log.d("DM","로그인 성공");
                                    startMain();
                                }
                            };
                            try{
                                Thread.sleep(5000);
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            finishSignIn.start();

                        } else {
                            // If sign in fails, display a message to the user
                            Toast.makeText(SignIn.this, "로그인 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void addNewUser(final User tmp,final String key,String uid){
        //FST
        tmp.diaryGroupList.add(key);

        Log.d("DM","사용자 추가 함수 시작 "+key);
        db.collection("UserList")
                .document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        Log.d("DM","해당 다이어리 없음");
                        if(!document.exists()){ //!document.exists()
                            Log.d("DM","사용자 존재 하지 않음");
                            document.getReference().set(tmp);
                            db.collection("DiaryGroupList").document(key)
                                    .set(new DiaryGroup(tmp.email))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void v) {
                                            Log.d("DM","사용자 정보 저장 성공");
                                        }
                                    });
                        }else{
                            Log.d("DM","사용자 존재함");
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();

            }
        });
    }

    void startMain(){
        //UI 업데이트
        Intent intent = new Intent(getApplicationContext(),Main.class);
        intent.putExtra("curDG",key);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}