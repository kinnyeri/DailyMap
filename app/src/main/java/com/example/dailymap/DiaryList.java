package com.example.dailymap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class DiaryList extends AppCompatActivity {
    int[] thumbnails = {R.drawable.gallery,R.drawable.gallery,R.drawable.gallery,
            R.drawable.gallery,R.drawable.jeju00};

    public Vector<Map<String,Object>> strImg;
    GridView gv;
    ImageView iv;
    DisplayMetrics metrics;
    private FirebaseFirestore db;
    //CST
    private FirebaseStorage storage;
    private StorageReference storageRef;
    String dgKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_list);

        System.out.println("onCreate--------------------------------------");
        strImg = new Vector<Map<String,Object>>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        dgKey = user.getUid()+"000";
        db = FirebaseFirestore.getInstance(); //Init Firestore
        Toast.makeText(DiaryList.this,strImg.size()+" ?",Toast.LENGTH_SHORT).show();

        gv = (GridView) findViewById(R.id.gridList);
        db.collection("DiaryGroupList").document(dgKey)
                .collection("diaryList")
                .whereEqualTo("feel",0)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    ArrayList<Map<String,Object>> imgLists = new ArrayList<Map<String, Object>>();
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                imgLists.add(document.getData());
                                Toast.makeText(DiaryList.this,"Succ",Toast.LENGTH_SHORT).show();
                            }

                            gv.setAdapter(new ImgAdapter(DiaryList.this,imgLists));
                        } else {
                            Toast.makeText(DiaryList.this,"Failed to get img",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        iv=(ImageView)findViewById(R.id.temp);

        //Storage
        storage=FirebaseStorage.getInstance("gs://daily-map-d47b1.appspot.com");
        storageRef = storage.getReference();

        metrics= new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
    }

    public class ImgAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<Map<String,Object>> list;

        public ImgAdapter(Context c, ArrayList<Map<String,Object>> list){
            context=c;
            this.list=list;
        }

        public int getCount(){
            return list.size();
        }
        public Object getItem(int pos){
            return list.get(pos);
        }
        public long getItemId(int pos){
            return pos;
        }
        public View getView(int pos,View convertView, ViewGroup parent){
            int rowWidth = (metrics.widthPixels)/3;
            final ImageView imageView;
            final Map<String,Object> listItem = list.get(pos);

            if(convertView==null ){
                imageView=new ImageView(context);
                imageView.setLayoutParams(new GridView.LayoutParams(rowWidth,rowWidth));
                imageView.setPadding(1,1,1,1);
            }else{
                imageView =(ImageView)convertView;
            }

            storageRef.child(dgKey+"/"+listItem.get("img").toString())
                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        ImageView tmpIV=imageView;
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(DiaryList.this)
                            .load(uri)
                            .into(tmpIV);
                    Toast.makeText(DiaryList.this,"iv updated !!!!!!!!!!!!!!!!!",Toast.LENGTH_LONG).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println(e);
                    Toast.makeText(DiaryList.this,"iv failed !!!!!!!!!!!!!!",Toast.LENGTH_LONG).show();
                }
            });
//            Glide.with(context)
//                    .load(thumbnails[pos])
//                    .into(imageView);

            imageView.setOnClickListener(new View.OnClickListener() {
                Map<String,Object> tmpList = listItem;
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(DiaryList.this,Diary.class);
                    intent.putExtra("writter",tmpList.get("writter").toString());
                    intent.putExtra("locationX",tmpList.get("locationX").toString());
                    intent.putExtra("locationY",tmpList.get("locationY").toString());
                    intent.putExtra("feel",tmpList.get("feel").toString());
                    intent.putExtra("img",tmpList.get("img").toString());
                    intent.putExtra("date",tmpList.get("date").toString());
                    intent.putExtra("content",tmpList.get("content").toString());
                    startActivity(intent);
                }
            });

            return imageView;
        }
    };
}