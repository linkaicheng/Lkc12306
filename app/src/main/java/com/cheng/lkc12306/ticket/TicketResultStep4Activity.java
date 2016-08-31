package com.cheng.lkc12306.ticket;

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

import com.cheng.lkc12306.MainActivity;
import com.cheng.lkc12306.R;
import com.cheng.lkc12306.utils.Constant;
import com.cheng.lkc12306.utils.NetUtils;
import com.cheng.lkc12306.utils.URLConnManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketResultStep4Activity extends AppCompatActivity {
    private TextView tvOrderId//订单编号及提交订单的结果提示
            ,tvPayAfter//暂不支付
            ,tvPayNow;//确定支付
    private ListView lvStep4;
    private SimpleAdapter adapter;
    List<Map<String ,Object>> data;
    //code 用于生成唯一的订单号
    private static long code;
    //用于生成唯一的座位
    private static int seatNum;
    // 订单号
    private long orderId;
    //进度对话框
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_result_step4);
        initView();
    }

    /**
     * 初始化界面并设置监听
     */
    private void initView(){
        tvOrderId = (TextView)findViewById(R.id.tvOrderId);
        tvPayAfter = (TextView)findViewById(R.id.tvPayAfter);
        tvPayNow = (TextView)findViewById(R.id.tvPayNow);
        lvStep4 = (ListView)findViewById(R.id.lvStep4);
        //获取上一界面传过来的数据，列车编号，出发日期，乘车人姓名
        Intent intent=getIntent();
        String trainNo=intent.getStringExtra("trainNo");
        String startDate=intent.getStringExtra("startTrainDate");
        List<String> names= (List<String>) intent.getSerializableExtra("names");
        //订单编号
        orderId=nextCode();
        tvOrderId.setText("订单提交成功，您的订单编号为："+orderId);
        data=new ArrayList<>();
        for(String name:names){
            Map<String,Object> row =new HashMap<>();
            row.put("name",name);
            row.put("trainNo",trainNo);
            row.put("startDate",startDate);
            //座位号
            int seat=nextSeat();
            row.put("seat","6车"+seat+"号");
            data.add(row);

        }
        //设置适配器
        adapter = new SimpleAdapter(this, data, R.layout.item_ticket_result_step4,
                new String[]{"name", "trainNo", "startDate","seat"}, new int[]{R.id.tvStep4Name
                , R.id.tvStep4TrainNo
                , R.id.tvStep4StartDate
                ,R.id.tvStep4Seat});
        lvStep4.setAdapter(adapter);
        //暂不支付,点击暂不支付，返回主界面
        tvPayAfter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(TicketResultStep4Activity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        //确定支付，向服务器发请求
        tvPayNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断网络是否可用
                if(!NetUtils.check(TicketResultStep4Activity.this)){
                    Toast.makeText(TicketResultStep4Activity.this, "当前网络不可用", Toast.LENGTH_SHORT).show();
                    return;
                }
                //开启异步任务，向服务器请求数据
                new Step4Task().execute();
            }
        });
    }


    /**
     * 异步任务，向服务器发出支付请求
     */
    private class Step4Task extends AsyncTask<Void,Void,String>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog= ProgressDialog.show(TicketResultStep4Activity.this,null,"支付中，请稍候",false,true);
        }

        /**
         * 发出支付请求，请求参数：orderId,cookie
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
                        + "/otn/Pay");
                //获取保存的cookie
                SharedPreferences sp = getSharedPreferences("user", Context.MODE_PRIVATE);
                String cookieValue = sp.getString("cookie", "");
                //设置请求属性
                conn.setRequestProperty("cookie", cookieValue);
                //封装请求参数
                List<NameValuePair> paramList = new ArrayList<>();
                paramList.add(new BasicNameValuePair("order",String.valueOf(orderId)));
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
                    //成功时返回的是Train对象
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
         * 处理请求结果，支付成功，跳转到支付成功界面，
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
                    //支付成功，将订单编号传给支付成功界面
                    Toast.makeText(TicketResultStep4Activity.this, "支付成功", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(TicketResultStep4Activity.this, TicketResultStep5Activity.class);
                    intent.putExtra("orderId",orderId);
                    startActivity(intent);
                    finish();
                    break;
                case "2":
                    Toast.makeText(TicketResultStep4Activity.this, "服务器错误2", Toast.LENGTH_SHORT).show();
                    break;
                case "3":
                    Toast.makeText(TicketResultStep4Activity.this, "请重新登录", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    }

    /**
     * 生成唯一的订单编号
     * @return
     */
    public static synchronized long nextCode() {
        code++;
        String str = new SimpleDateFormat("yyyyMM").format(new Date());
        long m = Long.parseLong((str)) * 10000;
        m += code;
        return m;
    }

    /**
     * 生成唯一的座位
     * @return
     */
    public static synchronized int nextSeat() {
            seatNum++;
        return seatNum;
    }

}
