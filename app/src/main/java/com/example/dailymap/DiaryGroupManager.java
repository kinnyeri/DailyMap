package com.example.dailymap;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

public class DiaryGroupManager extends AppCompatActivity {

    TextView name,Email;
    ImageView manageOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary_group_manager);

        name=findViewById(R.id.nameText);
        Email=findViewById(R.id.editTextEmailAddress);
        manageOk=findViewById(R.id.manageOk);

        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(signInAccount!=null){
            name.setText(signInAccount.getDisplayName());
            Email.setText(signInAccount.getEmail());
        }

        manageOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),"Manage OK", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(DiaryGroupManager.this,Account.class);
                startActivity(intent);
            }
        });
    }
}