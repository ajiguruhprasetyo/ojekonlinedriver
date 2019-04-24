package com.rigen.rigendriver.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.rigen.rigendriver.EntityAccount;

import java.io.IOException;

public class DBQuery {
	DBHelper dbHelper;
	SQLiteDatabase db;
	private final Context context;
	
	public DBQuery(Context context) {
		this.context = context;
	}

	public DBQuery open() throws SQLException {
		dbHelper = new DBHelper(context);
		try {
			dbHelper.createDataBase();
		} catch (IOException e) {
			e.printStackTrace();
		}
		db = dbHelper.getWritableDatabase();
		return this;
	}
	public void close() {
		dbHelper.close();
	}
	public Cursor getAll(){
		return db.query("account", new String[]{
				"*"
		}, null, null, null, null, null,null);
	}
	public void clear(){
		db.execSQL("DELETE FROM account");
	}
	public void insert(EntityAccount e){
		db.execSQL("INSERT INTO account(username,password,nama,no_telp,alamat) values('"+e.getUsername()+"','"+e.getPassword()+"'," +
				"'"+e.getNama()+"','"+e.getNo_telp()+"','"+e.getAlamat()+"')");
	}
	public void updatePass(String pass, String username){
		db.execSQL("UPDATE account SET password='"+pass+"' WHERE username ='"+username+"'");
	}
}
