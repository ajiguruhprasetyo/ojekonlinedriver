package com.rigen.rigendriver;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.rigen.rigendriver.db.DBQuery;
import com.rigen.rigendriver.service.BookingService;


/**
 * Created by ILHAM HP on 11/11/2016.
 */

public class SplashScreenActivity extends AppCompatActivity {
    private DBQuery db;
    private Intent mainIntent;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        db = new DBQuery(this);
        db.open();
        Cursor c = db.getAll();
        if (c.getCount()!=0){
            mainIntent = new Intent(SplashScreenActivity.this,
                    HomeActivity.class);
        }else {
            mainIntent = new Intent(SplashScreenActivity.this,
                    LoginActivity.class);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(mainIntent);
                finish();
            }
        }, 5000);
    }
}
