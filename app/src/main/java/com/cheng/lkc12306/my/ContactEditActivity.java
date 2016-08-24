package com.cheng.lkc12306.my;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.utils.Constant;
import com.cheng.lkc12306.utils.DialogUtil;
import com.cheng.lkc12306.utils.NetUtils;
import com.cheng.lkc12306.utils.URLConnManager;
import com.google.gson.Gson;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ContactEditActivity extends AppCompatActivity {
    private ListView lvContactEdit;
    List<Map<String, Object>> data;
    SimpleAdapter adapter;
    private Button btnContactEditSave;
    private String action = "";//用来判断操作的类型：添加，删除，修改
    private ProgressDialog pDialog;
    //修改或删除联系人后更新视图的操作
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if(pDialog!=null){
                pDialog.dismiss();
            }
            switch (msg.what) {
                case  1:
                    //获取服务器返回的结果
                    String result= (String) msg.obj;
                    //判断是修改操作还是删除操作，然后给出相应的提示
                    String info="修改";
                    if("remove".equals(action)){
                        info="删除";
                    }
                    if("1".equals(result)){//更新成功
                        Toast.makeText(ContactEditActivity.this, info+"成功", Toast.LENGTH_SHORT).show();
                        finish();
                    }else if("-1".equals(result)){//更新失败
                        Toast.makeText(ContactEditActivity.this, info+"失败，请重试", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case  2:
                    Toast.makeText(ContactEditActivity.this, "服务器错误，请重试", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //使菜单栏出现回退箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_edit);
        initView();

    }

    //初始化控件并设置监听
    private void initView() {
        lvContactEdit = (ListView) findViewById(R.id.lvContactEdit);
        btnContactEditSave = (Button) findViewById(R.id.btnContactEditSave);
        Intent intent = getIntent();
        Map<String, Object> contact = (Map<String, Object>) intent.getSerializableExtra("contact");
        data = getData(contact);
        adapter = new SimpleAdapter(ContactEditActivity.this, data, R.layout.item_my_contact_edit,
                new String[]{"key1", "key2", "key3"},
                new int[]{R.id.tvContactEditKey,
                        R.id.tvContactEditValue, R.id.imContactEditFlag});
        lvContactEdit.setAdapter(adapter);
        lvContactEdit.setOnItemClickListener(new LvContactEditOnItCkListener());
        //保存按钮设置点击监听
        btnContactEditSave.setOnClickListener(new BtnContactEditSaveOnCkListener());
    }

    //保存按钮的点击监听（修改联系人）
    private class BtnContactEditSaveOnCkListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //判断网络是否可用
            if (!NetUtils.check(ContactEditActivity.this)) {
                Toast.makeText(ContactEditActivity.this, "当前网络不可用，请稍后再试", Toast.LENGTH_SHORT).show();
                return;//网络不可用时直接返回，不再进行其他操作
            }
            //显示进度对话框
            pDialog = ProgressDialog.show(ContactEditActivity.this, null, "修改中，请稍后", false, true);
            action = "update";
            contactThread.start();
        }
    }
//向服务器发出修改联系人的请求
    Thread contactThread = new Thread() {
        @Override
        public void run() {
            /**
             * 添加/删除/修改联系人
             地址 ：http:// 127.0.0.1:8080/My12306/otn/Passenger
             请求头 ：
             Name:cookie
             Value:JSESSIONID=XXXXXX
             请求数据 ：姓名，证件类型，证件号码，乘客类型，电话
             action:new update remove
             响应接口：0: 添加联系人已存在；1: 成功；-1: 错误
             */
            Message msg = new Message();
            InputStream inputStream = null;
            HttpURLConnection conn = null;
            try {

                conn = URLConnManager.getHttpURLConnection(Constant.HOST + "/otn/Passenger");
                //获取保存的cookie
                SharedPreferences sp = getSharedPreferences("user", Context.MODE_PRIVATE);
                String cookieValue = sp.getString("cookie", "");
                //设置请求属性
                conn.setRequestProperty("cookie", cookieValue);
                //封装请求参数
                List<NameValuePair> params = new ArrayList<>();
                params.add(new BasicNameValuePair("姓名", (String) data.get(0).get("key2")));
                params.add(new BasicNameValuePair("证件类型", (String) data.get(1).get("key2")));
                params.add(new BasicNameValuePair("证件号码", (String) data.get(2).get("key2")));
                params.add(new BasicNameValuePair("乘客类型", (String) data.get(3).get("key2")));
                params.add(new BasicNameValuePair("电话", (String) data.get(4).get("key2")));
                params.add(new BasicNameValuePair("action", action));
                URLConnManager.postParams(conn.getOutputStream(), params);
                conn.connect();
                //获取响应码
                int code = conn.getResponseCode();
                //连接成功
                if (code == 200) {
                    inputStream = conn.getInputStream();
                    String response = URLConnManager.converStreamToString(inputStream);
                    Gson gson=new Gson();
                    String result=gson.fromJson(response,String.class);
                    msg.what = 1;
                    msg.obj = result;
                } else {//连接失败
                    msg.what = 2;
                }
                handler.sendMessage(msg);
            } catch (IOException e) {
                msg.what = 2;
                e.printStackTrace();
            } finally {
                // 最后关闭连接和输入流
                try {
                    if (conn != null) {
                        conn.disconnect();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    //联系人列表的点击监听
    private class LvContactEditOnItCkListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0:
                    changeNameAndTel(position, "请输入姓名");
                    break;
                case 3:
                    changePassengerType(position);
                    break;
                case 4:
                    changeNameAndTel(position, "请输入号码");
                    break;
            }
        }
    }

    //修改乘客类型对话框
    private void changePassengerType(final int position) {
        int idx = 0;
        String keyValue = (String) data.get(position).get("key2");
        final String[] items = new String[]{"成人", "儿童", "学生", "其他"};
        //得到原来的值在items中对应的索引，用来作为默认选中
        for (int i = 0; i < items.length; i++) {
            if (items[i].equals(keyValue)) {
                idx = i;
                break;
            }
        }
        //创建一个Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(ContactEditActivity.this);
        //设置标题
        builder.setTitle("你选择乘客类型");
        builder.setIcon(android.R.drawable.btn_star);

    /*
    * 第一个参数为单选按钮的数据集合
    * 第二个为默认的勾选的项
    * 第三个参数为监听器
    * */
        builder.setSingleChoiceItems(items, idx, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                data.get(position).put("key2", items[which]);
                adapter.notifyDataSetChanged();
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
        //显示对话框
        builder.create().show();
    }


    //弹出修改姓名或号码的对话框
    private void changeNameAndTel(final int position, final String title) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setIcon(android.R.drawable.btn_star);
        final EditText edtInput = new EditText(this);
        builder.setView(edtInput);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String input = edtInput.getText().toString();
                if (TextUtils.isEmpty(input)) {
                    DialogUtil.dialogClose(dialog, false);
                    edtInput.setError(title);
                    edtInput.requestFocus();
                } else {

                    DialogUtil.dialogClose(dialog, true);
                    data.get(position).put("key2", input);
                    adapter.notifyDataSetChanged();

                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DialogUtil.dialogClose(dialog, true);
            }
        });
        builder.create().show();

    }


    private List<Map<String, Object>> getData(Map<String, Object> contact) {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();
        String name = (String) contact.get("name");
        String idCard = (String) contact.get("idCard");
        String tel = (String) contact.get("tel");
        row.put("key1", "姓名");
        row.put("key2", name.split("\\(")[0]);
        row.put("key3", R.mipmap.forward_25);
        data.add(row);

        row = new HashMap<>();
        row.put("key1", "证件类型");
        row.put("key2", idCard.split(":")[0]);
        row.put("key3", null);
        data.add(row);

        row = new HashMap<>();
        row.put("key1", "证件号码");
        row.put("key2", idCard.split(":")[1]);
        row.put("key3", null);
        data.add(row);

        row = new HashMap<>();
        row.put("key1", "乘客类型");
        row.put("key2", name.split("\\(")[1].split("\\)")[0]);
        row.put("key3", R.mipmap.forward_25);
        data.add(row);

        row = new HashMap<>();
        row.put("key1", "电话");
        row.put("key2", tel.split(":")[1]);
        row.put("key3", R.mipmap.forward_25);
        data.add(row);
        return data;
    }

    //菜单栏图标的创建
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_contact_edit, menu);
        return true;
    }

    //菜单栏图标的点击监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {//点击删除联系人的图标，删除联系人，代码和修改联系人相同，只是把action改为remove
            case R.id.deleteContact:
                action="remove";
                contactThread.start();
                break;
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
