package com.env.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import com.env.easypatrol.R;
import java.io.File;

/**
 * Created by sk on 7/3/15.
 */
public class AddPicConfirmWindow extends PopupWindow implements View.OnClickListener{

    private Context context;
    private Button take,pick,cancel;
    private View rootView;
    private String tmpFile;
    public AddPicConfirmWindow(Context context,String filePath){
        this.context = context;
        this.tmpFile = filePath;
        rootView = LayoutInflater.from(context).inflate(R.layout.img_recorder_resource, null);
        setContentView(rootView);
        initialView();
    }
    private void initialView(){
        setWidth(LinearLayout.LayoutParams.MATCH_PARENT);
        setHeight(LinearLayout.LayoutParams.MATCH_PARENT);
        setAnimationStyle(R.style.popupwindow_anim);
        take = (Button)rootView.findViewById(R.id.resource_take);
        pick = (Button)rootView.findViewById(R.id.resource_pick);
        cancel = (Button)rootView.findViewById(R.id.resource_cancel);
        take.setOnClickListener(this);
        pick.setOnClickListener(this);
        cancel.setOnClickListener(this);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int top = rootView.findViewById(R.id.resource_layout).getTop();
                int Y = (int)event.getY();
                if(event.getAction()==MotionEvent.ACTION_UP&&Y<top){
                    dismiss();
                }
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()){
            case R.id.resource_take:
//                SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
                //filePathFolder+sdf.format(Calendar.getInstance(Locale.CHINA).getTime())+ ".jpg"
//                String filePathFolder = Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(tmpFile)));
                ((Activity)context).startActivityForResult(intent, R.id.resource_take);
                break;
            case R.id.resource_pick:
                intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/");
                ((Activity)context).startActivityForResult(intent, R.id.resource_pick);
                break;
            case R.id.resource_cancel:
                break;
        }
        dismiss();
    }
}
