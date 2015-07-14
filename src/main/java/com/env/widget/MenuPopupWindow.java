package com.env.widget;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.env.easypatrol.R;

/**
 * Created by SK on 2015/7/7.
 */
public class MenuPopupWindow extends PopupWindow {
    private Context context;
    private View.OnClickListener listener;
    private View view;
    private TextView dowunLoad,upload,logout,settings;

    public MenuPopupWindow(Context context,View.OnClickListener listener){
        this.context = context;
        this.listener = listener;
        view = LayoutInflater.from(context).inflate(R.layout.menu_popupwind, null);
        ini();

    }

    private void ini(){
        setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
        setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
        setAnimationStyle(R.style.popupwindow_anim);
        dowunLoad = (TextView)view.findViewById(R.id.menu_popwind_download);
        upload = (TextView)view.findViewById(R.id.menu_popwind_upload);
        settings = (TextView)view.findViewById(R.id.menu_popwind_setting);
        logout = (TextView)view.findViewById(R.id.menu_popwind_logout);
        dowunLoad.setOnClickListener(listener);
        upload.setOnClickListener(listener);
        settings.setOnClickListener(listener);
        logout.setOnClickListener(listener);
        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.menu_popupwind_bg));
        setOutsideTouchable(true);
        setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((keyCode == KeyEvent.KEYCODE_MENU)&&(isShowing())) {
                    dismiss();// 这里写明模拟menu的PopupWindow退出就行
                    return true;
                }
                return false;
            }
        });
        setContentView(view);
    }
}
