package com.rigen.rigendriver.db;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.rigen.rigendriver.R;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by ILHAM HP on 17/11/2016.
 */

public class AdapaterOrder extends BaseAdapter{
    private Context c;
    private LayoutInflater l;
    private ArrayList<EntityOrder> data;
    public AdapaterOrder(Context c, ArrayList<EntityOrder> d){
        data = d;
        this.c=c;
        l =LayoutInflater.from(c);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = l.inflate(R.layout.adapter_order, null);
        DecimalFormat df = new DecimalFormat("#.#");
        NumberFormat formatKurensi = NumberFormat.getCurrencyInstance(new Locale("in","id"));
        TextView txt_jarak = (TextView) convertView.findViewById(R.id.txt_jarak);
        TextView txt_harga = (TextView) convertView.findViewById(R.id.txt_biaya);
        TextView txt_dari = (TextView) convertView.findViewById(R.id.txt_dari);
        TextView txt_tujuan = (TextView) convertView.findViewById(R.id.txt_tujuan);
        CardView cardView = (CardView) convertView.findViewById(R.id.cardview);
        if (data.get(position).getStatus_order().equalsIgnoreCase("CONFIRM")){
            cardView.setBackgroundColor(Color.parseColor("#FFE6A21F"));
        }else if(data.get(position).getStatus_order().equalsIgnoreCase("pickup")){
            cardView.setBackgroundColor(Color.parseColor("#FF96E287"));
        }

        txt_jarak.setText(df.format(data.get(position).getJarak())+" KM");
        txt_harga.setText(formatKurensi.format(data.get(position).getBayar())+"");
        txt_dari.setText(data.get(position).getAlamat_jemput());
        txt_tujuan.setText(data.get(position).getAlamat_tujuan());
        return convertView;
    }
}
