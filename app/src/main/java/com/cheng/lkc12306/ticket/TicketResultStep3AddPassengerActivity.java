package com.cheng.lkc12306.ticket;

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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.bean.Passenger;
import com.cheng.lkc12306.bean.Step3AddPassViewHolder;
import com.cheng.lkc12306.my.ContactAddActivity;
import com.cheng.lkc12306.my.ContactEditActivity;
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

public class TicketResultStep3AddPassengerActivity extends AppCompatActivity {
    private ProgressDialog pDialog;
    //自定义的适配器
    private Adapter adapter=null;
    //数据源
    private List<Map<String,Object>> data=null;
//联系人ListView
    private ListView lvStep3AddPass;
    //添加乘车人按钮
    private Button btnAddPassenger;
    //要回传的联系人（即保存选中的联系人)
    private List<Map<String,Object>> passengers=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_result_step3_add_passenger);
        initView();
    }
    private void initView(){
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        lvStep3AddPass = (ListView)findViewById(R.id.lvStep3AddPass);
        btnAddPassenger = (Button)findViewById(R.id.btnAddPassenger);
        passengers=new ArrayList<>();
        //初始化数据源
        data = new ArrayList<Map<String, Object>>();
        adapter = new Adapter(data);
        lvStep3AddPass.setAdapter(adapter);
        //联系人列表的点击监听，点击进入编辑联系人界面
        lvStep3AddPass.setOnItemClickListener(new LvStep3AddPassOnItCkListener());
        //添加乘车人按钮点击监听
        btnAddPassenger.setOnClickListener(new BtnAddPassengerListener());
    }
    //添加乘车人按钮点击监听，将选中的联系人回传
    private class BtnAddPassengerListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent=getIntent();
            intent.putExtra("passengers", (Serializable) passengers);
            setResult(RESULT_OK,intent);
            finish();
        }
    }
    //联系人列表的点击监听
    private class LvStep3AddPassOnItCkListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //点击后跳转到相应的编辑界面
            Intent intent = new Intent(TicketResultStep3AddPassengerActivity.this, ContactEditActivity.class);
            //获取相应联系人的信息
            Map<String, Object> contact = data.get(position);
            //将联系人的信息以序列化的方式保存到intent
            intent.putExtra("contact", (Serializable) contact);
            startActivity(intent);
        }
    }
    //重写onResume方法，执行联系人数据的获取操作
    @Override
    protected void onResume() {
        super.onResume();
        //判断网络是否可用
        if (!NetUtils.check(TicketResultStep3AddPassengerActivity.this)) {
            Toast.makeText(TicketResultStep3AddPassengerActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        //显示进度对话框
        pDialog = ProgressDialog.show(TicketResultStep3AddPassengerActivity.this, null, "请稍候。。", false, true);
        new Thread() {
            @Override
            public void run() {
                Message msg = new Message();
                try {
                    HttpURLConnection conn = URLConnManager.getHttpURLConnection(Constant.HOST + "/otn/TicketPassengerList");
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
                    Toast.makeText(TicketResultStep3AddPassengerActivity.this, "服务器错误，请重试", Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    Toast.makeText(TicketResultStep3AddPassengerActivity.this, "请重新登录", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    //自定义适配器
    private class Adapter extends BaseAdapter {
        List<Map<String, Object>> data;

        public Adapter(List<Map<String, Object>> data) {
            this.data = data;
        }

        public Adapter() {
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            //自定义的ViewHolder来优化适配器
            Step3AddPassViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new Step3AddPassViewHolder();
                convertView = View.inflate(TicketResultStep3AddPassengerActivity.this
                        , R.layout.item_ticket_result_step3_add_passenger
                        , null);
                viewHolder.cbStep3AddPassenger= (CheckBox) convertView.findViewById(R.id.cbStep3AddPassenger);
                viewHolder.imStep3ContactDetail= (ImageView) convertView.findViewById(R.id.imStep3ContactDetail);
                viewHolder.tvStep3ContactIdCard= (TextView) convertView.findViewById(R.id.tvStep3ContactIdCard);
                viewHolder.tvStep3ContactName= (TextView) convertView.findViewById(R.id.tvStep3ContactName);
                viewHolder.tvStep3ContactTel= (TextView) convertView.findViewById(R.id.tvStep3ContactTel);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (Step3AddPassViewHolder) convertView.getTag();
            }
            viewHolder.tvStep3ContactTel.setText((String) data.get(position).get("tel"));
            viewHolder.tvStep3ContactName.setText((String) data.get(position).get("name"));
            viewHolder.tvStep3ContactIdCard.setText((String)data.get(position).get("idCard"));
            viewHolder.imStep3ContactDetail.setImageResource(R.mipmap.forward_25);
            viewHolder.cbStep3AddPassenger.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(isChecked){
                     passengers.add(data.get(position));
                    }else{
                        passengers.remove(data.get(position));
                    }
                }
            });
            return convertView;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
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
                Intent intent = new Intent(TicketResultStep3AddPassengerActivity.this, ContactAddActivity.class);
                startActivity(intent);
                break;
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
