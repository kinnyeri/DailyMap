package com.example.dailymap;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Vector;

public class ServiceThread extends Thread{
    Handler handler;
    boolean isRun = true;
    private FirebaseFirestore db;
    String curDG = null;
    String uid = null;
    int count =0;
    ListenerRegistration registration[];

    public ServiceThread(Handler handler){
        this.handler = handler;
        db = FirebaseFirestore.getInstance();
    }

    public ServiceThread(Handler handler, String curDG,String uid){
        this.handler = handler;
        this.curDG = curDG;
        this.uid = uid;
        Log.d("NotiAll","uid ST "+uid);

        db = FirebaseFirestore.getInstance();
    }

    public void stopForever(){
        synchronized (this) {
            this.isRun = false;
        }
    }
    public void setCurDG(String curDG,String uid){
        if(!this.curDG.equals(curDG)){
            System.out.println("ServiceTest(remove): "+ this.curDG);
            Log.d("ServiceTest", "REMOVE" + this.curDG);
            for(int i=0;i<registration.length;i++){
                registration[i].remove();
            }

            isRun=false;
            System.out.println("ServiceTest(setCurDG): "+ curDG);
            this.curDG = curDG;
            this.uid=uid;
            Log.d("NotiAll","uid Set"+uid);

            isRun=true;
            ListingDB();
        }
    }

    public void run(){
        System.out.println("ServiceTest(run): "+ curDG);
        ListingDB();
    }
    public void ListingDB(){
        count=0;
        if(isRun&&curDG!=null){
            System.out.println("ServiceTest(Thread): "+ curDG);
            db.collection("UserList").document(uid)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                String list[];
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot data = task.getResult();
                    String tmp =data.getData().get("diaryGroupList").toString();
                    Log.d("NotiAll","tmp "+tmp);
                    tmp = tmp.replace("[","").replace("]","");
                    list =tmp.split(",");
                    for(int i=1;i<list.length;i++){
                        list[i]=list[i].replaceFirst(" ","");
                    }
                    registration=new ListenerRegistration[list.length];
                    for(int i=0;i<registration.length;i++){
                        registration[i] = db.collection("DiaryGroupList").document(list[i])
                                .collection("diaryList")
                                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                                        @Nullable FirebaseFirestoreException e) {

                                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                            switch (dc.getType()) {
                                                case ADDED:
                                                    if(count>0){ // 추가적으로 업데이트되는 부분이 있을 때 알림 울리도록
                                                        handler.sendEmptyMessage(0);
                                                        Log.d("ServiceTest", "ADDED: " + count);
                                                    }
                                                    break;
                                                case MODIFIED:
                                                    Log.d("ServiceTest", "MODIFIED" + dc.getDocument().getData());
                                                    break;
                                                case REMOVED:
                                                    Log.d("ServiceTest", "REMOVED" + dc.getDocument().getData());
                                                    break;
                                            }
                                        }
                                        count++;
                                    }
                                });
                    }

                }
            });
//            registration = db.collection("DiaryGroupList").document(curDG)
//                    .collection("diaryList")
//                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
//                        @Override
//                        public void onEvent(@Nullable QuerySnapshot snapshots,
//                                            @Nullable FirebaseFirestoreException e) {
//
//                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
//                                switch (dc.getType()) {
//                                    case ADDED:
//                                        if(count>0){ // 추가적으로 업데이트되는 부분이 있을 때 알림 울리도록
//                                            handler.sendEmptyMessage(0);
//                                            Log.d("ServiceTest", "ADDED: " + count);
//                                        }
//                                        break;
//                                    case MODIFIED:
//                                        Log.d("ServiceTest", "MODIFIED" + dc.getDocument().getData());
//                                        break;
//                                    case REMOVED:
//                                        Log.d("ServiceTest", "REMOVED" + dc.getDocument().getData());
//                                        break;
//                                }
//                            }
//                            count++;
//                        }
//                    });
        }
    }
}