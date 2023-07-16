package com.example.plotmaster.events;

public class StreamingCompleteEvent {

    private String message;
    private String fileName;
    private int rowsSent;
    private long timeMillis;
    private String timeTaken;

    public StreamingCompleteEvent(String message){
        this.message = message;
    }

    public StreamingCompleteEvent(String message, String fileName, int rowsSent, long timeMillis, String timeTaken){
        this.message = message;
        this.fileName = fileName;
        this.rowsSent = rowsSent;
        this.timeMillis = timeMillis;
        this.timeTaken = timeTaken;
    }

    public String getMessage(){ return this.message; }
    public void setMessage(String message){ this.message = message; }

    public String getFileName(){ return this.fileName; }
    public void setFileName(String fileName){ this.fileName = fileName; }

    public int getRowsSent(){ return this.rowsSent; }
    public void setRowsSent(int rowsSent){ this.rowsSent = rowsSent; }

    public long getTimeMillis(){ return this.timeMillis; }
    public void setTimeMillis(long timeMillis){ this.timeMillis = timeMillis; }

    public String getTimeTaken(){ return this.timeTaken; }
    public void setTimeTaken(String timeTaken){ this.timeTaken = timeTaken; }

}
