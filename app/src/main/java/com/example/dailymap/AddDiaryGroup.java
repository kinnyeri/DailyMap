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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.internal.$Gson$Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

public class AddDiaryGroup extends AppCompatActivity {

    EditText diaryName;
    EditText email;

    // 이메일 추가버튼
    Button addEmailBtn;

    // 완료 버튼
    ImageView submit;

    // 공유하는 계정 수 (디폴트 :1 자기 계정)
    int numEmail=1;
    Vector<String> emailList = new Vector<String>(); // 이메일 리스트 저장

    private FirebaseFirestore db;
    private DiaryGroup newDG;

    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_diary_group);

        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        emailList.add(user.getEmail()); // 현재 로그인된 사용자 emailList에 추가

        submit = (ImageView)findViewById(R.id.editSubmit);
        diaryName= (EditText)findViewById(R.id.edit_diary_name); // 다이어리 이름 입력창
        email=(EditText)findViewById(R.id.edit_email);
        addEmailBtn = findViewById(R.id.btn_add_email);

        addEmailBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                HideKeyboard(email);

                String getEdit = email.getText().toString();

                // 빈 값이 넘어올 때
                if(getEdit.getBytes().length<=0){
                    Toast.makeText(getApplicationContext(), "값을 입력하세요.", Toast.LENGTH_SHORT).show();
                }
                // 이메일 입력 값이 있을 때
                if(getEdit.getBytes().length>0){
                    emailList.add(email.getText().toString()); // 추가 버튼 누른 경우 입력한 이메일 emailList에 추가

                    numEmail++; // 공유하는 계정 수 +1

                    // emailList의 인덱스는 0부터 numEmail-1
                    Toast.makeText(getApplicationContext(), emailList.size()+", "+ emailList.get(numEmail-1), Toast.LENGTH_SHORT).show();

                    LayoutInflater mInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                    LinearLayout mRootLinear =(LinearLayout)findViewById(R.id.linear_root);

                    mInflater.inflate(R.layout.add_email, mRootLinear,true);
                    email = (EditText)findViewById(R.id.edit_email_add);
                    email.setId(numEmail);

                    //addEmailBtn = (Button)findViewById(R.id.btn_email_add);
                    //Button add_email = (Button)addLayout.findViewById(R.id.btn_add_email);
                    //mRootLinear.addView(addLayout);
                    //Toast.makeText(getApplicationContext(),"이메일 추가 버튼"+add_email.getId(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        //Submit
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String getName = diaryName.getText().toString();
                Toast.makeText(getApplicationContext(),getName, Toast.LENGTH_SHORT).show();
                //Toast.makeText(getApplicationContext(), "SUBMIT"+emailList.size()+", "+ emailList.get(numEmail), Toast.LENGTH_SHORT).show();
                System.out.println("NAME: "+getName);
                System.out.println("SUBMIT: "+emailList.size()+", "+ emailList.get(numEmail-1)); // 인덱스는 0부터 시작하므로 접근할 때는 numEmail-1

                // 디비에 내용 저장
                if(getName.getBytes().length>0 &&emailList.size()>0){

                    // @@@@@@@@ 내용 저장 @@@@@@@@@
                    addNewDiaryGroup(user.getUid(), getName);

                    Toast.makeText(getApplicationContext(),"Submit OK", Toast.LENGTH_SHORT).show();
                } else{
                    Toast.makeText(getApplicationContext(),"이메일을 적어주세요", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addNewDiaryGroup(String uid, String name){
        //String shareKey= "share"+uid+"000";
        // 랜덤으로 생기게
        db.collection("DiaryGroupList")
                .document()
                .set(new DiaryGroup(name,emailList))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(AddDiaryGroup.this,"DiaryGroup Add SUCC",Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(),Main.class);
                        startActivity(intent); //추가와 동시에 메인페이지로 이동
                    }
                });
    }

    // 가상 키보드 숨기기
    private void HideKeyboard(EditText et) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }
}