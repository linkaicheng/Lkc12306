package com.cheng.lkc12306.my;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.bean.Account;
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

public class AccountActivity extends AppCompatActivity {
    private ListView lvAccount;
    //保存按钮
    private Button btnAccountSave;
    private List<Map<String,Object>> data;
    SimpleAdapter adapter;
    ProgressDialog pDialog;
    private String action;//用来判断是更新账户还是获取账户信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //使界面左上方出现回退箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        //判断网络是否可用
        if (!NetUtils.check(AccountActivity.this)) {
            Toast.makeText(AccountActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //显示进度对话框
        pDialog = ProgressDialog.show(AccountActivity.this, null, "请稍候", false, true);
        initView();
        //开启一个线程，执行查询我的账户操作
        action="query";
        new Thread(accountRunnable).start();
    }
    //初始化控件，并设置监听
    private void initView(){
        data=new ArrayList<>();
        lvAccount = (ListView) findViewById(R.id.lvAccount);
        btnAccountSave = (Button) findViewById(R.id.btnAccountSave);
        adapter = new SimpleAdapter(AccountActivity.this, data,
                R.layout.item_my_account, new String[]{"key1", "key2", "key3"},
                new int[]{R.id.tvAccountKey, R.id.tvAccountValue, R.id.imAccountFlag});
        lvAccount.setAdapter(adapter);
        //我的账户信息列表的点击监听
        lvAccount.setOnItemClickListener(new lvAccountListener());
        //保存按钮
        btnAccountSave.setOnClickListener(new BtnAccountSaveListener());
    }
    //更新账户或获取账户,使用Runnable的形式，每次调用都会开启一个新线程
    Runnable accountRunnable=new Runnable() {
        @Override
        public void run() {
            Message msg = new Message();
            try {
                HttpURLConnection conn = URLConnManager.getHttpURLConnection(Constant.HOST + "/otn/Account");
                //获取登陆时保存的cookie
                SharedPreferences sp = getSharedPreferences("user", Context.MODE_PRIVATE);
                String cookieValue = sp.getString("cookie", "");
                conn.setRequestProperty("cookie", cookieValue);
                //封装请求参数
                List<NameValuePair> params = new ArrayList<>();
                //判断如果是更新账户的操作，需要将乘客类型和电话作为请求参数
                //如果是获取账户的操作，则只需要action作为请求参数
                if("update".equals(action)){
                    params.add(new BasicNameValuePair("乘客类型", (String) data.get(4).get("key2")));
                    params.add(new BasicNameValuePair("电话", (String) data.get(5).get("key2")));
                }
                params.add(new BasicNameValuePair("action", action));
                URLConnManager.postParams(conn.getOutputStream(), params);
                conn.connect();
                int code = conn.getResponseCode();
                if (code == 200) {
                    //获取输入流
                    InputStream inputStream = conn.getInputStream();
                    //将流转成字符串
                    String response = URLConnManager.converStreamToString(inputStream);
                    //利用Gson解析Json数据
                    Gson gson = new Gson();
                    Account account = gson.fromJson(response, Account.class);
                    msg.what = 1;
                    msg.obj = account;
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
    //获取账户或更新账户后的更新视图的操作
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //清空data数据，然后重新添加，避免界面出现双份的数据
            data.clear();
            if(pDialog!=null){
                pDialog.dismiss();
            }
            switch (msg.what) {
                case  1:
                    Account account= (Account) msg.obj;
                    updateView(account);
                    //更新成功时给出一个提示
                    if("update".equals(action)){
                        Toast.makeText(AccountActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case  2:
                    Toast.makeText(AccountActivity.this, "服务器错误，请重试", Toast.LENGTH_SHORT).show();
                    break;
                case  3:
                    Toast.makeText(AccountActivity.this, "请重新登录", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    //将更新视图的操作封装成一个方法
    private void updateView(Account account){
        Map<String,Object> row=new HashMap<>();
        row.put("key1","用户名");
        row.put("key2",account.getUsername());
        row.put("key3",null);
        data.add(row);

        row=new HashMap<>();
        row.put("key1","姓名");
        row.put("key2",account.getName());
        row.put("key3",null);
        data.add(row);
        row=new HashMap<>();
        row.put("key1","证件类型");
        row.put("key2",account.getIdType());
        row.put("key3",null);
        data.add(row);

        row=new HashMap<>();
        row.put("key1","证件号码");
        row.put("key2",account.getId());
        row.put("key3",null);
        data.add(row);

        row=new HashMap<>();
        row.put("key1","乘客类型");
        row.put("key2",account.getType());
        row.put("key3",R.mipmap.forward_25);
        data.add(row);

        row=new HashMap<>();
        row.put("key1","电话");
        row.put("key2",account.getTel());
        row.put("key3",R.mipmap.forward_25);
        data.add(row);
        adapter.notifyDataSetChanged();
    }
    //乘客类型和电话号码的点击监听
    private class lvAccountListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            switch (position) {
                case 4:
                    String[] passengerItem = new String[]{"成人", "儿童", "学生", "其他"};
                    changeType("请你选择乘客类型", position, passengerItem);
                    break;
                case 5:
                    change(position, "请输电话号码");
                    break;
            }
        }
    }
    //保存按钮的点击监听
    private class BtnAccountSaveListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            //判断网络是否可用
            if (!NetUtils.check(AccountActivity.this)) {
                Toast.makeText(AccountActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            //显示进度对话框
            pDialog = ProgressDialog.show(AccountActivity.this, null, "请稍候", false, true);
            //开启一个新线程，执行更新账户的操作
            action="update";
            new Thread(accountRunnable).start();
        }
    }
    //弹出类型选择的对话框
    private void changeType(final String title,final int position,String[] items){
        final String[] items2;
        //创建一个Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(AccountActivity.this);
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
                data.get(position).put("key2",items2[which]);
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
    //弹出号码编辑框
    private void change(final int position,final String title){
        final AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setIcon(android.R.drawable.btn_star);
        final EditText edtInput=new EditText(this);
        builder.setView(edtInput);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String input=edtInput.getText().toString();
                if(TextUtils.isEmpty(input)){
                    //如果号码编辑框是空，让对话框不消失
                    DialogUtil.dialogClose(dialog,false);
                    //提示用户错误信息
                    edtInput.setError(title);
                    edtInput.requestFocus();
                }else{
                    DialogUtil.dialogClose(dialog,true);
                    data.get(position).put("key2",input);
                    adapter.notifyDataSetChanged();
                }
            }
        });
        builder.setNegativeButton("取消",new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DialogUtil.dialogClose(dialog,true);
            }
        });
        builder.create().show();
    }
    //界面左上角箭头，点击，回到上一个界面
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}

