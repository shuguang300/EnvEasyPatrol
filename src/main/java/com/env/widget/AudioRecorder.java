package com.env.widget;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.env.utils.SystemMethodUtil;
import com.env.utils.ViewUtil;
import com.env.easypatrol.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class AudioRecorder extends LinearLayout{
	
	private Button controller;
	private ImageView voice;
	private ProgressBar time;
	private MediaRecorder mRecorder;
	private MediaPlayer mPlayer;	
	private TextView stTime,endTime,recordTime;
	private File recordFile;
	private String filePathFolder;
	private SimpleDateFormat sdf1 = new SimpleDateFormat("mm:ss");
	private TimeThread timeThread;
	private VoiceThread voiceThread;
	private Context context;
	private GridView recordList;
	private ArrayList<File> fileList;
	private ArrayList<String> fileDuration;
	private RecordListAdapter adapter;
	private final int record = 0;
	private final int play = 1;
	private final int voiceLevel = 2;
	private int [] voiceStatu = {R.drawable.mic_2,R.drawable.mic_3,R.drawable.mic_4,R.drawable.mic_5};
	private boolean running = false,playing = false;
	private int lastPlaying = -1,taskMode;
	private Handler timeHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case record:
				recordTime.setText("录音中:"+sdf1.format(new Date(msg.arg1*1000)));
				break;
			case play:
				time.setMax(msg.arg1);
				time.setProgress(msg.arg2);
				endTime.setText(sdf1.format(new Date(msg.arg1*1000)));
				stTime.setText(sdf1.format(new Date(msg.arg2*1000)));
				recordTime.setText("播放中:");
				break;
			}
		};
	};
	private Handler voiceLevelHandler = new Handler(){
		public void handleMessage(Message msg) {
			voice.setBackgroundResource(voiceStatu[msg.arg1]);	
		};
	};

	public AudioRecorder(Context context,int mode) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.audio_record, this);
		this.context = context;
		taskMode = mode;
		initialize();
	}
	
	public AudioRecorder(Context context,AttributeSet attrs) {
		super(context,attrs);
		LayoutInflater.from(context).inflate(R.layout.audio_record, this);
		this.context =  context;
		initialize();
	}
	
	private void initialize(){
		controller = (Button)findViewById(R.id.audio_record_controller);
		voice = (ImageView)findViewById(R.id.audio_record_voice);
		time = (ProgressBar)findViewById(R.id.audio_record_time);
		stTime = (TextView)findViewById(R.id.audio_record_sttime);
		endTime = (TextView)findViewById(R.id.audio_record_endtime);
		recordTime = (TextView)findViewById(R.id.audio_record_recordtime);
		recordList = (GridView)findViewById(R.id.audio_record_list);	
		iniView();
		filePathFolder = Environment.getExternalStorageDirectory().getAbsolutePath();
		fileList = new ArrayList<File>();
		fileDuration = new ArrayList<String>();
		adapter = new RecordListAdapter(context);
		recordList.setAdapter(adapter);	
		switch (taskMode) {
		case ViewUtil.VIEW_DOING_TASK:
			controller.setOnClickListener(new OnClickListener() {			
				@Override
				public void onClick(View v) {
					if(running){
						stopRecord();
					}else {
						if(playing){
							Toast.makeText(context, "正在播放录音，无法录音", Toast.LENGTH_SHORT).show();
						}else {
							startRecord();
						}						
					}			
				}
			});
			recordList.setOnItemLongClickListener(new OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view,
						final int position, long id) {
					new AlertDialog.Builder(context).setTitle("删除录音").setMessage("确定删除录音吗？").setPositiveButton("确定", new DialogInterface.OnClickListener() {						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							fileList.get(position).delete();
							fileList.remove(position);
							fileDuration.remove(position);
							adapter.notifyDataSetChanged();
							dialog.cancel();
							
						}
					}).setNegativeButton("取消", null).create().show();
					return false;
				}
			});
			break;

		case ViewUtil.VIEW_HISTORY_TASK:
			recordList.setOnItemLongClickListener(null);
			controller.setOnClickListener(null);
			break;
		case ViewUtil.VIEW_NFC_TASK:
			recordList.setOnItemLongClickListener(null);
			controller.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					Toast.makeText(context, getResources().getString(R.string.toast_must), Toast.LENGTH_SHORT).show();						
				}
			});
			break;
		}		
		recordList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				playRecord(position,lastPlaying);
				lastPlaying = position;
			}
		});				
		
		
		
	}
	
	public void setPath(String pathFolder){
		File folder = new File(filePathFolder+"/"+pathFolder);
		if(!folder.exists()){
			folder.mkdirs();
		}
		filePathFolder = folder.getAbsolutePath();
	}
	
	public String getFilePath(){
		if(recordFile!=null){
			return recordFile.getAbsolutePath();
		}
		return "";
	}
	
	public ArrayList<File> getFileList(){
		return fileList;
	}
	
	public void OnDestroy(int btnID){
		if(playing){
			StopPlay();
		}
		if (btnID == R.id.bt_cancel) {
			controller.setBackgroundResource(R.drawable.record_start);
			voice.setBackgroundResource(R.drawable.mic_2);
			if(mRecorder!=null){
				mRecorder.stop();
				mRecorder.release();
				mRecorder = null;			
			}
		} else if (btnID == R.id.bt_ok) {
			if(running){
				stopRecord();
			}
		}	
		
		
	}
	
	public void setFileList(ArrayList<File> list){
		fileList = list;
		fileDuration = new ArrayList<String>();
		for(int i=0;i<list.size();i++){
			fileDuration.add(getFileDuration(list.get(i)));			
		}
		adapter.notifyDataSetChanged();
	}
	
	public String getFileDuration(File file){
		mPlayer = new MediaPlayer();
		mPlayer.setLooping(false);
		String result ="";
		try {
			mPlayer.setDataSource(file.getAbsolutePath());
			mPlayer.prepare();
			result = sdf1.format(new Date(mPlayer.getDuration()));
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mPlayer.release();
		return result;
	}
	
	private void iniView(){
		stTime.setText("00:00");
		endTime.setText("00:00");
		recordTime.setText("00:00");
		time.setProgress(0);
	}
	
	
	@SuppressWarnings("deprecation")
	private void startRecord(){
		running = true;
		controller.setBackgroundResource(R.drawable.record_stop);
		iniView();		
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
		mRecorder.setOutputFile(filePathFolder+"/"+sdf.format(new Date())+".amr");
		recordFile = new File(filePathFolder+"/"+sdf.format(new Date())+".amr");
		try {
			mRecorder.prepare();
		} catch (Exception e) {
		}
		if(voiceThread==null){
			
		}
		if(timeThread == null){
			
		}
		voiceThread = new VoiceThread();
		voiceThread.start();
		timeThread = new TimeThread(record);
		timeThread.start();
		
		mRecorder.start();
		
	}
	
	private void stopRecord(){
		running = false;
		controller.setBackgroundResource(R.drawable.record_start);
		voice.setBackgroundResource(R.drawable.mic_2);
		if(voiceThread!=null){			
			voiceThread.interrupt();
			voiceThread = null;			
		}
		if(timeThread!=null){
			timeThread.interrupt();
			timeThread = null;
		}
		if(mRecorder!=null){
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;			
		}
		fileList.add(recordFile);
		fileDuration.add(getFileDuration(recordFile));
		adapter.notifyDataSetChanged();
		
		
	}
	
	private void playRecord(int nowPlaying,int lastPlaying){
		if(running){
			Toast.makeText(context,"正在录音，无法为您播放录音",Toast.LENGTH_SHORT).show();
		}else {
			if(playing){
				if(nowPlaying==lastPlaying){
					PausePlay();
				}else {
					StopPlay();
					StartPlay(nowPlaying);
				}
			}else {
				if(nowPlaying==lastPlaying){
					ReStart();
				}
				if(nowPlaying!=lastPlaying){
					StartPlay(nowPlaying);
				}
			}
		}
		
	
	}
	private void StartPlay(int position){
		playing = true;
		mPlayer = new MediaPlayer();
		mPlayer.setLooping(false);
		try {
			mPlayer.setDataSource(fileList.get(position).getAbsolutePath());
			mPlayer.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		mPlayer.start();							
		timeThread = new TimeThread(play);
		timeThread.start();
		mPlayer.setOnCompletionListener(new OnCompletionListener() {									
			@Override
			public void onCompletion(MediaPlayer mp) {	
				StopPlay();
			}
		});
	}
	
	private void ReStart(){
		mPlayer.start();
		playing = true;
		timeThread = new TimeThread(play);
		timeThread.start();
	}
	private void PausePlay(){
		mPlayer.pause();
		playing = false;
	//	timeThread.interrupt();
	//	timeThread = null;
	}
	private void StopPlay(){		
		playing = false;
		lastPlaying = -1;
		if(mPlayer!=null){
			mPlayer.stop();	
			mPlayer.release();
		}
	//	mPlayer = null;		
	}
	
	
	
	private class VoiceThread extends Thread{		
		@Override
		public void run() {
			super.run();		
			while (running) {
				if(mRecorder==null || !running){
					break;
				}				
				int x = mRecorder.getMaxAmplitude();
				Message msg = voiceLevelHandler.obtainMessage();
				msg.what = voiceLevel;
				if (x != 0) {
					int f = (int) (10 * Math.log(x) / Math.log(10));
					if (f < 26)
						msg.arg1=0;
					else if (f < 32)
						msg.arg1=1;
					else if (f < 38)
						msg.arg1=2;
					else
						msg.arg1=3;
				}else {
					msg.arg1 = 0;
				}
				
				msg.sendToTarget();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					e.printStackTrace();
				}
			}
		}
	}
	
	private class TimeThread extends Thread{
		private int timeThreadKind;
		public TimeThread (int kind){
			timeThreadKind = kind;
		}
		@Override
		public void run() {			
			super.run();
			switch (timeThreadKind) {
			case record:
				int timeCount = 0;			
				while (running) {	
					Message msg = timeHandler.obtainMessage();
					msg.what = record;
					msg.arg1 = timeCount;
					msg.sendToTarget();
					timeCount++;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						running = false;
						Thread.currentThread().interrupt();
						e.printStackTrace();
					}
				}
				break;
			case play:
				while (playing) {				
					Message msg = timeHandler.obtainMessage();
					msg.what = play;
					msg.arg1 = mPlayer.getDuration()/1000;
					msg.arg2 = mPlayer.getCurrentPosition()/1000;
					msg.sendToTarget();
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						playing = false;
						Thread.currentThread().interrupt();						
						e.printStackTrace();
					}
				}
				break;				
			}
			
		}
	}

	private class RecordListAdapter extends BaseAdapter{
		private Context context;
		
		public RecordListAdapter(Context context){
			this.context = context;
		}
				
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = LayoutInflater.from(context).inflate(R.layout.media_list_item, null);
			ImageView imageView = (ImageView)view.findViewById(R.id.media_list_item_img);
			TextView title = (TextView)view.findViewById(R.id.media_list_item_title);
			imageView.setBackgroundResource(R.drawable.record_play);
			title.setText(fileDuration.get(position));
			return view;
		}


		@Override
		public int getCount() {
			return fileList.size();
		}


		@Override
		public Object getItem(int position) {
			return fileList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}		
	} 
	
}
