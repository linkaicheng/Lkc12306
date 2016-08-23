package com.cheng.lkc12306.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Xml;
import android.widget.Toast;

import com.cheng.lkc12306.MainActivity;
import com.cheng.lkc12306.R;
import com.cheng.lkc12306.utils.Constant;
import com.cheng.lkc12306.utils.Md5Utils;
import com.cheng.lkc12306.utils.NetUtils;
import com.cheng.lkc12306.utils.URLConnManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {
    private ProgressDialog pDialog;
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(pDialog!=null){
                pDialog.dismiss();
            }
          switch (msg.what) {
              //连接服务器成功
              case  1:
                    int result=msg.arg1;
                  String jssionid= (String) msg.obj;
                  //服务器返回0，登录失败，转到登录界面
                  if(0==result){
                      Intent intent=new Intent(SplashActivity.this,LoginActivity.class);
                      startActivity(intent);
                      finish();
                      //服务器返回1，登录成功，保存jssionid,转到mainactivity界面
                  }else if(1==result){
                      SharedPreferences sp=getSharedPreferences("user", Context.MODE_PRIVATE);
                      SharedPreferences.Editor editor=sp.edit();
                      //记录jssionid
                      editor.putString("cookie",jssionid);
                      editor.commit();
                      Intent intent=new Intent(SplashActivity.this, MainActivity.class);
                      startActivity(intent);
                      finish();
                  }
                  break;
              //连接服务器失败或其他错误
              case  2:
                    Toast.makeText(SplashActivity.this, "服务器错误，请重试", Toast.LENGTH_SHORT).show();
                  Intent intent=new Intent(SplashActivity.this,LoginActivity.class);
                  startActivity(intent);
                  finish();
                  break;
          }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        //从SharedPreferences中取值，如果取到值不为空则为自动登录，跳转到MainActivity
        //否则为非自动登录，则跳转到LoginActivity中。
        SharedPreferences sp=getSharedPreferences("user", Context.MODE_PRIVATE);
        final String name=sp.getString("username","");
        final String password=sp.getString("password","");
        if(TextUtils.isEmpty(name)||TextUtils.isEmpty(password)){
            new Thread(){
                @Override
                public void run() {
                    try {
                        Thread.sleep(1500);
                        Intent intent=new Intent(SplashActivity.this,LoginActivity.class);
                        startActivity(intent);
                        finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }.start();

        }else{
            //将用户名和密码提交到服务器进行验证
            //判断网络是否有连接
            if (!NetUtils.check(SplashActivity.this)) {
                Toast.makeText(SplashActivity.this, "当前网络不可用", Toast.LENGTH_SHORT).show();
                return;//返回，不执行后续代码
            }
            pDialog=ProgressDialog.show(SplashActivity.this,null,"自动登录中，请稍后",false,true);
            //创建子线程，进行登录处理
            new Thread() {
                @Override
                public void run() {
                    Message msg = new Message();
                    HttpURLConnection conn = URLConnManager.getHttpURLConnection(Constant.HOST + "/Login");
                    try {
                        //延时，使显示启动界面
                        //Thread.sleep(1000);

                        List<NameValuePair> paramlist = new ArrayList<NameValuePair>();
                        paramlist.add(new BasicNameValuePair("username",name));
                        paramlist.add(new BasicNameValuePair("password", Md5Utils.MD5(password)));
                        URLConnManager.postParams(conn.getOutputStream(), paramlist);
                        conn.connect();
                        int code = conn.getResponseCode();
                        // Log.e("cheng","*********"+code);
                        if (code == 200) {
                            InputStream is = conn.getInputStream();
                            XmlPullParser parser = Xml.newPullParser();
                            parser.setInput(is, "UTF-8");
                            int eventType = parser.getEventType();
                            String xmlResult = null;
                            while (eventType != XmlPullParser.END_DOCUMENT) {

                                switch (eventType) {
                                    case XmlPullParser.START_TAG:
                                        if ("result".equals(parser.getName())) {
                                            xmlResult = parser.nextText();
                                        }
                                        break;
                                }
                                eventType = parser.next();
                            }
                            //Log.e("cheng","********"+xmlResult);
                            //关闭连接
                            conn.disconnect();
                            String cookieValue = null;
                            String cookie = conn.getHeaderField("Set-Cookie");
                            cookieValue = cookie.substring(0, cookie.indexOf(";"));
                            msg.what = 1;
                            msg.arg1 = Integer.parseInt(xmlResult);
                            msg.obj = cookieValue;
                        } else {
                            msg.what = 2;
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        msg.what = 2;
                    } catch (XmlPullParserException e) {
                        e.printStackTrace();
                        msg.what = 2;
                    }
                    handler.sendMessage(msg);
                }

            }.start();
        }

    }
}
