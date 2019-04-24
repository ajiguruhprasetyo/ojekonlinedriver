package com.rigen.rigendriver;

import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.rigen.rigendriver.db.DBQuery;
import com.rigen.rigendriver.db.EntityOrder;
import com.rigen.rigendriver.lib.ClientConnectPost;
import com.rigen.rigendriver.lib.DirectionsJSONParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by ILHAM HP on 17/11/2016.
 */

public class DetailOrderActivity extends AppCompatActivity implements OnMapReadyCallback {
    private TextView txt_pelanggan, txt_tunai, txt_from, txt_tujuan, txt_jarak;
    private Button btn_pelanggan, btn_jemput;
    private GoogleMap mMap;
    private String phone_number;

    private LatLng from, to;

    private EntityOrder entityOrder;
    private String email_customer;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_antar);
        txt_pelanggan = (TextView) findViewById(R.id.txt_pelanggan);
        txt_tunai = (TextView) findViewById(R.id.txt_tunai);
        txt_from = (TextView) findViewById(R.id.txt_dari);
        txt_tujuan = (TextView) findViewById(R.id.txt_tujuan);
        txt_jarak = (TextView) findViewById(R.id.txt_jarak);
        btn_pelanggan = (Button) findViewById(R.id.btn_pelanggan);
        btn_jemput = (Button) findViewById(R.id.btn_jemput);

        Reload();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        btn_pelanggan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                call();
            }
        });
        btn_jemput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(DetailOrderActivity.this);
                if (btn_jemput.getText().toString().equalsIgnoreCase("Jemput")){
                    alert.setMessage("Jemput pelanggan?");
                    alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Jemput();
                        }
                    });
                    alert.setNegativeButton("Batal", null);
                    alert.show();
                }else if (btn_jemput.getText().toString().equalsIgnoreCase("antar")){
                    alert.setMessage("Antarkan  pelanggan?");
                    alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            antar();
                        }
                    });
                    alert.setNegativeButton("Batal", null);
                    alert.show();
                }else{
                    alert.setMessage("Order selesai?");
                    alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            selesai();
                        }
                    });
                    alert.setNegativeButton("Batal", null);
                    alert.show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_order, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.mn_pesan:
                Intent i = new Intent(this, PesanActivity.class);
                i.putExtra("id",  getIntent().getStringExtra("id"));
                i.putExtra("email",  email_customer);
                i.putExtra("nama",  txt_pelanggan.getText().toString());
                startActivity(i);
                break;
            case R.id.mn_call:
                call();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setTitle("GPS is settings");
        alertDialog
                .setMessage("GPS is not enabled. Do you want to go to settings menu?");
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(
                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(intent);
                    }
                });
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                });
        alertDialog.show();
    }
    private void antar(){
        Map<String, String> dataToSend = new HashMap<String, String>();
        DBQuery db = new DBQuery(this);
        db.open();
        Cursor c = db.getAll();
        c.moveToFirst();
        String username = c.getString(c.getColumnIndex("username"));
        dataToSend.put("id", getIntent().getStringExtra("id"));
        dataToSend.put("username", username);
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(this);
        client.execute(getResources().getString(R.string.server) + "androiddriver/antar", encodedStr);
        try {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            JSONObject j = client.get();
            int status = j.getInt("status");
            alert.setMessage(j.getString("msg"));
            if (status==0){
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
            }else{
                alert.setPositiveButton("OK", null);
                btn_jemput.setText("SELESAI");
            }
            alert.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private void selesai(){
        Map<String, String> dataToSend = new HashMap<String, String>();
        DBQuery db = new DBQuery(this);
        db.open();
        Cursor c = db.getAll();
        c.moveToFirst();
        String username = c.getString(c.getColumnIndex("username"));
        dataToSend.put("id", getIntent().getStringExtra("id"));
        dataToSend.put("username", username);
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(this);
        client.execute(getResources().getString(R.string.server) + "androiddriver/selesaiorder", encodedStr);
        try {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            JSONObject j = client.get();
            int status = j.getInt("status");
            alert.setMessage(j.getString("msg"));
            if (status==0){
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                alert.show();
            }else{
                SelesaiView();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void SelesaiView(){
        setContentView(R.layout.layout_finish);
        TextView txt_cash = (TextView) findViewById(R.id.txt_cash);
        Button btn_finish = (Button) findViewById(R.id.btn_keluar);
        NumberFormat formatKurensi = NumberFormat.getCurrencyInstance(new Locale("in", "id"));
        txt_cash.setText(formatKurensi.format(entityOrder.getBayar()));
        btn_finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void Jemput(){
        Map<String, String> dataToSend = new HashMap<String, String>();
        DBQuery db = new DBQuery(this);
        db.open();
        Cursor c = db.getAll();
        c.moveToFirst();
        String username = c.getString(c.getColumnIndex("username"));
        dataToSend.put("id", getIntent().getStringExtra("id"));
        dataToSend.put("username", username);
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(this);
        client.execute(getResources().getString(R.string.server) + "androiddriver/jemput", encodedStr);
        try {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            JSONObject j = client.get();
            int status = j.getInt("status");
            alert.setMessage(j.getString("msg"));
            if (status==0){
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
            }else{
                alert.setPositiveButton("OK", null);
                btn_jemput.setText("ANTAR");
            }
            alert.show();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void call() {
        String uri = "tel:" + phone_number.trim();
        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(uri));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }

    private void Reload() {
        try {
            NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(this.NOTIFICATION_SERVICE);
            nMgr.cancelAll();
        }catch (Exception e){}
        Map<String, String> dataToSend = new HashMap<String, String>();

        DBQuery db = new DBQuery(this);
        db.open();
        Cursor c = db.getAll();
        c.moveToFirst();
        String username = c.getString(c.getColumnIndex("username"));
        dataToSend.put("id", getIntent().getStringExtra("id"));
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(this);
        client.execute(getResources().getString(R.string.server) + "androiddriver/checkdetailorder", encodedStr);
        try {
            DecimalFormat df = new DecimalFormat("#.#");
            NumberFormat formatKurensi = NumberFormat.getCurrencyInstance(new Locale("in", "id"));
            JSONObject j = client.get();
            JSONObject ja = j.getJSONObject("data");
            entityOrder = new EntityOrder();
            entityOrder.setBayar(ja.getInt("bayar"));
            entityOrder.setId_order(ja.getInt("id_order"));
            entityOrder.setTgl_order(ja.getString("tgl_order"));

            email_customer = ja.getString("email_customer");

            txt_pelanggan.setText(ja.getString("nama"));
            txt_tunai.setText(formatKurensi.format(ja.getInt("bayar")));
            txt_from.setText(ja.getString("alamat_jemput"));
            txt_tujuan.setText(ja.getString("alamat_tujuan"));
            txt_jarak.setText(df.format(ja.getDouble("jarak"))+" Km");
            phone_number = ja.getString("no_telp");
            from = new LatLng(ja.getDouble("lat_jemput"), ja.getDouble("long_jemput"));
            to = new LatLng(ja.getDouble("lat_tujuan"), ja.getDouble("long_tujuan"));
            if (ja.getString("status_order").equalsIgnoreCase("Not Confirm")){
                btn_jemput.setText("JEMPUT");
            }else if (ja.getString("status_order").equalsIgnoreCase("Confirm")){
                btn_jemput.setText("ANTAR");
            }else if(ja.getString("status_order").equalsIgnoreCase("Pickup")){
                btn_jemput.setText("SELESAI");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromResource(R.drawable.ic_map_from);
        mMap.addMarker(new MarkerOptions().position(from).icon(descriptor));
        BitmapDescriptor descriptor_drop = BitmapDescriptorFactory.fromResource(R.drawable.ic_map_drop);
        mMap.addMarker(new MarkerOptions().position(to).icon(descriptor_drop));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(from, 14));
        String url = getDirectionsUrl(from,to);
        DownloadTask downloadTask = new DownloadTask();
        downloadTask.execute(url);
    }

    private String getDirectionsUrl(LatLng origin,LatLng dest){
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        String str_dest = "destination="+dest.latitude+","+dest.longitude;
        String sensor = "sensor=false";
        String parameters = str_origin+"&"+str_dest+"&"+sensor;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters;
        return url;
    }
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb  = new StringBuffer();
            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        }catch(Exception e){
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private class DownloadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try{
                data = downloadUrl(url[0]);
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);

        }
    }
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.RED);

            }
            mMap.addPolyline(lineOptions);
        }
    }
}
