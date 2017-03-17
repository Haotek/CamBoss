package com.tutk.P2PCam264.DELUX;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.tutk.Kalay.general.R;

/**
 * Created by James Huang on 2015/4/3.
 */
public class Custom_Ok_img_Dialog extends AlertDialog {

    @SuppressWarnings("deprecation")
    public Custom_Ok_img_Dialog(Context context, String contentText) {
        super(context);

        LayoutInflater mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = mInflater.inflate(R.layout.ok_img_dialog, null);

        setView(view);

        TextView tvText = (TextView) view.findViewById(R.id.tvText);
        Button btnSingle = (Button) view.findViewById(R.id.btnSingle);

        tvText.setText(contentText);
        btnSingle.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
