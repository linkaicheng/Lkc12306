package com.cheng.lkc12306.order;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.cheng.lkc12306.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OrderNotPaidActivity extends AppCompatActivity {
private TextView tvOrderId,tvCancel,tvPay;
    private ListView lvOrderNotPaid;
    private List<Map<String,Object>> data=null;
    private SimpleAdapter adapter=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_not_paid);
        initView();
    }
    private void initView(){
        lvOrderNotPaid = (ListView)findViewById(R.id.lvOrder);
        tvCancel = (TextView)findViewById(R.id.tvCancel);
        tvPay = (TextView)findViewById(R.id.tvPay);
        lvOrderNotPaid = (ListView)findViewById(R.id.lvOrderNotPaid);
        data=new ArrayList<>();
        adapter=new SimpleAdapter(OrderNotPaidActivity.this,data
                ,R.layout.item_order_not_paid
                ,new String[]{"name","trainNo","date","seatNO"}
                ,new int[]{R.id.tvName,R.id.tvTrainNo,R.id.tvDate,R.id.tvSeatNO});
        lvOrderNotPaid.setAdapter(adapter);
    }
}
