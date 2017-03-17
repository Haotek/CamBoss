package com.tutk.P2PCam264;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.MotionEvent;


public class IOTCProgressDialog extends ProgressDialog
{

	private static IOTCProgressDialog instance;
	
	public IOTCProgressDialog(Context context) 
	{
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public boolean onTouchEvent (MotionEvent event) 
	{
		return true;
	}
	
	public static IOTCProgressDialog show (Context context, CharSequence title, CharSequence message, boolean indeterminate,boolean cancelable)
	{
		if(instance!=null)
			instance.dismiss();
		instance=new IOTCProgressDialog(context);
		if(message!=null)
			instance.setMessage(message);
		if(title!=null)
			instance.setTitle(title);
		instance.setCancelable(cancelable);
		instance.setIndeterminate(indeterminate);
		instance.show();
		
		return instance;
	}
}
