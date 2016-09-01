package com.cheng.lkc12306.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.bean.Order;
import com.cheng.lkc12306.bean.Order1ViewHolder;
import com.cheng.lkc12306.bean.OrderNotPaidItem;
import com.cheng.lkc12306.bean.OrderPassenger;
import com.cheng.lkc12306.order.OrderNotPaidActivity;
import com.cheng.lkc12306.order.PaidActivity;
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
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by cheng on 2016/8/17.
 */
public class OrderFragment extends Fragment {
    private ListView lvOrder;
    private TextView tvStatus;
    private List<Map<String,Object>> data=null;
    private MyAdapter adapter;
    private ProgressDialog pDialog;
    private RadioGroup rgOrder;
    private List<OrderNotPaidItem> orderItems=null;
    List<Order> orders=null;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_order,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }
    private void initView(){
        lvOrder = (ListView) getActivity().findViewById(R.id.lvOrder);
        rgOrder = (RadioGroup) getActivity().findViewById(R.id.rgOrder);
        tvStatus = (TextView) getActivity().findViewById(R.id.tvStatus);
        data=new ArrayList<>();
        orderItems=new ArrayList<>();
        adapter=new MyAdapter(data);
        lvOrder.setAdapter(adapter);
        //判断网络是否可用
        if (!NetUtils.check(getActivity())) {
            Toast.makeText(getActivity(), "网络不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        new OrderTask().execute("0");

        rgOrder.setOnCheckedChangeListener(new CheckChangeListener());
        lvOrder.setOnItemClickListener(new LvOrderOnItListener());

    }
    private class LvOrderOnItListener implements AdapterView.OnItemClickListener{
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Order order=orders.get(position);

            orderItems.clear();
            for(OrderPassenger passenger:order.getPassengerList()){
                OrderNotPaidItem item=new OrderNotPaidItem();
                item.setName(passenger.getName());
                item.setTrainNo(order.getTrain().getTrainNo());
                item.setDate(order.getTrain().getStartTrainDate());
                item.setSeat(passenger.getSeat().getSeatNo());
                orderItems.add(item);
            }

            switch ((int)data.get(position).get("status")) {
                case  0://未支付
                    Intent intent=new Intent(getActivity(), OrderNotPaidActivity.class);
                    intent.putExtra("position",position);
                    intent.putExtra("orderId",order.getId());
                    intent.putExtra("orderItems", (Serializable) orderItems);
                    startActivityForResult(intent,0);
                    break;
                case  1://已支付
                    Intent intent2=new Intent(getActivity(),PaidActivity.class);
                    intent2.putExtra("position",position);
                    intent2.putExtra("orderId",order.getId());
                    intent2.putExtra("orderItems", (Serializable) orderItems);
                    startActivityForResult(intent2,1);
                    break;
                case  2://已取消

                    Toast.makeText(getActivity(), "已取消", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
//处理intent回传
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        switch (requestCode) {
            case  0:
                if(resultCode==getActivity().RESULT_OK){
                    if(intent.getStringExtra("result").equals("cancel")){
                        data.get(intent.getIntExtra("position",0)).put("status",2);
                        adapter.notifyDataSetChanged();
                    }else if(intent.getStringExtra("result").equals("pay")){
                        data.get(intent.getIntExtra("position",0)).put("status",1);
                        adapter.notifyDataSetChanged();
                    }

                }
                break;
        }
    }

    private class MyAdapter extends BaseAdapter{
    List<Map<String,Object>> data;
    public MyAdapter(List<Map<String,Object>> data){
        this.data=data;
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //自定义的ViewHolder来优化适配器
        Order1ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new Order1ViewHolder();
            convertView = View.inflate(getActivity(), R.layout.item_order_list
                    , null);
          viewHolder.imForward= (ImageView) convertView.findViewById(R.id.imForward);
            viewHolder.tvOrderId= (TextView) convertView.findViewById(R.id.tvOrderId);
            viewHolder.tvPrice= (TextView) convertView.findViewById(R.id.tvPrice);
            viewHolder.tvStartDate= (TextView) convertView.findViewById(R.id.tvStartDate);
            viewHolder.tvStationInfo= (TextView) convertView.findViewById(R.id.tvStationInfo);
            viewHolder.tvStatus= (TextView) convertView.findViewById(R.id.tvStatus);
            viewHolder.tvTrainNO= (TextView) convertView.findViewById(R.id.tvTrainNO);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (Order1ViewHolder) convertView.getTag();
        }
       viewHolder.tvOrderId.setText("订单:"+data.get(position).get("orderId"));
        viewHolder.tvTrainNO.setText((String)data.get(position).get("trainNo"));
        viewHolder.tvStartDate.setText((String)data.get(position).get("startDate"));
        viewHolder.tvStationInfo.setText((String)data.get(position).get("stationInfo"));
        switch ((int)data.get(position).get("status")) {
            case  0:
                viewHolder.tvStatus.setText("未支付");
                viewHolder.tvStatus.setTextColor(0xffFF0000);
                break;
            case  1:
                viewHolder.tvStatus.setText("已支付");
                viewHolder.tvStatus.setTextColor(0xff599bff);
                break;
            case  2:
                viewHolder.tvStatus.setText("已取消");
               viewHolder.tvStatus.setTextColor(0xffD8D7D8);
                break;
        }
        viewHolder.tvPrice.setText(String.valueOf(data.get(position).get("price")));
        viewHolder.imForward.setImageResource(R.mipmap.forward_25);

        return convertView;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}
    private class CheckChangeListener implements RadioGroup.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            pDialog = ProgressDialog.show(getActivity(), null, "请稍候。。11", false, true);
            switch (checkedId) {
                case  R.id.rbPayWait:
                    new OrderTask().execute("0");
                    break;
                case  R.id.rbOrderAll:
                    new OrderTask().execute("1");
                    break;
            }
        }
    }
    private class OrderTask extends AsyncTask<String,Void,Object>{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Object doInBackground(String... params) {
            String result=null;
            InputStream inputStream=null;
            HttpURLConnection conn=null;
            try {
                //获取连接
                conn = URLConnManager.getHttpURLConnection(Constant.HOST
                        + "/otn/OrderList");
                //获取保存的cookie
                SharedPreferences sp = getActivity().getSharedPreferences("user", Context.MODE_PRIVATE);
                String cookieValue = sp.getString("cookie", "");
                //设置请求属性
                conn.setRequestProperty("cookie", cookieValue);
                //封装请求参数
                List<NameValuePair> paramList = new ArrayList<>();
                String status=params[0];
                paramList.add(new BasicNameValuePair("status",status));
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
                   // Log.e("cheng","*********response"+reponse);
                    //用Gson解析数据
                    Gson gson=new Gson();
                    List<Order> orders=gson.fromJson(reponse,new TypeToken<List<Order>>(){}.getType());
                   // Log.e("cheng","*******"+orders);
                    //成功时返回的是List<Order>对象
                    return orders;
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

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);

            data.clear();
            //关闭进度对话框
            if(pDialog!=null){
                pDialog.dismiss();
            }

            if(o instanceof List<?>){
                orders=(List<Order>)o;
                if(orders.size()==0){
                    Toast.makeText(getActivity(), "没有查询到相关的订单", Toast.LENGTH_SHORT).show();
                    //查询到空，将数据清空，更新界面
                    data.clear();
                    adapter.notifyDataSetChanged();
                }else{
                    // 往data中填充数据
                    for(Order order:orders){
                        Map<String,Object> row=new HashMap<>();
                        row.put("orderId",order.getId());

                        row.put("trainNo",order.getTrain().getTrainNo());
                        row.put("startDate",order.getTrain().getStartTrainDate());
                        row.put("stationInfo",order.getTrain().getFromStationName()+"->"
                                +order.getTrain().getToStationName()
                                +" "+order.getPassengerList().size()+"人");
                        row.put("status",order.getStatus());

                        row.put("price",order.getOrderPrice());
                        data.add(row);
                    }
                    adapter.notifyDataSetChanged();



                }
            }else if(o instanceof String){
                if("2".equals(o)){
                    Toast.makeText(getActivity(), "服务器错误", Toast.LENGTH_SHORT).show();
                }else if("3".equals(o)){
                    Toast.makeText(getActivity(), "请重新登录", Toast.LENGTH_SHORT).show();
                }

            }else{
                Toast.makeText(getActivity(), "服务器错误", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
