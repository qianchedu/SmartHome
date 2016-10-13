package com.dsz.home;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.dsz.threads.MyThread;
import com.dsz.view.MySurfaceView;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VideoActivity extends Activity {
	private String TAG = "VideoActivity";
	public final static String CONTROLER_POSITION = "/remoteCamera/controler/";		//文件路径
	private LinearLayout mRelativeLayout;		//布局
	private Socket socket = null;						//socket对象
	private String buffer = "";							//（好像没用到）
	private MySurfaceView mySurfaceView;							//自定义SurfaceView
	private ImageButton takePhotoBtn = null;
	private ImageButton ibClose = null;
	private ImageButton takeVideoBtn = null;
	private ImageButton checkBtn = null;
	private ImageView receImageView = null;
	private boolean buttonFlag = true;					//（好像没用到）
	private static String lastFileName = null;
	private String ip;
	 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//设置为没有title模式
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
         
        //获取wifi服务  
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//        //判断wifi是否开启
//        if (!wifiManager.isWifiEnabled()) {
//        wifiManager.setWifiEnabled(true);
//        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();    	//用来获取当前已连接上的wifi的信息。
        int ipAddress = wifiInfo.getIpAddress();   					//用来获取当前已连接上的wifi的地址。
        //将获得的ip地址转化为8位8位的格式，并将主机号用1代替，表示被控制端的IP地址
        ip = tranIP(ipAddress);
		Log.v(TAG,"服务器地址：" + ip);
		setContentView(R.layout.activity_video);

		mySurfaceView =(MySurfaceView) this.findViewById(R.id.mySurfaceViewVideo1);
		mRelativeLayout =(LinearLayout) this.findViewById(R.id.relativeLayout);
		String VideoIP="http://" + ip + ":8080/?action=stream";	//需要服务的相机ip地址
		mySurfaceView.GetVideoIP(VideoIP);
		findView();
		Log.v(TAG,"初始化完成");



        MyThread myThread = new MyThread("opentests",ip);
        myThread.start();



    }

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mySurfaceView.surfacePause();

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mySurfaceView.surfaceResume();
	}
	
	void findView(){
		takePhotoBtn = (ImageButton)findViewById(R.id.takePhoto);
		ibClose = (ImageButton)findViewById(R.id.video_IB_close);
		takeVideoBtn = (ImageButton)findViewById(R.id.takeVideoBtn);

		checkBtn = (ImageButton)findViewById(R.id.settingBtn);
		//receImageView = (ImageView)findViewById(R.id.receImageView);

		//自动对焦点击事件
		this.mRelativeLayout.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				new MyThreads("antufocus").start();
				
			}
		});


		//远程指令 拍照 点击事件
		ibClose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//发送拍照指令
				new MyThreads("closephot").start();
//				Toast.makeText (VideoActivity.this, "拍照指令已发送，等待图片回传！ ", Toast.LENGTH_SHORT).show ();
				finish();
			}
		});

		//远程指令 拍照 点击事件
		takePhotoBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//发送拍照指令
                new MyThreads("takephoto").start();
                Toast.makeText (VideoActivity.this, "拍照指令已发送，等待图片回传！ ", Toast.LENGTH_SHORT).show ();
			}
		});
		
		//对拍照控件进行图片设置
		takePhotoBtn.setOnTouchListener(new OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				 if(event.getAction() == MotionEvent.ACTION_DOWN){
                     //更改为按下时的背景图片     
                     ((ImageButton)v).setImageDrawable(getResources().getDrawable(R.mipmap.take_photo_down));
                     
	             }else if(event.getAction() == MotionEvent.ACTION_UP){
	                     //改为抬起时的图片     
	            	 ((ImageButton)v).setImageDrawable(getResources().getDrawable(R.mipmap.take_photo));
	             }     
				return false;
				}
 		});
		

		
		//查看图片	按钮监听函数
		checkBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
					//跳转查看图片
				if(lastFileName != null){
					Intent intent = new Intent(VideoActivity.this,PictureActivity.class);
					intent.setType(lastFileName);
					startActivity(intent);
				}
			}
		});
     }
    
    //ip地址转化函数
    private String tranIP(int i) {
        
        return (i & 0xFF ) + "." +       
      ((i >> 8 ) & 0xFF) + "." +       
      ((i >> 16 ) & 0xFF) + "." + 1;  //最后的主机号转化为1
   } 
    /** 
     * 将接收到的照片存放在SD卡中 
     * @param data   
     * @throws IOException
     */  
    public static void saveToSDCard(byte[] data,Context context) throws IOException {

        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); // 格式化时间
        String filename = format.format(date) + ".jpg";
        File fileFolder = new File(Environment.getExternalStorageDirectory()
                + CONTROLER_POSITION);  
        if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"finger"的目录  
            fileFolder.mkdir();  
        }  
        File jpgFile = new File(fileFolder, filename);
        FileOutputStream outputStream = new FileOutputStream(jpgFile); // 文件输出流
        outputStream.write(data); // 写入sd卡中  
        outputStream.close(); // 关闭输出流  
        
        lastFileName = filename;
        // 其次把文件插入到系统图库
        try {
            MediaStore.Images.Media.insertImage(context.getContentResolver(),
            		jpgFile.getAbsolutePath(), filename, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // 最后通知图库更新,没有通知是不会更新的
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + CONTROLER_POSITION));
        context.sendBroadcast(intent);
    }


    /**
	 * 更新ui
	 */
	public Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0x11) {
				Bundle bundle = msg.getData();
				int size = bundle.getInt("size");
				System.out.println("接受到一次数据大小为:" + size);
				byte[] data = new byte[size];
				data = bundle.getByteArray("msg");
                try {
				saveToSDCard(data,VideoActivity.this);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                //byte转化为指定大小，放置作为imagebutton的背景
                Bitmap Bigbmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                Bitmap bitmap = zoomImage(Bigbmp,60,60);
                checkBtn.setImageBitmap(bitmap);
                //有拍摄到图片后控件才可见
                checkBtn.setVisibility(View.VISIBLE);
                Toast.makeText (VideoActivity.this, "图片回传成功！ ", Toast.LENGTH_SHORT).show ();
			}
		}

	};
	/**
    * 图片的缩放方法
    *
    * @param bgimage ：源图片资源
    * @param newWidth
    *            ：缩放后宽度
    * @param newHeight
    *            ：缩放后高度dp
    * @return
    */
   public Bitmap zoomImage(Bitmap bgimage, double newWidth,
						   double newHeight) {
		 //将px转化为dp
		 final float scale = getResources().getDisplayMetrics().density; 
		 int dpnewWidth = (int) (newWidth * scale + 10.5f);//进行dp与pix转化
		 int dpnewHeight = (int) (newHeight * scale + 10.5f);//进行dp与pix转化
		 
           // 获取这个图片的宽和高 
           float width = bgimage.getWidth(); 
           float height = bgimage.getHeight(); 
           // 创建操作图片用的matrix对象 
           Matrix matrix = new Matrix();

//			matrix.setRotate(90);
           // 计算宽高缩放率
           float scaleWidth = ((float) dpnewWidth) / width; 
           float scaleHeight = ((float) dpnewHeight) / height; 
           // 缩放图片动作 
           matrix.postScale(scaleWidth, scaleHeight); 
           Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                           (int) height, matrix, true); 
           return bitmap; 
   } 
	class MyThreads extends Thread {

		public String txt1;
		
		public MyThreads(String str) {
			txt1 = str;
		}

		@Override
		public void run() {
			//定义消息
			Message msg = new Message();
			msg.what = 0x11;
			Bundle bundle = new Bundle();
			bundle.clear();
			try {
				//连接服务器 并设置连接超时为5秒
				socket = new Socket();
				socket.connect(new InetSocketAddress(ip, 30000), 1000);
//				socket.connect(new InetSocketAddress("192.168.43.1", 30000), 1000);
				//获取输入输出流
				OutputStream ou = socket.getOutputStream();
				DataInputStream dataInput = new DataInputStream(socket.getInputStream());
				
				//向服务器发送信息
				ou.write(txt1.getBytes("gbk"));
				ou.flush();
				
				//获取图片
				int size = dataInput.readInt(); 
				byte[] data = new byte[size]; 
				int len = 0;    
                while (len < size) {    
                    len += dataInput.read(data, len, size - len);    
                } 
                bundle.putInt("size", size);
                bundle.putByteArray("msg", data);              		
				msg.setData(bundle);
				//发送消息 修改UI线程中的组件
				myHandler.sendMessage(msg);
				
				//关闭各种输入输出流
                dataInput.close();
				ou.close();
				socket.close();
                
			} catch (SocketTimeoutException aa) {
				//连接超时 在UI界面显示消息
				bundle.putString("msg", "服务器连接失败！请检查网络是否打开");
				msg.setData(bundle);
				//发送消息 修改UI线程中的组件
				myHandler.sendMessage(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    
}


