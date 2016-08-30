package com.cheng.lkc12306.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.bean.Order;
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
    private SimpleAdapter adapter;
    private ProgressDialog pDialog;
    private RadioGroup rgOrder;
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
        adapter=new SimpleAdapter(getActivity(),data,R.layout.item_order_list
        ,new String[]{"orderId","trainNo","startDate","stationInfo"
              ,"status","price"}
                , new int[]{R.id.tvOrderId,R.id.tvTrainNO,R.id.tvStartDate,R.id.tvStationInfo
        ,R.id.tvStatus,R.id.tvPrice});
        lvOrder.setAdapter(adapter);
        //判断网络是否可用
        if (!NetUtils.check(getActivity())) {
            Toast.makeText(getActivity(), "网络不可用", Toast.LENGTH_SHORT).show();
            return;
        }
        new OrderTask().execute("0");
        ;
        rgOrder.setOnCheckedChangeListener(new MyCheckChangeListener());

    }
    private class MyCheckChangeListener implements RadioGroup.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
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
            pDialog = ProgressDialog.show(getActivity(), null, "请稍候。。", false, true);
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
            List<Order> orders=null;
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

                        switch (order.getStatus()) {
                            case  0:
                                row.put("status","未支付");
                                break;
                            case  1:
                                row.put("status","已支付");
                                break;
                            case  2:
                                row.put("status","已取消");
                                break;
                        }
                        row.put("price",order.getOrderPrice());
                        data.add(row);
                    }
                    adapter.notifyDataSetChanged();
//                    for(int i=0;i<adapter.getCount();i++){
//                        adapter.getView(i,null,null);
//                       Log.e("cheng","********"+adapter.getItem(i));
//                    }


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
