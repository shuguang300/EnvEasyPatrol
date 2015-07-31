package com.env.utils;

import android.os.Environment;

import java.io.File;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * Created by Administrator on 2015/7/31.
 */
public class ConfigLog4j {
    public void configLog(){
        final LogConfigurator configurator = new LogConfigurator();
        configurator.setFileName(Environment.getExternalStorageDirectory()+ File.separator+"EnvEasyPatrol"+File.separator+".logs"+File.separator+"log4j.log");

    }
}
