package com.rigen.rigendriver;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.rigen.rigendriver.R;
import com.rigen.rigendriver.db.DBQuery;
import com.rigen.rigendriver.lib.ClientConnectPost;

import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ILHAM HP on 15/12/2016.
 */

public class ChangePassActivity extends AppCompatActivity {
    private EditText txt_old_pass, txt_new_pass, txt_retype_pass;
    private TextInputLayout tilold_pass, til_new_pass, til_retype;
    private Button btn_cancel, btn_simpan;
    private DBQuery db;
    private String old_pass, username;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setTitle("Ganti Password");
        setContentView(R.layout.activity_changepass);
        txt_old_pass = (EditText) findViewById(R.id.et_old_pass);
        txt_new_pass = (EditText) findViewById(R.id.et_newpass);
        txt_retype_pass = (EditText) findViewById(R.id.et_retype);
        tilold_pass = (TextInputLayout) findViewById(R.id.til_old);
        til_new_pass = (TextInputLayout) findViewById(R.id.til_new);
        til_retype = (TextInputLayout) findViewById(R.id.til_retype);
        btn_cancel = (Button) findViewById(R.id.btn_batal);
        btn_simpan = (Button) findViewById(R.id.btn_ganti);
        db = new DBQuery(this);
        db.open();
        Cursor c = db.getAll();
        c.moveToFirst();
        old_pass = c.getString(c.getColumnIndex("password"));
        username = c.getString(c.getColumnIndex("username"));
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_simpan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (md5(txt_old_pass.getText().toString()).equals(old_pass)){
                    if (validate()){
                        Ganti();
                    }
                }else {
                    tilold_pass.setError("Kata sandi salah.");
                }
            }
        });
    }
    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
    private boolean validate(){
        boolean b=true;
        if (txt_new_pass.getText().toString().equals(txt_retype_pass.getText().toString())){
            til_retype.setError(null);
            b = true;
        }else{
            til_retype.setError("Kata sandi tidak sama.");
        }
        return  b;
    }
    private void Ganti(){
        Map<String, String> dataToSend = new HashMap<String, String>();
        dataToSend.put("username", username);
        dataToSend.put("password", txt_new_pass.getText().toString());
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(this);
        client.execute(getString(R.string.server) + "androiddriver/updatepass", encodedStr);
        try {
            JSONObject j = client.get();
            int res = j.getInt("r");
            String msg = j.getString("msg");
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage(msg);
            if (res == 1) {
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    db.open();
                    db.updatePass(md5(txt_new_pass.getText().toString()), username);
                    finish();
                    }
                });
            } else {
                alert.setPositiveButton("OK", null);
            }
            alert.show();
        }catch (Exception e){}
    }
}
