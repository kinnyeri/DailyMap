package com.example.dailymap;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class AlarmService extends Service {
    public AlarmService() {
    }

    NotificationManager Notifi_M;
    ServiceThread thread=null;
    Notification Notifi ;
    NotificationCompat.Builder builder;
    NotificationManager manager;
    String curDG,uid;

    private static String CHANNEL_ID = "channel1";
    private static String CHANEL_NAME = "Channel1";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("ServiceTest(onStartCommand): "+ curDG);
        curDG = intent.getStringExtra("curDG");
        uid=intent.getStringExtra("uid");
        Log.d("NotiAll","uid  "+uid);
        System.out.println("ServiceTest(onStartCommand): "+ curDG);

        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler handler = new myServiceHandler();
        if(thread==null){
            thread = new ServiceThread(handler,curDG,uid);
            System.out.println("ServiceTest(Service): "+ curDG);
            //thread.setCurDG(curDG);
            thread.start();
        }
        else{
            System.out.println("ServiceTest(onStartCommand->setCurDG): "+ curDG);
            thread.setCurDG(curDG,uid);
        }
        return START_STICKY;
    }

    //서비스가 종료될 때 할 작업

    public void onDestroy() {
        thread.stopForever();
        thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
    }

    class myServiceHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);

            Bundle bundle = msg.getData();
            curDG = bundle.getString("curDG");

            Intent intent = new Intent(AlarmService.this, Main.class);
            intent.putExtra("curDG",curDG);
            PendingIntent pendingIntent = PendingIntent.getActivity(AlarmService.this, 0, intent,PendingIntent.FLAG_UPDATE_CURRENT);

            builder = null;
            manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE); //버전 오레오 이상일 경우
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel( new NotificationChannel(CHANNEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_HIGH) ); ///
                builder = new NotificationCompat.Builder(getApplicationContext(),CHANNEL_ID);
                //하위 버전일 경우
            }
            else{
                builder = new NotificationCompat.Builder(getApplicationContext());
            }


            //알림창 제목
            builder.setContentTitle("Daily Map");

            //알림창 메시지
            builder.setContentText(curDG+"에 다이어리가 추가되었습니다.");

            //알림창 아이콘
            builder.setSmallIcon(R.drawable.ic_pin_dm_green_32);

            //알림창 터치시 상단 알림상태창에서 알림이 자동으로 삭제되게 합니다.
            builder.setAutoCancel(true);

            //head up settings
            builder.setPriority(Notification.PRIORITY_HIGH); //headup
            builder.setTimeoutAfter(3500);

            //pendingIntent를 builder에 설정 해줍니다.
            // 알림창 터치시 인텐트가 전달할 수 있도록 해줍니다.
            //builder.setContentIntent(pendingIntent);
            builder.setFullScreenIntent(pendingIntent,true); //head up settings

            Notification notification = builder.build();

            //알림창 실행
            manager.notify(1,notification);
        }
    };
}
