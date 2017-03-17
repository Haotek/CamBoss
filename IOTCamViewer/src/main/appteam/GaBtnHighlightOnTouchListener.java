package appteam;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;

public class GaBtnHighlightOnTouchListener implements OnTouchListener {

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		
		if( event.getAction() == MotionEvent.ACTION_DOWN ) {
		      //grey color filter, you can change the color as you like
			Drawable bg = v.getBackground();
			bg.setAlpha( (int)(0.8 * 255) );
			
		} else if( event.getAction() == MotionEvent.ACTION_UP ) {
			Drawable bg = v.getBackground();
			bg.setAlpha( (int)(1.0 * 255) ); 
		}
		    
		return false;
	}

}
