package com.tutk.P2PCam264.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class myGridView extends GridView {
	public myGridView(Context context) {
		super(context);

	}

	public myGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, expandSpec);
	}
}
