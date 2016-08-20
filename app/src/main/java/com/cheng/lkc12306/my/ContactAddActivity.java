package com.cheng.lkc12306.my;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.cheng.lkc12306.utils.DialogUtil;

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
            Toast.makeText(ContactAddActivity.this, "ContactAddActivity:保存", Toast.LENGTH_SHORT).show();
        }
    }
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
