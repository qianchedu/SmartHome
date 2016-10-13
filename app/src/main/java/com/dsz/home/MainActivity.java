package com.dsz.home;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import com.dsz.threads.MyThread;
import com.dsz.view.CustomDialog;
import com.dsz.wifi.WifiSetup;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    public final String TAG = "MainActivity";
    public final static String CONTROLER_POSITION = "/remoteCamera/";
    SharedPreferences mSharedPreferences;

    private Button btnOpen;
    private Button btnClose;
    private String ip;

    private Thread openWifiThread;         //声明一个子线程

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initWifiApSetting();

        setContentView(R.layout.activity_main);
        infoSys();

        if (!initialize()) {
            Toast.makeText(this, "不能初始化参数", Toast.LENGTH_LONG).show();
        }
        initViews();
        initEvents();
    }

    private void initWifiApSetting() {
        WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        int ipAddress = wifiInfo.getIpAddress();

        ip  = tartIP(ipAddress);

    }

    private String tartIP(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." + 1;  //最后的主机号转化为1
    }

    private void initEvents() {
        btnOpen.setOnClickListener(this);
        btnClose.setOnClickListener(this);
    }

    private void initViews() {
        btnClose = (Button)findViewById(R.id.main_btn_close);
        btnOpen = (Button)findViewById(R.id.main_btn_open);
    }


    private void infoSys() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.main_btn_open:
                openTest();
                break;
            case R.id.main_btn_close:
                closeTest();
                break;
        }
    }

    private void closeTest() {
//        MyThread myThread = new MyThread("closetest",ip);
//        myThread.start();
        Toast.makeText(this, "测试早就结束了", Toast.LENGTH_SHORT).show();
    }

    private void openTest() {
//        MyThread myThread = new MyThread("opentests",ip);
//        myThread.start();

        WifiSetup wifiSet = new WifiSetup(MainActivity.this);	//wifi设置
        if(wifiSet.isWifiConnected()){
            startActivity(new Intent(this , VideoActivity.class));//进入遥控界面
        }else{
            //进入系统wifi设置界面
            setWifiDialog();
        }



    }
    //对话框----没有连接到被遥控端的wifi热点
    public void setWifiDialog() {

        CustomDialog.Builder builder = new CustomDialog.Builder(this);
        builder.setMessage("   亲，尚未连接相机！\n   为您跳转到连接设置？");
        builder.setTitle("提示");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                //一下是按确认键响应函数
                //在切到系统之前先打开wifi，（创建一个线程去做，以提高UI的响应,好像没有什么效果，后边再找找问题）
                openWifiThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        try {
                            //必须先延时，等待系统跳转到wifi界面之后再打开wifi
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        WifiSetup wifiSet = new WifiSetup(MainActivity.this);
                        wifiSet.openWifi();
                    }
                });
                openWifiThread.start();
                //进入系统wifi设置界面
                gotoSysWifi();
            }
        });
        builder.setNegativeButton("取消",
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.create().show();

    }

    /**
     * Function:到系统中设置wifi<br>
     * <br>
     */
    private void gotoSysWifi() {
        Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        startActivityForResult(intent, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    private boolean initialize() {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        File fileFolder = new File(Environment.getExternalStorageDirectory()
                + CONTROLER_POSITION);
        if (!fileFolder.exists()) { // 如果目录不存在，则创建一个目录
            fileFolder.mkdir();
        }

        boolean firstRun = ! mSharedPreferences.contains("settings_camera");
        if (firstRun) {
            Log.v(TAG, "First run");

            SharedPreferences.Editor editor = mSharedPreferences.edit();

            int cameraNumber = Camera.getNumberOfCameras();     //获取相机的数量
            Log.v(TAG, "Camera number: " + cameraNumber);


            //相机名
            TreeSet<String> cameraNameSet = new TreeSet<String>();
            if (cameraNumber == 1) {
                cameraNameSet.add("back");
            } else if (cameraNumber == 2) {
                cameraNameSet.add("back");
                cameraNameSet.add("front");
            } else if (cameraNumber > 2) {           // rarely happen
                for (int id = 0; id < cameraNumber; id++) {
                    cameraNameSet.add(String.valueOf(id));
                }
            } else {                                 // no camera available
                Log.v(TAG, "No camrea available");
                Toast.makeText(this, "No camera available", Toast.LENGTH_SHORT).show();
                return false;
            }



            //相机ID
            String[] cameraIds = new String[cameraNumber];
            TreeSet<String> cameraIdSet = new TreeSet<String>();
            for (int id = 0; id < cameraNumber; id++) {
                cameraIdSet.add(String.valueOf(id));
            }


            editor.putStringSet("camera_name_set", cameraNameSet);//相机名称
            editor.putStringSet("camera_id_set", cameraIdSet);      //相机ID


            for (int id = 0; id < cameraNumber; id++) {
                Camera camera = Camera.open(id);
                if (camera == null) {
                    String msg = "Camera " + id + " is not available";
                    Log.v(TAG, msg);
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

                    return false;
                }

                Camera.Parameters parameters = camera.getParameters();     //获取相机参数


                List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();

                TreeSet<String> sizeSet = new TreeSet<String>(new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        int spaceIndex1 = s1.indexOf(" ");
                        int spaceIndex2 = s2.indexOf(" ");
                        int width1 = Integer.parseInt(s1.substring(0, spaceIndex1));
                        int width2 = Integer.parseInt(s2.substring(0, spaceIndex2));

                        return width2 - width1;
                    }
                });
                Log.v(TAG,sizeSet.toString());
                for (Camera.Size size : sizes) {
                    sizeSet.add(size.width + " x " + size.height);
                }
                editor.putStringSet("preview_sizes_" + id, sizeSet);

                Log.v(TAG, sizeSet.toString());


                if (id == 0) {
                    Log.v(TAG, "Set default preview size");

                    Camera.Size defaultSize = parameters.getPreviewSize();
                    editor.putString("settings_size", defaultSize.width + " x " + defaultSize.height);
                }


                List<int[]> ranges = parameters.getSupportedPreviewFpsRange();
                TreeSet<String> rangeSet = new TreeSet<String>();
                for (int[] range : ranges) {
                    rangeSet.add(range[0] + " ~ " + range[1]);
                }
                editor.putStringSet("preview_ranges_" + id, rangeSet);

                if (id == 0) {
                    Log.v(TAG, "Set default fps range");

                    int[] defaultRange = new int[2];
                    parameters.getPreviewFpsRange(defaultRange);
                    editor.putString("settings_range", defaultRange[0] + " ~ " + defaultRange[1]);
                }

                camera.release();

            }

            editor.putString("settings_camera", "0");
            editor.commit();
        }

        return true;
    }

    private long firstTime = 0;
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                long secondTime = System.currentTimeMillis();
                if(secondTime - firstTime > 2000){
                    Toast.makeText(this, "再按一次将会退出程序", Toast.LENGTH_SHORT).show();
                    firstTime = secondTime;
                    return true;
                }else{
                    System.exit(0);
                }
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
}
