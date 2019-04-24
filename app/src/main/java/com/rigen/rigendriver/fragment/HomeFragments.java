package com.rigen.rigendriver.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.rigen.rigendriver.ChangePassActivity;
import com.rigen.rigendriver.R;
import com.rigen.rigendriver.db.DBQuery;
import com.rigen.rigendriver.db.EntityOrder;
import com.rigen.rigendriver.lib.ClientConnectPost;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by ILHAM HP on 01/12/2016.
 */

public class HomeFragments extends Fragment {
    private Context context;
    private TextView nama, email, telp, alamat, pendapatan, setoran, nama_kendaraan, nopol, warna, silinder,merek, tipe, tahun, ringkasan;
    private ImageView img_photo, img_kendaraan;
    private Button btn_password;
    private String[] month = {"Januari", "Februari","Maret","April","Mei","Juni","Juli","Agustus","September","Oktober","Nopember","Desember"};

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_homes, container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ringkasan = (TextView) view.findViewById(R.id.pendapatan);
        nama = (TextView) view.findViewById(R.id.txt_nama);
        email = (TextView) view.findViewById(R.id.email);
        telp = (TextView) view.findViewById(R.id.phone);
        alamat = (TextView) view.findViewById(R.id.address);
        pendapatan = (TextView) view.findViewById(R.id.txt_pendapatan);
        setoran = (TextView) view.findViewById(R.id.txt_setoran);
        nama_kendaraan = (TextView) view.findViewById(R.id.txt_nama_kendaraan);
        nopol = (TextView) view.findViewById(R.id.txt_nopol);
        warna = (TextView) view.findViewById(R.id.txt_warna);
        silinder = (TextView) view.findViewById(R.id.txt_silinder);
        merek = (TextView) view.findViewById(R.id.txt_merek);
        tipe = (TextView) view.findViewById(R.id.txt_tipe);
        tahun = (TextView) view.findViewById(R.id.txt_tahun);
        img_photo = (ImageView) view.findViewById(R.id.img_photo);
        img_kendaraan = (ImageView) view.findViewById(R.id.img_kendaraan);
        btn_password = (Button) view.findViewById(R.id.btn_changepassword);
        Calendar c = Calendar.getInstance();
        ringkasan.setText("Ringkasan pendapatan "+ month[c.get(Calendar.MONTH)]+" "+c.get(Calendar.YEAR));
        Reload(getActivity());
        btn_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startActivity(new Intent(getActivity(), ChangePassActivity.class));
            }
        });
    }
    private void Reload(Activity activity) {
        int total = 0;
        Map<String, String> dataToSend = new HashMap<String, String>();

        NumberFormat formatKurensi = NumberFormat.getCurrencyInstance(new Locale("in", "id"));
        DBQuery db = new DBQuery(activity);
        db.open();
        Cursor c = db.getAll();
        c.moveToFirst();
        String username = c.getString(c.getColumnIndex("username"));
        dataToSend.put("username", username);
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(activity);
        client.execute(getString(R.string.server) + "androiddriver/dashboard", encodedStr);
        try {
            JSONObject j = client.get();
            JSONObject jp = j.getJSONObject("profile");
            JSONObject jk = j.getJSONObject("kendaraan");
            JSONObject jt = j.getJSONObject("transaksi");
            nama.setText(jp.getString("nama"));
            alamat.setText(jp.getString("alamat"));
            email.setText(jp.getString("email"));
            telp.setText(jp.getString("no_telp"));
            int tot = 0;
            int fee =0;
            try {
                tot = jt.getInt("total");
                fee = jt.getInt("fee");
            }catch (Exception e){}
            pendapatan.setText(jt.getInt("jumlah")+" Transaksi("+ formatKurensi.format(tot)+")");
            setoran.setText(jt.getInt("jumlah")+" Transaksi("+formatKurensi.format(fee)+")");
            nama_kendaraan.setText(jk.getString("nama_kendaraan"));
            nopol.setText(jk.getString("no_kendaraan"));
            warna.setText(jk.getString("warna"));
            silinder.setText(jk.getString("isi_silinder"));
            merek.setText(jk.getString("merek"));
            tipe.setText(jk.getString("jenis"));
            tahun.setText(jk.getString("tahun"));
            Picasso.with(activity)
                    .load(getString(R.string.server)+jp.getString("foto"))
                    .error(R.drawable.noimage)
                    .placeholder(R.drawable.progress_loading)
                    .into(img_photo);
            Picasso.with(activity)
                    .load(getString(R.string.server)+jk.getString("foto_kendaraan"))
                    .error(R.drawable.noimage)
                    .placeholder(R.drawable.progress_loading)
                    .into(img_kendaraan);

        } catch (InterruptedException e) {} catch (ExecutionException e) {} catch (JSONException e) {}
    }
}
