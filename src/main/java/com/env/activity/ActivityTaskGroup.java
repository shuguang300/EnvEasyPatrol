package com.env.activity;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import com.env.bean.EP_Device;
import com.env.component.DataService;
import com.env.component.PatrolApplication;
import com.env.easypatrol.R;
import com.env.nfc.NfcActivity;
import com.env.utils.DataBaseUtil;
import com.env.utils.DialogUtil;
import com.env.utils.LocalDataHelper;
import com.env.utils.SystemMethodUtil;
import com.env.utils.SystemParamsUtil;
import com.env.utils.ViewUtil;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by SK on 2015/9/9.
 */
public class ActivityTaskGroup extends NfcActivity {
    private int VIEW_HISTORY_TASK = 1;
    private int days,  mode;
    private boolean  mustUseCard,showTaskTime = false;
    private long oneDayMiles = 86400000;
    private String [] LONG_CLICK_DIALOG_ITEM = {"查看本地数据","查看远程数据"};
    private Intent getIntent, service;
    private String toastInfo;
    private HashMap<String, String> card;
    private HashMap<Integer,ArrayList<HashMap<String, String>>> tasks;
    private List<EP_Device> devices ;
    private TaskGroupAdapter adapter;
    private TaskNFCCardReceiver taskNFCCardReceiver;
    private SQLiteDatabase db;
    private Timer timer = null, fastUpload = null;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Message msg;
    private ExpandableListView groupListView;
    private DataService.DataServiceBinder binder;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (DataService.DataServiceBinder) service;
        }
    };
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0:
                    adapter.notifyDataSetChanged();
                    break;
                case 1:
                    switch (msg.arg1) {
                        case DataService.Success:
                            update();
                            break;
                        case DataService.Error:
                            Toast.makeText(getApplicationContext(), "上传失败", Toast.LENGTH_SHORT).show();
                            break;
                    }
                    if (fastUpload != null) {
                        fastUpload.cancel();
                        fastUpload = null;
                    }
                    break;
                case DataService.WifiDisabled:
                    Toast.makeText(ActivityTaskGroup.this, "网络不可用", Toast.LENGTH_SHORT).show();
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taskgroup);
        initial();
    }

    private void initial(){
        mBindService();
        iniData();
        iniView();
    }

    @Override
    public void iniData() {
        db = DataBaseUtil.getInstance(ActivityTaskGroup.this).getReadableDatabase();
        getIntent = getIntent();
        sp = getSharedPreferences(PatrolApplication.PREFS_NAME, MODE_PRIVATE);
        editor = sp.edit();
        card = (HashMap<String, String>) getIntent.getSerializableExtra("Child");
        mode = getIntent.getExtras().getInt("Mode");
        toastInfo = getResources().getString(R.string.toast_must);

        setData();

    }

    @Override
    public void iniView() {
        groupListView = (ExpandableListView)findViewById(R.id.grouplist);

        adapter = new TaskGroupAdapter();



        initialActionBar();

        groupListView.setAdapter(adapter);

        for(int i =0;i<adapter.getGroupCount();i++){
            groupListView.expandGroup(i);
        }

        groupListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                HashMap<String, String> map = adapter.getChild(groupPosition, childPosition);
                boolean IsMustUseNFCCard = map.get("IsMustUseNFCCard").equals("1") ? true : false;
                int resultType = Integer.parseInt(map.get("ResultType"));
                boolean clickMode = canClick(IsMustUseNFCCard, mode);
                if (clickMode) {
                    Intent intent = new Intent();
                    intent.putExtra("task", map);
                    switch (resultType) {
                        case 0:
                            intent.setClass(ActivityTaskGroup.this, ActivityNumberTask.class);
                            break;
                        case 2:
                            intent.setClass(ActivityTaskGroup.this, ActivityMultiTask.class);
                            break;
                        case 3:
                            intent.setClass(ActivityTaskGroup.this, ActivitySingleTask.class);
                            break;
                    }
                    startActivityForResult(intent, 0);
                } else {
                    Toast.makeText(ActivityTaskGroup.this, toastInfo, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

        groupListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @SuppressWarnings("unchecked")
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (view.getTag() != null) {
                    HashMap<String, String> map = ((ViewHolder) view.getTag()).data;
                    final Intent intent = new Intent();
                    intent.putExtra("task", map);
                    AlertDialog.Builder longClickDialg = new AlertDialog.Builder(ActivityTaskGroup.this);
                    longClickDialg.setCancelable(true);
                    longClickDialg.setSingleChoiceItems(LONG_CLICK_DIALOG_ITEM, -1, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            switch (which) {
                                case 0:
                                    intent.setClass(ActivityTaskGroup.this, ActivityTaskEachTag.class);
                                    startActivityForResult(intent, VIEW_HISTORY_TASK);
                                    break;
                                case 1:
                                    intent.setClass(ActivityTaskGroup.this, ActivityEachTagWebView.class);
                                    startActivityForResult(intent, VIEW_HISTORY_TASK);
                                    break;
                            }
                        }
                    });
                    longClickDialg.create().show();
                }
                return true;
            }
        });



        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    calculateData();
                    Message msg = handler.obtainMessage();
                    msg.what = 0;
                    msg.sendToTarget();
                }
            }, 0, 5000);
        }

        taskNFCCardReceiver = new TaskNFCCardReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.env.view.PatrolTaskNFCCard.TaskNFCCardReceiver");
        registerReceiver(taskNFCCardReceiver, intentFilter);
    }

    private void initialActionBar(){
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(card.get("CardName"));
        actionBar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (SystemParamsUtil.getInstance().getIsLogin()) {
            if (binder == null) {
                mBindService();
            }
        } else {
            startActivity(new Intent(ActivityTaskGroup.this, ActivityLogin.class));
            finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(conn);
        unregisterReceiver(taskNFCCardReceiver);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.patroltasknfccard_popupmenu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.patroltasknfccard_menu_upload) {
            uploadData();
        } else if (itemId == R.id.patroltasknfccard_menu_showtag) {
            showTag();
        } else if (itemId == R.id.patroltasknfccard_menu_showtime) {
            if (showTaskTime) {
                showTaskTime = false;
                item.setTitle(getResources().getString(R.string.tasknfccard_menu_showtime));
            } else {
                showTaskTime = true;
                item.setTitle(getResources().getString(R.string.tasknfccard_menu_notime));
            }
        } else if (itemId == R.id.patroltasknfccard_menu_changeview){
            Intent intent = new Intent(ActivityTaskGroup.this, ActivityTaskList.class);
            intent.putExtra("Child", card);
            intent.putExtra("Mode", mode);
            startActivity(intent);
            finish();
        } else if(itemId ==android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            super.onActivityResult(requestCode, resultCode, data);
            update();
        }
    }

    class TaskNFCCardReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            update();
        }
    }

    private boolean canClick(boolean isMustUseNfc,int mode){
        boolean arg0;
        if(isMustUseNfc){
            if(mode==ActivityTaskConstruction.CARD_NFC){
                arg0 = true;
            }else {
                arg0 = false;
            }
        }else {
            arg0 = true;
        }
        return arg0;
    }


    private void update() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        setData();
        adapter.notifyDataSetChanged();
        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    calculateData();
                    Message msg = handler.obtainMessage();
                    msg.what = 0;
                    msg.sendToTarget();
                }
            }, 0, 5000);
        }

    }

    private void setData(){
        devices = LocalDataHelper.getDeviceByCardId(db, card.get("CardID"));
        if (devices == null || devices.size()==0){
            devices = new ArrayList<EP_Device>();
            tasks = new HashMap<Integer,ArrayList<HashMap<String, String>>>();
        }else{
            SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
            Calendar nowClendar = Calendar.getInstance();
            long todayInt = Long.valueOf(sdf.format(nowClendar.getTime()));
            String nowDTStr = sdf.format(nowClendar.getTime());
            String startDateStr = nowDTStr.substring(0, 8);
            nowClendar.add(Calendar.DAY_OF_YEAR, 1);
            String endDateStr = sdf.format(nowClendar.getTime()).substring(0, 8);
            tasks = LocalDataHelper.getNewestTasksByDeviceGroups(db, nowDTStr, startDateStr + "000000", endDateStr + "000000", devices);
            for (int i =0;i<devices.size();i++){
                ArrayList<HashMap<String,String>> tmpTasks = tasks.get(devices.get(i).getDeviceID());
                for(int j = 0;j<tmpTasks.size();j++){
                    HashMap<String,String> tmpTask = tmpTasks.get(j);
                    tmpTask.put("Value", "");
                    tmpTask.put("IsEnable", "1");
                    tmpTask.put("Visible", "1");
                    tmpTask.put("StatuCode", ViewUtil.VIEW_STATU_CODE_DOING + "");
                    if (tmpTask.get("IsDone").equals("1")) {
                        tmpTask.put("SampleTime",tmpTask.get("OPDateTime"));
                        tmpTask.put("IsEnable", "0");
                        String [] valueAndStep = LocalDataHelper.getTaskValuesAndSteps(db, tmpTask.get("ResultType") == "0" ? "EP_PatrolResult_Number" : "EP_PatrolResult_String", Integer.valueOf(tmpTask.get("TaskID")));
                        tmpTask.put("Value", valueAndStep[0]);
                        tmpTask.put("Step", valueAndStep[1]);
                    } else {
                        mustUseCard = tmpTask.get("IsMustUseNFCCard").equals("1") ? true : false;
                        if ((mode == ActivityTaskConstruction.CARD_NFC && mustUseCard) || !mustUseCard) {
                            if (tmpTask.get("DefaultDicIntValues") != null) {
                                tmpTask.put("Value", LocalDataHelper.getTaskDefaultValues(db, tmpTask.get("DefaultDicIntValues"), Integer.parseInt(tmpTask.get("DicID"))));
                                tmpTask.put("Step", "");
                                tmpTask.put("SampleTime", new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf).format(Calendar.getInstance().getTime()));
                                tmpTask.put("IsEnable", "0");
                            }
                        }
                    }
                    tmpTask.put("StatuCode", getStatuCode(todayInt, tmpTask) + "");
                }
            }
            calculateData();
        }
    }

    private void calculateData() {
        HashMap<String, String> task;
        SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
        String nowDateTimeStr = sdf.format(Calendar.getInstance().getTime());
        String result, pre;
        int statuCode;
        for (int i =0;i<devices.size();i++){
            ArrayList<HashMap<String,String>> tmpTasks = tasks.get(devices.get(i).getDeviceID());
            for(int j = 0;j<tmpTasks.size();j++){
                task = tmpTasks.get(j);
                statuCode = Integer.valueOf(task.get("StatuCode"));
                result = getTimeCount(nowDateTimeStr, task);
                pre = "";
                switch (statuCode) {
                    case ViewUtil.VIEW_STATU_CODE_UPLOADED:
                        pre = "已上传";
                        break;
                    case ViewUtil.VIEW_STATU_CODE_WAIT:
                        pre = "后开始";
                        if (result.equals("00时00分00")) {
                            task.put("StatuCode", ViewUtil.VIEW_STATU_CODE_DOING + "");
                            task.put("IsEnable", "1");
                        }
                        break;
                    case ViewUtil.VIEW_STATU_CODE_UNDO_PAST:
                        pre = "已过期";
                        break;
                    case ViewUtil.VIEW_STATU_CODE_DONE_PAST:
                        try {
                            SimpleDateFormat sdf2 = new SimpleDateFormat(SystemMethodUtil.ShortTimeCHSdf);
                            pre = "任务完成时间 :" + sdf2.format(sdf.parse(task.get("SampleTime")));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        break;
                    case ViewUtil.VIEW_STATU_CODE_DOING:
                        if (task.get("EndDateTime").equals(task.get("StopDateTime"))) {
                            pre = "后关闭";
                        } else {
                            pre = "后过期";
                        }
                        if (result.equals("00时00分00")) {
                            task.put("StatuCode", ViewUtil.VIEW_STATU_CODE_DELAY + "");
                            task.put("IsEnable", "1");
                        } else if (result.equals("该任务可一直执行")) {
                            pre = "";
                        }
                        break;
                    case ViewUtil.VIEW_STATU_CODE_DELAY:
                        pre = "后关闭";
                        if (result.equals("00时00分00")) {
                            if (task.get("IsDone").equals("1")) {
                                task.put("StatuCode", ViewUtil.VIEW_STATU_CODE_DONE_PAST + "");
                                task.put("IsEnable", "0");
                            } else {
                                task.put("StatuCode", ViewUtil.VIEW_STATU_CODE_UNDO_PAST + "");
                                task.put("IsEnable", "0");
                            }
                        } else if (result.equals("该任务可一直执行")) {
                            pre = "";
                        }
                        break;
                }
                if(Long.valueOf(nowDateTimeStr)>Long.valueOf(task.get("EndDateTime"))) task.put("IsEnable", "0");
                task.put("TimeDescibe", ViewUtil.getInstance().getTimeDescribe(task.get("StartDateTime")));
                task.put("Statu", result + pre);
            }
        }
    }

    private String getTimeCount(String nowDateTime, HashMap<String, String> task) {
        String timerCount = "";
        Calendar calendar = Calendar.getInstance();
        int statuCode = Integer.valueOf(task.get("StatuCode"));
        long mileSpan;
        Date date;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(SystemMethodUtil.LongDateTimeSdf);
            switch (statuCode) {
                case ViewUtil.VIEW_STATU_CODE_UPLOADED:// 已上传
                    timerCount = "";
                    break;
                case ViewUtil.VIEW_STATU_CODE_WAIT:// 等待执行
                    mileSpan = sdf.parse(task.get("StartDateTime")).getTime() - sdf.parse(nowDateTime).getTime();
                    days = (int) (mileSpan / oneDayMiles);
                    if (days <= 0) {
                        date = new Date(mileSpan);
                        calendar.setTime(date);
                        calendar.add(Calendar.HOUR, -8);
                        SimpleDateFormat sdf2 = new SimpleDateFormat(SystemMethodUtil.ShortTimeCHSdf);
                        timerCount = sdf2.format(calendar.getTime());
                    } else {
                        date = new Date(mileSpan % oneDayMiles);
                        calendar.setTime(date);
                        calendar.add(Calendar.HOUR, -8);
                        SimpleDateFormat sdf2 = new SimpleDateFormat(SystemMethodUtil.ShortTimeCHSdf);
                        timerCount = days + "天" + sdf2.format(calendar.getTime());
                    }
                    break;
                case ViewUtil.VIEW_STATU_CODE_UNDO_PAST:// 未执行，且过期
                    timerCount = "";
                    break;
                case ViewUtil.VIEW_STATU_CODE_DONE_PAST:// 已执行，且过期
                    timerCount = "";
                    break;
                case ViewUtil.VIEW_STATU_CODE_DOING:// 正在执行
                    if (task.get("StopDateTime").equals(SystemMethodUtil.EndlessDate)) {
                        timerCount = "该任务可一直执行";
                    } else {
                        mileSpan = sdf.parse(task.get("StopDateTime")).getTime() - sdf.parse(nowDateTime).getTime();
                        days = (int) (mileSpan / oneDayMiles);
                        if (days <= 0) {
                            date = new Date(mileSpan);
                            calendar.setTime(date);
                            calendar.add(Calendar.HOUR, -8);
                            SimpleDateFormat sdf2 = new SimpleDateFormat(SystemMethodUtil.ShortTimeCHSdf);
                            timerCount = sdf2.format(calendar.getTime());
                        } else {
                            date = new Date(mileSpan % oneDayMiles);
                            calendar.setTime(date);
                            calendar.add(Calendar.HOUR, -8);
                            SimpleDateFormat sdf2 = new SimpleDateFormat(SystemMethodUtil.ShortTimeCHSdf);
                            timerCount = days + "天" + sdf2.format(calendar.getTime());
                        }
                    }

                    break;
                case ViewUtil.VIEW_STATU_CODE_DELAY:// 延迟
                    if (task.get("EndDateTime").equals(SystemMethodUtil.EndlessDate)) {
                        timerCount = "该任务可一直执行";
                    } else {
                        mileSpan = sdf.parse(task.get("EndDateTime")).getTime() - sdf.parse(nowDateTime).getTime();
                        days = (int) (mileSpan / oneDayMiles);
                        if (days <= 0) {
                            date = new Date(mileSpan);
                            calendar.setTime(date);
                            calendar.add(Calendar.HOUR, -8);
                            SimpleDateFormat sdf2 = new SimpleDateFormat(SystemMethodUtil.ShortTimeCHSdf);
                            timerCount = sdf2.format(calendar.getTime());
                        } else {
                            date = new Date(mileSpan % oneDayMiles);
                            calendar.setTime(date);
                            calendar.add(Calendar.HOUR, -8);
                            SimpleDateFormat sdf2 = new SimpleDateFormat(SystemMethodUtil.ShortTimeCHSdf);
                            timerCount = days + "天" + sdf2.format(calendar.getTime());
                        }
                    }
                    break;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return timerCount;
    }

    private int getStatuCode(long todayNum, HashMap<String, String> task) {
        boolean candelay = task.get("EnableDelay").equals("1") ? true : false;
        int statuCode = ViewUtil.VIEW_STATU_CODE_DOING;
        if (task.get("IsUpload").equals("1")) {
            statuCode = ViewUtil.VIEW_STATU_CODE_UPLOADED;
            task.put("IsEnable", "0");
        } else {
            if (task.get("IsDone").equals("1")) {
                if (todayNum < Long.valueOf(task.get("StopDateTime")) && todayNum >= Long.valueOf(task.get("StartDateTime"))) {
                    statuCode = ViewUtil.VIEW_STATU_CODE_DOING;
                    task.put("IsEnable", "1");
                } else if (todayNum >= Long.valueOf(task.get("StopDateTime"))) {
                    if (candelay) {
                        if (todayNum >= Long.valueOf(task.get("StopDateTime")) && todayNum < Long.valueOf(task.get("EndDateTime"))) {
                            statuCode = ViewUtil.VIEW_STATU_CODE_DELAY;
                            task.put("IsEnable", "1");
                        } else {
                            statuCode = ViewUtil.VIEW_STATU_CODE_DONE_PAST;
                            task.put("IsEnable", "0");
                        }
                        ;
                    } else {
                        statuCode = ViewUtil.VIEW_STATU_CODE_DONE_PAST;
                        task.put("IsEnable", "0");
                    }
                }
            } else {
                if (todayNum < Long.valueOf(task.get("StopDateTime")) && todayNum >= Long.valueOf(task.get("StartDateTime"))) {
                    statuCode = ViewUtil.VIEW_STATU_CODE_DOING;
                    task.put("IsEnable", "1");
                } else if (todayNum >= Long.valueOf(task.get("StopDateTime"))) {
                    if (candelay) {
                        if (todayNum >= Long.valueOf(task.get("StopDateTime")) && todayNum < Long.valueOf(task.get("EndDateTime"))) {
                            statuCode = ViewUtil.VIEW_STATU_CODE_DELAY;
                            task.put("IsEnable", "1");
                        } else {
                            statuCode = ViewUtil.VIEW_STATU_CODE_UNDO_PAST;
                            task.put("IsEnable", "0");
                        }
                        ;
                    } else {
                        statuCode = ViewUtil.VIEW_STATU_CODE_UNDO_PAST;
                        task.put("IsEnable", "0");
                    }
                } else if (todayNum < Long.valueOf(task.get("StartDateTime"))) {
                    statuCode = ViewUtil.VIEW_STATU_CODE_WAIT;
                    task.put("IsEnable", "0");
                }
            }
        }
        return statuCode;
    }






    private void showTag() {
        Intent intent = new Intent(ActivityTaskGroup.this, ActivityTaskEachCard.class);
        intent.putExtra("Child", card);
        intent.putExtra("Mode", ActivityTaskConstruction.CARD_NORMAL);
        startActivityForResult(intent, 0);
    }

    private void mBindService() {
        if (binder == null) {
            service = new Intent();
            service.setAction("com.env.component.DataService");
            bindService(service, conn, Context.BIND_AUTO_CREATE);
        }

    }


    private void uploadData() {
        mBindService();
        int apnType = SystemMethodUtil.getAPNType(ActivityTaskGroup.this);
        if (binder != null) {
            if (binder.isUploading) {
                Toast.makeText(getApplicationContext(), "正在上传任务，请不要重复操作", Toast.LENGTH_LONG).show();
            } else {
                switch (apnType) {
                    case SystemMethodUtil.NoNetWork:
                        DialogUtil.setApnDialog(ActivityTaskGroup.this);
                        break;
                    case SystemMethodUtil.MobileNetWork:
                        if (sp.getBoolean(PatrolApplication.HAS_DATAEXCHANGE_TIPS, true)) {
                            DialogUtil.confirmNetWork(ActivityTaskGroup.this, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "上传数据中....", Toast.LENGTH_LONG).show();
                                    uploadWithMobileNet();
                                }
                            }, editor);
                        } else {
                            Toast.makeText(getApplicationContext(), "上传数据中....", Toast.LENGTH_LONG).show();
                            uploadWithMobileNet();
                        }
                        break;
                    case SystemMethodUtil.WifiNetWork:
                        Toast.makeText(getApplicationContext(), "上传数据中....", Toast.LENGTH_LONG).show();
                        uploadWithWifi();
                        break;
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "服务正在启动中，请稍后重试上传数据", Toast.LENGTH_LONG).show();
        }
    }

    public void uploadWithMobileNet() {
        binder.getDataService().uploadOnlyTextData(db);
        if (fastUpload == null) {
            fastUpload = new Timer();
            fastUpload.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!binder.isUploading) {
                        msg = handler.obtainMessage();
                        msg.what = 1;
                        if (binder.isExchangeDataSucss) {
                            msg.arg1 = DataService.Success;
                        } else {
                            msg.arg1 = DataService.Error;
                        }
                        msg.sendToTarget();
                    }
                }
            }, 0, 1000);
        }

    }

    public void uploadWithWifi() {
        binder.getDataService().uploadDataJsonObject(db);
        if (fastUpload == null) {
            fastUpload = new Timer();
            fastUpload.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!binder.isUploading) {
                        msg = handler.obtainMessage();
                        msg.what = 1;
                        if (binder.isExchangeDataSucss) {
                            msg.arg1 = DataService.Success;
                        } else {
                            msg.arg1 = DataService.Error;
                        }
                        msg.sendToTarget();
                    }
                }
            }, 0, 1000);
        }

    }

    private class ViewHolder{
        public ImageView status;
        public TextView name;
        public TextView time;
        public HashMap<String,String> data;
    }

    private class TaskGroupAdapter extends BaseExpandableListAdapter{

        @Override
        public int getGroupCount() {
            return devices.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            int key = devices.get(groupPosition).getDeviceID();
            return tasks.get(key).size();
        }

        @Override
        public EP_Device getGroup(int groupPosition) {
            return devices.get(groupPosition);
        }

        @Override
        public HashMap<String,String> getChild(int groupPosition, int childPosition) {
            return tasks.get(devices.get(groupPosition).getDeviceID()).get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return devices.get(groupPosition).getDeviceID();
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            HashMap<String ,String> map = getChild(groupPosition,childPosition);
            long taskId = Long.parseLong(map.get("TaskID"));
            return taskId;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            EP_Device device = getGroup(groupPosition);
            if(convertView==null){
                convertView = LayoutInflater.from(ActivityTaskGroup.this).inflate(R.layout.cons2cards_cons, null);
            }
            TextView name = (TextView)convertView.findViewById(R.id.cons2cards_cons_name);
            name.setText(device.getDeviceName());
            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
            ViewHolder viewHolder = null ;
            HashMap<String, String> task = getChild(groupPosition,childPosition);
            if(convertView == null){
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(ActivityTaskGroup.this).inflate(R.layout.item_tasklist, null);
                convertView.setBackgroundResource(R.drawable.cons2cards_cards_bg);
                viewHolder.name = (TextView)convertView.findViewById(R.id.item_tasklist_name);
                viewHolder.time = (TextView)convertView.findViewById(R.id.item_tasklist_sttime);
                viewHolder.status = (ImageView)convertView.findViewById(R.id.item_tasklist_logo);
                viewHolder.data = task;
                convertView.setTag(viewHolder);
            }else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            String statu = task.get("Statu");
            String shortStatu="",timeDescribe = "";
            if (statu.equals("该任务可一直执行")) {
                shortStatu = "该任务可一直执行";
            } else {
                try {
                    shortStatu = statu.substring(0, statu.length() - 5) + statu.substring(statu.length() - 3, statu.length());
                } catch (Exception e) {
                }
            }
            boolean IsEnable = task.get("IsEnable").equals("1") ? true : false;
            int statuCode = Integer.valueOf(task.get("StatuCode"));
            if (!showTaskTime) {
                timeDescribe = "";
            } else {
                timeDescribe = "开始于" + task.get("TimeDescibe") + "\n";
            }
            switch (statuCode) {
                case ViewUtil.VIEW_STATU_CODE_DONE_PAST:
                    viewHolder.status.setBackgroundResource(R.drawable.task_done_past);
                    break;
                case ViewUtil.VIEW_STATU_CODE_DELAY:
                    viewHolder.status.setBackgroundResource(R.drawable.task_delay);
                    break;
                default :
                    viewHolder.status.setBackgroundResource(R.drawable.task_doing);
                    break;
            }
            if(task.get("IsDone").equals("1"))viewHolder.status.setBackgroundResource(R.drawable.task_done_past);
            viewHolder.name.setText(task.get("PatrolName"));
            viewHolder.time.setText(timeDescribe + shortStatu);
            convertView.setVisibility(IsEnable?View.VISIBLE:View.GONE);
            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }


}
