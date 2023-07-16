package com.example.plotmaster.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.example.plotmaster.databinding.FragmentJoggingTabBinding;
import com.example.plotmaster.events.GrblOkEvent;
import com.example.plotmaster.events.JogCommandEvent;
import com.example.plotmaster.events.UiToastEvent;
import com.example.plotmaster.helpers.EnhancedSharedPreferences;
import com.example.plotmaster.listeners.MachineStatusListener;
import com.example.plotmaster.R;
import com.example.plotmaster.model.Constants;
import com.example.plotmaster.util.GrblUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class JoggingTabFragment extends BaseFragment implements View.OnClickListener{

    private static final String TAG = JoggingTabFragment.class.getSimpleName();
    private MachineStatusListener machineStatus;
    private EnhancedSharedPreferences sharedPref;
    private BlockingQueue<Integer> completedCommands;
    private CustomCommandsAsyncTask customCommandsAsyncTask;
    private String pointsCoords;
    FragmentJoggingTabBinding binding;

    public JoggingTabFragment() {}

    public static JoggingTabFragment newInstance() {
        return new JoggingTabFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        machineStatus = MachineStatusListener.getInstance();
        sharedPref = EnhancedSharedPreferences.getInstance(requireActivity().getApplicationContext(), getString(R.string.shared_preference_key));
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_jogging_tab, container, false);
        binding.setMachineStatus(machineStatus);
        View view = binding.getRoot();

            final FloatingActionButton jogXNegative = binding.jogXNegative;
            final FloatingActionButton jogXPositive = binding.jogXPositive;
            final FloatingActionButton jogYPositive = binding.jogYPositive;
            final FloatingActionButton jogYNegative = binding.jogYNegative;
            final FloatingActionButton jogXyTopLeft = binding.jogXyTopLeft;
            final FloatingActionButton jogXyTopRight = binding.jogXyTopRight;
            final FloatingActionButton jogXyBottomRight = binding.jogXyBottomRight;
            final FloatingActionButton jogXyBottomLeft = binding.jogXyBottomLeft;
            final FloatingActionButton runHomingCycle = binding.runHomingCycle;
            final FloatingActionButton penUp = binding.penUp;
            final FloatingActionButton penDown = binding.penDown;

            final EditText stepSize = binding.stepSize;
            final EditText feedRate = binding.joggingFeed;

        stepSize.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                    machineStatus.setJogging(Float.parseFloat(stepSize.getText().toString()), machineStatus.getJogging().feed, false);
                    InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(stepSize.getWindowToken(), 0);
                    stepSize.clearFocus();
                    return true;
                }
                return false;
            }
        });

        feedRate.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                    machineStatus.setJogging(Float.parseFloat(String.valueOf(machineStatus.getJogging().stepXY)), Double.parseDouble(feedRate.getText().toString()), false);
                    InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(feedRate.getWindowToken(), 0);
                    feedRate.clearFocus();
                    return true;
                }
                return false;
            }
        });
            jogXNegative.setOnClickListener(this);
            jogXPositive.setOnClickListener(this);
            jogYNegative.setOnClickListener(this);
            jogYPositive.setOnClickListener(this);
            jogXyTopLeft.setOnClickListener(this);
            jogXyTopRight.setOnClickListener(this);
            jogXyBottomLeft.setOnClickListener(this);
            jogXyBottomRight.setOnClickListener(this);
            runHomingCycle.setOnClickListener(this);
            penUp.setOnClickListener(this);
            penDown.setOnClickListener(this);

            //iconButton.setOnTouchListener(new RepeatListener(false, 300, 35));




//        TableRow wposLayout = view.findViewById(R.id.wpos_layout);
//        for(int i=0; i<wposLayout.getChildCount(); i++){
//            View wposLayoutView = wposLayout.getChildAt(i);
//            if(wposLayoutView instanceof Button){
//                wposLayoutView.setOnClickListener(view13 -> {
//                    if(machineStatus.getState().equals(Constants.MACHINE_STATUS_IDLE)){
//                        sendCommandIfIdle(view13.getTag().toString());
//                        sendCommandIfIdle(GrblUtils.GRBL_VIEW_PARSER_STATE_COMMAND);
//                        EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_selected_coordinate_system) + view13.getTag().toString()));
//                    }else{
//                        EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_machine_not_idle), true, true));
//                    }
//                });
//                wposLayoutView.setOnLongClickListener(this);
//            }
//        }

        return view;
    }



    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        int id = view.getId();

        switch(id){
            case R.id.run_homing_cycle:
                if(machineStatus.getState().equals(Constants.MACHINE_STATUS_IDLE) || machineStatus.getState().equals(Constants.MACHINE_STATUS_ALARM)){
                    new AlertDialog.Builder(getActivity())
                            .setTitle(getString(R.string.text_homing_cycle))
                            .setMessage(getString(R.string.text_do_homing_cycle))
                            .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> getFragmentInteractionListener().onGcodeCommandReceived(GrblUtils.GRBL_RUN_HOMING_CYCLE))
                            .setNegativeButton(getString(R.string.text_no_confirm), null)
                            .show();
                }else{
                    EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_machine_not_idle), true, true));
                }
                break;

            case R.id.jog_x_positive:
                sendJogCommand("$J=%sG91X%sF%s");
                break;
            case R.id.jog_x_negative:
                sendJogCommand("$J=%sG91X-%sF%s");
                break;
            case R.id.jog_y_positive:
                sendJogCommand("$J=%sG91Y%sF%s");
                break;
            case R.id.jog_y_negative:
                sendJogCommand("$J=%sG91Y-%sF%s");
                break;
            case R.id.jog_xy_top_left:
                sendJogCommand("$J=%1$sG91X-%2$sY%2$sF%3$s");
                break;
            case R.id.jog_xy_top_right:
                sendJogCommand("$J=%1$sG91X%2$sY%2$sF%3$s");
                break;
            case R.id.jog_xy_bottom_left:
                sendJogCommand("$J=%1$sG91X-%2$sY-%2$sF%3$s");
                break;
            case R.id.jog_xy_bottom_right:
                sendJogCommand("$J=%1$sG91X%2$sY-%2$sF%3$s");
                break;
            case R.id.pen_up:
                machineStatus.setPenPosString(false);
                EventBus.getDefault().post(new JogCommandEvent("M3 S125"));
                break;
            case R.id.pen_down:
                machineStatus.setPenPosString(true);
                EventBus.getDefault().post(new JogCommandEvent("M3 S0"));
                break;
        }


    }

    private class CustomCommandsAsyncTask extends AsyncTask<String, Integer, Integer>{

        private int MAX_RX_SERIAL_BUFFER = Constants.DEFAULT_SERIAL_RX_BUFFER - 3;
        private int CURRENT_RX_SERIAL_BUFFER;
        private LinkedList<Integer> activeCommandSizes;

        protected void onPreExecute(){
            MachineStatusListener.CompileTimeOptions compileTimeOptions = MachineStatusListener.getInstance().getCompileTimeOptions();
            if(compileTimeOptions.serialRxBuffer > 0) MAX_RX_SERIAL_BUFFER = compileTimeOptions.serialRxBuffer - 3;

            completedCommands = new ArrayBlockingQueue<>(Constants.DEFAULT_SERIAL_RX_BUFFER);
            activeCommandSizes = new LinkedList<>();
            CURRENT_RX_SERIAL_BUFFER = 0;
        }

        protected Integer doInBackground(String... commands){

            String[] lines = commands[0].split("[\r\n]+");
            for(String command: lines){
                if(isCancelled()) break;
                streamLine(command);
            }

            return 1;
        }

        private void streamLine(String gcodeCommand){

            int commandSize = gcodeCommand.length() + 1;

            // Wait until there is room, if necessary.
            while (MAX_RX_SERIAL_BUFFER < (CURRENT_RX_SERIAL_BUFFER + commandSize)) {
                try {
                    completedCommands.take();
                    if(activeCommandSizes.size() > 0) CURRENT_RX_SERIAL_BUFFER -= activeCommandSizes.removeFirst();
                } catch (InterruptedException e) {
                    Log.e(TAG, e.getMessage(), e);
                    return;
                }
            }

            activeCommandSizes.offer(commandSize);
            CURRENT_RX_SERIAL_BUFFER += commandSize;
            getFragmentInteractionListener().onGcodeCommandReceived(gcodeCommand);
        }

    }

    private void gotoAxisZero(final String axis){
        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.text_move) + axis + getString(R.string.text_axis_to_zero_position))
                .setMessage(getString(R.string.text_go_to_zero_position) + axis + "0")
                .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> sendCommandIfIdle("G0 " + axis + "0"))
                .setNegativeButton(getString(R.string.text_no_confirm), null)
                .show();
    }

    private void saveWPos(Button button){
        String wpos = button.getTag().toString();
        final String slot;

        switch (wpos){
            case "G54":
                slot = "P1";
                break;
            case "G56":
                slot = "P3";
                break;
            case "G57":
                slot = "P4";
                break;
            default:
//              //G55
                slot = "P2";
        }

        new AlertDialog.Builder(getActivity())
                .setTitle(R.string.text_save_coordinate_system)
                .setMessage(getString(R.string.text_save_coordinate_system_desc) + " " + wpos + "?")
                .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> sendCommandIfIdle(String.format("G10 L20 %s X0Y0Z0", slot)))
                .setNegativeButton(getString(R.string.text_no_confirm), null)
                .show();
    }

    private void sendJogCommand(String tag){
        if(machineStatus.getState().equals(Constants.MACHINE_STATUS_IDLE) || machineStatus.getState().equals(Constants.MACHINE_STATUS_JOG)){

            String units = "G21";
            double jogFeed = machineStatus.getJogging().feed;

            if(machineStatus.getJogging().inches){
                units = "G20";
                jogFeed = jogFeed / 25.4;
            }

            Double stepSize;

                stepSize = machineStatus.getJogging().stepXY;


            String jog = String.format(tag, units, stepSize, jogFeed);
            EventBus.getDefault().post(new JogCommandEvent(jog));
        }else{
            EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_machine_not_idle), true, true));
        }
    }


    private void sendCommandIfIdle(String command){
        if(machineStatus.getState().equals(Constants.MACHINE_STATUS_IDLE)){
            getFragmentInteractionListener().onGcodeCommandReceived(command);
        }else{
            EventBus.getDefault().post(new UiToastEvent(getString(R.string.text_machine_not_idle), true, true));
        }
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onGrblOkEvent(GrblOkEvent event){
        if(customCommandsAsyncTask != null && customCommandsAsyncTask.getStatus() == AsyncTask.Status.RUNNING){
            completedCommands.offer(1);
        }
    }


}
