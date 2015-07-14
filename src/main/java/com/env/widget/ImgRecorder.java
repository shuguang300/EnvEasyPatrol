package com.env.widget;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import com.env.utils.SystemMethodUtil;
import com.env.utils.ViewUtil;
import com.env.easypatrol.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView.ScaleType;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


@SuppressWarnings("deprecation")
public class ImgRecorder extends LinearLayout{
	
	private TextView addImg;
	private Resource resourceView;
	private Delete deleteView;
	private ImgShow imgShowView;
	private Context context;
	private GridView imgShow;
	private String filePathFolder;
	private ArrayList<File> imgList;
	private ImgShowAdapter imgAdapter;
	private GalleryAdapter galleryAdapter;
	private File temFile;
	private boolean IsImgShow;
	private View tempView;
	private int pos=0,taskMode;

	public ImgRecorder(Context context,int mode){
		super(context);
		this.context = context;
		IsImgShow = false;
		taskMode = mode;
		LayoutInflater.from(context).inflate(R.layout.img_record, this); 
		ini();
	}
	
	public ImgRecorder(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		LayoutInflater.from(context).inflate(R.layout.img_record, this);
		ini();
	}
	
	public void ini(){
		imgList = new ArrayList<File>();
		filePathFolder = Environment.getExternalStorageDirectory().getAbsolutePath();
		tempView = LayoutInflater.from(context).inflate(R.layout.datainput_popup, null);
		addImg = (TextView)findViewById(R.id.img_record_controll);				
		imgShow = (GridView)findViewById(R.id.img_record_grid);
		imgAdapter = new ImgShowAdapter(context);
		imgShow.setAdapter(imgAdapter);
		imgShow.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				pos = position;
				imgShowView = new ImgShow(context);
				imgShowView.showAtLocation(tempView, Gravity.NO_GRAVITY, 0, 0);
				IsImgShow = true;
			}
		});
		switch (taskMode) {
		case ViewUtil.VIEW_DOING_TASK:
			addImg.setOnClickListener(new OnClickListener() {			
				@Override
				public void onClick(View v) {
					resourceView = new Resource(context);
					resourceView.showAtLocation(tempView, Gravity.NO_GRAVITY, 0, 0);
					IsImgShow = true;
				}
			});
			imgShow.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view,int position, long id) {
					deleteView = new Delete(context);
					deleteView.setPosition(position);
					deleteView.showAtLocation(tempView, Gravity.NO_GRAVITY, 0, 0);
					IsImgShow = true;
					return true;
				}
			});
			break;
		case ViewUtil.VIEW_HISTORY_TASK:
			addImg.setOnClickListener(null);
			imgShow.setOnItemLongClickListener(null);
			break;
		case ViewUtil.VIEW_NFC_TASK:
			addImg.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					Toast.makeText(context, getResources().getString(R.string.toast_must), Toast.LENGTH_SHORT).show();					
				}
			});
			imgShow.setOnItemLongClickListener(null);
			break;
		}
		
	}
	
	public boolean IsImgShow(){
		return IsImgShow;
	}
	
	public void HideImaShow(){
		if(imgShowView!=null){
			imgShowView.dismiss();
		}
		if(deleteView!=null){
			deleteView.dismiss();
		}
		if(resourceView!=null){
			resourceView.dismiss();
		}
		IsImgShow = false;
	}
	
	public void setPath(String pathFolder){
		File folder = new File(filePathFolder+"/"+pathFolder);
		if(!folder.exists()){
			folder.mkdirs();
		}
		filePathFolder = folder.getAbsolutePath();
	}
	public void notifyAdapter(){
		imgAdapter.notifyDataSetChanged();
	}
	public ArrayList<File> getImgList(){
		return imgList;
	}
	public void setImgList(ArrayList<File> list){
		imgList = list;
		imgAdapter.notifyDataSetChanged();
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		HideImaShow();
		File file = null;
		if(resultCode==Activity.RESULT_OK){
			if(requestCode==R.id.resource_take){
				file = temFile;		
				imgList.add(file);				
		 	}else if(requestCode==R.id.resource_pick) {
		 		Uri uri = data.getData();
		 		if(uri.getScheme().equals("content")){
		 			file = new File(SystemMethodUtil.changeURItoPath(uri, context));
		 			if(file.getAbsolutePath().toLowerCase().endsWith(".jpg")){
		 				imgList.add(file);
		 			}else {
		 				Toast.makeText(context, "不支持的图片格式", Toast.LENGTH_SHORT).show();
					}			 		
		 		}else if(uri.getScheme().equals("file")){
		 			file = new File(uri.getEncodedPath());
		 			if(file.getAbsolutePath().toLowerCase().endsWith(".jpg")){
		 				imgList.add(file);
		 			}else {
		 				Toast.makeText(context, "不支持的图片格式", Toast.LENGTH_SHORT).show();
					}	
				}
			}
		}		
		notifyAdapter();
	}
		
	private class Resource extends PopupWindow implements OnClickListener{
		private Button take,pick,cancel;
		private Context context;
		private View resource;
		public Resource(Context context){	
			super(context);
			this.context = context;
		//	LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			resource = LayoutInflater.from(context).inflate(R.layout.img_recorder_resource, null);
			setContentView(resource);
			ini();						
		}
		
		private void ini(){
			setWidth(LayoutParams.MATCH_PARENT);
			setHeight(LayoutParams.MATCH_PARENT);
			setAnimationStyle(R.style.popupwindow_anim);
			take = (Button)resource.findViewById(R.id.resource_take);
			pick = (Button)resource.findViewById(R.id.resource_pick);
			cancel = (Button)resource.findViewById(R.id.resource_cancel);
			take.setOnClickListener(this);
			pick.setOnClickListener(this);
			cancel.setOnClickListener(this);
			resource.setOnTouchListener(new OnTouchListener() {				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					int top = resource.findViewById(R.id.resource_layout).getTop();
					int Y = (int)event.getY();
					if(event.getAction()==MotionEvent.ACTION_UP&&Y<top){
						dismiss();
						IsImgShow=false;
					}
					return true;
				}
			});
		}

		@Override
		public void onClick(View v) {
			Intent intent = null;
			int id = v.getId();
			if (id == R.id.resource_take) {
				intent = new Intent();
				SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
				temFile = new File(filePathFolder+"/"+sdf.format(Calendar.getInstance(Locale.CHINA).getTime())+ ".jpg");
				intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(temFile));
				((Activity)context).startActivityForResult(intent, R.id.resource_take);
			} else if (id == R.id.resource_pick) {
				intent = new Intent();
				intent.setAction(Intent.ACTION_GET_CONTENT);
				intent.addCategory(Intent.CATEGORY_OPENABLE);
				intent.setType("image/");
				((Activity)context).startActivityForResult(intent, R.id.resource_pick);
			} else if (id == R.id.resource_cancel) {
				IsImgShow=false;
			}
			dismiss();
			
		}
	}
	private class Delete extends PopupWindow implements OnClickListener{

		private Button delete,cancel;
		private View resource;
		private int position;
		public Delete(Context context){	
			super(context);
			resource = LayoutInflater.from(context).inflate(R.layout.img_recorder_delete, null);
			setContentView(resource);
			ini();						
		}
		
		private void ini(){
			setWidth(LayoutParams.MATCH_PARENT);
			setHeight(LayoutParams.MATCH_PARENT);
			setAnimationStyle(R.style.popupwindow_anim);
			delete = (Button)resource.findViewById(R.id.delete_photo);
			cancel = (Button)resource.findViewById(R.id.delete_cancel);
			delete.setOnClickListener(this);
			cancel.setOnClickListener(this);
			resource.setOnTouchListener(new OnTouchListener() {				
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					int top = resource.findViewById(R.id.delete_layout).getTop();
					int Y = (int)event.getY();
					if(event.getAction()==MotionEvent.ACTION_UP&&Y<top){
						dismiss();
						IsImgShow=false;
					}
					return true;
				}
			});
		}

		public void setPosition(int arg0){
			position = arg0;
		}
		@Override
		public void onClick(View v) {
			int id = v.getId();
			if (id == R.id.delete_photo) {
				dismiss();
				imgList.remove(position);
				imgAdapter.notifyDataSetChanged();
				IsImgShow=false;
			} else if (id == R.id.delete_cancel) {
				dismiss();
				IsImgShow=false;
			}
			
		}
	
	}
	private class ImgShow extends PopupWindow{
		private View resource;
		private Gallery gallery;
		private TextView postionTV;
		public ImgShow (Context context){
			resource = LayoutInflater.from(context).inflate(R.layout.img_show, null);
			setContentView(resource);
			ini();
		}
		private void ini(){
			setWidth(LayoutParams.MATCH_PARENT);
			setHeight(LayoutParams.MATCH_PARENT);
			setAnimationStyle(R.style.popupwindow_anim);
			gallery = (Gallery)resource.findViewById(R.id.img_show_gallery);
			postionTV = (TextView)resource.findViewById(R.id.img_show_pos);
			galleryAdapter = new GalleryAdapter();
			gallery.setAdapter(galleryAdapter);
			gallery.setSelection(pos,true);	
			postionTV.setText(pos+1+"/"+imgList.size());
			gallery.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
					ImgShow.this.dismiss();	
					IsImgShow = false;
				}
			});
			gallery.setOnItemSelectedListener(new OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view,int position, long id) {	
					postionTV.setText(position+1+"/"+imgList.size());
				}

				@Override
				public void onNothingSelected(AdapterView<?> parent) {										
				}				
			});
			
		}
	}
	
	private class GalleryAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return imgList.size();
		}

		@Override
		public Object getItem(int position) {
			return imgList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = new ImageView(context);
			imageView.setImageBitmap(SystemMethodUtil.compressBitmap(imgList.get(position).getAbsolutePath()));
			imageView.setScaleType(ScaleType.FIT_XY);	
			imageView.setLayoutParams(new android.widget.Gallery.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));					
			return imageView;
		}
	}
	
	private class ImgShowAdapter extends BaseAdapter{

		private Context context;
		
		public ImgShowAdapter(Context context){
			this.context = context;
		}
		
		@Override
		public int getCount() {
			return imgList.size();
		}

		@Override
		public Object getItem(int position) {
			return imgList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = LayoutInflater.from(context).inflate(R.layout.media_list_item, null);
			ImageView imageView = (ImageView)view.findViewById(R.id.media_list_item_img);
			imageView.setImageBitmap(SystemMethodUtil.compressBitmap(imgList.get(position).getAbsolutePath()));
			return view;
		}
		
	}

}
