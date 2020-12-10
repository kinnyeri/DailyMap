package com.example.dailymap;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

public class ServiceThread extends Thread{
    Handler handler;
    boolean isRun = true;
    private FirebaseFirestore db;
    String curDG = null;
    int count =0;

    public ServiceThread(Handler handler){
        this.handler = handler;
        db = FirebaseFirestore.getInstance();
    }

    public ServiceThread(Handler handler, String curDG){
        this.handler = handler;
        this.curDG = curDG;
        db = FirebaseFirestore.getInstance();
    }

    public void stopForever(){
        synchronized (this) {
            this.isRun = false;
        }
    }
    public void setCurDG(String curDG){
        isRun=false;
        this.curDG = curDG;
        isRun=true;
    }

    public void run(){
        if(isRun){
            // DB 리스너 - "20년 잘가" 다이어리 상태 리스닝 >> curDG로 변경 필요
            System.out.println("ServiceTest(Thread): "+ curDG);
            db.collection("DiaryGroupList").document("20년 잘가")
                    .collection("diaryList")
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot snapshots,
                                            @Nullable FirebaseFirestoreException e) {

                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                switch (dc.getType()) {
                                    case ADDED:
                                        Log.d("ServiceTest", "ADDED" + dc.getDocument().getData());
                                        Log.d("ServiceTest", "ADDED" + count);
                                        if(count>0){ // 추가적으로 업데이트되는 부분이 있을 때 알림 울리도록
                                            Log.d("ServiceTest", "ADDED" + count);
                                            handler.sendEmptyMessage(0);//쓰레드에 있는 핸들러에게 메세지를 보냄
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
}