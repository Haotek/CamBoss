package com.tutk.P2PCam264;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.tutk.Kalay.general.R;

public class ModifySecurityCodeDialog extends AlertDialog  {

	protected ModifySecurityCodeDialog(Context context, String title) {
		super(context);		
		
		LayoutInflater mInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);   
		View view = mInflater.inflate(R.layout.modify_security_code, null);
		setView(view);
        setTitle(title);
	}	
}
