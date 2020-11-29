package com.example.dailymap;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

public class DiaryGroupManager extends AppCompatActivity {

    TextView name,Email;

    ListView userListView;
    ImageView manageOk;

    private FirebaseFirestore db;
    String dgKey;
    String userListEmail[];
    Vector<String> userListName;
    ArrayList<HashMap<String,String>> userListData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_group_manager);

        userListView=findViewById(R.id.userList);
        manageOk=findViewById(R.id.manageOk);
        userListName = new Vector<String>();
        userListData = new ArrayList<>();

        dgKey = FirebaseAuth.getInstance().getCurrentUser().getUid()+"000";
        db = FirebaseFirestore.getInstance();
        db.collection("DiaryGroupList").document(dgKey)
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
                                Toast.makeText(DiaryGroupManager.this,list.length+"",Toast.LENGTH_LONG).show();
                            } else{
                                Toast.makeText(DiaryGroupManager.this,"no docs",Toast.LENGTH_LONG).show();
                            }
                        } else{
                            Toast.makeText(DiaryGroupManager.this,"Task has nothing",Toast.LENGTH_LONG).show();
                        }
                        userListView.setAdapter(new ListAdapter(DiaryGroupManager.this,list));
                    }
                });
        manageOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Manage OK", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DiaryGroupManager.this,Account.class);
                startActivity(intent);
            }
        });
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
}