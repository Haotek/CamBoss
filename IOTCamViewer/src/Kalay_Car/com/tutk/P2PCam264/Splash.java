package com.tutk.P2PCam264;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.tutk.Kalay.general.R;

public class Splash extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder mSurfaceHolder = null;
	private int mScreenWidth = 1;
	private int mScreenHeight = 1;
	private Context mContext;
	private String mVersion;

	public Splash(Context context, AttributeSet attrs) {
		super(context, attrs);

		mContext = context;
		mSurfaceHolder = getHolder();
		mSurfaceHolder.addCallback(this);
	}
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

		mScreenWidth = width;
		mScreenHeight = height;

		TextPaint paint = new TextPaint();
		paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		paint.setTextSize(20);
		paint.setAntiAlias(true);
		paint.setARGB(200, 255, 255, 255);

		Bitmap bmp = null;

		bmp = getTextureFromBitmapResource(mContext, R.drawable.splash_bg);

		/*
		 * if (width == 480 && height == 800) bmp =
		 * getTextureFromBitmapResource(mContext, R.drawable.splash_480x800);
		 * else if (width == 320 && height == 480) bmp =
		 * getTextureFromBitmapResource(mContext, R.drawable.splash_320x480);
		 * else bmp = getTextureFromBitmapResource(mContext,
		 * R.drawable.splash_480x800);
		 */

		Rect rect = new Rect(0, 0, mScreenWidth, mScreenHeight);

		Canvas canvas = mSurfaceHolder.lockCanvas(null);

		if (canvas != null) {
			canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG));
			canvas.drawBitmap(bmp, null, rect, new Paint());
			canvas.drawText(mVersion, 20, mScreenHeight - 30, paint);

			mSurfaceHolder.unlockCanvasAndPost(canvas);
			canvas = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	public static Bitmap getTextureFromBitmapResource(Context context, int resourceId) {

		InputStream is = context.getResources().openRawResource(resourceId);
		Bitmap bitmap = null;

		try {

			bitmap = BitmapFactory.decodeStream(is);

		} finally {
			// Always clear and close
			try {
				is.close();
				is = null;
			} catch (IOException e) {
			}
		}

		return bitmap;
	}

	public void setVersion(String ver) {

		mVersion = ver;
	}
}
