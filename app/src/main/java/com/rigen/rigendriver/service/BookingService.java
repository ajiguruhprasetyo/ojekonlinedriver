package com.rigen.rigendriver.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.rigen.rigendriver.DetailOrderActivity;
import com.rigen.rigendriver.R;
import com.rigen.rigendriver.db.DBQuery;
import com.rigen.rigendriver.db.EntityOrder;
import com.rigen.rigendriver.lib.ClientConnectPost;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class BookingService extends Service implements LocationListener {

	private boolean isGPSEnabled = false;
	private boolean isNetworkEnabled = false;
	private boolean canGetLocation = false;
	protected LocationManager locationManager;
	private Location location; // location
	private double latitude; // latitude
	private double longitude; // longitude
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // 1 meters
	private static final long MIN_TIME_BW_UPDATES = 1000 * 5 * 1; // 1 minute
	private String username=null;
	private boolean off=false;

	private ArrayList<EntityOrder> data = new ArrayList<>();
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		off = check();
		if (off==false) {
			getLocation();
			if (canGetLocation) {
				Map<String, String> dataToSend = new HashMap<>();
				dataToSend.put("username", username);
				dataToSend.put("lat", location.getLatitude() + "");
				dataToSend.put("lng", location.getLongitude() + "");
				String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
				ClientConnectPost client = new ClientConnectPost(this);
				client.execute(getResources().getString(R.string.server) + "androiddriver/mylocation", encodedStr);
			}
			mTimer = new Timer();
			mTimer.schedule(timerTask, 2000, 2 * 1000);
		}
	}

	private boolean check(){
		DBQuery db = new DBQuery(this);
		db.open();
		Cursor c = db.getAll();
		if (c.getCount() ==1) {
			c.moveToFirst();
			username = c.getString(c.getColumnIndex("username"));
			c.close();
			db.close();
			return false;
		}else {
			username = null;
			c.close();
			db.close();
			return true;
		}
	}
	private void  checkOrder(){
		DBQuery db = new DBQuery(this);
		db.open();
		Cursor c = db.getAll();
		if (c.getCount() ==1) {
			c.moveToFirst();
			String username = c.getString(c.getColumnIndex("username"));
			Map<String, String> dataToSend = new HashMap<String, String>();
			dataToSend.put("username", username);
			String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
			ClientConnectPost client = new ClientConnectPost(this);
			client.execute(getResources().getString(R.string.server) + "androiddriver/checkorder", encodedStr);
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
					if (data.size()>0) {
						boolean isexist=false;
						for (EntityOrder eo : data) {
							if(e.getId_order() == eo.getId_order()){
								isexist = true;
							}
						}
                        if(!isexist){
							data.add(e);
							addNotification(e);
						}
					}else{
						data.add(e);
						addNotification(e);
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		db.close();
		c.close();
	}
	private void addNotification(EntityOrder e) {
		Intent resultIntent = new Intent(this, DetailOrderActivity.class);
		resultIntent.putExtra("id", e.getId_order()+"");
		PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0,
				resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationCompat.Builder mNotifyBuilder;
		NotificationManager mNotificationManager;
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// Sets an ID for the notification, so it can be updated
		mNotifyBuilder = new NotificationCompat.Builder(this);
		mNotifyBuilder.setContentTitle("Order Baru");
		mNotifyBuilder.setContentText(e.getAlamat_jemput());
		mNotifyBuilder.setSmallIcon(R.drawable.ic_map_drop);
		mNotifyBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(e.getAlamat_jemput()+" TO :"+e.getAlamat_tujuan()));
		// Set pending intent
		mNotifyBuilder.setContentIntent(resultPendingIntent);
		mNotifyBuilder.setPriority(Notification.PRIORITY_MAX);
		// Set Vibrate, Sound and Light
		int defaults = 0;
		defaults = defaults | Notification.DEFAULT_LIGHTS;
		defaults = defaults | Notification.DEFAULT_VIBRATE;
		defaults = defaults | Notification.DEFAULT_SOUND;
		mNotifyBuilder.setDefaults(defaults);
		// Set the content for Notification
		DecimalFormat df = new DecimalFormat("#,#");
		mNotifyBuilder.setContentInfo(df.format(e.getJarak())+" Km");
		mNotifyBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_map_drop));
		// Set autocancel
		mNotifyBuilder.setAutoCancel(true);
		mNotificationManager.notify(e.getId_order(), mNotifyBuilder.build());
	}
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {

		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	private Timer mTimer;

	TimerTask timerTask = new TimerTask() {
		private Handler mHandler = new Handler();
		@Override
		public void run() {
			new Thread(new Runnable(){
				@Override
				public void run(){
					mHandler.post(new Runnable(){
						@Override
						public void run(){
							off = check();
							if (off==false) {
								checkOrder();
							}
						}
					});
				}
			}).start();
		}
	};

	public void onDestroy() {
		try {
			mTimer.cancel();
			timerTask.cancel();
		} catch (Exception e) {
			e.printStackTrace();
		}
		Intent intent = new Intent("com.test.Receiver");
		intent.putExtra("yourvalue", "torestore");
		sendBroadcast(intent);
	}

	@Override
	public void onLocationChanged(Location location) {
		if (!username.isEmpty()) {
			Map<String, String> dataToSend = new HashMap<>();
			dataToSend.put("username", username);
			dataToSend.put("lat", location.getLatitude() + "");
			dataToSend.put("lng", location.getLongitude() + "");
			String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
			ClientConnectPost client = new ClientConnectPost(this);
			client.execute(getResources().getString(R.string.server) + "androiddriver/mylocation", encodedStr);
		}
	}

	private Location getLocation() {
		try {
			locationManager = (LocationManager) this
					.getSystemService(Context.LOCATION_SERVICE);
			isGPSEnabled = locationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER);
			isNetworkEnabled = locationManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			if (isGPSEnabled == false) {
			} else {
				this.canGetLocation = true;
				if (isNetworkEnabled) {
					if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
						return null;
					}
					locationManager.requestLocationUpdates(
							LocationManager.NETWORK_PROVIDER,
							MIN_TIME_BW_UPDATES,
							MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
					Log.d("Network", "Network");
					if (locationManager != null) {
						location = locationManager
								.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
						if (location != null) {
							latitude = location.getLatitude();
							longitude = location.getLongitude();
						}
					}
				}
				if (isGPSEnabled) {
					if (location == null) {
						locationManager.requestLocationUpdates(
								LocationManager.GPS_PROVIDER,
								MIN_TIME_BW_UPDATES,
								MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
						Toast.makeText(this, "Gps Enabled", Toast.LENGTH_SHORT)
								.show();
						if (locationManager != null) {
							location = locationManager
									.getLastKnownLocation(LocationManager.GPS_PROVIDER);
							if (location != null) {
								latitude = location.getLatitude();
								longitude = location.getLongitude();
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return location;
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {
		//showAlert();
	}
	private void showAlert(){
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext());
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
