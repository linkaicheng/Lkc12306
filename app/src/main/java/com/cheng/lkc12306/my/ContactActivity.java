package com.cheng.lkc12306.my;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.bean.Passenger;
import com.cheng.lkc12306.utils.Constant;
import com.cheng.lkc12306.utils.NetUtils;
import com.cheng.lkc12306.utils.URLConnManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactActivity extends AppCompatActivity {
    private ListView lvContact;
    List<Map<String, Object>> data;
    private ProgressDialog pDialog;
    SimpleAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        initView();
    }
    //初始化控件并添加监听
    private void initView() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        data=new ArrayList<Map<String,Object>>();
        lvContact = (ListView) findViewById(R.id.lvContact);
      adapter = new SimpleAdapter(this, data, R.layout.item_my_contact,
                new String[]{"name", "idCard", "tel"}, new int[]{R.id.tvContactName, R.id.tvContactIdCard
                , R.id.tvContactTel});
        lvContact.setAdapter(adapter);
        lvContact.setOnItemClickListener(new lvContactOnItListener());
    }
//重写onResume方法，执行联系人数据的获取操作
    @Override
    protected void onResume() {
        super.onResume();
        //判断网络是否可用
        if (!NetUtils.check(ContactActivity.this)) {
            Toast.makeText(ContactActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //显示进度对话框
        pDialog = ProgressDialog.show(ContactActivity.this, null, "请稍候。。", false, true);
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                try {
                    HttpURLConnection conn = URLConnManager.getHttpURLConnection(Constant.HOST + "/otn/PassengerList");
                    //获取登陆时保存的cookie
                    SharedPreferences sp = getSharedPreferences("user", Context.MODE_PRIVATE);
                    String cookieValue = sp.getString("cookie", "");
                    conn.setRequestProperty("cookie", cookieValue);
                    conn.connect();
                    int code = conn.getResponseCode();
                    if (code == 200) {
                        //获取输入流
                        InputStream inputStream = conn.getInputStream();
                        //将流转成字符串
                        String response = URLConnManager.converStreamToString(inputStream);
                        //利用Gson解析Json数据
                        Gson gson = new Gson();
                        Passenger[] passengers = gson.fromJson(response, Passenger[].class);
                        msg.what = 1;
                        msg.obj = passengers;
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
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //将数据清空，然后重新装载
            data.clear();
            if (pDialog != null) {
                pDialog.dismiss();
            }
            switch (msg.what) {
                case 1:
                    //获取联系人列表
                    Passenger[] passengers= (Passenger[]) msg.obj;
                    //将联系人列表更新到界面
                    for(Passenger passenger:passengers){
                        Map<String,Object> row=new HashMap<>();
                        row.put("name",passenger.getName()+"("+passenger.getType()+")");
                        row.put("idCard",passenger.getIdType()+":"+passenger.getId());
                        row.put("tel","电话号码:"+passenger.getTel());
                        data.add(row);
                    }
                    //每次回到联系人界面都更新一下数据
                    adapter.notifyDataSetChanged();
                    break;
                case 2:
                    Toast.makeText(ContactActivity.this, "服务器错误，请重试", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(ContactActivity.this, "请重新登录", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

//联系人列表的点击监听
    private class lvContactOnItListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //点击后跳转到相应的编辑界面
            Intent intent = new Intent(ContactActivity.this, ContactEditActivity.class);
            //获取相应联系人的信息
            Map<String, Object> contact = data.get(position);
            //将联系人的信息以序列化的方式保存到intent
            intent.putExtra("contact", (Serializable) contact);
            startActivity(intent);
        }
    }

    //创建右上角添加联系人的图标
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_contact, menu);
        return true;
    }

    //为添加联系人的图标和回退箭头设置点击监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.addContact:
                Intent intent = new Intent(ContactActivity.this, ContactAddActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
