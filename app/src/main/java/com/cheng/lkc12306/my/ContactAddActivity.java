package com.cheng.lkc12306.my;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
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


public class ContactAddActivity extends AppCompatActivity {
    private ListView lvContactAdd;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    //保存按钮
    private Button btnSave;
    private List<Map<String, Object>> data;
    SimpleAdapter adapter;
    private ProgressDialog pDialog;
    private String action="";//判断要执行的操作
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(pDialog!=null){
                pDialog.dismiss();
            }
            switch (msg.what) {
                case  1:
                    //获取服务器返回的结果
                    String result= (String) msg.obj;
                    if("1".equals(result)){//更新成功
                        Toast.makeText(ContactAddActivity.this, "添加联系人成功", Toast.LENGTH_SHORT).show();
                        finish();
                    }else if("-1".equals(result)){//更新失败
                        Toast.makeText(ContactAddActivity.this, "添加联系人失败，请重试", Toast.LENGTH_SHORT).show();
                    }else if("0".equals(result)){
                        Toast.makeText(ContactAddActivity.this, "联系人已经存在", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case  2:
                    Toast.makeText(ContactAddActivity.this, "服务器错误，请重试", Toast.LENGTH_SHORT).show();
                    break;
                case  3:
                    Toast.makeText(ContactAddActivity.this, "请重新登录", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //使菜单栏出现回退箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_add);
        initView();

    }
    //初始化控件并设置监听
    private void initView(){
        lvContactAdd = (ListView) findViewById(R.id.lvContactAdd);
        btnSave = (Button) findViewById(R.id.btnSave);
        data = getData();
        adapter = new SimpleAdapter(ContactAddActivity.this, data,
                R.layout.item_my_contact_add, new String[]{"key1", "key2", "key3"},
                new int[]{R.id.tvContactAddKey, R.id.tvContactAddValue, R.id.imContactAddFlg});
        lvContactAdd.setAdapter(adapter);
        lvContactAdd.setOnItemClickListener(new LvContactAddOnItCkListener());
        btnSave.setOnClickListener(new BtnSaveOnCkListener());
    }
    //联系人信息列表的点击监听
    private class LvContactAddOnItCkListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 0:
                    change(position, "请输入姓名");
                    break;
                case 1:
                    String[] items = new String[]{"身份证", "学生证", "军人证"};
                    changeType("你选择证件类型", position, items);
                    break;
                case 2:
                    change(position, "请输证件号码");
                    break;
                case 3:
                    String[] passengerItem = new String[]{"成人", "儿童", "学生", "其他"};
                    changeType("请你选择乘客类型", position, passengerItem);

                    break;
                case 4:
                    change(position, "请输电话号码");
                    break;
            }
        }
    }
    //保存按钮的点击监听
    private class BtnSaveOnCkListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            //判断网络是否可用
            if (!NetUtils.check(ContactAddActivity.this)) {
                Toast.makeText(ContactAddActivity.this, "当前网络不可用，请稍后再试", Toast.LENGTH_SHORT).show();
                return;//网络不可用时直接返回，不再进行其他操作
            }
            //显示进度对话框
            pDialog = ProgressDialog.show(ContactAddActivity.this, null, "正在添加联系人，请稍后", false, true);
            action = "update";
            contactThread.start();
        }
    }
    //向服务器发出添加联系人的请求
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
            }catch(JsonSyntaxException e){
                msg.what=3;
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
//弹出乘客类型或证件类型的选择对话框
    private void changeType(final String title, final int position, String[] items) {
        final String[] items2;
        //创建一个Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(ContactAddActivity.this);
        //设置标题
        builder.setTitle(title);
        builder.setIcon(android.R.drawable.btn_star);
        items2 = items;
    /*
    * 第一个参数为单选按钮的数据集合
    * 第二个为默认的勾选的项
    * 第三个参数为监听器
    * */
        builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                data.get(position).put("key2", items2[which]);
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
//其他信息的输入对话框
    private void change(final int position, final String title) {

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

    private List<Map<String, Object>> getData() {
        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Object> row = new HashMap<>();

        row.put("key1", "姓名");
        row.put("key2", null);
        row.put("key3", R.mipmap.forward_25);
        data.add(row);

        row = new HashMap<>();
        row.put("key1", "证件类型");
        row.put("key2", null);
        row.put("key3", R.mipmap.forward_25);
        data.add(row);

        row = new HashMap<>();
        row.put("key1", "证件号码");
        row.put("key2", null);
        row.put("key3", R.mipmap.forward_25);
        data.add(row);

        row = new HashMap<>();
        row.put("key1", "乘客类型");
        row.put("key2", null);
        row.put("key3", R.mipmap.forward_25);
        data.add(row);

        row = new HashMap<>();
        row.put("key1", "电话");
        row.put("key2", null);
        row.put("key3", R.mipmap.forward_25);
        data.add(row);
        return data;
    }
//菜单栏图标的创建
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_my_contact_add, menu);
        return true;
    }
//菜单栏图标的点击监听
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.searhContact:
                //读通读录权限（兼容android M（api23)需要这段代码）
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    int hasWriteContactsPermission = checkSelfPermission(Manifest.permission.READ_CONTACTS);
                    if (hasWriteContactsPermission
                            != PackageManager.PERMISSION_GRANTED) {

                        requestPermissions(new String[]
                                {Manifest.permission.READ_CONTACTS}, REQUEST_CODE_ASK_PERMISSIONS);
                        break;
                    }
                }

                //从通讯录中拿到用户名和电话
                getContacts();
                break;
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 获取手机通讯录
     */
    private void getContacts(){
        //获得内容解析器
        ContentResolver cr=getContentResolver();
        Uri uri= ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        //查询联系人数据
        Cursor cursor=cr.query(uri,null,null,null,null);
        List<String> contacts=new ArrayList<>();
        while(cursor.moveToNext()){
            String name=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));//联系人姓名
            String phone=cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));//联系人号码
            contacts.add(name+"("+phone+")");
        }
        cursor.close();
        if(contacts.size()==0){
            new AlertDialog.Builder(this).setTitle("请选择")
                    .setMessage("通讯录为空")
                    .setNegativeButton("取消",null).show();
        }else{
            final String[] items=new String[contacts.size()];
            contacts.toArray(items);
            new AlertDialog.Builder(this)
                    .setTitle("请选择")
                    .setItems(items, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String select=items[which];
                            String name=select.substring(0,select.indexOf("("));
                            String phone=select.substring(select.indexOf("(")+1,select.indexOf(")"));

                            data.get(0).put("key2",name);
                            data.get(4).put("key2",phone);
                            adapter.notifyDataSetChanged();
                        }
                    }).setNegativeButton("取消",null).show();
        }

    }

}
