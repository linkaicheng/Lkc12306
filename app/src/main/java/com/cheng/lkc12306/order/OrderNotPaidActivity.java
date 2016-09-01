package com.cheng.lkc12306.order;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.bean.OrderNotPaidItem;
import com.cheng.lkc12306.utils.Constant;
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

public class OrderNotPaidActivity extends AppCompatActivity {
private TextView tvOrderId,tvCancel,tvPay;
    private ListView lvOrderNotPaid;
    private List<Map<String,Object>> data=null;
    private SimpleAdapter adapter=null;
    private String   orderId;
    private ProgressDialog pDialog;
    private String action;
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
        //取消订单
        tvCancel.setOnClickListener(new MyOnClickListener());
        //确认支付
        tvPay.setOnClickListener(new MyOnClickListener());
    }

    /**
     * 取消订单和确认支付的点击监听
     */
    private class MyOnClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            //确认支付和取消订单的url不同，用action区别，用同一个异步任务处理
            switch (v.getId()) {
                case R.id.tvCancel:
                    action="/otn/Cancel";
                   new CancelOrderTask().execute();
                    break;
                case  R.id.tvPay://确认支付
                    action="/otn/Pay";
                    new CancelOrderTask().execute();
                    break;
            }
        }
    }
    /**
     * 异步任务，向服务器发出取消订单请求
     */
    private class CancelOrderTask extends AsyncTask<Void,Void,String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog= ProgressDialog.show(OrderNotPaidActivity.this,null,"取消订单中，请稍候",false,true);
        }

        /**
         * 发出取消订单，请求参数：orderId,cookie
         * @param params
         * @return
         */
        @Override
        protected String doInBackground(Void... params) {
            String result=null;
            InputStream inputStream=null;
            HttpURLConnection conn=null;
            try {
                //获取连接
                conn = URLConnManager.getHttpURLConnection(Constant.HOST
                        + action);
                //获取保存的cookie
                SharedPreferences sp = getSharedPreferences("user", Context.MODE_PRIVATE);
                String cookieValue = sp.getString("cookie", "");
                //设置请求属性
                conn.setRequestProperty("cookie", cookieValue);
                //封装请求参数
                List<NameValuePair> paramList = new ArrayList<>();
                paramList.add(new BasicNameValuePair("orderId",orderId));
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
            Intent intent=getIntent();
            switch (s) {
                case "1":
                    if(action.equals("/otn/Cancel")){
                        intent.putExtra("result","cancel");
                        setResult(RESULT_OK,intent);
                        finish();
                    }else if(action.equals("/otn/Pay")){
                        intent.putExtra("result","pay");
                        setResult(RESULT_OK,intent);
                        finish();
                    }
                    break;
                case "2":
                    Toast.makeText(OrderNotPaidActivity.this, "服务器错误2", Toast.LENGTH_SHORT).show();
                    break;
                case "3":
                    Toast.makeText(OrderNotPaidActivity.this, "请重新登录", Toast.LENGTH_SHORT).show();
                    break;
            }

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
