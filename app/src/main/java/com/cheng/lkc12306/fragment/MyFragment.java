package com.cheng.lkc12306.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.login.LoginActivity;
import com.cheng.lkc12306.my.AccountActivity;
import com.cheng.lkc12306.my.ContactActivity;
import com.cheng.lkc12306.my.PassWordActivity;

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
            //清除保存的密码
            //1、获得SharedPreferences对象
            SharedPreferences sp = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
            //2.通过sp对象获得编辑器
            SharedPreferences.Editor editor = sp.edit();
            //调用remove()方法清除数据
            editor.remove("name");
            editor.remove("pwd");
            editor.clear();
            //提交数据
            editor.commit();
            //到登录界面
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
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

