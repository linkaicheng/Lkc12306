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
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.bean.Seat;
import com.cheng.lkc12306.bean.Train;
import com.cheng.lkc12306.bean.ViewHolder;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketResultStep2Activity extends AppCompatActivity {
    //日期，站点标题
    private TextView tvTicketResultStep2DateTitle, tvTicketResultStep2StationTitle;
    //前一天，后一天
    private TextView tvTicketResultStep2Before, tvTicketResultStep2After;
    //listview
    private ListView lvTicketResultStep2;
    //出发站点
    private String stationFrom = null;
    //目的站点
    private String stationTo = null;
    //列车编号
    private String trainNo = null;
    //数据源
    private List<Map<String,Object>> data=null;
    private ProgressDialog pDialog=null;
    //自定义的适配器
    private Adapter adapter=null;
    //列车编号和历时
    private TextView tvStep2TrainNo,tvStep2DurationTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_result_step2);
        initView();
    }

    //初始或控件，并设置监听
    private void initView() {
        tvTicketResultStep2StationTitle = (TextView) findViewById(R.id.tvTicketResultStep2StationTitle);
        tvTicketResultStep2DateTitle = (TextView) findViewById(R.id.tvTicketResultStep2DateTitle);
        tvTicketResultStep2Before = (TextView) findViewById(R.id.tvTicketResultStep2Before);
        tvTicketResultStep2After = (TextView) findViewById(R.id.tvTicketResultStep2After);
        lvTicketResultStep2 = (ListView) findViewById(R.id.lvTicketResultStep2);
        tvStep2TrainNo = (TextView)findViewById(R.id.tvStep2TrainNo);
        tvStep2DurationTime = (TextView)findViewById(R.id.tvStep2DurationTime);

        //获取上一界面TicketResultStep1Activity带过来的数据
        Intent intent = getIntent();
        //获取出发站
        stationFrom = intent.getStringExtra("fromStationName");
        //获取到达站
        stationTo = intent.getStringExtra("toStationName");
        //获取列车编号
        trainNo = intent.getStringExtra("trainNo");
        //车发时间
        String dateFrom = intent.getStringExtra("startTrainDate");
        //显示出发地目的地，出发日期
        tvTicketResultStep2StationTitle.setText(stationFrom + "-" + stationTo);
        tvTicketResultStep2DateTitle.setText(dateFrom);
        //前一天，后一天设置监听
        tvTicketResultStep2Before.setOnClickListener(new TvTicketBeAndAfListener());
        tvTicketResultStep2After.setOnClickListener(new TvTicketBeAndAfListener());
        //设置适配器，BaseAdapter的方式
        //初始化数据源
        data=new ArrayList<Map<String,Object>>();
        adapter=new Adapter(data);
        lvTicketResultStep2.setAdapter(adapter);
        //判断网络是否可用
        if(!NetUtils.check(TicketResultStep2Activity.this)){
            Toast.makeText(TicketResultStep2Activity.this, "当前网络不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        //获取截去星期后的日期，传给异步任务
        String startTrainDate =dateFrom.split(" ")[0];
        //开启异步任务，向服务器请求数据
        new Step2Task(startTrainDate).execute();

    }

    /**
     * 执行获取列车详细信息的异步任务
     */
    private class Step2Task extends AsyncTask<Void,Void,Object >{
        String startTrainDate;

        public Step2Task(String startTrainDate) {
            this.startTrainDate = startTrainDate;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //弹出进度对话框
            pDialog=ProgressDialog.show(TicketResultStep2Activity.this,null,"获取列车信息中，请稍候",false,true);
        }

        /**
         * 执行网络连接，获取数据的操作，将得到的结果返回给onPostExecute方法处理
         * @param params
         * @return
         */
        @Override
        protected Object doInBackground(Void... params) {
            String result=null;
            InputStream inputStream=null;
            HttpURLConnection conn=null;
            try {
                //获取连接
                conn = URLConnManager.getHttpURLConnection(Constant.HOST
                        + "/otn/Train");
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
                paramList.add(new BasicNameValuePair("trainNo",trainNo));
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
                    Train train=gson.fromJson(reponse,Train.class);
                    //成功时返回的是Train对象
                    return train;
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

        /**
         * 处理doInBackground传来的结果，并解析数据，填充数据源，更新视图
         * @param o
         */
        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            Train train=null;
            //每次查询后清空以前查到的数据
            data.clear();
            //关闭进度对话框
            if(pDialog!=null){
                pDialog.dismiss();
            }
            //如果查询到的是空
            if(o==null){
                Toast.makeText(TicketResultStep2Activity.this, "没有查询到相关的车次信息", Toast.LENGTH_SHORT).show();
                //更新视图
                adapter.notifyDataSetChanged();
                return;
            }
            if(o instanceof Train){
                train=(Train)o;
                    tvStep2TrainNo.setText(train.getTrainNo());
                    String durationTime=train.getStartTime()+"-"+train.getArriveTime()
                            +",历时"+train.getDurationTime();
                    tvStep2DurationTime.setText(durationTime);
                    // 往data中填充数据
                    Map<String,Seat> seats=train.getSeats();
                   for(String key:seats.keySet()){
                    Map<String,Object> row=new HashMap<>();
                       Seat seat=seats.get(key);
                       row.put("seatName",seat.getSeatName());
                       row.put("seatNum",seat.getSeatNum());
                       row.put("seatPrice","¥"+seat.getSeatPrice());
                       data.add(row);
                   }
                    adapter.notifyDataSetChanged();

            }else if(o instanceof String){
                String result=(String)o;
                if("2".equals(result)){
                    Toast.makeText(TicketResultStep2Activity.this, "服务器错误2", Toast.LENGTH_SHORT).show();
                }else if("3".equals(result)){
                    Toast.makeText(TicketResultStep2Activity.this, "请重新登录", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(TicketResultStep2Activity.this, "获取数据失败", Toast.LENGTH_SHORT).show();
                }
                adapter.notifyDataSetChanged();

            }
            else{
                Toast.makeText(TicketResultStep2Activity.this, "服务器错误", Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }
        }
    }
    //自定义适配器
    private class Adapter extends BaseAdapter{
        List<Map<String,Object>> data;
        public Adapter(List<Map<String,Object>> data){
            this.data=data;
        }

        public Adapter() {
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //自定义的ViewHolder来优化适配器
            ViewHolder viewHolder;
            if(convertView==null){
                viewHolder=new ViewHolder();
                convertView=View.inflate(TicketResultStep2Activity.this,R.layout.item_ticket_result_step2
                ,null);
                viewHolder.tvSeatName= (TextView) convertView.findViewById(R.id.tvSeatName);
                viewHolder.tvSeatNum= (TextView) convertView.findViewById(R.id.tvSeatNum);
                viewHolder.tvSeatPrice= (TextView) convertView.findViewById(R.id.tvSeatPrice);
                viewHolder.btnOrder= (TextView) convertView.findViewById(R.id.btnOrder);
                convertView.setTag(viewHolder);
            }else{
                viewHolder= (ViewHolder) convertView.getTag();
            }
            viewHolder.tvSeatName.setText((String)data.get(position).get("seatName"));
            viewHolder.tvSeatNum.setText(data.get(position).get("seatNum")+"张");
            viewHolder.tvSeatPrice.setText((String)data.get(position).get("seatPrice"));
            viewHolder.btnOrder.setOnClickListener(new BtnOrderListener());

            return convertView;
        }
        //预订按钮的点击监听
        private class BtnOrderListener implements View.OnClickListener{
            @Override
            public void onClick(View v) {

            }
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }
    }

    //前一天，后一天的点击监听
    private class TvTicketBeAndAfListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //获取原来的日期
            String oldDateFrom = tvTicketResultStep2DateTitle.getText().toString();
            Calendar calendar = Calendar.getInstance();
            int year = Integer.parseInt(oldDateFrom.split("-")[0]);
            //Calendar中月份从0开始，因此减1，相应的从calendar中获取月份作为显示的时候要加1
            int month = Integer.parseInt(oldDateFrom.split("-")[1]) - 1;
            int day = Integer.parseInt(oldDateFrom.split("-")[2].split(" ")[0]);
            calendar.set(year, month, day);
            switch (v.getId()) {
                case R.id.tvTicketResultStep2Before:
                    //减一天
                    calendar.add(Calendar.DAY_OF_MONTH, -1);
                    break;
                case R.id.tvTicketResultStep2After:
                    //加一天
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
                    break;
            }
            //根据日期获取星期
            String weekday = DateUtils.formatDateTime(TicketResultStep2Activity.this
                    , calendar.getTimeInMillis()
                    , DateUtils.FORMAT_ABBREV_WEEKDAY | DateUtils.FORMAT_SHOW_WEEKDAY);
            //拼接日期显示字符串
            String ticketDate = calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1)
                    + "-" + calendar.get(Calendar.DAY_OF_MONTH) + " " + weekday;
            tvTicketResultStep2DateTitle.setText(ticketDate);
            //检查网络是否可用
            if (!NetUtils.check(TicketResultStep2Activity.this)) {
                Toast.makeText(TicketResultStep2Activity.this, "当前网络不可用", Toast.LENGTH_SHORT).show();
                return;
            }
            //获取截掉星期后的时间，传给异步任务
            String startTrainDate = tvTicketResultStep2DateTitle.getText().toString().split(" ")[0];
            //开启异步任务
            new Step2Task(startTrainDate).execute();
        }
    }
}
