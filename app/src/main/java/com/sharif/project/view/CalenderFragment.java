package com.sharif.project.view;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sharif.project.R;
import com.sharif.project.StudyActivity;
import com.sharif.project.adapter.DailyItemsAdapter;
import com.sharif.project.adapter.RecyclerItemClickListener;
import com.sharif.project.controller.BoxController;
import com.sharif.project.controller.LimitationController;
import com.sharif.project.databinding.FragmentCalenderBinding;
import com.sharif.project.model.Box;
import com.sharif.project.model.DailyItem;
import com.sharif.project.model.DailyItemType;
import com.sharif.project.model.Limitation;
import com.sharif.project.util.MapUtil;
import com.sharif.project.util.PersianDateUtil;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ir.hamsaa.persiandatepicker.PersianDatePickerDialog;
import ir.hamsaa.persiandatepicker.api.PersianPickerDate;
import ir.hamsaa.persiandatepicker.api.PersianPickerListener;

public class CalenderFragment extends Fragment {

    private Date historyStartDate;
    private Date historyEndDate;
    private EditText historyStartDateEdittext;
    private EditText historyEndDateEdittext;

    private Date currentDate;
    private TextView currentDateTextView;

    private DailyItemsAdapter dailyItemsAdapter;

    private FragmentCalenderBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.calender_fragment_actionbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalenderBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        currentDate = PersianDateUtil.today();
        currentDateTextView = binding.currentDateTextview;
        ImageView prevDayImageview = binding.prevDayImageview;
        ImageView nextDayImageview = binding.nextDayImageview;

        currentDateTextView.setText(PersianDateUtil.getDateString(currentDate));

        currentDateTextView.setOnClickListener(v -> {
            getDate();
        });
        prevDayImageview.setOnClickListener(v -> {
            currentDate = PersianDateUtil.prevDay(currentDate);
            currentDateTextView.setText(PersianDateUtil.getDateString(currentDate));
            updateDailyPlan();
        });
        nextDayImageview.setOnClickListener(v -> {
            currentDate = PersianDateUtil.nextDay(currentDate);
            currentDateTextView.setText(PersianDateUtil.getDateString(currentDate));
            updateDailyPlan();
        });

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        dailyItemsAdapter = new DailyItemsAdapter(getDailyItems());
        recyclerView.setAdapter(dailyItemsAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener
                (getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        DailyItem dailyItem = dailyItemsAdapter.mDailyItems.get(position);
                        if (dailyItem.type == DailyItemType.STUDY) {
                            Box box = BoxController.getBoxById(getContext(), dailyItem.id);
                            if (!box.finished)
                                showModifyBoxStateDialog(box.id);
                        }
                        if (dailyItem.type == DailyItemType.LIMITATION) {
                            showModifyLimitationDialog(dailyItem.id);
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        DailyItem dailyItem = dailyItemsAdapter.mDailyItems.get(position);
                        if (dailyItem.type == DailyItemType.STUDY)
                            showBoxNoteDialog(dailyItem.id);
                    }
                }));

        binding.addBoxLinearlayout.setOnClickListener(v -> {
            showAddBoxTypeDialog();
        });

        binding.addLimitationLinearlayout.setOnClickListener(v -> {
            showAddLimitationToDailyPlan();
        });


        return root;
    }

    private int parseStringAsMinute(String str) {
        int minute = 0;
        try {
            minute += 60 * Integer.parseInt(str.split(":")[0]);
            minute += Integer.parseInt(str.split(":")[1]);
        } catch (Exception e) {
            if (str.endsWith(":"))
                str = str.substring(0, str.length() - 1);
            minute = 60 * Integer.parseInt(str);
        }
        return minute;
    }

    private void showAddBoxTypeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialog);
        builder.setTitle("زمان\u200Cبندی بسته\u200Cها");
        builder.setIcon(R.drawable.ic_schedule_box_green);
        builder.setMessage("نوع زمان\u200Cبندی مورد نظر را انتخاب کنید.");
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "انصراف", (dialog, which) -> {});
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "دستی", (dialog, which) -> {});
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "خودکار", (dialog, which) -> {});
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            alertDialog.dismiss();
            showManualAddBoxDialog();
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            alertDialog.dismiss();
            showAutoAddBoxDialog();
        });
    }

    private void showAutoAddBoxDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_auto_add_box, null);

        EditText minStudyTimeEditText = view.findViewById(R.id.min_study_time);
        EditText maxStudyTimeEditText = view.findViewById(R.id.max_study_time);
        EditText minRestEditText = view.findViewById(R.id.min_rest);

        minStudyTimeEditText.setFilters(PersianDateUtil.getTimeInputFilter());
        maxStudyTimeEditText.setFilters(PersianDateUtil.getTimeInputFilter());
        minRestEditText.setFilters(PersianDateUtil.getTimeInputFilter());

        builder.setView(view);
        builder.setTitle("زمان\u200Cبندی خودکار بسته\u200Cها");
        builder.setIcon(R.drawable.ic_schedule_box_green);
        builder.setMessage("اطلاعات مورد نیاز را وارد کنید.");
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "انصراف", (dialog, which) -> {});
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "انجام", (dialog, which) -> {});
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String minStudyTimeStr = minStudyTimeEditText.getText().toString();
            String maxStudyTimeStr = maxStudyTimeEditText.getText().toString();
            String minRestStr = minRestEditText.getText().toString();
            if (minStudyTimeStr.isEmpty()) {
                minStudyTimeEditText.setError(getResources().getString(R.string.error_blank_field));
            } else if (maxStudyTimeStr.isEmpty()) {
                maxStudyTimeEditText.setError(getResources().getString(R.string.error_blank_field));
            } else if (minRestStr.isEmpty()) {
                minRestEditText.setError(getResources().getString(R.string.error_blank_field));
            } else {
                int minStudyTime = parseStringAsMinute(minStudyTimeStr);
                int maxStudyTime = parseStringAsMinute(maxStudyTimeStr);
                int minRest = parseStringAsMinute(minRestStr);
                if (maxStudyTime < minStudyTime) {
                    maxStudyTimeEditText.setError(getResources().getString(R.string.error_study_time_interval));
                } else {
                    alertDialog.dismiss();
                    final ProgressDialog[] dialog = new ProgressDialog[1];
                    getActivity().runOnUiThread(() -> dialog[0] = ProgressDialog.show(getActivity(), null, "لطفاً منتظر بمانید...", true));
                    new Thread(() -> {
                        boolean result = autoAddBoxToDailyPlan(minStudyTime, maxStudyTime, minRest);
                        getActivity().runOnUiThread(() -> {
                            dialog[0].dismiss();
                            if (result) {
                                updateDailyPlan();
                                Toast.makeText(getActivity(), "زمان\u200Cبندی خودکار بسته\u200Cها با موفقیت انجام شد.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getActivity(), "زمان\u200Cبندی خودکار بسته\u200Cها امکان\u200Cپذیر نبود.", Toast.LENGTH_LONG).show();
                            }
                        });
                    }).start();
                }
            }
        });
    }

    private boolean autoAddBoxToDailyPlan(int minStudyTime, int maxStudyTime, int minRest) {
        ArrayList<Box> unspecifiedBoxes = BoxController.getAllUnspecifiedBoxesByDate(getContext(), currentDate);
        ArrayList<Pair<Date, Integer>> freeTimes = new ArrayList<>();
        for (DailyItem dailyItem : dailyItemsAdapter.mDailyItems) {
            if (dailyItem.type == DailyItemType.FREE) {
                Date startTime = dailyItem.startTime;
                int capacity = (int) ((dailyItem.endTime.getTime() - dailyItem.startTime.getTime()) / 60000);
                freeTimes.add(new Pair<>(startTime, capacity));
            }
        }
        Collections.shuffle(unspecifiedBoxes);
        ArrayList<Box> boxes = autoAddBoxToDailyPlanHelper(freeTimes, 0, unspecifiedBoxes, minStudyTime, maxStudyTime, minRest);
        if (boxes == null)
            return false;
        for (Box box : boxes)
            if (box.startTime > 0)
                BoxController.updateBox(getContext(), box, String.valueOf(box.id));
        return true;
    }

    private ArrayList<Box> autoAddBoxToDailyPlanHelper(ArrayList<Pair<Date, Integer>> freeTimes, int index, ArrayList<Box> boxes, int minStudyTime, int maxStudyTime, int minRest) {
        if (minStudyTime <= 0)
            return boxes;
        for (int i = index; i < freeTimes.size(); i++) {
            Date startTime = freeTimes.get(i).first;
            int capacity = freeTimes.get(i).second;
            for (Box box : boxes) {
                if (box.startTime != -1L || box.duration > capacity || box.duration > maxStudyTime)
                    continue;
                ArrayList<Box> newBoxes;

                box.startTime = startTime.getTime();
                Date newStartTime = new Date(startTime.getTime() + 60000L * (box.duration + minRest));
                freeTimes.set(i, new Pair<>(newStartTime, capacity - (box.duration + minRest)));
                int newMinStudyTime = minStudyTime - box.duration;
                int newMaxStudyTime = maxStudyTime - box.duration;
                newBoxes = autoAddBoxToDailyPlanHelper(freeTimes, i, boxes, newMinStudyTime, newMaxStudyTime, minRest);
                if (newBoxes != null)
                    return newBoxes;

                box.startTime = -2L;
                freeTimes.set(i, new Pair<>(startTime, capacity));
                newBoxes = autoAddBoxToDailyPlanHelper(freeTimes, i, boxes, minStudyTime, maxStudyTime, minRest);
                if (newBoxes != null)
                    return newBoxes;

                box.startTime = -1L;
            }
        }
        return null;
    }

    @SuppressLint("MissingInflatedId")
    private void showManualAddBoxDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_manual_add_box, null);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        DailyItemsAdapter possibleDailyItemsAdapter = new DailyItemsAdapter(new ArrayList<>());
        recyclerView.setAdapter(possibleDailyItemsAdapter);

        EditText startTimeEditText = view.findViewById(R.id.start_time_edittext);
        startTimeEditText.setFilters(PersianDateUtil.getTimeInputFilter());
        startTimeEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int minute = -1;
                String startTimeAsStr = startTimeEditText.getText().toString();
                if (startTimeAsStr.isEmpty()) {
                    possibleDailyItemsAdapter.mDailyItems = new ArrayList<>();
                    possibleDailyItemsAdapter.notifyDataSetChanged();
                } else {
                    minute = parseStringAsMinute(startTimeAsStr);
                    Date date = new Date(currentDate.getTime() + 60000L * minute);
                    possibleDailyItemsAdapter.mDailyItems = getPossibleDailyItems(date);
                    possibleDailyItemsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        builder.setView(view);
        builder.setTitle("زمان\u200Cبندی دستی بسته\u200Cها");
        builder.setIcon(R.drawable.ic_schedule_box_green);
        builder.setMessage("زمان شروع مطالعه را وارد کرده و بسته مورد نظر را انتخاب کنید.");
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "انصراف", (dialog, which) -> {});
        alertDialog.show();

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener
                (getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        DailyItem dailyItem = possibleDailyItemsAdapter.mDailyItems.get(position);
                        if (dailyItem.availablilty) {
                            Box box = BoxController.getBoxById(getContext(), dailyItem.id);
                            box.startTime = dailyItem.startTime.getTime();
                            BoxController.updateBox(getContext(), box, String.valueOf(box.id));
                            updateDailyPlan();
                            alertDialog.dismiss();
                            Toast.makeText(getContext(), "زمان شروع بسته با موفقیت مشخص شد", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        DailyItem dailyItem = possibleDailyItemsAdapter.mDailyItems.get(position);
                        showBoxNoteDialog(dailyItem.id);
                    }
                }));
    }

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    private void showAddLimitationToDailyPlan() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_limitation, null);

        EditText nameEditText = view.findViewById(R.id.name_edittext);
        EditText startTimeEditText = view.findViewById(R.id.start_time_edittext);
        EditText endTimeEditText = view.findViewById(R.id.end_time_edittext);

        startTimeEditText.setFilters(PersianDateUtil.getTimeInputFilter());
        endTimeEditText.setFilters(PersianDateUtil.getTimeInputFilter());

        RadioGroup periodRadioGroup = view.findViewById(R.id.period_radiogroup);
        RadioButton todayRadioButton = view.findViewById(R.id.today_radiobutton);
        RadioButton weeklyRadioButton = view.findViewById(R.id.weekly_radiobutton);
        RadioButton dailyRadioButton = view.findViewById(R.id.daily_radiobutton);
        weeklyRadioButton.setText("هر " + PersianDateUtil.getDateString(currentDate).split(" ")[0]);

        builder.setView(view);
        builder.setTitle("افزودن محدودیت جدید");
        builder.setIcon(R.drawable.ic_add);
        builder.setMessage("نام محدودیت و بازه زمانی آن را وارد کنید");
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "افزودن", (dialog, which) -> {});
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "انصراف", (dialog, which) -> {});
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            if (name.isEmpty()) {
                nameEditText.setError(getResources().getString(R.string.error_blank_limitation_name));
            } else {
                String startTimeAsStr = startTimeEditText.getText().toString().trim();
                if (startTimeAsStr.isEmpty()) {
                    startTimeEditText.setError(getResources().getString(R.string.error_blank_start_time));
                } else {
                    String endTimeAsStr = endTimeEditText.getText().toString().trim();
                    if (endTimeAsStr.isEmpty()) {
                        endTimeEditText.setError(getResources().getString(R.string.error_blank_end_time));
                    } else {
                        long startTime = currentDate.getTime() + 60000L * parseStringAsMinute(startTimeAsStr);
                        long endTime = currentDate.getTime() + 60000L * parseStringAsMinute(endTimeAsStr);
                        long duration = endTime - startTime;
                        if (duration <= 0) {
                            endTimeEditText.setError(getResources().getString(R.string.error_nonpositive_duration));
                        } else {
                            long period = -1L;
                            if (weeklyRadioButton.isChecked())
                                period = 7 * 86400000L;
                            if (dailyRadioButton.isChecked())
                                period = 86400000L;
                            Limitation limitation = new Limitation(null, name, startTime, period, duration);
                            String timeError = LimitationController.getLimitationTimeError(getContext(), limitation, currentDate);
                            if (timeError == null) {
                                LimitationController.addLimitation(getContext(), limitation);
                                updateDailyPlan();
                                alertDialog.dismiss();
                                Toast.makeText(getActivity(), "افزودن محدودیت با موفقیت انجام شد", Toast.LENGTH_LONG).show();
                            } else {
                                endTimeEditText.setError(timeError);
                            }
                        }
                    }
                }
            }
        });



    }

    @SuppressLint("MissingInflatedId")
    private void showModifyLimitationDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_limitation, null);

        EditText nameEditText = view.findViewById(R.id.name_edittext);
        EditText startTimeEditText = view.findViewById(R.id.start_time_edittext);
        EditText endTimeEditText = view.findViewById(R.id.end_time_edittext);

        Limitation limitation = LimitationController.getLimitationById(getContext(), id);
        nameEditText.setText(limitation.name);
        Date currentStartTime = new Date(limitation.getStartTimeFromDate(currentDate).getTime());
        Date currentEndTime = new Date(limitation.getStartTimeFromDate(currentDate).getTime() + limitation.length);
        startTimeEditText.setText(PersianDateUtil.getTimeString(currentStartTime));
        endTimeEditText.setText(PersianDateUtil.getTimeString(currentEndTime));

        startTimeEditText.setFilters(PersianDateUtil.getTimeInputFilter());
        endTimeEditText.setFilters(PersianDateUtil.getTimeInputFilter());

        RadioGroup periodRadioGroup = view.findViewById(R.id.period_radiogroup);
        RadioButton todayRadioButton = view.findViewById(R.id.today_radiobutton);
        RadioButton weeklyRadioButton = view.findViewById(R.id.weekly_radiobutton);
        RadioButton dailyRadioButton = view.findViewById(R.id.daily_radiobutton);
        weeklyRadioButton.setText("هر " + PersianDateUtil.getDateString(currentDate).split(" ")[0]);

        if (limitation.period == -1L)
            todayRadioButton.setChecked(true);
        else if (limitation.period == 7 * 86400000L)
            weeklyRadioButton.setChecked(true);
        else if (limitation.period == 86400000L)
            dailyRadioButton.setChecked(true);

        builder.setView(view);
        builder.setTitle("ویرایش یا حذف محدودیت");
        builder.setIcon(R.drawable.ic_edit);
        builder.setMessage("این محدودیت را حذف و یا اطلاعات آن را ویرایش کنید");
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ویرایش", (dialog, which) -> {});
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "حذف", (dialog, which) -> {});
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "انصراف", (dialog, which) -> {});
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String name = nameEditText.getText().toString().trim();
            if (name.isEmpty()) {
                nameEditText.setError(getResources().getString(R.string.error_blank_limitation_name));
            } else {
                String startTimeAsStr = startTimeEditText.getText().toString().trim();
                if (startTimeAsStr.isEmpty()) {
                    startTimeEditText.setError(getResources().getString(R.string.error_blank_start_time));
                } else {
                    String endTimeAsStr = endTimeEditText.getText().toString().trim();
                    if (endTimeAsStr.isEmpty()) {
                        endTimeEditText.setError(getResources().getString(R.string.error_blank_end_time));
                    } else {
                        Date prevDate = PersianDateUtil.truncateToStartOfDay(new Date(limitation.offset));
                        long startTime = prevDate.getTime() + 60000L * parseStringAsMinute(startTimeAsStr);
                        long endTime = prevDate.getTime() + 60000L * parseStringAsMinute(endTimeAsStr);
                        long duration = endTime - startTime;
                        if (duration <= 0) {
                            endTimeEditText.setError(getResources().getString(R.string.error_nonpositive_duration));
                        } else {
                            long period = -1L;
                            if (weeklyRadioButton.isChecked())
                                period = 7 * 86400000L;
                            if (dailyRadioButton.isChecked())
                                period = 86400000L;
                            Limitation updatedLimitation = new Limitation(id, name, startTime, period, duration);
                            String timeError = LimitationController.getLimitationTimeError(getContext(), updatedLimitation, currentDate);
                            if (timeError == null) {
                                LimitationController.updateLimitation(getContext(), updatedLimitation, String.valueOf(id));
                                updateDailyPlan();
                                alertDialog.dismiss();
                                Toast.makeText(getActivity(), "ویرایش محدودیت با موفقیت انجام شد", Toast.LENGTH_LONG).show();
                            } else {
                                endTimeEditText.setError(timeError);
                            }
                        }
                    }
                }
            }
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            LimitationController.deleteLimitation(getContext(), String.valueOf(id));
            updateDailyPlan();
            alertDialog.dismiss();
            Toast.makeText(getActivity(), "حذف محدودیت با موفقیت انجام شد", Toast.LENGTH_LONG).show();
        });
    }

    private ArrayList<DailyItem> getPossibleDailyItems(Date date) {
        ArrayList<DailyItem> dailyItems = new ArrayList<>();
        for (Box box: BoxController.getAllUnspecifiedBoxesByDate(getContext(), date)) {
            box.startTime = date.getTime();
            String timeError = BoxController.getBoxTimeError(getContext(), box);
            Date endTime = new Date(date.getTime() + 60000L * box.duration);
            DailyItem dailyItem = new DailyItem(DailyItemType.STUDY, date, endTime, box.id);
            if (timeError != null)
                dailyItem.availablilty = false;
            dailyItems.add(dailyItem);
        }
        return dailyItems;
    }

    @SuppressLint("SetTextI18n")
    private void showModifyBoxStateDialog(int boxId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialog);
        Box box = BoxController.getBoxById(getContext(), boxId);
        builder.setTitle("تغییر وضعیت بسته");
        builder.setIcon(R.drawable.ic_edit);
        builder.setMessage("در صورت انجام بسته، پایان آن را اعلام کنید؛ در غیر این صورت، می\u200Cتوانید برنامه\u200Cریزی مجدد داشته باشید.");
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "اعلام پایان", (dialog, which) -> {});
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "برنامه\u200Cریزی مجدد", (dialog, which) -> {});
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "انصراف", (dialog, which) -> {});
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext(), R.style.MyAlertDialog);
            View view1 = getLayoutInflater().inflate(R.layout.dialog_box_finish_announcement, null);
            TextView efficiencyTextView = view1.findViewById(R.id.efficiency_textview);
            SeekBar efficiencySeekBar = view1.findViewById(R.id.efficiency_seekbar);
            EditText noteEditText = view1.findViewById(R.id.note_edittext);

            efficiencyTextView.setText((100 * efficiencySeekBar.getProgress() / efficiencySeekBar.getMax()) + "%");
            efficiencySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    efficiencyTextView.setText((100 * progress / seekBar.getMax()) + "%");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            noteEditText.setText(box.note);

            builder1.setView(view1);
            builder1.setTitle("اعلام انجام بسته");
            builder1.setIcon(R.drawable.ic_box_done_green);
            builder1.setMessage("کیفیت مطالعه خود را مشخص کرده و در صورت نیاز، بازخورد فعالیت خود را روی توضیحات بسته ثبت کنید.");
            AlertDialog alertDialog1 = builder1.create();
            alertDialog1.setButton(AlertDialog.BUTTON_POSITIVE, "اعلام پایان", (dialog, which) -> {
            });
            alertDialog1.setButton(AlertDialog.BUTTON_NEUTRAL, "انصراف", (dialog, which) -> {
            });
            alertDialog1.show();
            alertDialog1.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                Box finishedBox = new Box(box);
                finishedBox.finished = true;
                finishedBox.note = noteEditText.getText().toString().trim();
                finishedBox.timeSpent = box.duration * efficiencySeekBar.getProgress() / efficiencySeekBar.getMax();
                BoxController.updateBox(getContext(), finishedBox, String.valueOf(box.id));
                updateDailyPlan();
                alertDialog1.dismiss();
                alertDialog.dismiss();
                Toast.makeText(getContext(), "انجام بسته با موفقیت ثبت شد", Toast.LENGTH_LONG).show();
            });
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            Box rescheduledBox = new Box(box);
            rescheduledBox.startTime = -1L;
            BoxController.updateBox(getContext(), rescheduledBox, String.valueOf(box.id));
            updateDailyPlan();
            alertDialog.dismiss();
            Toast.makeText(getContext(), "زمان شروع بسته با موفقیت بازنشانی شد", Toast.LENGTH_LONG).show();
        });

    }

    private void showBoxNoteDialog(int boxId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialog);
        Box box = BoxController.getBoxById(getContext(), boxId);
        builder.setTitle("توضیحات بسته");
        builder.setIcon(R.drawable.ic_info);
        builder.setMessage(box.note.isEmpty() ? "توضیحاتی برای این بسته پیدا نشد ):" : box.note);
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "متوجه شدم", (dialog, which) -> {});
        alertDialog.show();
    }

    private ArrayList<DailyItem> getDailyItems() {
        ArrayList<DailyItem> dailyItems = new ArrayList<>();
        Map<Integer, Long> startTimes = new HashMap<>();

        for (Box box: new ArrayList<>(BoxController.getAllBoxes(getContext()).values())) {
            Date startDate = PersianDateUtil.truncateToStartOfDay(new Date(box.startTime));
            if (startDate.getTime() == currentDate.getTime()) {
                Date startTime = new Date(box.startTime);
                Date endTime = new Date(box.startTime + 60000L * box.duration);
                dailyItems.add(new DailyItem(DailyItemType.STUDY, startTime, endTime, box.id));
                startTimes.put(dailyItems.size() - 1, box.startTime);
            }
        }

        for (Limitation limitation: LimitationController.getLimitationsByDate(getContext(), currentDate)) {
            Date startTime = limitation.getStartTimeFromDate(currentDate);
            Date endTime = new Date(startTime.getTime() + limitation.length);
            dailyItems.add(new DailyItem(DailyItemType.LIMITATION, startTime, endTime, limitation.id));
            startTimes.put(dailyItems.size() - 1, startTime.getTime());
        }

        startTimes = MapUtil.sortByValue(startTimes);
        ArrayList<DailyItem> sortedDailyItems = new ArrayList<>();
        for (Integer index: startTimes.keySet())
            sortedDailyItems.add(dailyItems.get(index));

        for (int i = 0; i < sortedDailyItems.size() - 1; i++) {
            DailyItem dailyItem1 = sortedDailyItems.get(i);
            DailyItem dailyItem2 = sortedDailyItems.get(i + 1);
            if (dailyItem1.endTime.getTime() != dailyItem2.startTime.getTime()) {
                sortedDailyItems.add(i + 1, new DailyItem(DailyItemType.FREE, dailyItem1.endTime, dailyItem2.startTime, -1));
                i += 1;
            }
        }

        if (sortedDailyItems.size() == 0) {
            Date startTime = new Date(currentDate.getTime());
            Date endTime = PersianDateUtil.nextDay(startTime);
            sortedDailyItems.add(new DailyItem(DailyItemType.FREE, startTime, endTime, -1));
        } else {
            DailyItem firstDailyItem = sortedDailyItems.get(0);
            Date startTime = new Date(currentDate.getTime());
            Date endTime = firstDailyItem.startTime;
            if (PersianDateUtil.getMinutesPassedFromStartOfDay(firstDailyItem.startTime) != 0)
                sortedDailyItems.add(0, new DailyItem(DailyItemType.FREE, startTime, endTime, -1));

            DailyItem lastDailyItem = sortedDailyItems.get(sortedDailyItems.size() - 1);
            startTime = lastDailyItem.endTime;
            endTime = PersianDateUtil.nextDay(currentDate);
            if (PersianDateUtil.getMinutesPassedFromStartOfDay(lastDailyItem.endTime) != 0)
                sortedDailyItems.add(new DailyItem(DailyItemType.FREE, startTime, endTime,-1));
        }
        return sortedDailyItems;
    }

    @SuppressLint("NotifyDataSetChanged")
    private void updateDailyPlan() {
        dailyItemsAdapter.mDailyItems = getDailyItems();
        dailyItemsAdapter.notifyDataSetChanged();
    }

    private void getDate(){
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
        int color = typedValue.data;

        PersianDatePickerDialog picker = new PersianDatePickerDialog(getContext())
                .setPositiveButtonString("انتخاب")
                .setNegativeButton("انصراف")
                .setTodayButton("امروز")
                .setTodayButtonVisible(true)
                .setActionTextColor(color)
                .setTitleType(PersianDatePickerDialog.WEEKDAY_DAY_MONTH_YEAR)
                .setListener(new PersianPickerListener() {
                    @Override
                    public void onDateSelected(@NotNull PersianPickerDate persianPickerDate) {
                        Date date = persianPickerDate.getGregorianDate();
                        date = PersianDateUtil.truncateToStartOfDay(date);
                        currentDate = date;
                        currentDateTextView.setText(PersianDateUtil.getDateString(date));
                        updateDailyPlan();
                    }

                    @Override
                    public void onDismissed() {
                    }
                });
        picker.show();
    }

    private void getDate(String when){
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
        int color = typedValue.data;

        PersianDatePickerDialog picker = new PersianDatePickerDialog(getContext())
                .setPositiveButtonString("انتخاب")
                .setNegativeButton("انصراف")
                .setTodayButton("امروز")
                .setTodayButtonVisible(true)
                .setMinYear(PersianDatePickerDialog.THIS_YEAR)
                .setActionTextColor(color)
                .setTitleType(PersianDatePickerDialog.WEEKDAY_DAY_MONTH_YEAR)
                .setListener(new PersianPickerListener() {
                    @Override
                    public void onDateSelected(@NotNull PersianPickerDate persianPickerDate) {
                        Date date = persianPickerDate.getGregorianDate();
                        date = PersianDateUtil.truncateToStartOfDay(date);
                        String dateAsStr = PersianDateUtil.getDateString(date);

                        if (when.equals("start")) {
                            historyStartDateEdittext.setText(dateAsStr);
                            historyStartDate = date;
                        }
                        else if (when.equals("end")) {
                            historyEndDateEdittext.setText(dateAsStr);
                            historyEndDate = date;
                        }
                    }

                    @Override
                    public void onDismissed() {
                    }
                });
        picker.show();
    }

    @SuppressLint("MissingInflatedId")
    private void showStudyHistiryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.MyAlertDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_show_history, null);

        historyStartDateEdittext = view.findViewById(R.id.start_date);
        historyStartDateEdittext.setOnClickListener(v -> getDate("start"));
        historyStartDate = PersianDateUtil.today();
        historyStartDateEdittext.setText(PersianDateUtil.getDateString(historyStartDate));

        historyEndDateEdittext = view.findViewById(R.id.end_date);
        historyEndDateEdittext.setOnClickListener(v -> getDate("end"));
        historyEndDate = PersianDateUtil.today();
        historyEndDateEdittext.setText(PersianDateUtil.getDateString(historyEndDate));

        builder.setView(view);
        builder.setTitle("مشاهده سابقه مطالعاتی");
        builder.setIcon(R.drawable.ic_history);
        builder.setMessage("بازه زمانی مورد نظر را مشخص کنید");
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "مشاهده", (dialog, which) -> {});
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "انصراف", (dialog, which) -> {});
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            if (historyStartDate.getTime() > historyEndDate.getTime()) {
                Toast.makeText(getActivity(), "تاریخ پایان نمی\u200Cتواند قبل از تاریخ شروع باشد", Toast.LENGTH_LONG).show();
            } else {
                alertDialog.dismiss();
                Intent intent = new Intent(getActivity(), StudyActivity.class);
                intent.putExtra("TITLE", "سابقه مطالعاتی");
                intent.putExtra("PLAN_ID", -1);
                intent.putExtra("START_DATE_TIMESTAMP", historyStartDate.getTime());
                intent.putExtra("END_DATE_TIMESTAMP", historyEndDate.getTime() + 86400000L);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int menuId = item.getItemId();
        if (menuId == R.id.show_history)
            showStudyHistiryDialog();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}