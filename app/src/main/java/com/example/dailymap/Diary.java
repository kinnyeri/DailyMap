package com.example.dailymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Map;

public class Diary extends AppCompatActivity {
    private TextView loc;
    //CFS
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);

        // 장소
        loc=(TextView)findViewById(R.id.diaryLoc);
        db = FirebaseFirestore.getInstance(); //Init Firestore
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String dgKey = user.getUid()+"000";
        db.collection("DiaryGroupList").document(dgKey)
                .collection("diaryList")
                .whereEqualTo("feel", 0)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                //DiaryDS tmp = document.getData();
                                //String tmp = document.getData().;
                                Map<String,Object> tmp = document.getData();
                                String writter = tmp.get("writter").toString();
                                String date = tmp.get("date").toString();
                                loc.setText(tmp.get("locationX").toString()+", "+tmp.get("locationY").toString());
                                date= date.substring(0,4)+"/"+date.substring(4,6)+"/"+date.substring(6,8); //완벽하다
                                Toast.makeText(Diary.this,date,Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(Diary.this,"Failed",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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