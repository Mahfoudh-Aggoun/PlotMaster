package com.example.plotmaster.listeners;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.plotmaster.GrblController;
import com.example.plotmaster.R;
import com.example.plotmaster.events.ConsoleMessageEvent;
import com.example.plotmaster.events.GrblAlarmEvent;
import com.example.plotmaster.events.GrblErrorEvent;
import com.example.plotmaster.events.GrblOkEvent;
import com.example.plotmaster.events.GrblProbeEvent;
import com.example.plotmaster.events.GrblSettingMessageEvent;
import com.example.plotmaster.events.UiToastEvent;
import com.example.plotmaster.model.Constants;
import com.example.plotmaster.model.Position;
import com.example.plotmaster.util.GrblLookups;
import com.example.plotmaster.util.GrblUtils;

import org.greenrobot.eventbus.EventBus;
import static org.greenrobot.eventbus.EventBus.TAG;

public abstract class SerialCommunicationHandler extends Handler {

    protected final MachineStatusListener machineStatus;

    private static GrblLookups GrblErrors;
    private static GrblLookups GrblAlarms;
    private static GrblLookups GrblSettings;

    private final String[] startUpCommands = {GrblUtils.GRBL_BUILD_INFO_COMMAND, GrblUtils.GRBL_VIEW_SETTINGS_COMMAND, GrblUtils.GRBL_VIEW_PARSER_STATE_COMMAND, GrblUtils.GRBL_VIEW_GCODE_PARAMETERS_COMMAND};

    public SerialCommunicationHandler(){
        machineStatus = MachineStatusListener.getInstance();
        GrblAlarms = new GrblLookups(GrblController.getInstance(), "alarm_codes");
        GrblErrors = new GrblLookups(GrblController.getInstance(), "error_codes");
        GrblSettings = new GrblLookups(GrblController.getInstance(), "setting_codes");
    }

    protected boolean onSerialRead(String message){
        boolean isVersionString = false;
        if(GrblUtils.isGrblOkMessage(message)){
            EventBus.getDefault().post(new GrblOkEvent(message));

        }else if(GrblUtils.isGrblStatusString(message)){
            this.updateMachineStatus(message);
            if(machineStatus.getVerboseOutput()) EventBus.getDefault().post(new ConsoleMessageEvent(message));

        }else if(GrblUtils.isGrblAlarmMessage(message)){
            GrblAlarmEvent alarmEvent = new GrblAlarmEvent(GrblAlarms, message);
            machineStatus.setState(MachineStatusListener.STATE_ALARM);
            EventBus.getDefault().post(alarmEvent);
            EventBus.getDefault().post(new UiToastEvent(alarmEvent.getAlarmDescription()));

        }else if(GrblUtils.isGrblFeedbackMessage(message)){
            EventBus.getDefault().post(new ConsoleMessageEvent(message));

        }else if(GrblUtils.isGrblErrorMessage(message)) {
            GrblErrorEvent errorEvent = new GrblErrorEvent(GrblErrors, message);
            EventBus.getDefault().post(errorEvent);
            EventBus.getDefault().post(new UiToastEvent(errorEvent.getErrorDescription()));

        }else if(GrblUtils.isGrblProbeMessage(message)){
            String probeString = GrblUtils.getProbeString(message);
            if(probeString != null){
                EventBus.getDefault().post(new GrblProbeEvent(probeString));
            }
            EventBus.getDefault().post(new ConsoleMessageEvent(message));

        }else if(GrblUtils.isGrblSettingMessage(message)){
            GrblSettingMessageEvent settingMessageEvent = new GrblSettingMessageEvent(GrblSettings, message);
            EventBus.getDefault().post(settingMessageEvent);
            EventBus.getDefault().post(new ConsoleMessageEvent(settingMessageEvent.toString()));

        }else if(GrblUtils.isBuildOptionsMessage(message)) {
            String buildOptions = GrblUtils.getBuildOptionString(message);
            machineStatus.setCompileTimeOptions(GrblUtils.getCompileTimeOptionsFromString(buildOptions));
            EventBus.getDefault().post(new ConsoleMessageEvent(message));

        }else if(GrblUtils.isParserStateMessage(message)) {
            String parserStateString = GrblUtils.getParserStateString(message);
            String[] parts = parserStateString.split("\\s+");
            if (parts.length >= 6) {
                machineStatus.setParserState(parserStateString);
                EventBus.getDefault().post(new ConsoleMessageEvent(message));
            }

        }else if(GrblUtils.isGrblToolLengthOffsetMessage(message)) {
            machineStatus.setToolLengthOffset(GrblUtils.getToolLengthOffset(message));
            EventBus.getDefault().post(new ConsoleMessageEvent(message));

        }else if(machineStatus.getCustomStartUpString().length() > 0 && message.equalsIgnoreCase(machineStatus.getCustomStartUpString())){
            EventBus.getDefault().post(new ConsoleMessageEvent(message));
            MachineStatusListener.BuildInfo buildInfo = new MachineStatusListener.BuildInfo(1.1, 'f');
            machineStatus.setBuildInfo(buildInfo);
            isVersionString = true;

        }else if(GrblUtils.isGrblVersionString(message)) {

            EventBus.getDefault().post(new ConsoleMessageEvent(message));
            double versionDouble = GrblUtils.getVersionDouble(message);
            Character versionLetter = GrblUtils.getVersionLetter(message);

            MachineStatusListener.BuildInfo buildInfo = new MachineStatusListener.BuildInfo(versionDouble, versionLetter);

            if (buildInfo.versionDouble >= Constants.MIN_SUPPORTED_VERSION) {
                machineStatus.setBuildInfo(buildInfo);
                isVersionString = true;
            } else {
                String messageNotSupported = GrblController.getInstance().getString(R.string.text_grbl_unsupported, String.valueOf(Constants.MIN_SUPPORTED_VERSION));
                EventBus.getDefault().post(new UiToastEvent(messageNotSupported));
                EventBus.getDefault().post(new ConsoleMessageEvent(messageNotSupported));
            }
        }else{
            EventBus.getDefault().post(new ConsoleMessageEvent(message));
            Log.d(TAG, "MESSAGE NOT HANDLED: " + message);
        }

        return isVersionString;
    }

    private synchronized void updateMachineStatus(String statusMessage){

        Position MPos = null;
        Position WPos = null;
        Position WCO = null;
        boolean hasOverrides = false;
        boolean enabledPinsChanged = false;
        boolean accessoryStatesChanged = false;

        for (String part : statusMessage.substring(0, statusMessage.length()-1).split("\\|")) {

            if(part.startsWith("<")) {
                int idx = part.indexOf(':');
                machineStatus.setState((idx == -1) ? part.substring(1) : part.substring(1, idx));
            }else if (part.startsWith("MPos:")) {
                MPos = GrblUtils.getPositionFromStatusString(statusMessage, GrblUtils.machinePattern);

            }else if (part.startsWith("WPos:")) {
                WPos = GrblUtils.getPositionFromStatusString(statusMessage, GrblUtils.workPattern);

            }else if (part.startsWith("WCO:")) {
                WCO = GrblUtils.getPositionFromStatusString(statusMessage, GrblUtils.wcoPattern);

            }else if(part.startsWith("Bf:")){
                String[] bufferStateParts = part.substring(3).trim().split(",");
                if(bufferStateParts.length == 2){
                    int planerBuffer = Integer.parseInt(bufferStateParts[0]);
                    int serialBuffer = Integer.parseInt(bufferStateParts[1]);
                    machineStatus.setPlannerBuffer(planerBuffer);
                    machineStatus.setSerialRxBuffer(serialBuffer);
                }

            }else if (part.startsWith("Ov:")) {
                String[] overrideParts = part.substring(3).trim().split(",");
                if (overrideParts.length == 3) {
                    machineStatus.setOverridePercents(Integer.parseInt(overrideParts[0]), Integer.parseInt(overrideParts[1]), Integer.parseInt(overrideParts[2]));
                }

                hasOverrides = true;

            }else if (part.startsWith("F:")) {
                machineStatus.setFeedRate(Double.parseDouble(part.substring(2)));

            }else if (part.startsWith("FS:")) {
                String[] parts = part.substring(3).split(",");
                machineStatus.setFeedRate(Double.parseDouble(parts[0]));
                machineStatus.setSpindleSpeed(Double.parseDouble(parts[1]));

            }else if (part.startsWith("Pn:")) {
                String value = part.substring(part.indexOf(':')+1);
                machineStatus.setEnabledPins(value);
                enabledPinsChanged = true;

            }else if (part.startsWith("A:")) {
                String value = part.substring(part.indexOf(':')+1);
                machineStatus.setAccessoryStates(value);
                accessoryStatesChanged = true;
            }
        }

        if(WCO == null){
            if(machineStatus.getWorkCoordsOffset() != null){
                WCO = machineStatus.getWorkCoordsOffset();
            } else {
                WCO = new Position(0,0,0);
            }
        }

        if(WPos == null && MPos != null) WPos = new Position(MPos.getCordX() - WCO.getCordX(), MPos.getCordY() - WCO.getCordY(), MPos.getCordZ() - WCO.getCordZ());
        if(MPos == null && WPos != null) MPos = new Position(WPos.getCordX() + WCO.getCordX(), WPos.getCordY() + WCO.getCordY(), WPos.getCordZ() + WCO.getCordZ());

        machineStatus.setMachinePosition(MPos);
        machineStatus.setWorkPosition(WPos);
        machineStatus.setWorkCoordsOffset(WCO);

        if(!enabledPinsChanged) machineStatus.setEnabledPins("");

        if(hasOverrides){
            if(!accessoryStatesChanged) machineStatus.setAccessoryStates("");
        }

    }

    public String[] getStartUpCommands(){
        return this.startUpCommands;
    }

    public abstract void handleMessage(Message msg);

}