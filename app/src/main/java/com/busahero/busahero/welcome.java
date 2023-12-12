package com.busahero.busahero;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome);
    }

    public void continuebtn(View view){
        Intent intent = new Intent(this, Authentication.class);
        startActivity(intent);
    }

}
