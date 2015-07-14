package com.env.adapter;

import android.app.ActionBar;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsoluteLayout;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;

import com.env.easypatrol.R;
import com.env.utils.SystemMethodUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by sk on 7/3/15.
 */
public class TaskPicAdapter extends BaseAdapter{

    public final static int PICTURE_COUNT = 5;
    private List<HashMap<String,String>> imgs = new ArrayList<HashMap<String, String>>();
    private Context context;
    public List<HashMap<String, String>> getImgs() {
        return imgs;
    }
    public void setImgs(List<HashMap<String, String>> imgs) {
        this.imgs = imgs;
    }

    public TaskPicAdapter(Context context,List<HashMap<String,String>> imgs){
        this.imgs = imgs;
        this.context = context;
    }

    public TaskPicAdapter(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return imgs.size();
    }

    @Override
    public HashMap<String,String> getItem(int position) {
        return imgs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HashMap<String,String> map = imgs.get(position);
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new AbsListView.LayoutParams(160,160));
        imageView.setTag(map);
        if(map.get("TaskID")==null || map.get("TaskID").length()<1){
            imageView.setImageResource(android.R.drawable.ic_input_add);
        }else{
            imageView.setImageBitmap(SystemMethodUtil.compressBitmap(imgs.get(position).get("FilePath"), 320, 320));
        }
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        return imageView;
    }
}
