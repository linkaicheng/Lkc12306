package com.cheng.lkc12306.my;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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


public class ContactEditActivity extends AppCompatActivity {
    private ListView lvContactEdit;
    List<Map<String, Object>> data;
    SimpleAdapter adapter;

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
        Intent intent = getIntent();
        Map<String, Object> contact = (Map<String, Object>) intent.getSerializableExtra("contact");
        data = getData(contact);
        adapter = new SimpleAdapter(ContactEditActivity.this, data, R.layout.item_my_contact_edit,
                new String[]{"key1", "key2", "key3"},
                new int[]{R.id.tvContactEditKey,
                        R.id.tvContactEditValue, R.id.imContactEditFlag});
        lvContactEdit.setAdapter(adapter);
        lvContactEdit.setOnItemClickListener(new LvContactEditOnItCkListener());

    }

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
        //创建一个Builder对象
        AlertDialog.Builder builder = new AlertDialog.Builder(ContactEditActivity.this);
        //设置标题
        builder.setTitle("你选择乘客类型");
        builder.setIcon(android.R.drawable.btn_star);
        final String[] items = new String[]{"成人", "儿童", "学生", "其他"};
    /*
    * 第一个参数为单选按钮的数据集合
    * 第二个为默认的勾选的项
    * 第三个参数为监听器
    * */
        builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
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
        switch (id) {
            case R.id.deleteContact:
                Toast.makeText(ContactEditActivity.this, "*********", Toast.LENGTH_SHORT).show();
                break;
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
