package com.example.dailymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.internal.$Gson$Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

public class AddDiaryGroup extends AppCompatActivity {

    EditText diaryName;
    EditText email;
    ImageView emailIV; // 이메일 EditText 상태 표시
    Button addEmailBtn; // 이메일 추가버튼
    ImageView submit; // 완료 버튼

    // 공유하는 계정 수 (디폴트 :0 자기 계정 + 공유 계정 한개)
    //                  공유 계정 추가 시 +1
    int numAddEmail=0;
    Vector<String> emailList = new Vector<String>(); // 이메일 리스트 저장

    private FirebaseFirestore db;
    private DiaryGroup newDG;

    private FirebaseUser user;

    //DiaryGroup 정보 유지
    String curDG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diary_group);
        //DiaryGroup 정보 유지
        curDG=getIntent().getStringExtra("curDG");

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        emailList.add(user.getEmail()); // 현재 로그인된 사용자 emailList에 추가

        submit = (ImageView)findViewById(R.id.editSubmit);
        diaryName= (EditText)findViewById(R.id.edit_diary_name); // 다이어리 이름 입력창
        email=(EditText)findViewById(R.id.edit_email);
        addEmailBtn = findViewById(R.id.btn_add_email);
        emailIV = findViewById(R.id.edit_email_state); // 이메일 입력창 우측 아이콘

        addEmailBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                HideKeyboard(email);
                email.setFocusable(false);  // 입력 완료한 EditText 비활성화
                email.setClickable(false);
                emailIV.setImageResource(R.drawable.ic_baseline_done_24); // 우측 아이콘 done 으로 변경

                String getEdit = email.getText().toString();

                // 빈 값이 넘어올 때
                if(getEdit.getBytes().length<=0){
                    Toast.makeText(getApplicationContext(), "값을 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                // 이메일 입력 값이 있을 때
                if(getEdit.getBytes().length>0){
                    emailList.add(email.getText().toString()); // 추가 버튼 누른 경우 입력한 이메일 emailList에 추가

                    numAddEmail++; // 공유하는 계정 수 +1

                    Toast.makeText(getApplicationContext(), emailList.size()+", "+ emailList.get(numAddEmail), Toast.LENGTH_SHORT).show();

                    LayoutInflater mInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                    LinearLayout mRootLinear =(LinearLayout)findViewById(R.id.linear_root);

                    mInflater.inflate(R.layout.add_email, mRootLinear,true);
                    email = (EditText)findViewById(R.id.edit_email_add);
                    email.setId(numAddEmail);
                    emailIV= findViewById(R.id.edit_email_state_add);
                    emailIV.setId(-numAddEmail);
                }
            }
        });

        //Submit
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getName = diaryName.getText().toString();

                // 디비에 내용 저장
                if(getName.getBytes().length>0 &&emailList.size()>1){
                    // @@@@@@@@ 내용 저장 @@@@@@@@@
                    addNewDiaryGroup(user.getUid(), getName);
                    // @@사용자 공유 다이어리 목록 업데이트
                    updateDGList(user.getUid(), getName);
                    Toast.makeText(getApplicationContext(),"Submit OK", Toast.LENGTH_SHORT).show();
                } else if(getName.getBytes().length<=0 &&emailList.size()>1){
                    Toast.makeText(getApplicationContext(),"다이어리 이름을 적어주세요", Toast.LENGTH_SHORT).show();
                }
                else if(getName.getBytes().length>0 &&emailList.size()<=1){
                    Toast.makeText(getApplicationContext(),"이메일을 적어주세요", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getApplicationContext(),"다이어리 이름, 이메일을 적어주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
        Toast.makeText(AddDiaryGroup.this,"현재 : "+curDG,Toast.LENGTH_LONG).show();
    }


    private void addNewDiaryGroup(String uid, final String name){
        //String shareKey= "share"+uid+"000";
        // 랜덤으로 생기게
        db.collection("DiaryGroupList")
                .document(name)
                .set(new DiaryGroup(emailList))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddDiaryGroup.this,"DiaryGroup Add SUCC",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(),Main.class);
                        intent.putExtra("curDG",name);
                        startActivity(intent); //추가와 동시에 메인페이지로 이동
                    }
                });
    }
    private void updateDGList(String uid,final String name) {
        for (int i = 0; i < emailList.size(); i++) {
            db.collection("UserList").whereEqualTo("email", emailList.get(i))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                document.getReference().update("diaryGroupList", FieldValue.arrayUnion(name));
                                System.out.println("++++"+document.getData().get("email").toString());
                            }
                        }
                    }
                });
        }
    }
    // 가상 키보드 숨기기
    private void HideKeyboard(EditText et) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }
}