package com.cheng.lkc12306.login;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.cheng.lkc12306.MainActivity;
import com.cheng.lkc12306.R;

public class LoginActivity extends AppCompatActivity {
    private EditText edtUserName;
    private EditText edtPassword;
    private Button btnLogin;
    private TextView tvLostPassword;
    private CheckBox ckLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();

    }
    //初始化控件并为控件设置监听
    private void initView(){
        edtUserName = (EditText) this.findViewById(R.id.edtUsername);
        edtPassword = (EditText) this.findViewById(R.id.edtPassword);
        btnLogin = (Button) this.findViewById(R.id.btnLogin);
        tvLostPassword= (TextView) this.findViewById(R.id.tvLostPassword);
        ckLogin = (CheckBox) this.findViewById(R.id.ckLogin);
        // 忘记密码链接
        tvLostPassword.setText(Html
                .fromHtml("<a href=\"http://www.12306.cn\">忘记密码？</a>"));
        tvLostPassword.setMovementMethod(LinkMovementMethod.getInstance());

        btnLogin.setOnClickListener(new BtnLoginOnCkListener());
    }
    //登录按钮的点击监听
   private class  BtnLoginOnCkListener implements View.OnClickListener{
       @Override
       public void onClick(View v) {
           //获取数据
           String userName = edtUserName.getText().toString();
           String password = edtPassword.getText().toString();

           if (TextUtils.isEmpty(userName)) {
               edtUserName.setError("用户名不能为空！");
               edtUserName.requestFocus();
           } else if (TextUtils.isEmpty(password)) {
               edtPassword.setError("密码不能为空！");
               edtPassword.requestFocus();
           } else if("cheng".equals(userName)&&"123".equals(password)) {

               if(ckLogin.isChecked()){ //如果被选中，则实现保存用户名和密码
                   //1、获得SharedPreferences对象
                   SharedPreferences sp=getSharedPreferences("user", Context.MODE_PRIVATE);
                   //2.通过sp对象获得编辑器
                   SharedPreferences.Editor editor=sp.edit();
                   //3、利用editor将用户名和密码通过键值对的形式保存到user中
                   editor.putString("name",userName);
                   editor.putString("pwd",password);
                   //4、提交数据
                   editor.commit();

               }else{//如果没有选中则清除以前保存的用户名和密码
                   //1、获得SharedPreferences对象
                   SharedPreferences sp=getSharedPreferences("user", Context.MODE_PRIVATE);
                   //2.通过sp对象获得编辑器
                   SharedPreferences.Editor editor=sp.edit();
                   //调用remove()方法清除数据
                   editor.remove("name");
                   editor.remove("pwd");
                   editor.clear();
                   //提交数据
                   editor.commit();
               }
               //创建Intent对象
               Intent intent = new Intent(LoginActivity.this,
                       MainActivity.class);
               startActivity(intent);
               finish();
           }else{
               Toast.makeText(LoginActivity.this,"用户名或密码不正确",Toast.LENGTH_LONG).show();
           }

       }
   }

}
