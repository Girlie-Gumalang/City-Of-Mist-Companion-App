package com.jtdev.cs2a_group3;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;


import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;


public class HomeScreen extends AppCompatActivity {

    private ImageButton charactersBtn, rulesBtn, accountBtn;


    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        mAuth = FirebaseAuth.getInstance();

        charactersBtn = findViewById(R.id.characters_btn);
        rulesBtn = findViewById(R.id.rules_btn);
        accountBtn = findViewById(R.id.account_btn);


        if (charactersBtn != null) {
            charactersBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeScreen.this, MainActivity.class);
                    intent.putExtra("FRAGMENT_TO_LOAD", "CHARACTERS");
                    startActivity(intent);
                }
            });
        }

        if (rulesBtn != null) {
            rulesBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeScreen.this, MainActivity.class);
                    intent.putExtra("FRAGMENT_TO_LOAD", "RULES");
                    startActivity(intent);
                }
            });
        }

        if (accountBtn != null) {
            accountBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(HomeScreen.this, MainActivity.class);
                    intent.putExtra("FRAGMENT_TO_LOAD", "ACCOUNT");
                    startActivity(intent);
                }
            });
        }


    }
}