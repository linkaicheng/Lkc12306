package com.cheng.lkc12306.my;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.cheng.lkc12306.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class ContactActivity extends AppCompatActivity {


    private ListView lvContact;
    List<Map<String,Object>> data;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        lvContact = (ListView)findViewById(R.id.lvContact);
         data=getData();
        SimpleAdapter adapter=new SimpleAdapter(this,data,R.layout.item_my_contact,
                new String[]{"name","idCard","tel"},new int[]{R.id.tvContactName,R.id.tvContactIdCard
        ,R.id.tvContactTel});
        lvContact.setAdapter(adapter);
        lvContact.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(ContactActivity.this,ContactEditActivity.class);
                Map<String,Object> contact=data.get(position);
                intent.putExtra("contact", (Serializable) contact);

                startActivity(intent);
            }
        });



    }
    private List<Map<String,Object>> getData(){
        List<Map<String,Object>> data=new ArrayList<>();
        Map<String,Object> row=new HashMap<>();
        row.put("name","小白(成人)");
        row.put("idCard","身份证:445758445211525896");
        row.put("tel","电话号码:18812457854");
        data.add(row);

        row=new HashMap<>();
        row.put("name","小灰(学生)");
        row.put("idCard","身份证:444587544522365969");
        row.put("tel","电话号码:18812457854");
        data.add(row);
        row=new HashMap<>();
        row.put("name","小黑(成人)");
        row.put("idCard","身份证:444587544522365969");
        row.put("tel","电话号码:18812457854");
        data.add(row);
        return data;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
      switch (id) {
          case  R.id.addContact:
              Intent intent=new Intent(ContactActivity.this,ContactAddActivity.class);
              startActivity(intent);
              break;

          case android.R.id.home:
              finish();
              break;
      }

        return super.onOptionsItemSelected(item);
    }
}
