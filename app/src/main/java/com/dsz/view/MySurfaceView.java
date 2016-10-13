package com.dsz.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MySurfaceView extends SurfaceView implements Callback
{
	private String TAG ="MySurfaceView";
	private String urlstr;
	private Paint mPaint;
	private Canvas canvas;
	private SurfaceHolder surfaceHolder;

	InputStream inputstream=null;
	private Bitmap mBitmap;
//	Bitmap bmp;
	private static int mScreenWidth;
	private static int mScreenHeight;


	public boolean runToOver = false;
	public boolean Is_Scale = false;
	public boolean isFirstRun = true;
	public boolean runFlag = true;		//用于保证线程会被执行完毕
	URL videoUrl;
	HttpURLConnection conn;


	public MySurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initialize();
		mPaint = new Paint();
		mPaint.setAntiAlias(true);				//用来防止边缘的锯齿
		surfaceHolder = this.getHolder();
		surfaceHolder.addCallback(this);
		this.setKeepScreenOn(true);				//保存屏幕常量
		setFocusable(true);
		this.getWidth();
		this.getHeight();
		Log.v(TAG, "initialize");
	}

	/**
	 *初始化屏幕参数
	 */
	private void initialize()
	{
		//将px转化为dp
		final float scale = getResources().getDisplayMetrics().density;
		int dp = (int) (150 * scale + 10.5f);//这里的100是在显示屏上用了100DP的空间用于显示控制按键，根据需要修改

		DisplayMetrics dm = getResources().getDisplayMetrics();//获取屏幕的宽度和高度
		mScreenWidth = dm.widthPixels;
		mScreenHeight = (dm.heightPixels * 8) / 9;
		this.setKeepScreenOn(true);//保持屏幕常亮
	}

	/**
	 * 绘制视频
 	 */
	class DrawVideo extends Thread
	{
		public DrawVideo(){}

		@Override
		protected Object clone() throws CloneNotSupportedException
		{
			// TODO Auto-generated method stub
			return super.clone();
		}

		public void run()
		{

			Paint pt = new Paint();
			pt.setAntiAlias(true);
			pt.setColor(Color.GREEN);
			pt.setTextSize(20);
			pt.setStrokeWidth(1);					//设置线宽

			int bufSize = 512 * 1024;
			byte[] jpg_buf = new byte[bufSize];	        // 缓冲区读取 jpg
			int readSize = 4096;
			byte[] buffer = new byte[readSize];	        // 缓冲区读取 流（Stream)
			HttpURLConnection urlConn = null;

			while (runFlag)
			{

				long Time = 0;
				long Span = 0;
				int fps = 0;			//画面的帧数  fpszhi值越高，画面越流畅
				String str_fps = "0 fps";
				URL url = null;

				try
				{
					url = new URL(urlstr);
					urlConn = (HttpURLConnection)url.openConnection();	//HTTPURLConnetion
					Time = System.currentTimeMillis();
					int read = 0;
					int status = 0;
					int jpg_count = 0;                          //jpg
					while (runFlag)
					{
						runToOver = false;
						read = urlConn.getInputStream().read(buffer, 0, readSize);
//						Log.i(TAG,buffer.toString());
						if (read  > 0)
						{

							for (int i = 0; i < read; i++)
							{
								switch (status)
								{
									//Content-Length:
									case 0: if (buffer[i] == (byte)'C')
										status++;
										else
										status = 0;

										break;
									case 1: if (buffer[i] == (byte)'o') status++; else status = 0; break;
									case 2: if (buffer[i] == (byte)'n') status++; else status = 0; break;
									case 3: if (buffer[i] == (byte)'t') status++; else status = 0; break;
									case 4: if (buffer[i] == (byte)'e') status++; else status = 0; break;
									case 5: if (buffer[i] == (byte)'n') status++; else status = 0; break;
									case 6: if (buffer[i] == (byte)'t') status++; else status = 0; break;
									case 7: if (buffer[i] == (byte)'-') status++; else status = 0; break;
									case 8: if (buffer[i] == (byte)'L') status++; else status = 0; break;
									case 9: if (buffer[i] == (byte)'e') status++; else status = 0; break;
									case 10: if (buffer[i] == (byte)'n') status++; else status = 0; break;
									case 11: if (buffer[i] == (byte)'g') status++; else status = 0; break;
									case 12: if (buffer[i] == (byte)'t') status++; else status = 0; break;
									case 13: if (buffer[i] == (byte)'h') status++; else status = 0; break;
									case 14: if (buffer[i] == (byte)':') status++; else status = 0; break;
									case 15:
										if (buffer[i] == (byte)0xFF) status++;
										jpg_count = 0;
										jpg_buf[jpg_count++] = (byte)buffer[i];
										break;
									case 16:
										if (buffer[i] == (byte)0xD8)
										{
											status++;
											jpg_buf[jpg_count++] = (byte)buffer[i];
										}
										else
										{
											if (buffer[i] != (byte)0xFF) status = 15;

										}
										break;
									case 17:
										jpg_buf[jpg_count++] = (byte)buffer[i];
										if (buffer[i] == (byte)0xFF) status++;
										if (jpg_count >= bufSize) status = 0;
										break;
									case 18:
										System.out.println(jpg_buf[jpg_count++] = (byte)buffer[i]);
										if (buffer[i] == (byte)0xD9)
										{
											System.out.println("shijian");
											status = 0;
											//jpg

											fps++;
											Span = System.currentTimeMillis()-Time;
											if(Span > 1000L)
											{
												Time = System.currentTimeMillis();
												str_fps = String.valueOf(fps)+" fps";
												fps = 0;
											}
											//
											canvas = surfaceHolder.lockCanvas();
											canvas.drawColor(Color.BLACK);
											Bitmap bmp = BitmapFactory.decodeStream(new ByteArrayInputStream(jpg_buf));
											bmp = adjustPhotoRotation(bmp,90);

											int width = mScreenWidth;
											int height = mScreenHeight;

											//屏幕与图片之比
											float rate_width = (float)mScreenWidth / (float)bmp.getWidth();
											float rate_height = (float)mScreenHeight / (float)bmp.getHeight();

											if(Is_Scale)
											{
												if(rate_width>rate_height) width = (int)(bmp.getWidth()*rate_width);
												if(rate_width<rate_height) height = (int)(bmp.getHeight()*rate_height);
											}
											mBitmap = Bitmap.createScaledBitmap(bmp, width, height, false);
//											canvas.drawBitmap(mBitmap, (mScreenWidth-width)/2,(mScreenHeight-height)/2, null);
											canvas.drawBitmap(mBitmap, new Rect(0, 0, mBitmap.getWidth(), mBitmap.getHeight()),new Rect(0, 0, width, height), null);
											canvas.drawText(str_fps, 2, 22, pt);

											surfaceHolder.unlockCanvasAndPost(canvas);

											Log.i(TAG,"============");

										}
										else
										{
											if (buffer[i] != (byte)0xFF) status = 17;
										}
										break;
									default:
										status = 0;
										break;

								}
//								System.out.println(buffer[i] + "hahah");
							}
						}
						// runToOver = true;
					}
				}
				catch (IOException ex)
				{
					urlConn.disconnect();
					ex.printStackTrace();
				}
				runToOver = true;
			}
		}
	}



	/**
	 *使图片旋转
	 * @param bitmap	图片
	 * @param orientationDegree 旋转的角度 0 - 360 范围
	 * @return
	 */
	Bitmap adjustPhotoRotation(Bitmap bitmap, int orientationDegree) {


		Matrix matrix = new Matrix();
		matrix.setRotate(orientationDegree, (float) bitmap.getWidth() / 2,
				(float) bitmap.getHeight() / 2);
		float targetX, targetY;
		if (orientationDegree == 90) {
			targetX = bitmap.getHeight();
			targetY = 0;
		} else {
			targetX = bitmap.getHeight();
			targetY = bitmap.getWidth();
		}


		final float[] values = new float[9];
		matrix.getValues(values);


		float x1 = values[Matrix.MTRANS_X];
		float y1 = values[Matrix.MTRANS_Y];


		matrix.postTranslate(targetX - x1, targetY - y1);


//		Bitmap canvasBitmap = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getWidth(),
//				Bitmap.Config.ARGB_8888);
		Bitmap canvasBitmap = Bitmap.createBitmap(bitmap.getHeight(),bitmap.getWidth(), Bitmap.Config.ARGB_8888);

		Paint paint = new Paint();
		Canvas canvas = new Canvas(canvasBitmap);
		canvas.drawBitmap(bitmap, matrix, paint);


		return canvasBitmap;
	}

	/**
	 * 获取视频地址
 	 * @param p		视频地址
     */
    public void GetVideoIP(String p){
		urlstr=p;
		Log.v(TAG,"GetVideoIP");
	}


	public void surfacePause(){
		while(runToOver);//必须等线程执行完毕才能暂停
		runFlag = false;
	}
	public void surfaceResume(){
		runFlag = true;
	}
	//自动调用，在surface创建时
	public void surfaceCreated(SurfaceHolder holder) {
		new DrawVideo().start();
	}
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
							   int height) {
	}
	public void surfaceDestroyed(SurfaceHolder holder) {

	}
}    