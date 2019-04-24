package com.rigen.rigendriver;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.rigen.rigendriver.db.DBQuery;
import com.rigen.rigendriver.lib.ClientConnectPost;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ILHAM HP on 16/11/2016.
 */

public class LoginActivity extends AppCompatActivity {
    private Button btn_login;
    private EditText et_username,et_password;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        btn_login = (Button) findViewById(R.id.btn_login);
        et_username = (EditText) findViewById(R.id.et_username);
        et_password = (EditText) findViewById(R.id.et_password);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
    }
    private void login(){
        Map<String, String> dataToSend = new HashMap<String, String>();
        dataToSend.put("username", et_username.getText().toString());
        dataToSend.put("password", et_password.getText().toString());
        String encodedStr = ClientConnectPost.getEncodedData(dataToSend);
        ClientConnectPost client = new ClientConnectPost(this);
        client.execute(getString(R.string.server) + "androiddriver/login", encodedStr);
        try {
            JSONObject j = client.get();
            int res = j.getInt("r");
            String msg = j.getString("msg");
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setMessage(msg);
            if (res == 1) {
                final JSONObject jo = j.getJSONObject("data");
                final EntityAccount e = new EntityAccount();
                if (res==1){
                    e.setUsername(jo.getString("username"));
                    e.setNama(jo.getString("nama"));
                    e.setPassword(jo.getString("password"));
                    e.setEmail(jo.getString("email"));
                    e.setNo_telp(jo.getString("no_telp"));
                    e.setAlamat(jo.getString("alamat"));
                }
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DBQuery db = new DBQuery(getApplicationContext());
                        db.open();
                        db.clear();
                        db.insert(e);
                        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
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
