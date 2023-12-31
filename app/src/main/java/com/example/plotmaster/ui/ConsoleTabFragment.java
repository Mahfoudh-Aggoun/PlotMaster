package com.example.plotmaster.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.plotmaster.R;
import com.example.plotmaster.adapters.CommandHistoryAdapter;
import com.example.plotmaster.databinding.FragmentConsoleTabBinding;
import com.example.plotmaster.helpers.EnhancedSharedPreferences;
import com.example.plotmaster.listeners.ConsoleLoggerListener;
import com.example.plotmaster.listeners.MachineStatusListener;
import com.example.plotmaster.model.CommandHistory;
import com.example.plotmaster.model.GcodeCommand;
import com.example.plotmaster.util.GrblUtils;

import java.util.List;

public class ConsoleTabFragment extends BaseFragment {

    private MachineStatusListener machineStatus;
    private ConsoleLoggerListener consoleLogger;
    private EnhancedSharedPreferences sharedPref;
    private ViewSwitcher viewSwitcher;
    private List<CommandHistory> dataSet;
    private CommandHistoryAdapter commandHistoryAdapter;
    private EditText commandInput;

    public ConsoleTabFragment() {}

    public static ConsoleTabFragment newInstance() {
        return new ConsoleTabFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = EnhancedSharedPreferences.getInstance(getActivity(), getString(R.string.shared_preference_key));
        consoleLogger = ConsoleLoggerListener.getInstance();
        machineStatus = MachineStatusListener.getInstance();
    }

    @SuppressLint({"NotifyDataSetChanged", "ClickableViewAccessibility"})
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        FragmentConsoleTabBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_console_tab, container, false);
        View view = binding.getRoot();
        binding.setConsole(consoleLogger);
        binding.setMachineStatus(machineStatus);

        viewSwitcher = view.findViewById(R.id.console_view_switcher);
        final TextView consoleLogView = view.findViewById(R.id.console_logger);
        consoleLogView.setMovementMethod(new ScrollingMovementMethod());
        commandInput = view.findViewById(R.id.command_input);

        final EditText commandInput = view.findViewById(R.id.command_input);

        consoleLogView.setOnTouchListener((v, event) -> {
            v.getParent().getParent().getParent().getParent().requestDisallowInterceptTouchEvent(true);
            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                v.getParent().getParent().getParent().getParent().requestDisallowInterceptTouchEvent(false);
            }
            return false;
        });

        Button sendCommand = view.findViewById(R.id.send_command);
        sendCommand.setOnClickListener(view12 -> {
            String commandText = commandInput.getText().toString();
            if(commandText.length() > 0){
                GcodeCommand gcodeCommand = new GcodeCommand(commandText);
                getFragmentInteractionListener().onGcodeCommandReceived(gcodeCommand.getCommandString());
                CommandHistory.saveToHistory(commandText, gcodeCommand.getCommandString());
                dataSet.clear();
                dataSet.addAll(CommandHistory.getHistory("0", "15"));
                commandHistoryAdapter.notifyDataSetChanged();
                if(gcodeCommand.getHasRomAccess()){
                    getFragmentInteractionListener().onGcodeCommandReceived(GrblUtils.GRBL_VIEW_PARSER_STATE_COMMAND);
                    getFragmentInteractionListener().onGcodeCommandReceived(GrblUtils.GRBL_VIEW_GCODE_PARAMETERS_COMMAND);
                }

                if(gcodeCommand.getCommandString().toUpperCase().contains("G43.1Z")){
                    getFragmentInteractionListener().onGcodeCommandReceived(GrblUtils.GRBL_VIEW_GCODE_PARAMETERS_COMMAND);
                }

                if(gcodeCommand.getCommandString().equals("$32=1")) machineStatus.setLaserModeEnabled(true);
                if(gcodeCommand.getCommandString().equals("$32=0")) machineStatus.setLaserModeEnabled(false);
                commandInput.setText(null);
                viewSwitcher.setDisplayedChild(0);
            }
        });

//        sendCommand.setOnLongClickListener(view1 -> {
//            new AlertDialog.Builder(getActivity())
//                    .setTitle(getString(R.string.text_clear_console))
//                    .setMessage(getString(R.string.text_clear_console_description))
//                    .setPositiveButton(getString(R.string.text_yes_confirm), (dialog, which) -> consoleLogger.clearMessages())
//                    .setNegativeButton(getString(R.string.text_no_confirm), null)
//                    .show();
//
//            return true;
//        });

        final SwitchCompat consoleVerboseOutput = view.findViewById(R.id.console_verbose_output);
        consoleVerboseOutput.setChecked(sharedPref.getBoolean(getString(R.string.preference_console_verbose_mode), false));
        consoleVerboseOutput.setOnCheckedChangeListener((compoundButton, b) -> {
            MachineStatusListener.getInstance().setVerboseOutput(b);
            sharedPref.edit().putBoolean(getString(R.string.preference_console_verbose_mode), b).apply();
        });

        Button consoleHistory = view.findViewById(R.id.console_history);
        consoleHistory.setOnClickListener(v -> viewSwitcher.showNext());

        dataSet = CommandHistory.getHistory("0", "15");
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        commandHistoryAdapter = new CommandHistoryAdapter(dataSet);
        commandHistoryAdapter.setItemClickListener(onItemClickListener);
        commandHistoryAdapter.setItemLongClickListener(onItemLongClickListener);
        recyclerView.setAdapter(commandHistoryAdapter);

        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);

//        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
//            @Override
//            public void onLoadMore(int page, int totalItemsCount) {
//                String offset = String.valueOf(page * 15);
//                List<CommandHistory> moreItems = CommandHistory.getHistory(offset, "15");
//                dataSet.addAll(moreItems);
//                commandHistoryAdapter.notifyItemRangeInserted(commandHistoryAdapter.getItemCount(), dataSet.size() - 1);
//            }
//        });

        return view;
    }

    private final View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            if(position == RecyclerView.NO_POSITION) return;
            CommandHistory commandHistory = dataSet.get(position);
            commandInput.append(commandHistory.getCommand());
        }
    };

    private final View.OnLongClickListener onItemLongClickListener = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View view) {
            final RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            final int position = viewHolder.getAdapterPosition();
            if(position == RecyclerView.NO_POSITION) return false;
            final CommandHistory commandHistory = dataSet.get(position);

            new AlertDialog.Builder(getActivity())
                    .setTitle(commandHistory.getCommand())
                    .setMessage(getString(R.string.text_delete_command_history_confirm))
                    .setPositiveButton(requireActivity().getString(R.string.text_yes_confirm), (dialog, which) -> {
                        commandHistory.delete();
                        dataSet.remove(position);
                        commandHistoryAdapter.notifyItemRemoved(position);
                        commandHistoryAdapter.notifyItemRangeChanged(position, dataSet.size());
                    }).setNegativeButton(requireActivity().getString(R.string.text_cancel), null).setCancelable(true).show();

            return true;
        }
    };

}
