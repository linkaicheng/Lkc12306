package com.cheng.lkc12306.ticket;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.bean.Seat;
import com.cheng.lkc12306.bean.Train;
import com.cheng.lkc12306.utils.Constant;
import com.cheng.lkc12306.utils.NetUtils;
import com.cheng.lkc12306.utils.URLConnManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketResultStep1Activity extends AppCompatActivity {
    private TextView tvTicketResultStep1DateTitle, tvTicketResultStep1StationTitle, tvTicketResultStep1Before, tvTicketResultStep1After;
    private ListView lvTicketResultStep1;
    private List<Map<String, Object>> data = null;
    private SimpleAdapter adapter;
    private ProgressDialog pDialog;
    //出发站点
    private String stationFrom;
    //目的地
    private String stationTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_result_step1);
        initView();
    }

    //初始化控件，并设置监听
    private void initView() {
        tvTicketResultStep1DateTitle = (TextView) findViewById(R.id.tvTicketResultStep1DateTitle);
        tvTicketResultStep1StationTitle = (TextView) findViewById(R.id.tvTicketResultStep1StationTitle);
        tvTicketResultStep1Before = (TextView) findViewById(R.id.tvTicketResultStep1Before);
        tvTicketResultStep1After = (TextView) findViewById(R.id.tvTicketResultStep1After);
        lvTicketResultStep1 = (ListView) findViewById(R.id.lvTicketResultStep1);
        //获得上一页的始发站、终点站和出发日期，并显示在标签上
        Intent intent = getIntent();
        stationFrom = intent.getStringExtra("ticketStationFrom");
        stationTo = intent.getStringExtra("ticketStationTo");
        String dateFrom = intent.getStringExtra("ticketDateFrom");
        tvTicketResultStep1DateTitle.setText(dateFrom);
        tvTicketResultStep1StationTitle.setText(stationFrom + "-" + stationTo);
        //为前一天，后一天设置监听
        tvTicketResultStep1Before.setOnClickListener(new TvTicketBeAndAfListerner());
        tvTicketResultStep1After.setOnClickListener(new TvTicketBeAndAfListerner());
        //创建适配器
        data = new ArrayList<>();
        adapter = new SimpleAdapter(TicketResultStep1Activity.this, data
                , R.layout.item_ticket_result_step1
                , new String[]{
                "trainNo", "flg1", "flg2", "timeFrom"
                , "timeTo", "seat1", "seat2", "seat3", "seat4"}
                , new int[]{
                R.id.tvTicketResultStep1TrainNo,
                R.id.imgTicketResultStep1Flg1,
                R.id.imgTicketResultStep1Flg2,
                R.id.tvTicketResultStep1TimeFrom,
                R.id.tvTicketResultStep1TimeTo,
                R.id.tvTicketResultStep1Seat1,
                R.id.tvTicketResultStep1Seat2,
                R.id.tvTicketResultStep1Seat3,
                R.id.tvTicketResultStep1Seat4,});
        lvTicketResultStep1.setAdapter(adapter);
        //判断网络是否可用
        if (!NetUtils.check(TicketResultStep1Activity.this)) {
            Toast.makeText(TicketResultStep1Activity.this, "当前网络不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        String startTrainDate = dateFrom.split(" ")[0];
        //异步任务方式从服务器获取数据
        new Step1Task(startTrainDate).execute();
        lvTicketResultStep1.setOnItemClickListener(new LvTicketResultSetp1OnItListener());

    }
    //列车列表的点击监听
    private class LvTicketResultSetp1OnItListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //跳转到显示列车详细信息的界面，并携带出发站点，目的地，出发时间，列车编号
            Intent intent=new Intent(TicketResultStep1Activity.this,TicketResultStep2Activity.class);
            String fromStationName=stationFrom;
            String toStationName=stationTo;
            String startTrainDate=tvTicketResultStep1DateTitle.getText().toString();
            String trainNo=(String)data.get(position).get("trainNo");
            intent.putExtra("fromStationName",fromStationName);
            intent.putExtra("toStationName",toStationName);
            intent.putExtra("startTrainDate",startTrainDate);
            intent.putExtra("trainNo",trainNo);
            startActivity(intent);
        }
    }

    //执行从服务器获取数据的异步任务
    private class Step1Task extends AsyncTask<Void, Void, Object> {
        String startTrainDate;

        public Step1Task(String startTrainDate) {
            this.startTrainDate = startTrainDate;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //显示进度对话框
            pDialog = ProgressDialog.show(TicketResultStep1Activity.this, null, "正从服务器获取数据，请稍候", false, true);
        }

        @Override
        protected Object doInBackground(Void... params) {
            String result=null;
            InputStream inputStream=null;
            HttpURLConnection conn=null;
            try {
                //获取连接
                 conn = URLConnManager.getHttpURLConnection(Constant.HOST
                        + "/otn/TrainList");
                //获取保存的cookie
                SharedPreferences sp = getSharedPreferences("user", Context.MODE_PRIVATE);
                String cookieValue = sp.getString("cookie", "");
                //设置请求属性
                conn.setRequestProperty("cookie", cookieValue);
                //封装请求参数
                List<NameValuePair> paramList = new ArrayList<>();
                paramList.add(new BasicNameValuePair("fromStationName", stationFrom));
                paramList.add(new BasicNameValuePair("toStationName", stationTo));
                paramList.add(new BasicNameValuePair("startTrainDate", startTrainDate));
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
                    List<Train> trains=gson.fromJson(reponse,new TypeToken<List<Train>>(){}.getType());
                    //成功时返回的是List<Train>对象
                    return trains;
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
            //失败时返回的是字符串result
            return result;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            //每次查询后清空以前查到的数据
            data.clear();
            List<Train> trains=null;
            //关闭进度对话框
            if(pDialog!=null){
                pDialog.dismiss();
            }

            if(o instanceof List<?>){
                trains=(List<Train>)o;
                if(trains.size()==0){
                    Toast.makeText(TicketResultStep1Activity.this, "没有查询到相关的车次", Toast.LENGTH_SHORT).show();
                    //查询到空，将数据清空，更新界面
                    data.clear();
                    adapter.notifyDataSetChanged();
                }else{
                    // 往data中填充数据
                    for(Train train:trains){
                        Map<String,Object> row=new HashMap<>();
                        row.put("trainNo",train.getTrainNo());
                        if(train.getStartStationName().equals(train.getFromStationName())){
                            row.put("flg1",R.mipmap.flg_shi);
                        }else{
                            row.put("flg1",R.mipmap.flg_guo);
                        }
                        if(train.getEndStationName().equals(train.getToStationName())){
                            row.put("flg2",R.mipmap.flg_zhong);
                        }else{
                            row.put("flg2",R.mipmap.flg_guo);
                        }
                        row.put("timeFrom",train.getStartTime());
                        row.put("timeTo",train.getArriveTime());
                        Map<String,Seat> seats=train.getSeats();
                        String[] seatKey=new String[]{"seat1", "seat2", "seat3", "seat4"};
                        int i=0;
                        for(String key:seats.keySet()){
                            Seat seat=seats.get(key);
                            row.put(seatKey[i++],seat.getSeatName()+":"+seat.getSeatNum());
                        }
                        data.add(row);
                    }
                    adapter.notifyDataSetChanged();
                }
            }else if(o instanceof String){
                if("2".equals(o)){
                    Toast.makeText(TicketResultStep1Activity.this, "服务器错误", Toast.LENGTH_SHORT).show();
                }else if("3".equals(o)){
                    Toast.makeText(TicketResultStep1Activity.this, "请重新登录", Toast.LENGTH_SHORT).show();
                }

            }else{
                Toast.makeText(TicketResultStep1Activity.this, "服务器错误", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //前一天后一天的监听
    private class TvTicketBeAndAfListerner implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //获取原来的日期
            String oldDateFrom = tvTicketResultStep1DateTitle.getText().toString();
            Calendar calendar = Calendar.getInstance();
            int year = Integer.parseInt(oldDateFrom.split("-")[0]);
            //Calendar中月份从0开始，因此减1，相应的从calendar中获取月份作为显示的时候要加1
            int month = Integer.parseInt(oldDateFrom.split("-")[1]) - 1;
            int day = Integer.parseInt(oldDateFrom.split("-")[2].split(" ")[0]);
            calendar.set(year, month, day);
            switch (v.getId()) {
                case R.id.tvTicketResultStep1Before:
                    //减一天
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    break;
                case R.id.tvTicketResultStep1After:
                    //加一天
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    break;
            }
            //根据日期获取星期
            String weekday = DateUtils.formatDateTime(TicketResultStep1Activity.this
                    , calendar.getTimeInMillis()
                    , DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_WEEKDAY);
            //拼接日期显示字符串
            String ticketDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1)
                    + "-" + calendar.get(Calendar.DAY_OF_MONTH) + " " + weekday;
            tvTicketResultStep1DateTitle.setText(ticketDate);
            //检查网络是否可用
            if (!NetUtils.check(TicketResultStep1Activity.this)) {
                Toast.makeText(TicketResultStep1Activity.this, "当前网络不可用", Toast.LENGTH_SHORT).show();
                return;
            }
            //获取截掉星期后的时间，传给异步任务
            String startTrainDate = tvTicketResultStep1DateTitle.getText().toString().split(" ")[0];
            //开启异步任务
            new Step1Task(startTrainDate).execute();

        }
    }
}
