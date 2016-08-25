package com.cheng.lkc12306.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.login.LoginActivity;
import com.cheng.lkc12306.my.AccountActivity;
import com.cheng.lkc12306.my.ContactActivity;
import com.cheng.lkc12306.my.PassWordActivity;
import com.cheng.lkc12306.utils.Constant;
import com.cheng.lkc12306.utils.NetUtils;
import com.cheng.lkc12306.utils.URLConnManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cheng on 2016/8/17.
 */
public class MyFragment extends Fragment {
    private ListView lvMy;
    //退出登录按钮
    private Button btnLogout;
    private ProgressDialog pDialog;
    //在点击我的密码的时候会弹出一个对话框，要求输入原来的密码，
    // 因为要传给子线程所以定义一个全局变量oldPassword
    //去保存输入的密码
    private String oldPassword;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();

    }

    //初始化控件并为控件设置监听
    private void initView() {
        btnLogout = (Button) getActivity().findViewById(R.id.btnLogout);
        lvMy = (ListView) getActivity().findViewById(R.id.lvMy);
        List<Map<String, Object>> data = getData();
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), data, R.layout.item_my_main,
                new String[]{"icon", "title"}, new int[]{R.id.imIcon, R.id.tvTitle});
        lvMy.setAdapter(adapter);
        lvMy.setOnItemClickListener(new LvMyOnItCkListener());
        btnLogout.setOnClickListener(new BtnLogoutOnCkListener());
    }

    //退出登录按钮的点击监听
    private class BtnLogoutOnCkListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //如果网络不可用，给出提示，并return
            if(!NetUtils.check(getActivity())){
                Toast.makeText(getActivity(), "当前网络不可用", Toast.LENGTH_SHORT).show();
                return;
            }
            //使用异步任务实现退出登录
            new LogoutTask().execute();

        }
    }
    //退出登录的异步任务
    private class  LogoutTask extends AsyncTask<String,Void,String>{
        @Override
        protected void onPreExecute() {
            //初始化操作，显示进度条
            pDialog=ProgressDialog.show(getActivity(),null,"正在退出",false,true);
        }

        @Override
        protected String doInBackground(String... params) {//执行网络等耗时操作
            String result=null;
            try {
            //
            HttpURLConnection conn= URLConnManager.getHttpURLConnection(Constant.HOST+"/otn/Logout");
            //拿到cookie
           SharedPreferences sp=getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
            String cookieValue=sp.getString("cookie","");
            conn.setRequestProperty("cookie",cookieValue);
                conn.connect();
                int code=conn.getResponseCode();
                //Log.e("cheng","*********"+code);
                if(code==200){
                     //获取输入流
                    InputStream inputStream=conn.getInputStream();
                    //将输入流转成字符串“0”，或“1”
                    String response=URLConnManager.converStreamToString(inputStream);
                    result=response.substring(1,2);
                    inputStream.close();
                }
               // Log.e("cheng","********"+result);
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) { // 更新ui
            //关闭进度对话框
            if(pDialog!=null){
                pDialog.dismiss();
            }
            if("1".equals(s)){
                //如果有保存密码，删除
                SharedPreferences sp=getActivity().getSharedPreferences("user",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor=sp.edit();
                String username=sp.getString("username","");
                String password=sp.getString("password","");
                if(username!=""){
                    editor.remove("username");
                }
                if(password!=""){
                    editor.remove("password");
                }
                editor.commit();
                Toast.makeText(getActivity(), "退出成功", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                getActivity().finish();
            }else if("0".equals(s)){
                Toast.makeText(getActivity(), "退出失败", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getActivity(), "服务器错误，请重试", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //我的12306模块功能列表的点击监听
    private class LvMyOnItCkListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Intent intent = new Intent();
            switch (position) {
                case 0:
                    //跳到我的联系人界面
                    intent.setClass(getActivity(), ContactActivity.class);
                    startActivity(intent);
                    break;
                case 1:
                    //跳到我的账户界面
                    intent.setClass(getActivity(), AccountActivity.class);
                    startActivity(intent);
                    break;
                case 2:
                    //点击我的密码，弹出对话框，要求输入原来的密码，向服务器进行确认
                    confirmOldPassword();
                    break;
            }

        }
    }
    //验证原来的密码是否正确
    private void confirmOldPassword(){
        //创建一个对话框用来输入原来的密码
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("请输入原密码");
        builder.setIcon(android.R.drawable.btn_star);
        final EditText edtOldPassword= new EditText(getActivity());
        builder.setView(edtOldPassword);

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //将输入的密码保存在全局变量oldPassword中，供子线程使用
                oldPassword=edtOldPassword.getText().toString();
                //显示一个进度对话框
                pDialog=ProgressDialog.show(getActivity(),null,"验证密码中，请稍候",false,true);
                //开启一个子线程，执行向服务器查询密码的操作
                new Thread(confirmOldPassRunnable).start();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //此在此处实现取消逻辑代码
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    Runnable confirmOldPassRunnable=new Runnable() {

        @Override
        public void run() {
            Message msg = new Message();
            String action="query";
            try {
                HttpURLConnection conn = URLConnManager.getHttpURLConnection(Constant.HOST
                        + "/otn/AccountPassword");
                //获取登陆时保存的cookie
                SharedPreferences sp = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
                String cookieValue = sp.getString("cookie", "");
                conn.setRequestProperty("cookie", cookieValue);
                //封装请求参数
                List<NameValuePair> params = new ArrayList<>();
                //查询密码的操作，需要action和原来的密码作为请求参数
               params.add(new BasicNameValuePair("oldPassword",oldPassword));
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
    };
Handler handler=new Handler(){
    @Override
    public void handleMessage(Message msg) {
        if(pDialog!=null){
            pDialog.dismiss();
        }
        String result= (String) msg.obj;
        switch (msg.what) {
            case  1://连接服务器成功
                if("1".equals(result)){//服务器返回1,验证成功
                    Toast.makeText(getActivity(), "验证成功", Toast.LENGTH_SHORT).show();
                    //跳转到密码修改界面
                    Intent intent=new Intent(getActivity(), PassWordActivity.class);
                    startActivity(intent);

                }else if("0".equals(result)){//服务器返回0，密码错误
                    Toast.makeText(getActivity(), "密码验证错误", Toast.LENGTH_SHORT).show();
                }
                break;
            case  2://连接服务器失败或其他错误
        Toast.makeText(getActivity(), "服务器错误，请稍候再试", Toast.LENGTH_SHORT).show();
                break;
            case  3://JsonSyntaxException
                Toast.makeText(getActivity(), "请重新登录", Toast.LENGTH_SHORT).show();
                break;

        }

    }
};

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> data = new ArrayList<>();

        Map<String, Object> row = new HashMap<>();
        row.put("icon", R.mipmap.contacts);
        row.put("title", "我的联系人");
        data.add(row);

        row = new HashMap<>();
        row.put("icon", R.mipmap.account);
        row.put("title", "我的账户");
        data.add(row);

        row = new HashMap<>();
        row.put("icon", R.mipmap.password);
        row.put("title", "我的密码");
        data.add(row);
        return data;
    }
}

