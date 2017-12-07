package com.brtbeacon.sdk.demo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.brtbeacon.sdk.BRTBeacon;
import com.brtbeacon.sdk.BRTBeaconManager;
import com.brtbeacon.sdk.BRTThrowable;
import com.brtbeacon.sdk.callback.BRTBeaconManagerListener;
import com.google.gson.Gson;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

public class DeviceScanActivity extends Activity implements OnItemClickListener {

    private ListView listView;
    private List<BRTBeacon> beaconList = new ArrayList<BRTBeacon>();
    private ArrayAdapter<BRTBeacon> beaconAdapter = null;
    private BRTBeaconManager beaconManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        listView = (ListView) findViewById(R.id.listView);

        beaconAdapter = new ArrayAdapter<BRTBeacon>(this, R.layout.item_device_info, android.R.id.text1, beaconList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                BRTBeacon beacon = getItem(position);
                TextView tvClass = (TextView) view.findViewById(R.id.tv_class);
                tvClass.setText(Integer.toHexString(beacon.getMajor()) + "-" + String.valueOf(beacon.getMinor()));
                return view;
            }
        };

        listView.setAdapter(beaconAdapter);
        listView.setOnItemClickListener(this);

        beaconManager = BRTBeaconManager.getInstance(this);

        checkBluetoothValid();
    }

    @Override
    protected void onResume() {
        super.onResume();
        beaconManager.setBRTBeaconManagerListener(scanListener);
        beaconManager.startRanging();
    }

    @Override
    protected void onPause() {
        super.onPause();
        beaconManager.stopRanging();
        beaconManager.setBRTBeaconManagerListener(null);
    }

    private void checkBluetoothValid() {
        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle("错误").setMessage("你的设备不具备蓝牙功能!").create();
            dialog.show();
            return;
        }

        if (!adapter.isEnabled()) {
            AlertDialog dialog = new AlertDialog.Builder(this).setTitle("提示")
                    .setMessage("蓝牙设备未打开,请开启此功能后重试!")
                    .setPositiveButton("确认", new OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            Intent mIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(mIntent, 1);
                        }
                    })
                    .create();
            dialog.show();
        }
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {

        BRTBeacon beacon = (BRTBeacon) parent.getItemAtPosition(position);
        final String classroom = Integer.toHexString(beacon.getMajor()) + "-" + String.valueOf(beacon.getMinor());

        final AlertDialog.Builder sureDialog =new AlertDialog.Builder(DeviceScanActivity.this);
        sureDialog.setIcon(R.mipmap.ic_launcher);
        sureDialog.setTitle("请确认教室");
        sureDialog.setMessage("是否在" + classroom + "教室签到");
        sureDialog.setPositiveButton("确定",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = getIntent();
                        String username = intent.getStringExtra("username");

                        Date date = new Date();
                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH");
                        String datestr = format.format(date);
                        String intime = datestr;

                        MyBeacon myBeacon = new MyBeacon(username, classroom, intime);
                        Gson gson = new Gson();

                        String strJson = gson.toJson(myBeacon);

                        Log.d("jsonaaa", strJson);
                        PostThread postThread = new PostThread(strJson);
                        postThread.start();
                        Toast.makeText(DeviceScanActivity.this,"已签到成功",Toast.LENGTH_SHORT).show();
                    }
                });
        sureDialog.setNegativeButton("关闭",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //...To-do
                    }
                });
        sureDialog.show();

    }

    class PostThread extends Thread {

        String str;

        public PostThread(String str) {
            this.str = str;
        }

        public void run() {
            //第一步，创建httpclient对象
            HttpClient httpClient = new DefaultHttpClient();
            String url = "http://192.168.0.103:8080/tp5/public/index.php";
            //第二步，生成使用post方法的请求对象
            HttpPost httpPost = new HttpPost(url);
            try {
                StringEntity stringEntity = new StringEntity(str, "UTF-8");
                stringEntity.setContentType("application/json;charset=utf-8");
                httpPost.setEntity(stringEntity);
                //执行请求对象
                try {
                    //第三步，执行请求对象，获取服务器发还的相应对象
                    HttpResponse response = httpClient.execute(httpPost);
                    HttpEntity en = response.getEntity();
                    Log.d("postcodeaaaaaa", EntityUtils.toString(en, "UTF-8"));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private BRTBeaconManagerListener scanListener = new BRTBeaconManagerListener() {

        @Override
        public void onUpdateBeacon(final ArrayList<BRTBeacon> arg0) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    beaconList.clear();
                    beaconAdapter.notifyDataSetChanged();

                    for (int i = 0; i <arg0.size(); i++) {
                        if (arg0.get(i).getMajor() == 0 || arg0.get(i).getMinor() == 0) {
                            Log.d("bbb2",arg0.get(i).getUuid());
                            arg0.remove(i);
                        }
                    }
                    beaconList.addAll(arg0);
                    beaconAdapter.notifyDataSetChanged();
                }
            });

        }

        @Override
        public void onNewBeacon(BRTBeacon arg0) {

        }

        @Override
        public void onGoneBeacon(BRTBeacon arg0) {

        }

        @Override
        public void onError(BRTThrowable arg0) {

        }

    };

}
