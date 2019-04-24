package com.rigen.rigendriver.fragment;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rigen.rigendriver.R;
import com.rigen.rigendriver.db.AdapaterOrder;
import com.rigen.rigendriver.db.DBQuery;
import com.rigen.rigendriver.db.EntityOrder;
import com.rigen.rigendriver.lib.ClientConnectPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by ILHAM HP on 18/11/2016.
 */

public class HistoryFragment extends Fragment {

    private Spinner sp_bulan, sp_tahun;
    private TextView txt_setoran;
    private Button btn_tampil;
    private ListView lv;
    private List<String> bulan, tahun;
    private ArrayList<EntityOrder> data;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        final Activity activity = getActivity();
        sp_bulan = (Spinner) v.findViewById(R.id.sp_bln);
        sp_tahun = (Spinner) v.findViewById(R.id.sp_tahun);
        txt_setoran = (TextView) v.findViewById(R.id.txt_setoran);
        lv = (ListView) v.findViewById(R.id.listView);
        btn_tampil = (Button) v.findViewById(R.id.btn_tampil);
        AturSpinner(activity);
        Reload(activity);
        btn_tampil.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reload(activity);
            }
        });
        return v;
    }
    private void Reload(Activity activity) {
        data = new ArrayList<>();
        int total = 0;
        Map<String, String> dataToSend = new HashMap<String, String>();

        NumberFormat formatKurensi = NumberFormat.getCurrencyInstance(new Locale("in", "id"));
        DBQuery db = new DBQuery(activity);
        db.open();
        Cursor c = db.getAll();
        c.moveToFirst();
        String username = c.getString(c.getColumnIndex("username"));
        dataToSend.put("username", username);
        dataToSend.put("bulan", (sp_bulan.getSelectedItemPosition()+1)+"" );
        dataToSend.put("tahun", tahun.get(sp_tahun.getSelectedItemPosition()));
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(activity);
        client.execute(getString(R.string.server) + "androiddriver/checkfinishorderbydate", encodedStr);
        try {
            JSONObject j = client.get();
            JSONArray ja = j.getJSONArray("data");
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                EntityOrder e = new EntityOrder();
                e.setId_order(jo.getInt("id_order"));
                e.setAlamat_tujuan(jo.getString("alamat_tujuan"));
                e.setAlamat_jemput(jo.getString("alamat_jemput"));
                e.setBayar(jo.getInt("bayar"));
                e.setJarak(jo.getDouble("jarak"));
                e.setStatus_order(jo.getString("status_order"));
                e.setFee_driver(jo.getInt("fee_driver"));
                total = total+e.getFee_driver();
                data.add(e);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        txt_setoran.setText(formatKurensi.format(total));
        lv.setAdapter(new AdapaterOrder(activity, data));
    }
    private void AturSpinner(Activity activity){
        bulan = new ArrayList<>();
        tahun = new ArrayList<>();
        bulan.add("Januari");
        bulan.add("Februari");
        bulan.add("Maret");
        bulan.add("April");
        bulan.add("Mei");
        bulan.add("Juni");
        bulan.add("Juli");
        bulan.add("Agustus");
        bulan.add("September");
        bulan.add("Oktober");
        bulan.add("November");
        bulan.add("Desember");
        int year  = Calendar.getInstance().get(Calendar.YEAR);
        int a = 0;
        int pos=0;
        for (int i = 2016; i <= year ; i++){
            tahun.add(i+"");
            if (i==year){pos =a;}
            a++;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_dropdown_item, bulan);
        sp_bulan.setAdapter(adapter);
        ArrayAdapter<String> adaptert = new ArrayAdapter<String>(activity,
                android.R.layout.simple_spinner_dropdown_item, tahun);
        sp_tahun.setAdapter(adaptert);
        sp_bulan.setSelection(Calendar.getInstance().get(Calendar.MONTH));
        sp_tahun.setSelection(pos);
    }

}
