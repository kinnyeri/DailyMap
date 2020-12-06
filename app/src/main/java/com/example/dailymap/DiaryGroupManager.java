package com.example.dailymap;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

public class DiaryGroupManager extends AppCompatActivity {
    EditText email;
    ImageView emailIV; // 이메일 EditText 상태 표시
    Button addEmailBtn; // 이메일 추가버튼
    ImageView submit; // 완료 버튼
    TextView name,Email;
    // 공유하는 계정 수 (디폴트 :0 자기 계정 + 공유 계정 한개)
    //                  공유 계정 추가 시 +1
    int numAddEmail=0;
    Vector<String> emailList = new Vector<String>(); // 이메일 리스트 저장
    ListView userListView;
    ImageView manageOk;
    private FirebaseUser user;


    private FirebaseFirestore db;
    String dgKey;
    String userListEmail[];
    Vector<String> userListName;
    ArrayList<HashMap<String,String>> userListData;
    //DiaryGroup 정보 유지
    String curDG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_group_manager);
        //다이어리 정보 유지
        curDG = getIntent().getStringExtra("curDG");
        Log.d("DM","현재 DG : "+curDG);

        userListView=findViewById(R.id.userList);
        manageOk=findViewById(R.id.manageOk);
        userListName = new Vector<String>();
        userListData = new ArrayList<>();

        email=(EditText)findViewById(R.id.get_email);
        addEmailBtn = findViewById(R.id.send_emailList);
        emailIV = findViewById(R.id.change_email_state); // 이메일 입력창 우측 아이콘
        user = FirebaseAuth.getInstance().getCurrentUser();

        db = FirebaseFirestore.getInstance();
        Toast.makeText(this,"현재 "+curDG,Toast.LENGTH_SHORT).show();

        db.collection("DiaryGroupList").document(curDG)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    String list[];
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot document = task.getResult();
                            if(document.exists()){
                                String tmp = document.getData().get("userList").toString();
                                tmp=tmp.replace("[","").replace("]","").replace(" ","");
                                list = tmp.split(",");
                                Log.d("DM","사용자 목록 불러오기 성공");

                            } else{
                                Toast.makeText(DiaryGroupManager.this,"no docs",Toast.LENGTH_LONG).show();
                                list[0]="error";
                            }
                        } else{
                            Toast.makeText(DiaryGroupManager.this,"Task has nothing",Toast.LENGTH_LONG).show();
                        }
                        userListView.setAdapter(new ListAdapter(DiaryGroupManager.this,list));
                    }
                });
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

                    Toast.makeText(getApplicationContext(), emailList.size()+", "+ emailList.get(emailList.size()-1), Toast.LENGTH_SHORT).show();

                    LayoutInflater mInflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
                    LinearLayout mRootLinear =(LinearLayout)findViewById(R.id.linear_root);

                    mInflater.inflate(R.layout.add_email, mRootLinear,true);
                    email = (EditText)findViewById(R.id.edit_email_add);
                    email.setId(emailList.size()-1);
                    emailIV= findViewById(R.id.edit_email_state_add);
                    emailIV.setId(-emailList.size()-1);
                }
            }
        });
        manageOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 디비에 내용 저장
                if(emailList.size()>0){
                    // @@사용자 공유 다이어리 목록 업데이트
                    updateDGList(user.getUid(), curDG);
                    updateDiaryGroup(user.getUid(), curDG);
                    Log.d("DM","새 사용자 등록 완료");

                    Intent intent = new Intent(getApplicationContext(),Account.class);
                    intent.putExtra("curDG",curDG);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent); //추가와 동시에 메인페이지로 이동
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(),"이메일을 적어주세요", Toast.LENGTH_SHORT).show();
                }
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
                                }
                            }
                        }
                    });

        }
    }

    private void updateDiaryGroup(String uid, String name){
        for(int i=0; i<emailList.size();i++){
            db.collection("DiaryGroupList").document(name)
                    .update("userList",FieldValue.arrayUnion(emailList.get(i)));
        }
    }

    public class ListAdapter extends BaseAdapter {
        private Context context;
        private String[] list;

        public ListAdapter(Context c, String[] list){
            context=c;
            this.list=list;
        }

        public int getCount(){
            return list.length;
        }
        public Object getItem(int pos){
            return list[pos];
        }
        public long getItemId(int pos){
            return pos;
        }
        public View getView(int pos,View convertView, ViewGroup parent){
            final TextView textView;
            final String listItem = list[pos];

            if(convertView==null ){
                textView=new TextView(context);
                textView.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
                textView.setPadding(10,10,10,10);
            }else{
                textView =(TextView) convertView;
            }

            textView.setText(list[pos]);

            return textView;
        }
    };
    // 가상 키보드 숨기기
    private void HideKeyboard(EditText et) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }
}