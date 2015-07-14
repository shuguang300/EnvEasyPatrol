package com.env.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.env.easypatrol.R;

/**
 * Created by sk on 7/3/15.
 */
public class DeleteConfirmWindow extends PopupWindow implements View.OnClickListener{
    private Context context;
    private Button delete,cancel;
    private View rootView;
    private View.OnClickListener listener;

    public DeleteConfirmWindow (Context context ,View.OnClickListener listener ){
        this.context = context;
        this.listener = listener;
        rootView = LayoutInflater.from(context).inflate(R.layout.img_recorder_delete, null);
        setContentView(rootView);
        initialView();
    }

    private void initialView(){
        setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        setAnimationStyle(R.style.popupwindow_anim);
        delete = (Button)rootView.findViewById(R.id.delete_photo);
        cancel = (Button)rootView.findViewById(R.id.delete_cancel);
        delete.setOnClickListener(listener);
        cancel.setOnClickListener(this);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int top = rootView.findViewById(R.id.delete_layout).getTop();
                int Y = (int)event.getY();
                if(event.getAction()==MotionEvent.ACTION_UP&&Y<top){
                    dismiss();
                }
                return false;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.delete_cancel:
                dismiss();
                break;
        }
    }
}
