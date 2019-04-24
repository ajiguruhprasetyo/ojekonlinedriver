package com.rigen.rigendriver.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBHelper extends SQLiteOpenHelper{
	
	private static String DB_NAME = "dbs.sqlite";
	private final Context context;  
	private String DB_PATH; 

	public DBHelper(Context context) {
		super(context, DB_NAME, null, 1);
		// TODO Auto-generated constructor stub
		this.context = context;  
		DB_PATH = "/data/data/" + context.getPackageName() + "/" + "databases/";  
	}
	
	public void createDataBase() throws IOException {  
		  
		  boolean dbExist = checkDataBase();  
		  if (dbExist) {  
		  } else {  
			  this.getReadableDatabase();  
			  try {  
				  copyDataBase();  
			  } catch (IOException e) {  
				  throw new Error("Error copying database");  
			  }  
		  }  
	}  
	
	private boolean checkDataBase() {  
		  File dbFile = new File(DB_PATH + DB_NAME);  
		  return dbFile.exists();  
	}  
		  
	private void copyDataBase() throws IOException {  
		  
		  InputStream myInput = context.getAssets().open(DB_NAME);  
		  String outFileName = DB_PATH + DB_NAME;  
		  OutputStream myOutput = new FileOutputStream(outFileName);  
		  byte[] buffer = new byte[1024];  
		  int length;  
		  while ((length = myInput.read(buffer)) > 0) {  
			  myOutput.write(buffer, 0, length);  
		  }  
		  
		  // Close the streams  
		  myOutput.flush();  
		  myOutput.close();  
		  myInput.close();  
		  
	}  
	@Override
	public void onCreate(SQLiteDatabase arg0) {
		// TODO Auto-generated method stub
		
	}

	public void onUpgrade(SQLiteDatabase database, int oldVersion,
	        int newVersion) {
	    Log.w(DBHelper.class.getName(),
	            "Upgrading database from version " + oldVersion  + " to "
	                    + newVersion + ", which will destroy all old data");
	    database.execSQL("DROP TABLE IF EXISTS " + DB_NAME);
	    onCreate(database);
	}

}
