package com.cheng.lkc12306.login;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
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

public class LoginActivity extends AppCompatActivity {
    private EditText edtUserName;
    private EditText edtPassword;
    private Button btnLogin;
    private TextView tvLostPassword;
    private CheckBox ckLogin;
    private ProgressDialog pDialog;

    Handler handler = new Handler() {//进行界面更新
        @Override
        public void handleMessage(Message msg) {
            //关闭pDailog对话框
            if (pDialog != null) {
                pDialog.dismiss();
            }
            switch (msg.what) {
                //1：连接上服务器
                case 1:
                    int result = msg.arg1;
                    String jssionid = (String) msg.obj;
                    //服务器返回0，验证失败
                    if (0 == result) {
                        edtUserName.selectAll();
                        edtUserName.requestFocus();
                        edtUserName.setError("用户名或密码错误");
                        //服务器返回1，登录成功
                    } else if (1 == result) {
                        SharedPreferences sp = getSharedPreferences("user", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        //记录JSESSIONID
                        editor.putString("cookie", jssionid);
                        //如果选中记住密码，将密码保存
                        if (ckLogin.isChecked()) {
                            editor.putString("username", edtUserName.getText().toString());
                            editor.putString("password", edtPassword.getText().toString());
                        } else {
                            //如果没勾选记住密码，删除保存的用户名和密码
                            editor.remove("username");
                            editor.remove("password");
                        }
                        editor.commit();
                        //登录成功，到MainActivity界面
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    break;
                //
                case 0:
                    Toast.makeText(LoginActivity.this, "服务器忙。。。。", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();

    }

    //初始化控件并为控件设置监听
    private void initView() {
        edtUserName = (EditText) this.findViewById(R.id.edtUsername);
        edtPassword = (EditText) this.findViewById(R.id.edtPassword);
        btnLogin = (Button) this.findViewById(R.id.btnLogin);
        tvLostPassword = (TextView) this.findViewById(R.id.tvLostPassword);
        ckLogin = (CheckBox) this.findViewById(R.id.ckLogin);
        // 忘记密码链接
        tvLostPassword.setText(Html
                .fromHtml("<a href=\"http://www.12306.cn\">忘记密码？</a>"));
        tvLostPassword.setMovementMethod(LinkMovementMethod.getInstance());
//为登录按钮设置监听
        btnLogin.setOnClickListener(new BtnLoginOnCkListener());
    }

    //登录按钮的点击监听
    private class BtnLoginOnCkListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //获取数据
            final String userName = edtUserName.getText().toString();
            final String password = edtPassword.getText().toString();

            if (TextUtils.isEmpty(userName)) {
                edtUserName.setError("用户名不能为空！");
                edtUserName.requestFocus();
            } else if (TextUtils.isEmpty(password)) {
                edtPassword.setError("密码不能为空！");
                edtPassword.requestFocus();
            } else {//将用户名和密码提交到服务器进行验证
                //判断网络是否有连接
                if (!NetUtils.check(LoginActivity.this)) {
                    Toast.makeText(LoginActivity.this, "当前网络不可用", Toast.LENGTH_SHORT).show();
                    return;//返回，不执行后续代码
                }


                //显示圆形进度对话框
                pDialog = ProgressDialog.show(LoginActivity.this, null, "正在登陆，请稍后", false, true);
                //创建子线程，进行登录处理
                new Thread() {
                    @Override
                    public void run() {
                        Message msg = new Message();
                        HttpURLConnection conn = URLConnManager.getHttpURLConnection(Constant.HOST + "/Login");
                        try {
                            List<NameValuePair> paramlist = new ArrayList<NameValuePair>();
                            paramlist.add(new BasicNameValuePair("username", userName));
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
}
