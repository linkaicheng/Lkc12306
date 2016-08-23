package com.cheng.lkc12306.login;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.utils.Constant;
import com.cheng.lkc12306.utils.Md5Utils;
import com.cheng.lkc12306.utils.NetUtils;
import com.cheng.lkc12306.utils.URLConnManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
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
                if(!NetUtils.check(LoginActivity.this)){
                    Toast.makeText(LoginActivity.this, "当前网络不可用", Toast.LENGTH_SHORT).show();
                    return;//返回，不执行后续代码
                }
                //显示圆形进度对话框
                pDialog=ProgressDialog.show(LoginActivity.this,null,"正在登陆，请稍后",false,true);
                new Thread(){
                    @Override
                    public void run() {
                        try {
                         HttpURLConnection conn= URLConnManager.getHttpURLConnection(Constant.HOST+"/Login");
                        List<NameValuePair> paramlist=new ArrayList<NameValuePair>();
                        paramlist.add(new BasicNameValuePair("username",userName));
                        paramlist.add(new BasicNameValuePair("password", Md5Utils.MD5(password)));
                            URLConnManager.postParams(conn.getOutputStream(),paramlist);
                            conn.connect();
                            int code=conn.getResponseCode();
                            if(code==200){

                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        }
    }

}
