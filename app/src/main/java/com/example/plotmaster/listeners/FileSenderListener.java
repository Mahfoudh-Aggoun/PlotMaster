package com.example.plotmaster.listeners;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;

import com.example.plotmaster.model.Constants;
import com.example.plotmaster.util.GrblUtils;
import com.example.plotmaster.BR;

import java.io.File;

public class FileSenderListener extends BaseObservable {

    private String gcodeFileName;
    private File gcodeFile;
    private Integer rowsInFile;
    private Integer rowsSent;

    private String status;

    public static final String STATUS_IDLE = "Idle";
    public static final String STATUS_READING = "Reading";
    public static final String STATUS_STREAMING = "Streaming";

    private long jobStartTime = 0L;
    private long jobEndTime = 0L;
    private String elapsedTime = "00:00:00";

    private static FileSenderListener fileSenderListener = null;
    public static FileSenderListener getInstance(){
        if(fileSenderListener == null) fileSenderListener = new FileSenderListener();
        return fileSenderListener;
    }

    public static void resetClass(){
        fileSenderListener = new FileSenderListener();
    }

    private FileSenderListener(){
        this.setStatus(STATUS_IDLE);
        this.gcodeFileName = " " + GrblUtils.implode(" | ", Constants.Companion.getSUPPORTED_FILE_TYPES());
        this.gcodeFile = null;
        this.rowsInFile = 0;
        this.rowsSent = 0;
    }

    @Bindable
    public String getGcodeFileName(){ return this.gcodeFileName; }
    private void setGcodeFileName(String gcodeFileName){
        this.gcodeFileName = gcodeFileName;
        notifyPropertyChanged(BR.gcodeFileName);
    }

    @Bindable
    public File getGcodeFile(){ return this.gcodeFile; }
    public void setGcodeFile(File gcodeFile){
        this.gcodeFile = gcodeFile;
        this.setGcodeFileName(gcodeFile.getName());
        notifyPropertyChanged(BR.gcodeFile);
    }

    @Bindable
    public Integer getRowsInFile(){ return this.rowsInFile; }
    public void setRowsInFile(Integer rowsInFile){
        this.rowsInFile = rowsInFile;
        notifyPropertyChanged(BR.rowsInFile);
    }

    @Bindable
    public Integer getRowsSent(){ return this.rowsSent; }
    public void setRowsSent(Integer rowsSent){
        this.rowsSent = rowsSent;
        notifyPropertyChanged(BR.rowsSent);
    }

    @Bindable
    public long getJobStartTime(){ return this.jobStartTime; }
    public void setJobStartTime(long startTime){
        this.jobStartTime = startTime;
        notifyPropertyChanged(BR.jobStartTime);
    }

    @Bindable
    public long getJobEndTime(){ return this.jobEndTime; }
    public void setJobEndTime(long endTime){
        this.jobEndTime = endTime;
        notifyPropertyChanged(BR.jobEndTime);
    }

    @Bindable
    public String getElapsedTime(){ return this.elapsedTime; }
    public void setElapsedTime(String elapsedTime){
        this.elapsedTime = elapsedTime;
        notifyPropertyChanged(BR.elapsedTime);
    }

    @Bindable
    public String getStatus(){ return this.status; }
    public void setStatus(String status){
        this.status = status;
        notifyPropertyChanged(BR.status);
    }

}
