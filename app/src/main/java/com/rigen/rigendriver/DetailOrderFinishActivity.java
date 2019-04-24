package com.rigen.rigendriver;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by ILHAM HP on 17/11/2016.
 */

public class DetailOrderFinishActivity extends AppCompatActivity implements OnMapReadyCallback {
    private TextView txt_pelanggan, txt_tunai, txt_from, txt_tujuan, txt_jarak, txt_setoran;
    private GoogleMap mMap;
    private String phone_number;

    private LatLng from, to;
    private EntityOrder entityOrder;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_detail_order);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Detail Order");
        txt_pelanggan = (TextView) findViewById(R.id.txt_pelanggan);
        txt_tunai = (TextView) findViewById(R.id.txt_tunai);
        txt_from = (TextView) findViewById(R.id.txt_dari);
        txt_tujuan = (TextView) findViewById(R.id.txt_tujuan);
        txt_jarak = (TextView) findViewById(R.id.txt_jarak);
        txt_setoran = (TextView) findViewById(R.id.txt_setor);
        Reload();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    private void Reload() {
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

            txt_pelanggan.setText(ja.getString("nama"));
            txt_tunai.setText(formatKurensi.format(ja.getInt("bayar")));
            txt_setoran.setText(formatKurensi.format(ja.getInt("fee_driver")));
            txt_from.setText(ja.getString("alamat_jemput"));
            txt_tujuan.setText(ja.getString("alamat_tujuan"));
            txt_jarak.setText(df.format(ja.getDouble("jarak"))+" Km");
            phone_number = ja.getString("no_telp");
            from = new LatLng(ja.getDouble("lat_jemput"), ja.getDouble("long_jemput"));
            to = new LatLng(ja.getDouble("lat_tujuan"), ja.getDouble("long_tujuan"));

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date date = fmt.parse(entityOrder.getTgl_order());
            SimpleDateFormat fmtOut = new SimpleDateFormat("dd-MM-yyyy hh:mm");
            getSupportActionBar().setSubtitle("Order : "+ fmtOut.format(date));
        } catch (InterruptedException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (ExecutionException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
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
