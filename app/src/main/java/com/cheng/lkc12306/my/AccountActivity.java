package com.cheng.lkc12306.my;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.utils.DialogUtil;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        lvAccount = (ListView) findViewById(R.id.lvAccount);
        btnAccountSave = (Button) findViewById(R.id.btnAccountSave);
        data = getData();
        adapter = new SimpleAdapter(AccountActivity.this, data,
                R.layout.item_my_account, new String[]{"key1", "key2", "key3"},
                new int[]{R.id.tvAccountKey, R.id.tvAccountValue, R.id.imAccountFlag});
        lvAccount.setAdapter(adapter);
        lvAccount.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {

                    case 3:
                        String[] passengerItem = new String[]{"成人", "儿童", "学生", "其他"};
                        changeType("请你选择乘客类型", position, passengerItem);

                        break;
                    case 4:
                        change(position, "请输电话号码");
                        break;
                }
            }
        });
        btnAccountSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(AccountActivity.this, "AccountActivity:保存", Toast.LENGTH_SHORT).show();
            }
        });
    }
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
                    DialogUtil.dialogClose(dialog,false);
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
    private List<Map<String, Object>> getData() {
        List<Map<String,Object>> data=new ArrayList<>();
        Map<String,Object> row=new HashMap<>();

        row.put("key1","姓名");
        row.put("key2","林凯城");
        row.put("key3",null);
        data.add(row);

        row=new HashMap<>();
        row.put("key1","证件类型");
        row.put("key2","身份证");
        row.put("key3",null);
        data.add(row);

        row=new HashMap<>();
        row.put("key1","证件号码");
        row.put("key2","44541525545521");
        row.put("key3",null);
        data.add(row);

        row=new HashMap<>();
        row.put("key1","乘客类型");
        row.put("key2","学生");
        row.put("key3",R.mipmap.forward_25);
        data.add(row);

        row=new HashMap<>();
        row.put("key1","电话");
        row.put("key2","15587845874");
        row.put("key3",R.mipmap.forward_25);
        data.add(row);
        return data;
    }
    }

