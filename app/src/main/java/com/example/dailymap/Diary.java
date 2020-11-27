package com.example.dailymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Map;

public class Diary extends AppCompatActivity {
    ImageView img;
    TextView loc;
    ImageView feel;
    TextView date;
    TextView content;

    int []feelThumbs ={R.drawable.good,R.drawable.mid,R.drawable.bad};
    //FS
    private FirebaseFirestore db;
    //ST
    private FirebaseStorage storage;
    private StorageReference storageRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
        //CST
        storage= FirebaseStorage.getInstance("gs://daily-map-d47b1.appspot.com");
        storageRef = storage.getReference();

        // view
        img=(ImageView) findViewById(R.id.diaryImg);
        loc=(TextView)findViewById(R.id.diaryLoc);
        feel=(ImageView)findViewById(R.id.diaryFeel);
        date=(TextView)findViewById(R.id.diaryDate);
        content=(TextView)findViewById(R.id.diaryContent);

        //Intent
        Intent intent = getIntent();
        loc.setText(intent.getExtras().getString("locationX")+", "+intent.getExtras().getString("locationY").toString());
        switch (intent.getExtras().getString("feel")){
            case "0":
                feel.setImageResource(feelThumbs[0]); break;
            case "1":
                feel.setImageResource(feelThumbs[1]); break;
            case "2":
                feel.setImageResource(feelThumbs[2]); break;
        }
        String tmpDate =intent.getExtras().getString("date"); //date 나중에
        content.setText(intent.getExtras().getString("content"));

        db = FirebaseFirestore.getInstance(); //Init Firestore
        //이미지 올리기
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String dgKey = user.getUid()+"000";
        storageRef.child(dgKey+"/"+intent.getExtras().getString("img"))
                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            ImageView tmpIV=img;
            @Override
            public void onSuccess(Uri uri) {
                Glide.with(Diary.this)
                        .load(uri)
                        .into(tmpIV);
                Toast.makeText(Diary.this,"iv updated @@@@",Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e);
                Toast.makeText(Diary.this,"iv failed !@@@",Toast.LENGTH_LONG).show();
            }
        });

//        db.collection("DiaryGroupList").document(dgKey)
//                .collection("diaryList")
//                .whereEqualTo("feel", 0)
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                //DiaryDS tmp = document.getData();
//                                //String tmp = document.getData().;
//                                Map<String,Object> tmp = document.getData();
//                                String writter = tmp.get("writter").toString();
//                                String date = tmp.get("date").toString();
//                                loc.setText(tmp.get("locationX").toString()+", "+tmp.get("locationY").toString());
//                                date= date.substring(0,4)+"/"+date.substring(4,6)+"/"+date.substring(6,8); //완벽하다
//                                Toast.makeText(Diary.this,date,Toast.LENGTH_SHORT).show();
//                            }
//                        } else {
//                            Toast.makeText(Diary.this,"Failed",Toast.LENGTH_SHORT).show();
//                        }
//                    }
//                });
//        db.collection("DiaryGroupList").document(dgKey).collection("diaryList")
//                .document()
//                .get()
//                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//                    @Override
//                    public void onSuccess(DocumentSnapshot documentSnapshot) {
//                        Map<String, Object> forms = documentSnapshot.get();
//                        for (Map.Entry<String, Object> form: forms.entrySet()) {
//                            String key = (String) form.getKey();
//                            Map<Object, Object> values = (Map<Object, Object>)form.getValue();
//                            String name = (String) values.get("writter");
//                            Toast.makeText(Diary.this,document.getData().toString(),Toast.LENGTH_SHORT).show();
//
//                        }
//                    }
//                });
//        DocumentReference docRef = db.collection("DiaryGroupList").document("dgKey")
//                .collection("diaryList").document(dgKey+"00");
//        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
//            @Override
//            public void onSuccess(DocumentSnapshot documentSnapshot) {
//                DiaryDS tmp = documentSnapshot.toObject(DiaryDS.class);
//                Toast.makeText(Diary.this,tmp.getContent().toString(),Toast.LENGTH_SHORT).show();
//            }
//        });

    }
}