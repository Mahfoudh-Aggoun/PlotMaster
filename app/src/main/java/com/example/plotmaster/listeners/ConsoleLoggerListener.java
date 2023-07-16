package com.example.plotmaster.listeners;

import androidx.databinding.BaseObservable;
import androidx.databinding.Bindable;
import androidx.lifecycle.MutableLiveData;

import com.example.plotmaster.model.Constants;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import com.example.plotmaster.BR;
import com.example.plotmaster.model.Position;

public class ConsoleLoggerListener extends BaseObservable{

    public MutableLiveData<String> observableMessages = new MutableLiveData<>();
    @Bindable
    private final CircularFifoQueue<String> loggedMessagesQueue;
    private String messages;

    private static ConsoleLoggerListener consoleLoggerListener = null;
    public static ConsoleLoggerListener getInstance(){
        if(consoleLoggerListener == null) consoleLoggerListener = new ConsoleLoggerListener();
        return consoleLoggerListener;
    }

    private void setObservable(String messages){
        observableMessages.postValue(messages);
    }

    public static void resetClass(){
        consoleLoggerListener = new ConsoleLoggerListener();
    }

    private ConsoleLoggerListener(){
        this.loggedMessagesQueue = new CircularFifoQueue<>(Constants.CONSOLE_LOGGER_MAX_SIZE);
        this.messages = "";
    }

    @Bindable
    public synchronized String getMessages(){
        return this.messages.trim();
    }

    public synchronized void offerMessage(String newMessage){
        this.loggedMessagesQueue.offer(newMessage);
        if(this.loggedMessagesQueue.isAtFullCapacity()){
            this.messages = this.messages.substring(this.messages.indexOf('\n')+1);
        }
        this.messages += newMessage + "\n";
        setObservable(newMessage);
        notifyPropertyChanged(BR.messages);
    }

    public void clearMessages(){
        this.loggedMessagesQueue.clear();
        this.messages = "";
        notifyPropertyChanged(BR.messages);
    }

}