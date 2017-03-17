package com.tutk.P2PCam264.DELUX;




import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;

import com.tutk.Kalay.general.R;


public class Multi_Setting_custom_Dialog extends AlertDialog implements DialogInterface.OnClickListener {
	Context mContext;
	String title_text="";
	String left_text="";
	String right_text="";
	int mChannelindex=0;
	boolean mDevice_online=true;
	boolean click_dismiss=true;
	On_Dialog_button_click_Listener  On_button_click_Listener;
	
	public Multi_Setting_custom_Dialog(Context context,int ChannelIndex,boolean device_online) {
		super(context,R.style.ThemeDialogCustom);
		mContext = context;
		mChannelindex = ChannelIndex;
		mDevice_online = device_online;
		LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = mInflater.inflate(R.layout.multi_setting_custom_dialog, null);

		setView(view);

	}
	@Override
	protected void onCreate( Bundle savedInstanceState ) {

	    super.onCreate(savedInstanceState );
	    setContentView(R.layout.multi_setting_custom_dialog);
    	ImageButton cancel_button  = (ImageButton)findViewById(R.id.cancel_button);
    	cancel_button.setOnClickListener(button_click);
    	ImageButton btn_more_event  = (ImageButton)findViewById(R.id.btn_more_event);
    	ImageButton btn_more_photo  = (ImageButton)findViewById(R.id.btn_more_photo);
    	btn_more_photo.setOnClickListener(button_click);
    	ImageButton btn_more_set  = (ImageButton)findViewById(R.id.btn_more_set);
    	btn_more_set.setOnClickListener(button_click);
        ImageButton btn_more_remove  = (ImageButton)findViewById(R.id.btn_more_remove);
        btn_more_remove.setOnClickListener(button_click);
	    if(mDevice_online)
	    {
	    	btn_more_event.setOnClickListener(button_click);
	    }
	    else
	    {
	    	btn_more_event.setBackgroundResource(R.drawable.btn_sdcard_h);
	    }
	}

	@Override
	public void onClick(DialogInterface arg0, int arg1) {
		// TODO Auto-generated method stub
	}
	
	private View.OnClickListener button_click = new View.OnClickListener() {
		public void onClick(View v) {

			if(On_button_click_Listener==null)
			{
				dismiss();
				return;
			}
			switch(v.getId()){
			
				case R.id.cancel_button:
					cancel_button_click(Multi_Setting_custom_Dialog.this);
					break;
				case R.id.btn_more_event:
					event_button_click(Multi_Setting_custom_Dialog.this,mChannelindex);
					break;
				case R.id.btn_more_photo:
					photo_button_click(Multi_Setting_custom_Dialog.this,mChannelindex);
					break;
				case R.id.btn_more_set:
					set_button_click(Multi_Setting_custom_Dialog.this,mChannelindex);
					break;
                case R.id.btn_more_remove:
                    delete_button_click(Multi_Setting_custom_Dialog.this,mChannelindex);
                    break;
				default:
                    break;
			}
			if(click_dismiss)
				dismiss();
			
		}
		
	};  
	
    public interface On_Dialog_button_click_Listener
    {
    	public void   cancel_click (final DialogInterface Dialog);
        public void   btn_event_click(final DialogInterface Dialog,int index);
        public void   btn_photo_click(final DialogInterface Dialog,int index);
        public void   btn_set_click(final DialogInterface Dialog,int index);
        public void   btn_delete_click(final DialogInterface Dialog,int index);
        
    }
    public void cancel_button_click(DialogInterface Dialog)
    {
    	On_button_click_Listener.cancel_click(Dialog);
    }
    public void event_button_click(DialogInterface Dialog,int index)
    {
    	On_button_click_Listener.btn_event_click(Dialog,index);
    }   
    public void photo_button_click(DialogInterface Dialog,int index)
    {
    	On_button_click_Listener.btn_photo_click(Dialog,index);
    }   
    public void set_button_click(DialogInterface Dialog,int index)
    {
    	On_button_click_Listener.btn_set_click(Dialog,index);
    }  
    public void delete_button_click(DialogInterface Dialog,int index)
    {
    	On_button_click_Listener.btn_delete_click(Dialog,index);
    } 
    public void  set_button_click_Listener( On_Dialog_button_click_Listener _On_button_click_Listener)
    {
    	On_button_click_Listener = _On_button_click_Listener ;
    }
    

}