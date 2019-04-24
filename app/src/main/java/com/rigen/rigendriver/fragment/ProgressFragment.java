package com.rigen.rigendriver.fragment;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.rigen.rigendriver.DetailOrderActivity;
import com.rigen.rigendriver.R;
import com.rigen.rigendriver.db.AdapaterOrder;
import com.rigen.rigendriver.db.DBQuery;
import com.rigen.rigendriver.db.EntityOrder;
import com.rigen.rigendriver.lib.ClientConnectPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by ILHAM HP on 18/11/2016.
 */

public class ProgressFragment extends Fragment {

    private ListView listView;
    private ArrayList<EntityOrder> data;
    public ProgressFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_main, container, false);

        final Activity activity = getActivity();
        listView = (ListView) v.findViewById(R.id.listView);
        Reload(activity);
        listView.setAdapter(new AdapaterOrder(activity.getApplicationContext(), data));
        final SwipeRefreshLayout sr = (SwipeRefreshLayout) v.findViewById(R.id.view);
        sr.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Reload(activity);
                listView.setAdapter(new AdapaterOrder(activity.getApplicationContext(), data));
                sr.setRefreshing(false);
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent i = new Intent(activity, DetailOrderActivity.class);
                i.putExtra("id", "" + data.get(position).getId_order());
                startActivity(i);
            }
        });
        return  v;
    }
    private void Reload(Activity activity) {
        data = new ArrayList<>();
        Map<String, String> dataToSend = new HashMap<String, String>();

        DBQuery db = new DBQuery(activity);
        db.open();
        Cursor c = db.getAll();
        c.moveToFirst();
        String username = c.getString(c.getColumnIndex("username"));
        dataToSend.put("username", username);
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(activity);
        client.execute(getString(R.string.server) + "androiddriver/checkmyorder", encodedStr);
        try {
            JSONObject j = client.get();
            JSONArray ja = j.getJSONArray("data");
            for (int i = 0; i < ja.length(); i++) {
                JSONObject jo = ja.getJSONObject(i);
                EntityOrder e = new EntityOrder();
                e.setId_order(jo.getInt("id_order"));
                e.setAlamat_tujuan(jo.getString("alamat_tujuan"));
                e.setAlamat_jemput(jo.getString("alamat_jemput"));
                e.setStatus_order(jo.getString("status_order"));
                e.setBayar(jo.getInt("bayar"));
                e.setJarak(jo.getDouble("jarak"));
                data.add(e);
            }
            listView.setAdapter(new AdapaterOrder(activity, data));
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
