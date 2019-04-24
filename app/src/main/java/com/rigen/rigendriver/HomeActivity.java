package com.rigen.rigendriver;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.rigen.rigendriver.db.DBQuery;
import com.rigen.rigendriver.db.EntityOrder;
import com.rigen.rigendriver.fragment.HistoryFragment;
import com.rigen.rigendriver.fragment.HomeFragments;
import com.rigen.rigendriver.fragment.OrderFragment;
import com.rigen.rigendriver.fragment.ProgressFragment;
import com.rigen.rigendriver.lib.ClientConnectPost;
import com.rigen.rigendriver.service.BookingService;
import com.rigen.rigendriver.service.MessagesService;

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

public class HomeActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;

    private DBQuery db;
    private String username, nama;

    private ArrayList<EntityOrder> data;
    LocationManager manager;
    private int pos =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, BookingService.class));
        startService(new Intent(this, MessagesService.class));
        setContentView(R.layout.drawer_home);
        data = new ArrayList<>();
        db = new DBQuery(getApplicationContext());
        db.open();
        Cursor c = db.getAll();
        c.moveToFirst();
        username = c.getString(c.getColumnIndex("username"));
        nama = c.getString(c.getColumnIndex("nama"));
        c.close();
        SetStatus("Ready");

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initNavigationDrawer();

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, new HomeFragments()).commit();
    }

    @Override
    protected void onStart() {
        manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            showAlert();
        }
        Check();
        super.onStart();
        if (pos==0){
            StartFragment(new HomeFragments());
            getSupportActionBar().setTitle("Home");
        }
        if (pos==1){
            StartFragment(new ProgressFragment());
            getSupportActionBar().setTitle("On Progress");
        }
        if (pos==2){
            StartFragment(new HistoryFragment());
            getSupportActionBar().setTitle("Order Selesai");
        }
        if (pos==3){
            StartFragment(new OrderFragment());
            getSupportActionBar().setTitle("Order Baru");
        }
    }
    private void StartFragment(Fragment f){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, f).commit();
    }

    public  void Check(){
        data = new ArrayList<>();
        Map<String, String> dataToSend = new HashMap<String, String>();

        DBQuery db = new DBQuery(this);
        db.open();
        Cursor c = db.getAll();
        c.moveToFirst();
        String u = c.getString(c.getColumnIndex("username"));
        dataToSend.put("username", u);
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(this);
        client.execute(getString(R.string.server) + "androiddriver/checkmyorder", encodedStr);
        try {
            JSONObject j = client.get();
            JSONArray ja = j.getJSONArray("data");
            if (ja.length()>0){
                for (int i = 0; i < ja.length(); i++) {
                    JSONObject jo = ja.getJSONObject(i);
                    EntityOrder e = new EntityOrder();
                    e.setId_order(jo.getInt("id_order"));
                    SetStatus("Busy");
                    Intent intent = new Intent(this, DetailOrderActivity.class);
                    intent.putExtra("id", e.getId_order()+"");
                    startActivity(intent);
                }
            }else {
                SetStatus("Ready");
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void initNavigationDrawer() {

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                int id = menuItem.getItemId();

                Fragment fragment = null;
                switch (id) {
                    case R.id.home:
                        pos = 0;
                        fragment = new HomeFragments();
                        getSupportActionBar().setTitle("Home");
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.on_progress:
                        pos = 1;
                        fragment = new ProgressFragment();
                        getSupportActionBar().setTitle("On Progress");
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.complete:
                        pos = 2;
                        fragment = new HistoryFragment();
                        getSupportActionBar().setTitle("Order Selesai");
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.new_order:
                        pos = 3;
                        fragment = new OrderFragment();
                        getSupportActionBar().setTitle("Order Baru");
                        drawerLayout.closeDrawers();
                        break;
                    case R.id.logout:
                        SetStatus("off");
                        db.open();
                        db.clear();
                        startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                        finish();

                }
                if (fragment != null) {
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                }

                return true;
            }
        });
        View header = navigationView.getHeaderView(0);
        TextView tv_email = (TextView) header.findViewById(R.id.tv_email);
        tv_email.setText(nama);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerClosed(View v) {
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }
    private void SetStatus(String status){
        Map<String, String> dataToSend = new HashMap<String, String>();
        dataToSend.put("username", username);
        dataToSend.put("s", status);
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(HomeActivity.this);
        client.execute(getString(R.string.server) + "androiddriver/setstatusdriver", encodedStr);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Anda akan offline?");
        alert.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SetStatus("off");
                finish();
            }
        });
        alert.setNegativeButton("No", null);
        alert.show();
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
                        Context c = null;
                        ((Activity) c).finish();
                    }
                });
        alertDialog.show();
    }
}
