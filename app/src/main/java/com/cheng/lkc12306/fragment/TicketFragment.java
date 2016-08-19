package com.cheng.lkc12306.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cheng.lkc12306.R;
import com.cheng.lkc12306.stationlist.StationListActivity;


/**
 * Created by cheng on 2016/8/17.
 */
public class TicketFragment extends Fragment {
    private TextView tvStationFrom,tvStationTo;
    private ImageView imExchange;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ticket,container,false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        tvStationFrom = (TextView) getActivity().findViewById(R.id.tvStationFrom);
        tvStationTo = (TextView) getActivity().findViewById(R.id.tvStationTo);
        imExchange = (ImageView) getActivity().findViewById(R.id.imExchange);
        tvStationFrom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), StationListActivity.class);
                startActivityForResult(intent,100);
            }
        });
        tvStationTo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), StationListActivity.class);
                startActivityForResult(intent,101);
            }
        });


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==200&&data!=null){
           switch (requestCode) {
               case  100:
                        tvStationFrom.setText(data.getStringExtra("name"));
                   break;
               case  101:
                        tvStationTo.setText(data.getStringExtra("name"));
                   break;
           }
        }else {
            Toast.makeText(getActivity(), "车站选择失败", Toast.LENGTH_LONG).show();
        }
    }
}
