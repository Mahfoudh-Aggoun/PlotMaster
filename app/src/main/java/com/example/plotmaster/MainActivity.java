package com.example.plotmaster;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.example.plotmaster.databinding.ActivityMainBinding;
import com.example.plotmaster.events.ConsoleMessageEvent;
import com.example.plotmaster.events.GrblAlarmEvent;
import com.example.plotmaster.events.GrblErrorEvent;
import com.example.plotmaster.events.UiToastEvent;
import com.example.plotmaster.helpers.EnhancedSharedPreferences;
import com.example.plotmaster.helpers.NotificationHelper;
import com.example.plotmaster.listeners.ConsoleLoggerListener;
import com.example.plotmaster.listeners.MachineStatusListener;
import com.example.plotmaster.model.Constants;
import com.example.plotmaster.services.GrblBluetoothSerialService;
import com.example.plotmaster.ui.BaseFragment;
import com.example.plotmaster.ui.GrblFragmentPagerAdapter;
import com.example.plotmaster.util.GrblUtils;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import es.dmoral.toasty.Toasty;

public abstract class MainActivity extends AppCompatActivity implements BaseFragment.OnFragmentInteractionListener{

    static {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
    }

    protected EnhancedSharedPreferences sharedPref;
    protected ConsoleLoggerListener consoleLogger = null;
    protected MachineStatusListener machineStatus = null;
    protected GrblBluetoothSerialService grblBluetoothSerialService = null;

    public static boolean isAppRunning;

    double xStart = 197;
    double yStart = 360;
    double xEnd = -1;
    double yEnd = -1;
    boolean checked = false;
    Bitmap bitmap;
    ImageView image;
    Canvas canvas;
    Paint paint = new Paint();
    CardView clearButton;

    private Toast lastToast;
    ActivityMainBinding binding;
    CardView bluetoothButton;
    CardView softResetButton;
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        sharedPref = EnhancedSharedPreferences.getInstance(GrblController.getInstance(), getString(R.string.shared_preference_key));
        bluetoothButton = binding.bluetoothBtn;
        softResetButton = binding.softReset;
        image = binding.grid;
        if(getSupportActionBar() != null) getSupportActionBar().setSubtitle(getString(R.string.text_not_connected));

        applicationSetup();
        binding.setMachineStatus(machineStatus);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        clearButton = binding.clearButton;
        clearButton.setOnClickListener(view -> {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            bitmap = Bitmap.createBitmap(image.getWidth(),
                    image.getHeight(),
                    Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            paint.setColor(Color.RED);
            paint.setAntiAlias(true);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4F);
            canvas.drawLine(
                    (float) 0,
                    (float) 0,
                    (float) 0,
                    (float) 0,
                    paint);

            image.setImageBitmap(bitmap);
        });

        machineStatus.observablePos.observe(this, (position -> {
            xEnd =  position.getCordX()*4 +197;
            yEnd =  -(position.getCordY()*4) +360;

            if (!checked) {
                bitmap = Bitmap.createBitmap(image.getWidth(),
                        image.getHeight(),
                        Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
                paint.setColor(Color.RED);
                paint.setAntiAlias(true);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2F);

                checked = true;
            }
            if(machineStatus.penDown) {
                canvas.drawLine(
                        (float) xStart,
                        (float) yStart,
                        (float) xEnd,
                        (float) yEnd,
                        paint);

                image.setImageBitmap(bitmap);
            }

            xStart = xEnd;
            yStart = yEnd;

        }));

        machineStatus.observablePenPos.observe(this, penPos -> {
//            if(penPos)
//                binding.penPos.setText("DOWN");
//            else
//                binding.penPos.setText("UP");
        });

        consoleLogger.observableMessages.observe(this, message -> {
            if(message.equals("M3 S125")){
                machineStatus.setPenPosString(false);
                binding.penPos.setText("UP");}
            if(message.equals("M3 S0") ) {
                machineStatus.setPenPosString(true);
                binding.penPos.setText("DOWN");
            }
        });





//        for(int resourceId: new Integer[]{R.id.wpos_edit_x, R.id.wpos_edit_y, R.id.wpos_edit_z}){
//            IconTextView positionTextView = findViewById(resourceId);
//            positionTextView.setOnClickListener(v -> setWorkPosition(v.getTag().toString()));
//        }

        setupTabLayout();
//        checkPowerManagement();
    }




    @Override
    public void onDestroy(){
        super.onDestroy();

       // stopService(new Intent(this, FileStreamerIntentService.class));
        ConsoleLoggerListener.resetClass();
       // FileSenderListener.resetClass();
        MachineStatusListener.resetClass();
        isAppRunning = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        lastToast = null;
    }

    @Override
    public void onBackPressed(){ moveTaskToBack(true); }





    protected void applicationSetup(){
        NotificationHelper notificationHelper = new NotificationHelper(this);
        notificationHelper.createChannels();

        consoleLogger = ConsoleLoggerListener.getInstance();
        machineStatus = MachineStatusListener.getInstance();
        machineStatus.setJogging(sharedPref.getDouble(getString(R.string.preference_jogging_step_size), 1.00),
                sharedPref.getDouble(getString(R.string.preference_jogging_feed_rate), 2400.0),
                sharedPref.getBoolean(getString(R.string.preference_jogging_in_inches), false));
        machineStatus.setVerboseOutput(sharedPref.getBoolean(getString(R.string.preference_console_verbose_mode), false));
        machineStatus.setIgnoreError20(sharedPref.getBoolean(getString(R.string.preference_ignore_error_20), false));
        machineStatus.setUsbBaudRate(Integer.parseInt(sharedPref.getString(getString(R.string.usb_serial_baud_rate), Constants.USB_BAUD_RATE)));
        machineStatus.setSingleStepMode(sharedPref.getBoolean(getString(R.string.preference_single_step_mode), false));
        machineStatus.setCustomStartUpString(sharedPref.getString(getString(R.string.preference_start_up_string), ""));

    }

    protected void setupTabLayout() {
        TabLayout tabLayout = binding.tabLayout;
//    tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.jogging_tab_icon));
//    tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.console_tab_icon));
//    tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.file_sender_tab_icon));

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        ViewPager2 viewPager = binding.tabLayoutPager;
        GrblFragmentPagerAdapter pagerAdapter = new GrblFragmentPagerAdapter(getSupportFragmentManager(), getLifecycle());
        viewPager.setAdapter(pagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setIcon(R.drawable.jogging_tab_icon);
                    break;
                case 1:
                    tab.setIcon(R.drawable.console_tab_icon);
                    break;
                case 2:
                    tab.setIcon(R.drawable.file_sender_tab_icon);
                    break;
            }
        }).attach();
    }




//    private void setWorkPosition(final String axisLabel){
//        LayoutInflater inflater = LayoutInflater.from(this);
//        View v = inflater.inflate(R.layout.dialog_input_decimal_signed, null, false);
//
//        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
//        alertDialogBuilder.setView(v);
//        alertDialogBuilder.setTitle(getString(R.string.test_set_cordinate_system, axisLabel));
//        alertDialogBuilder.setMessage(getString(R.string.test_set_cordinate_system_description, axisLabel));
//
//        final EditText editText = v.findViewById(R.id.dialog_input_decimal_signed);
//        if(axisLabel.equalsIgnoreCase("X")) editText.setText(String.valueOf(machineStatus.getWorkPosition().getCordX()));
//        if(axisLabel.equalsIgnoreCase("Y")) editText.setText(String.valueOf(machineStatus.getWorkPosition().getCordY()));
//        if(axisLabel.equalsIgnoreCase("Z")) editText.setText(String.valueOf(machineStatus.getWorkPosition().getCordZ()));
//        editText.setSelection(editText.getText().length());
//
//        alertDialogBuilder.setCancelable(true)
//                .setPositiveButton(getString(R.string.text_ok), (dialog, id) -> {
//                    String axisValue = editText.getText().toString();
//                    if(axisValue.length() > 0){
//                        sendCommandIfIdle("G10L20P0" + axisLabel + axisValue);
//                    }
//                })
//                .setNeutralButton("/2",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialog, int id) {
//
//                                Double val = Double.parseDouble( editText.getText().toString());
//                                val=val/2;
//                                String axisValue=val.toString();
//                                if(axisValue.length() > 0){
//                                    sendCommandIfIdle("G10L20P0" + axisLabel + axisValue);
//                                }
//                            }
//                        })
//                .setNegativeButton(getString(R.string.text_cancel), (dialog, id) -> dialog.cancel());
//
//        AlertDialog dialog = alertDialogBuilder.create();
//        if(dialog.getWindow() != null) dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
//        dialog.show();
//    }

    private void sendCommandIfIdle(String command){
        if(machineStatus.getState().equals(Constants.MACHINE_STATUS_IDLE)){
            onGcodeCommandReceived(command);
        }else{
            showToastMessage(getString(R.string.text_machine_not_idle), true, true);
        }
    }

    protected void showToastMessage(String message){
        this.showToastMessage(message, false, false);
    }

    @SuppressLint("ShowToast")
    protected void showToastMessage(String message, Boolean longToast, Boolean isWarning){
        if(isWarning){
            lastToast = Toasty.warning(this, message, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT, true);
        }else{
            lastToast = Toasty.success(this, message, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT);
        }

        lastToast.setGravity(Gravity.FILL_HORIZONTAL|Gravity.TOP, 0, 120);
        lastToast.show();
    }

    @Override
    public void onGcodeCommandReceived(String command) {

    }

    @Override
    public void onGrblRealTimeCommandReceived(byte command) {

    }

    public static boolean isTablet(Context context){
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    private void checkPowerManagement(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);

            if(pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())){

                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.text_power_management_warning_title))
                        .setMessage(getString(R.string.text_power_management_warning_description))
                        .setPositiveButton(getString(R.string.text_settings), (dialog, which) -> {
                            try {
                                Intent myIntent = new Intent();
                                myIntent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                                startActivity(myIntent);
                            } catch (RuntimeException ignored) {}
                        })
                        .setNegativeButton(getString(R.string.text_cancel), null)
                        .setCancelable(false)
                        .show();

            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGrblAlarmEvent(GrblAlarmEvent event){
        consoleLogger.offerMessage(event.toString());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void  onGrblErrorEvent(GrblErrorEvent event){
        consoleLogger.offerMessage(event.toString());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConsoleMessageEvent(ConsoleMessageEvent event){
        consoleLogger.offerMessage(event.getMessage());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUiToastEvent(UiToastEvent event){
        showToastMessage(event.getMessage(), event.getLongToast(), event.getIsWarning());
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void OnStreamingCompleteEvent(StreamingCompleteEvent event){
//        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void OnStreamingStartEvent(StreamingStartedEvent event){
//        if(sharedPref.getBoolean(getString(R.string.preference_keep_screen_on), false)){
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        }
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.tab_layout_pager + ":" + 1);
        if(fragment != null) fragment.onActivityResult(requestCode, resultCode, data);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event){

        if(keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE){
            if(machineStatus.getState().equals(Constants.MACHINE_STATUS_RUN)){
                onGrblRealTimeCommandReceived(GrblUtils.GRBL_PAUSE_COMMAND);
                return true;
            }

            if(machineStatus.getState().equals(Constants.MACHINE_STATUS_HOLD)){
                onGrblRealTimeCommandReceived(GrblUtils.GRBL_RESUME_COMMAND);
                return true;
            }
        }

        return false;
    }

}
