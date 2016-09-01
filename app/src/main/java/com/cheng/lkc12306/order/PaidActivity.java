package com.cheng.lkc12306.order;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.bean.OrderNotPaidItem;
import com.cheng.lkc12306.utils.ZxingUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaidActivity extends AppCompatActivity {
private TextView tvOrderId;
    private ListView lvOrderPaid;
    private Button btnQrCode;
    private SimpleAdapter adapter;
    private List<Map<String,Object>> data=null;
    private String orderId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paid);
        initView();
    }

    private void initView(){
        tvOrderId = (TextView)findViewById(R.id.tvOrderId);
        lvOrderPaid = (ListView)findViewById(R.id.lvOrderPaid);
        btnQrCode = (Button)findViewById(R.id.btnQrCode);
        data=getData();
        tvOrderId.setText("您的订单编号为："+orderId);
        adapter=new SimpleAdapter(PaidActivity.this,data,R.layout.item_order_paid
                ,new String[]{"name","trainNo","date","seatNO"}
                ,new int[]{R.id.tvName,R.id.tvTrainNo,R.id.tvDate,R.id.tvSeatNO});
        lvOrderPaid.setAdapter(adapter);
        btnQrCode.setOnClickListener(new BtnQrcodeListener());
        lvOrderPaid.setOnItemClickListener(new LvOrderPaidListener());

    }
    private class LvOrderPaidListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //创建一个Builder对象,改签或退票的选择对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(PaidActivity.this);
            //设置标题
            builder.setTitle("你选择操作");
            builder.setIcon(android.R.drawable.btn_star);
            final String[] items = new String[]{"退票", "改签"};
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                   // tvShow.setText(items[which]);
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //此在此处实现取消逻辑代码
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }
    }
private class BtnQrcodeListener implements View.OnClickListener{
    @Override
    public void onClick(View v) {
        final AlertDialog.Builder builder=new AlertDialog.Builder(PaidActivity.this);
        builder.setTitle("我的二维码");
        builder.setIcon(android.R.drawable.btn_star);
        ImageView imQrCode=new ImageView(PaidActivity.this);
        builder.setView(imQrCode);
        //生成二维码
        ZxingUtils.createQRImage("订单号："+orderId,imQrCode,300,300);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              dialog.dismiss();
            }
        });
        builder.create().show();

    }
}
    /**
     * 获取数据
     * @return
     */
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
