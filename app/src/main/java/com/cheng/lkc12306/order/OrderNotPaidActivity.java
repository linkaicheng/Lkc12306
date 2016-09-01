package com.cheng.lkc12306.order;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.bean.OrderNotPaidItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OrderNotPaidActivity extends AppCompatActivity {
private TextView tvOrderId,tvCancel,tvPay;
    private ListView lvOrderNotPaid;
    private List<Map<String,Object>> data=null;
    private SimpleAdapter adapter=null;
    private String   orderId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_not_paid);
        initView();
    }
    private void initView(){
        lvOrderNotPaid = (ListView)findViewById(R.id.lvOrder);
        tvCancel = (TextView)findViewById(R.id.tvCancel);
        tvOrderId = (TextView)findViewById(R.id.tvOrderId);
        tvPay = (TextView)findViewById(R.id.tvPay);
        lvOrderNotPaid = (ListView)findViewById(R.id.lvOrderNotPaid);

        data=getData();
        tvOrderId.setText("订单提交成功，您的订单编号为："+orderId);
        adapter=new SimpleAdapter(OrderNotPaidActivity.this,data
                ,R.layout.item_order_not_paid
                ,new String[]{"name","trainNo","date","seatNO"}
                ,new int[]{R.id.tvName,R.id.tvTrainNo,R.id.tvDate,R.id.tvSeatNO});
        lvOrderNotPaid.setAdapter(adapter);
        tvCancel.setOnClickListener(new MyOnClickListener());
        tvPay.setOnClickListener(new MyOnClickListener());
    }
    //取消订单和确认支付的点击监听
    private class MyOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent=getIntent();
            switch (v.getId()) {
                case  R.id.tvCancel:
                    intent.putExtra("result","cancel");
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
                case  R.id.tvPay:
                    intent.putExtra("result","pay");
                    setResult(RESULT_OK,intent);
                    finish();
                    break;
            }
        }
    }

    private List<Map<String,Object>>  getData(){
        Intent intent=getIntent();
        List<OrderNotPaidItem> items= (List<OrderNotPaidItem>) intent.getSerializableExtra("orderItems");
        orderId=intent.getStringExtra("orderId");
        List<Map<String,Object>> data2=new ArrayList<>();
        for(OrderNotPaidItem item:items){
            Map<String,Object> row=new HashMap<>();
            row.put("name",item.getName());
            row.put("trainNo",item.getTrainNo());
            row.put("date",item.getDate());
            row.put("seatNO",item.getSeat());
            data2.add(row);
        }
        return data2;


    }
}
