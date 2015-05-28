package com.dlnetwork;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CustomProgressBar extends LinearLayout {
	private Context context;
	public boolean isShow = false;
	private int screenWidth, screenHeight;
	private WindowManager windowManager;

	private WindowManager.LayoutParams windowManagerParams = new WindowManager.LayoutParams();

	private float mTouchX;
	private float mTouchY;
	private float x;
	private float y;
	private int startX;
	private int startY;
	private OnClickListener mClickListener;
	private int controlledSpace = 20;
	ImageView imgView;
	TextView textView;
	public Integer xFloat=0;
	public Integer yFloat=0;

	public CustomProgressBar(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.context = context;
		initView(context);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		x = event.getRawX();
		y = event.getRawY();

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			mTouchX = event.getX();
			mTouchY = event.getY();
			startX = (int) event.getRawX();
			startY = (int) event.getRawY();
			break;

		}
		case MotionEvent.ACTION_MOVE: {
			updateViewPosition();
			break;
		}
		case MotionEvent.ACTION_UP: {

			if (Math.abs(x - startX) < controlledSpace
					&& Math.abs(y - startY) < controlledSpace) {
				if (mClickListener != null) {
					mClickListener.onClick(this);
					break;
				}
			}
			updateViewPosition();
			break;
		}
		}

		return super.onTouchEvent(event);
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		this.mClickListener = l;
	}

	private void updateViewPosition() {
		windowManagerParams.x = (int) (x - mTouchX);
		windowManagerParams.y = (int) (y - mTouchY);
		windowManager.updateViewLayout(this, windowManagerParams); // 刷新显示
	}

	public void initView(Context c) {
		// 构造window
		windowManager = (WindowManager) c.getApplicationContext()
				.getSystemService(Context.WINDOW_SERVICE);
		screenWidth = windowManager.getDefaultDisplay().getWidth();
		screenHeight = windowManager.getDefaultDisplay().getHeight();
		windowManagerParams.type = WindowManager.LayoutParams.TYPE_PHONE;
		windowManagerParams.format = PixelFormat.RGBA_8888; // 背景透明
		windowManagerParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
		windowManagerParams.gravity = Gravity.LEFT | Gravity.TOP;
		windowManagerParams.x =screenHeight*xFloat/100; //screenHeight / 9 * 2;
		windowManagerParams.y =screenWidth*yFloat/100; //screenWidth / 10;
		windowManagerParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		windowManagerParams.height = WindowManager.LayoutParams.WRAP_CONTENT;

		// 构造小猫控件
		this.setOrientation(LinearLayout.VERTICAL);
		this.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);

		textView = new TextView(context);
		LayoutParams textViewParams=new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		textView.setPadding(5, 5, 5, 5);
	    textView.setTextColor(Color.parseColor("#336699"));
		textView.setLayoutParams(textViewParams);
		LinearLayout textLayout = new LinearLayout(context);
		LayoutParams textLayoutParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		textLayout.setLayoutParams(textLayoutParams);
		final GradientDrawable localGradientDrawable1 = new GradientDrawable(
				GradientDrawable.Orientation.TOP_BOTTOM, new int[] {
						Color.parseColor("#90ffffff"),
						Color.parseColor("#90ffffff"),
						Color.parseColor("#90ffffff"),
						Color.parseColor("#90ffffff") });
		localGradientDrawable1.setShape(GradientDrawable.RECTANGLE);
		float[] arrayOfFloat2 = { 8, 8, 8, 8, 8, 8, 8, 8 };
		localGradientDrawable1.setCornerRadii(arrayOfFloat2);
		textView.setBackgroundDrawable(localGradientDrawable1);
		textLayout.addView(textView);
		this.addView(textLayout);

		imgView = new ImageView(context);
		LayoutParams imgViewParams = new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		this.addView(imgView, imgViewParams);

	}

	public void hide() {
		if (isShow) {
			windowManager.removeView(this);
			isShow = false;
		}
	}

	public void show() {
		if (isShow == false) {
			windowManagerParams.x =screenHeight*xFloat/100; 
			windowManagerParams.y =screenWidth*yFloat/100; 
			windowManager.addView(this, windowManagerParams);
			isShow = true;
		}
	}

	public void setImages(Bitmap bitMap) {
		imgView.setImageBitmap(bitMap);
	}

	public void setProgress(String progress) {
		textView.setText(progress);
	}
}