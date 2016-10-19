package com.cheng.lkc12306.utils;

import android.text.TextUtils;

import org.apache.http.NameValuePair;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

/**
 * Created by cheng.
 */
public class URLConnManager {
    //获得网络连接
    public static HttpURLConnection getHttpURLConnection(String url) {
        HttpURLConnection httpURLConnection = null;
        try {

            URL mUrl = new URL(url);
            httpURLConnection = (HttpURLConnection) mUrl.openConnection();
            //设置链接超时时间
            httpURLConnection.setConnectTimeout(Constant.CONNECTTIME);
            //设置读取超时时间
            httpURLConnection.setReadTimeout(Constant.CONNECTTIME);
            //设置请求参数
            httpURLConnection.setRequestMethod("POST");
            //添加Header
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            //接收输入流
            httpURLConnection.setDoInput(true);
            //传递参数时需要开启
            httpURLConnection.setDoInput(true);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return httpURLConnection;
    }
    //对传过来的参数进行连接，并将数据写入服务器
    public static void postParams(OutputStream output, List<NameValuePair> paramList) throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (NameValuePair pair : paramList) {
            if (!TextUtils.isEmpty(stringBuilder)) {
                stringBuilder.append("&");
            }
            stringBuilder.append(URLEncoder.encode(pair.getName(), "UTF-8"));//将
            // Log.i("My12306", "******" + pair.getName());
            stringBuilder.append("=");
            stringBuilder.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
            //  Log.i("My12306", "******" + pair.getValue());
        }
        // Log.i("My12306", "******" + stringBuilder.toString());
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));
        writer.write(stringBuilder.toString());
        writer.flush();
        writer.close();
    }

    //将流转换成字符串
    public static String converStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuffer sb = new StringBuffer();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        String respose = sb.toString();
        return respose;
    }
}
