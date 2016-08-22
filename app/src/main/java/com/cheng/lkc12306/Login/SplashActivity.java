package com.cheng.lkc12306.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.cheng.lkc12306.MainActivity;
import com.cheng.lkc12306.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //从SharedPreferences中取值，如果取到值不为空则为自动登录，跳转到MainActivity
                //否则为非自动登录，则跳转到LoginActivity中。
                SharedPreferences sp=getSharedPreferences("user", Context.MODE_PRIVATE);
                String userName=sp.getString("name","");
                String password=sp.getString("pwd","");
                if(TextUtils.isEmpty(userName)||TextUtils.isEmpty(password)){

                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    //启动Activity
                    startActivity(intent);
                    finish();
                }else{
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    //启动Activity
                    startActivity(intent);
                    finish();
                }
            }
        }, 3000);
    }
}
