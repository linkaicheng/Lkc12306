package com.cheng.lkc12306.ticket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cheng.lkc12306.MainActivity;
import com.cheng.lkc12306.R;
import com.cheng.lkc12306.utils.ZxingUtils;

public class TicketResultStep5Activity extends AppCompatActivity {
    //放置二维码的控件
private ImageView imQrCode;
    private TextView tvStepInfo;
    private Button btnStep5Back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_result_step5);
        initView();
    }
    //初始化控件
    private void initView(){
        Intent intent=getIntent();
        long orderId=intent.getLongExtra("orderId",0);
        imQrCode = (ImageView)findViewById(R.id.imQrCode);
        tvStepInfo = (TextView)findViewById(R.id.tvStepInfo);
        btnStep5Back = (Button)findViewById(R.id.btnStep5Back);
        tvStepInfo.setText("您的订单"+orderId+"支付成功，可以凭此二维码办理取票业务，也可以在订单中查看相关信息及二维码");
        //生成二维码
        ZxingUtils.createQRImage("订单号："+orderId,imQrCode,600,600);
        //返回按钮设置监听
        btnStep5Back.setOnClickListener(new BtnStep5BackListener());
    }
    //返回按钮监听，返回主界面
    private class BtnStep5BackListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent=new Intent(TicketResultStep5Activity.this, MainActivity.class);
            startActivity(intent);
        }
    }
}
