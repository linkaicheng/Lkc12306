package com.cheng.lkc12306.order;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.bean.OrderNotPaidItem;
import com.cheng.lkc12306.utils.Constant;
import com.cheng.lkc12306.utils.URLConnManager;
import com.cheng.lkc12306.utils.ZxingUtils;
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

public class PaidActivity extends AppCompatActivity {
private TextView tvOrderId;
    private ListView lvOrderPaid;
    private Button btnQrCode;
    private SimpleAdapter adapter;
    private List<Map<String,Object>> data=null;
    private String orderId;

    private  List<OrderNotPaidItem>  items=null;
    private ProgressDialog pDialog;
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
        lvOrderPaid.setOnItemClickListener(new LvOrderPaidListener(items));




    }
    private class LvOrderPaidListener implements AdapterView.OnItemClickListener{
        List<OrderNotPaidItem> items2;
        public LvOrderPaidListener(List<OrderNotPaidItem> items){
            items2=items;
        }
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //创建一个Builder对象,改签或退票的选择对话框
            AlertDialog.Builder builder = new AlertDialog.Builder(PaidActivity.this);
            //设置标题
            builder.setTitle("请选择操作");
            builder.setIcon(android.R.drawable.btn_star);
            final String[] items = new String[]{"退票", "改签"};
            builder.setItems(items, new MyDialogClickListener(items,position,items2));

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
    private class MyDialogClickListener implements DialogInterface.OnClickListener{
        String[] items;
        int position;
        List<OrderNotPaidItem> items2;
        public MyDialogClickListener(String[] items,int position,List<OrderNotPaidItem> items2){
            this.items=items;
            this.position=position;
            this.items2=items2;


        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //退票
            if(items[which].equals("退票")){
                String passengerId=items2.get(position).getId();
                String passengerIdType=items2.get(position).getIdType();
                new RefundTask().execute(passengerId,passengerIdType,orderId);
                Intent intent=getIntent();
                intent.putExtra("result","退票");
                setResult(RESULT_OK,intent);
                finish();
            }else if(items[which].equals("改签")){//改签
Toast.makeText(PaidActivity.this, "改签", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 异步任务，向服务器发出退票请求
     */
    private class RefundTask extends AsyncTask<String,Void,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog= ProgressDialog.show(PaidActivity.this,null,"退票中，请稍候",false,true);
        }

        /**
         * 退票，请求参数：orderId,id,idType
         * @param params
         * @return
         */
        @Override
        protected String doInBackground(String... params) {
            String result=null;
            InputStream inputStream=null;
            HttpURLConnection conn=null;

            try {
                String id=params[0];
                String idType=params[1];
                String orderId=params[2];

                //获取连接
                conn = URLConnManager.getHttpURLConnection(Constant.HOST
                        + "/otn/Refund");
                //获取保存的cookie
                SharedPreferences sp = getSharedPreferences("user", Context.MODE_PRIVATE);
                String cookieValue = sp.getString("cookie", "");
                //设置请求属性
                conn.setRequestProperty("cookie", cookieValue);
                //封装请求参数
                List<NameValuePair> paramList = new ArrayList<>();
                paramList.add(new BasicNameValuePair("orderId",orderId));
                paramList.add(new BasicNameValuePair("id",id));
                paramList.add(new BasicNameValuePair("idType",idType));
                URLConnManager.postParams(conn.getOutputStream(), paramList);
                //连接
                conn.connect();
                //获得响应码
                int code=conn.getResponseCode();
                if(code==200){//连接成功
                    //获取服务器返回的数据
                    inputStream=conn.getInputStream();
                    //将输入流转成字符串
                    String reponse=URLConnManager.converStreamToString(inputStream);
                    //用Gson解析数据
                    Gson gson=new Gson();
                    result=gson.fromJson(reponse,String.class);
                    return result;
                }else{//连接失败
                    result="2";
                }

            } catch (IOException e) {
                e.printStackTrace();
                result = "2";
            }catch (JsonSyntaxException e){
                e.printStackTrace();
                result="3";
            }
            finally {
                //关闭输入流和连接等资源
                if(inputStream!=null){
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if(conn!=null){
                    conn.disconnect();
                }
            }
            return result;
        }

        /**
         * 处理请求结果，返回主界面
         * @param s
         */
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //关闭进度对话框
            if (pDialog != null) {
                pDialog.dismiss();
            }

            switch (s) {
                case "1":
                    Toast.makeText(PaidActivity.this, "退票成功", Toast.LENGTH_SHORT).show();
                    break;
                case "0":
                    Toast.makeText(PaidActivity.this, "退票失败", Toast.LENGTH_SHORT).show();
                    break;
                case "2":
                    Toast.makeText(PaidActivity.this, "服务器错误2", Toast.LENGTH_SHORT).show();
                    break;
                case "3":
                    Toast.makeText(PaidActivity.this, "请重新登录", Toast.LENGTH_SHORT).show();
                    break;
            }

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
          items= (List<OrderNotPaidItem>) intent.getSerializableExtra("orderItems");
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
