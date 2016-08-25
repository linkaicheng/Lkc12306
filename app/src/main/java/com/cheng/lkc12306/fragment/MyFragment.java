package com.cheng.lkc12306.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
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
                    break;
                case 1:
                    //跳到我的账户界面
                    intent.setClass(getActivity(), AccountActivity.class);
                    break;
                case 2:
                    //跳到我的密码界面
                    intent.setClass(getActivity(), PassWordActivity.class);
                    break;
            }
            startActivity(intent);
        }
    }

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

