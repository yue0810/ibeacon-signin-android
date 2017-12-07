package com.brtbeacon.sdk.demo;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class LoginActivity extends Activity {

    TextView mes;
    EditText edname, edpwd;
    Button btlogin;
    String sname, spwd;
    Intent intent;

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 100) {
                mes.setText("用户名或密码错误");
                edname.setText("");
                edpwd.setText("");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mes = (TextView) findViewById(R.id.mes);
        edname = (EditText) findViewById(R.id.edname);
        edpwd = (EditText) findViewById(R.id.edpwd);
        btlogin = (Button) findViewById(R.id.btlogin);

        btlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sname = edname.getText().toString().trim();
                spwd = edpwd.getText().toString().trim();
                if (TextUtils.isEmpty(sname) || TextUtils.isEmpty(spwd)) {
                    Toast.makeText(LoginActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                } else {

                    User auser = new User(sname, spwd);
                    Gson gson = new Gson();
                    final String jsonstr = gson.toJson(auser);
                    Log.d("jsonsssss", jsonstr);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HttpClient httpClient = new DefaultHttpClient();
                            String url = "http://192.168.0.103:8080/tp5/public/admin.php";
                            HttpPost httpPost = new HttpPost(url);
                            try {
                                StringEntity stringEntity = new StringEntity(jsonstr, "UTF-8");

                                stringEntity.setContentType("application/json;charset=utf-8");
                                httpPost.setEntity(stringEntity);
                                try {
                                    HttpResponse response = httpClient.execute(httpPost);
                                    Log.d("code", String.valueOf(response.getStatusLine().getStatusCode()));
                                    Log.d("mess", response.getStatusLine().toString());

                                    HttpEntity en = response.getEntity();
                                    if (EntityUtils.toString(en, "UTF-8").equals("success")) {
                                        Intent intent = new Intent(LoginActivity.this, DeviceScanActivity.class);
                                        intent.putExtra("username", sname);
                                        intent.putExtra("userpwd", spwd);
                                        startActivity(intent);
                                        Log.d("right", EntityUtils.toString(en, "UTF-8"));
                                    } else {
                                        handler.sendEmptyMessage(100);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });
    }
}
