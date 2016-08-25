package com.cheng.lkc12306.my;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.utils.Constant;
import com.cheng.lkc12306.utils.URLConnManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class PassWordActivity extends AppCompatActivity {
    private EditText edtPass,edtConfirmPass;
    private Button btnPassSave;
    ProgressDialog pDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pass_word);
        initView();
    }
    //初始化控件并为保存按钮设置监听
    private void initView(){
        edtPass = (EditText)findViewById(R.id.edtPass);
        edtConfirmPass = (EditText)findViewById(R.id.edtConfirmPass);
        btnPassSave = (Button)findViewById(R.id.btnPassSave);
        btnPassSave.setOnClickListener(new BtnPassSaveOnCkListener());
    }
    //保存按钮的点击监听，开启一个线程向服务器提出修改密码的请求
    private class BtnPassSaveOnCkListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            final String newPassWord=edtPass.getText().toString();
            String confirmNewPass=edtConfirmPass.getText().toString();
            if(newPassWord.trim().equals("")||confirmNewPass.trim().equals("")){
                Toast.makeText(PassWordActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }
            if(!newPassWord.equals(confirmNewPass)){
                Toast.makeText(PassWordActivity.this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
                return;
            }
            pDialog=ProgressDialog.show(PassWordActivity.this,null,"更改密码中，请稍候",false,true);
            new Thread(){
                @Override
                public void run() {
                    Message msg = new Message();
                    String action="update";
                    try {
                        HttpURLConnection conn = URLConnManager.getHttpURLConnection(Constant.HOST
                                + "/otn/AccountPassword");
                        //获取登陆时保存的cookie
                        SharedPreferences sp = getSharedPreferences("user", Context.MODE_PRIVATE);
                        String cookieValue = sp.getString("cookie", "");
                        conn.setRequestProperty("cookie", cookieValue);
                        //封装请求参数
                        List<NameValuePair> params = new ArrayList<>();
                        //修改密码的操作，需要action和新的密码作为请求参数
                        params.add(new BasicNameValuePair("newPassword",newPassWord));
                        params.add(new BasicNameValuePair("action",action));
                        URLConnManager.postParams(conn.getOutputStream(), params);
                        conn.connect();
                        int code = conn.getResponseCode();
                        if (code == 200) {
                            //获取输入流
                            InputStream inputStream = conn.getInputStream();
                            //将流转成字符串
                            String response = URLConnManager.converStreamToString(inputStream);
                            // Log.e("cheng","**********response"+response);
                            //利用Gson解析Json数据
                            Gson gson = new Gson();
                            String result = gson.fromJson(response, String.class);
                            msg.what = 1;
                            msg.obj =result;
                            inputStream.close();
                        } else {
                            msg.what = 2;
                        }
                        //断开连接
                        conn.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                        msg.what = 2;
                    }catch(JsonSyntaxException e){
                        e.printStackTrace();
                        msg.what=3;
                    }
                    handler.sendMessage(msg);
                }

            }.start();

        }
            Handler handler=new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    if(pDialog!=null){
                        pDialog.dismiss();
                    }
                    String result= (String) msg.obj;
                    switch (msg.what) {
                        case  1://服务器连接成功
                            if("1".equals(result)){//服务器返回1，修改成功
                                Toast.makeText(PassWordActivity.this, "密码更改成功", Toast.LENGTH_SHORT).show();
                                finish();
                            }else if("0".equals(result)){//服务器返回0，修改失败
                                Toast.makeText(PassWordActivity.this, "密码更改失败", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case  2://服务器连接失败
                            Toast.makeText(PassWordActivity.this, "服务器错误，请稍候再试", Toast.LENGTH_SHORT).show();
                            break;
                        case  3://json解析异常
                            Toast.makeText(PassWordActivity.this, "请重新登录", Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            };
    }
}
