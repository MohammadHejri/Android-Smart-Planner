package com.sharif.project;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewAdapter;
import com.amrdeveloper.treeview.TreeViewHolderFactory;
import com.sharif.project.adapter.DailyItemsAdapter;
import com.sharif.project.adapter.TopicPriorityAdapter;
import com.sharif.project.controller.BoxController;
import com.sharif.project.controller.PlanController;
import com.sharif.project.controller.TopicController;
import com.sharif.project.model.Box;
import com.sharif.project.model.DateSearchModel;
import com.sharif.project.model.StudyNode;
import com.sharif.project.model.Topic;
import com.sharif.project.util.PersianDateUtil;
import com.sharif.project.view.StudyBoxViewHolder;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;

public class StudyActivity extends AppCompatActivity {

    private String TITLE;
    private int PLAN_ID;
    private Long START_DATE_TIMESTAMP;
    private Long END_DATE_TIMESTAMP;

    private TreeViewAdapter treeViewAdapter;

    private Date startDate;
    private EditText startDateEditText;
    private TextView totalTimeTextView;
    private TextView remainingTimeTextView;
    private ProgressBar progressBar;

    @Override
    @SuppressLint({"MissingInflatedId", "LocalSuppress"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            TITLE = extras.getString("TITLE");
            PLAN_ID = extras.getInt("PLAN_ID");
            START_DATE_TIMESTAMP = extras.getLong("START_DATE_TIMESTAMP");
            END_DATE_TIMESTAMP = extras.getLong("END_DATE_TIMESTAMP");
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setTitle(TITLE);

        TextView startDateTextView = findViewById(R.id.start_date);
        TextView endDateTextView = findViewById(R.id.end_date);
        startDateTextView.setText(PersianDateUtil.getDateString(new Date(START_DATE_TIMESTAMP)));
        endDateTextView.setText(PersianDateUtil.getDateString(new Date(END_DATE_TIMESTAMP)));

        totalTimeTextView = findViewById(R.id.total_time_textview);
        remainingTimeTextView = findViewById(R.id.remaining_time_textview);
        totalTimeTextView.setText(getPlanTotalTime());
        remainingTimeTextView.setText(getPlanRemainingTime());

        progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress(getProgress());

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setNestedScrollingEnabled(false);

        TreeViewHolderFactory factory = (v, layout) -> new StudyBoxViewHolder(v);

        treeViewAdapter = new TreeViewAdapter(factory);
        recyclerView.setAdapter(treeViewAdapter);

        updateTreeNodes();

        treeViewAdapter.setTreeNodeLongClickListener((treeNode, nodeView) -> {
            int topicId = ((StudyNode) treeNode.getValue()).topicId;
            int boxId = ((StudyNode) treeNode.getValue()).boxId;
            if (topicId != -1 && PLAN_ID != -1)
                showAddBoxDialog(topicId);
            if (boxId != -1 && PLAN_ID != -1)
                showModifyBoxDialog(boxId);
            return true;
        });

        if (PLAN_ID == -1)
            findViewById(R.id.linearLayout7).setVisibility(View.GONE);

        LinearLayout createBoxLinearLayout = findViewById(R.id.create_box_linearlayout);
        createBoxLinearLayout.setOnClickListener(v -> {
            showAutoCreateBoxDialog();
        });
    }

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    private void showAutoCreateBoxDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_auto_create_box, null);

        EditText capacityEditText = view.findViewById(R.id.capacity_edittext);
        EditText maxDurationEditText = view.findViewById(R.id.max_duration_edittext);
        TextView prevStudyImportanceTextView = view.findViewById(R.id.prev_study_importance_textview);
        SeekBar prevStudyImportanceSeekBar = view.findViewById(R.id.prev_study_importance_seekbar);

        prevStudyImportanceSeekBar.setProgress(0);
        prevStudyImportanceTextView.setText("0%");
        prevStudyImportanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prevStudyImportanceTextView.setText(progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        capacityEditText.setFilters(PersianDateUtil.getHourInputFilter());
        maxDurationEditText.setFilters(PersianDateUtil.getTimeInputFilter());

        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        ArrayList<Integer> topicsId = PlanController.getPlanTopicsID(this, PLAN_ID);
        TopicPriorityAdapter topicPriorityAdapter = new TopicPriorityAdapter(topicsId);
        recyclerView.setAdapter(topicPriorityAdapter);

        builder.setView(view);
        builder.setTitle("ایجاد خودکار بسته\u200Cها");
        builder.setIcon(R.drawable.ic_add);
        builder.setMessage("اطلاعات مربوط به مطالعه خود را وارد کنید");
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ایجاد", (dialog, which) -> {});
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "انصراف", (dialog, which) -> {});
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String capacityStr = capacityEditText.getText().toString();
            String maxDurationStr = maxDurationEditText.getText().toString();
            int maxDuration = 0;
            try {
                maxDuration += 60 * Integer.parseInt(maxDurationStr.split(":")[0]);
                maxDuration += Integer.parseInt(maxDurationStr.split(":")[1]);
            } catch (Exception e) {
                if (maxDurationStr.endsWith(":"))
                    maxDurationStr = maxDurationStr.substring(0, maxDurationStr.length() - 1);
                maxDuration = 60 * Integer.parseInt(maxDurationStr);
            }
            if (capacityStr.isEmpty()) {
                capacityEditText.setError("ظرفیت مطالعه نمی\u200Cتواند خالی باشد");
            } else if (maxDuration < 15) {
                maxDurationEditText.setError("حداکثر زمان بسته\u200Cها نمی\u200Cتواند کمتر از 30 دقیقه باشد");
            } else {
                int capacity = Integer.parseInt(capacityStr) * 60;
                double prevStudyImportance = (double) prevStudyImportanceSeekBar.getProgress() / prevStudyImportanceSeekBar.getMax();
                BoxController.autoCreateBox(this, PLAN_ID, capacity, maxDuration, prevStudyImportance, topicPriorityAdapter.priorityDict);
                updateTreeNodes();
                alertDialog.dismiss();
                totalTimeTextView.setText(getPlanTotalTime());
                remainingTimeTextView.setText(getPlanRemainingTime());
                progressBar.setProgress(getProgress());
                Toast.makeText(this, "ایجاد خودکار بسته\u200Cها با موفقیت انجام شد", Toast.LENGTH_LONG).show();
            }
        });
    }

    @SuppressLint("SimpleDateFormat")
    private void updateTreeNodes() {
        List<TreeNode> roots = new ArrayList<>();
        Map<Integer, TreeNode> nodesById = new HashMap<>();
        Map<Integer, Topic> topicsById = TopicController.getAllTopics(this);
        Date start = PLAN_ID == -1 ? new Date(START_DATE_TIMESTAMP) : null;
        Date end = PLAN_ID == -1 ? new Date(END_DATE_TIMESTAMP) : null;

        List<Integer> selectedTopicsId = PlanController.getPlanTopicsID(this, PLAN_ID);
        List<Topic> topics = new ArrayList<>();
        for (Integer topicId : topicsById.keySet())
            if (selectedTopicsId.contains(topicId))
                topics.add(topicsById.get(topicId));

        if (PLAN_ID == -1)
            topics = new ArrayList<>(TopicController.getAllTopics(this).values());

        for (Topic topic : topics)
            nodesById.put(topic.id, null);

        for (Topic topic : topics) {
            String hierarchicalName = topic.name;
            int parentId = topic.parentId;
            while (parentId != 0 && nodesById.get(parentId) == null) {
                Topic parentTopic = topicsById.get(parentId);
                hierarchicalName = parentTopic.name + " › " + hierarchicalName;
                parentId = parentTopic.parentId;
            }
            StudyNode studyNode = new StudyNode(PLAN_ID, topic.id, -1, hierarchicalName, start, end);
            TreeNode node = new TreeNode(studyNode, R.layout.layout_study_topic_item);
            nodesById.put(topic.id, node);
            if (parentId == 0)
                roots.add(node);
            else nodesById.get(parentId).addChild(node);
        }

        for (Topic topic : topics) {
            ArrayList<Box> boxes = BoxController.getAllBoxesById(this, PLAN_ID, topic.id);
            for (Box box : boxes) {
                String timeAsStr = "زمان شروع نامشخص";
                if (box.startTime != -1L) {
                    timeAsStr = PersianDateUtil.getDateString(new Date(box.startTime));
                    timeAsStr += " - ";
                    timeAsStr += PersianDateUtil.getTimeString(new Date(box.startTime));
                }
                StudyNode studyNode = new StudyNode(PLAN_ID, -1, box.id, timeAsStr, start, end);
                TreeNode node = new TreeNode(studyNode, R.layout.layout_study_box_item);
                if (PLAN_ID != -1 || (box.startTime != -1L && start.getTime() <= box.startTime && box.startTime < end.getTime()))
                    nodesById.get(topic.id).addChild(node);
            }
        }

        treeViewAdapter.updateTreeNodes(roots);
    }

    @SuppressLint({"MissingInflatedId", "LocalSuppress"})
    private void showAddBoxDialog(int topicId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_box, null);
        EditText durationEditText = view.findViewById(R.id.duration_edittext);
        startDateEditText = view.findViewById(R.id.start_date_edittext);
        EditText startTimeEditText = view.findViewById(R.id.start_time_edittext);
        EditText noteEditText = view.findViewById(R.id.note_edittext);

        startDateEditText.setEnabled(true);
        startTimeEditText.setEnabled(false);
        startDateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                startTimeEditText.setEnabled(!startDateEditText.getText().toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        startDateEditText.setOnClickListener(v -> {
            startTimeEditText.setText("");
            showSearchDateDialog();
        });
        startDate = null;

        durationEditText.setFilters(PersianDateUtil.getTimeInputFilter());
        startTimeEditText.setFilters(PersianDateUtil.getTimeInputFilter());

        builder.setView(view);

        builder.setTitle("افزودن بسته جدید");
        builder.setIcon(R.drawable.ic_add);
        builder.setMessage("اطلاعات مربوط به بسته را وارد کنید");
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "افزودن", (dialog, which) -> {
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "انصراف", (dialog, which) -> {
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "بازنشانی زمان شروع", (dialog, which) -> {
        });
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
            startDate = null;
            startDateEditText.setText("");
            startTimeEditText.setText("");
        });
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String durationAsStr = durationEditText.getText().toString();
            if (durationAsStr.isEmpty()) {
                durationEditText.setError("مدت زمان نمی\u200Cتواند خالی باشد");
            } else {
                int minute = 0;
                try {
                    minute += 60 * Integer.parseInt(durationAsStr.split(":")[0]);
                    minute += Integer.parseInt(durationAsStr.split(":")[1]);
                } catch (Exception e) {
                    if (durationAsStr.endsWith(":"))
                        durationAsStr = durationAsStr.substring(0, durationAsStr.length() - 1);
                    minute = 60 * Integer.parseInt(durationAsStr);
                }
                if (minute == 0) {
                    durationEditText.setError("مدت زمان نمی\u200Cتواند برابر صفر باشد");
                } else {
                    String note = noteEditText.getText().toString().trim();
                    String startTimeAsStr = startTimeEditText.getText().toString();
                    int minute2 = -1;
                    if (startDate != null) {
                        if (startTimeAsStr.isEmpty()) {
                            startTimeEditText.setError("ساعت شروع نمی\u200Cتواند خالی باشد");
                        } else {
                            minute2 = 0;
                            try {
                                minute2 += 60 * Integer.parseInt(startTimeAsStr.split(":")[0]);
                                minute2 += Integer.parseInt(startTimeAsStr.split(":")[1]);
                            } catch (Exception e) {
                                if (startTimeAsStr.endsWith(":"))
                                    startTimeAsStr = startTimeAsStr.substring(0, startTimeAsStr.length() - 1);
                                minute2 = 60 * Integer.parseInt(startTimeAsStr);
                            }
                        }
                    }
                    if (startDate == null || minute2 != -1) {
                        long startTime = startDate == null ? -1L : startDate.getTime() + 60000L * minute2;
                        Box box = new Box(null, PLAN_ID, topicId, minute, 0, startTime, note, false);
                        String timeError = BoxController.getBoxTimeError(this, box);
                        if (timeError == null) {
                            BoxController.addBox(this, box);
                            updateTreeNodes();
                            alertDialog.dismiss();
                            totalTimeTextView.setText(getPlanTotalTime());
                            remainingTimeTextView.setText(getPlanRemainingTime());
                            progressBar.setProgress(getProgress());
                            Toast.makeText(this, "افزودن بسته با موفقیت انجام شد", Toast.LENGTH_LONG).show();
                        } else {
                            startTimeEditText.setError(timeError);
                        }
                    }
                }
            }
        });
    }

    @SuppressLint({"MissingInflatedId", "LocalSuppress", "SetTextI18n", "SimpleDateFormat"})
    private void showModifyBoxDialog(int boxId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.MyAlertDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_box_management, null);
        EditText durationEditText = view.findViewById(R.id.duration_edittext);
        startDateEditText = view.findViewById(R.id.start_date_edittext);
        EditText startTimeEditText = view.findViewById(R.id.start_time_edittext);
        EditText noteEditText = view.findViewById(R.id.note_edittext);
        TextView finishedTextview = view.findViewById(R.id.finished_textview);

        startDateEditText.setEnabled(true);
        Box box = BoxController.getBoxById(this, boxId);

        String hour = String.valueOf(box.duration / 60);
        String min = String.valueOf(box.duration % 60);
        durationEditText.setText(hour + (min.equals("0") ? "" : ":" + ("00" + min).substring(min.length())));

        if (box.startTime == -1L) {
            startTimeEditText.setEnabled(false);
            startDate = null;
        } else {
            startDate = PersianDateUtil.truncateToStartOfDay(new Date(box.startTime));
            startDateEditText.setText(PersianDateUtil.getDateString(startDate));
            startTimeEditText.setText(new SimpleDateFormat("HH:mm").format(new Date(box.startTime)));
        }

        noteEditText.setText(box.note);

        startDateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                startTimeEditText.setEnabled(!startDateEditText.getText().toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        startDateEditText.setOnClickListener(v -> {
            startTimeEditText.setText("");
            showSearchDateDialog();
        });

        durationEditText.setFilters(PersianDateUtil.getTimeInputFilter());
        startTimeEditText.setFilters(PersianDateUtil.getTimeInputFilter());

        builder.setView(view);
        builder.setTitle("ویرایش یا حذف بسته");
        builder.setIcon(R.drawable.ic_edit);
        builder.setMessage("اطلاعات مربوط به بسته را وارد کنید");
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ویرایش", (dialog, which) -> {
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "حذف", (dialog, which) -> {
        });
        if (!box.finished) {
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "بازنشانی زمان شروع", (dialog, which) -> {
            });
        }
        alertDialog.show();
        if (!box.finished) {
            alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v -> {
                startDate = null;
                startDateEditText.setText("");
                startTimeEditText.setText("");
            });
        }
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String durationAsStr = durationEditText.getText().toString();
            if (durationAsStr.isEmpty()) {
                durationEditText.setError("مدت زمان نمی\u200Cتواند خالی باشد");
            } else {
                int minute = 0;
                try {
                    minute += 60 * Integer.parseInt(durationAsStr.split(":")[0]);
                    minute += Integer.parseInt(durationAsStr.split(":")[1]);
                } catch (Exception e) {
                    if (durationAsStr.endsWith(":"))
                        durationAsStr = durationAsStr.substring(0, durationAsStr.length() - 1);
                    minute = 60 * Integer.parseInt(durationAsStr);
                }
                if (minute == 0) {
                    durationEditText.setError("مدت زمان نمی\u200Cتواند برابر صفر باشد");
                } else {
                    String note = noteEditText.getText().toString().trim();
                    String startTimeAsStr = startTimeEditText.getText().toString();
                    int minute2 = -1;
                    if (startDate != null) {
                        if (startTimeAsStr.isEmpty()) {
                            startTimeEditText.setError("ساعت شروع نمی\u200Cتواند خالی باشد");
                        } else {
                            minute2 = 0;
                            try {
                                minute2 += 60 * Integer.parseInt(startTimeAsStr.split(":")[0]);
                                minute2 += Integer.parseInt(startTimeAsStr.split(":")[1]);
                            } catch (Exception e) {
                                if (startTimeAsStr.endsWith(":"))
                                    startTimeAsStr = startTimeAsStr.substring(0, startTimeAsStr.length() - 1);
                                minute2 = 60 * Integer.parseInt(startTimeAsStr);
                            }
                        }
                    }
                    if (startDate == null || minute2 != -1) {
                        long startTime = startDate == null ? -1L : startDate.getTime() + 60000L * minute2;
                        Box newBox = new Box(null, PLAN_ID, box.topicId, minute, box.timeSpent, startTime, note, false);
                        String timeError = BoxController.getBoxTimeError(this, newBox);
                        if (timeError == null) {
                            BoxController.updateBox(this, newBox, String.valueOf(box.id));
                            updateTreeNodes();
                            alertDialog.dismiss();
                            totalTimeTextView.setText(getPlanTotalTime());
                            remainingTimeTextView.setText(getPlanRemainingTime());
                            progressBar.setProgress(getProgress());
                            Toast.makeText(this, "ویرایش بسته با موفقیت انجام شد", Toast.LENGTH_LONG).show();
                        } else {
                            startTimeEditText.setError(timeError);
                        }
                    }
                }
            }
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            BoxController.deleteBox(this, String.valueOf(boxId));
            updateTreeNodes();
            alertDialog.dismiss();
            totalTimeTextView.setText(getPlanTotalTime());
            remainingTimeTextView.setText(getPlanRemainingTime());
            progressBar.setProgress(getProgress());
            Toast.makeText(this, "حذف بسته با موفقیت انجام شد", Toast.LENGTH_LONG).show();
        });

        if (box.finished) {
            durationEditText.setEnabled(false);
            startDateEditText.setEnabled(false);
            startTimeEditText.setEnabled(false);
            finishedTextview.setEnabled(false);
            finishedTextview.setText("این بسته انجام شده است (:");
        }

        if (box.startTime == -1)
            finishedTextview.setVisibility(View.GONE);

        finishedTextview.setOnClickListener(v -> {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this, R.style.MyAlertDialog);
            View view1 = getLayoutInflater().inflate(R.layout.dialog_box_finish_announcement, null);
            TextView efficiencyTextView = view1.findViewById(R.id.efficiency_textview);
            SeekBar efficiencySeekBar = view1.findViewById(R.id.efficiency_seekbar);
            EditText noteEditText2 = view1.findViewById(R.id.note_edittext);

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
            noteEditText2.setText(box.note);

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
                finishedBox.note = noteEditText2.getText().toString().trim();
                finishedBox.timeSpent = box.duration * efficiencySeekBar.getProgress() / efficiencySeekBar.getMax();
                BoxController.updateBox(this, finishedBox, String.valueOf(box.id));
                updateTreeNodes();
                alertDialog1.dismiss();
                alertDialog.dismiss();
                totalTimeTextView.setText(getPlanTotalTime());
                remainingTimeTextView.setText(getPlanRemainingTime());
                progressBar.setProgress(getProgress());
                Toast.makeText(this, "انجام بسته با موفقیت ثبت شد", Toast.LENGTH_LONG).show();
            });
        });
    }

    private ArrayList<DateSearchModel> getDateSearchItems() {
        ArrayList<DateSearchModel> dateSearchModels = new ArrayList<>();
        for (long timeStamp = START_DATE_TIMESTAMP; timeStamp <= END_DATE_TIMESTAMP; timeStamp += 86400000) {
            String dateAsStr = PersianDateUtil.getDateString(new Date(timeStamp));
            dateSearchModels.add(new DateSearchModel(new Date(timeStamp), dateAsStr));
        }
        return dateSearchModels;
    }

    private void showSearchDateDialog() {
        startDateEditText.setText("");
        startDate = null;
        SimpleSearchDialogCompat simpleSearchDialogCompat = new SimpleSearchDialogCompat(
                this, "تاریخ شروع", "جستجو کنید...", null, getDateSearchItems(),
                (SearchResultListener<DateSearchModel>) (dialog, item, position) -> {
                    startDateEditText.setText(item.getTitle());
                    startDate = item.mDate;
                    dialog.dismiss();
                });
        simpleSearchDialogCompat.show();
        EditText searchBox = simpleSearchDialogCompat.getSearchBox();
        searchBox.setSingleLine();
    }

    private String getPlanTotalTime() {
        Date start = PLAN_ID == -1 ? new Date(START_DATE_TIMESTAMP) : null;
        Date end = PLAN_ID == -1 ? new Date(END_DATE_TIMESTAMP) : null;
        int totalTime = BoxController.getTotalTimeInRange(this, PLAN_ID, -1, start, end);
        int hour = totalTime / 60;
        int min = totalTime % 60;
        String hourStr = hour > 0 ? hour + " ساعت" : "";
        String minStr = min > 0 ? min + " دقیقه" : "";
        String totalTimeAsStr = hourStr.isEmpty() ? minStr : minStr.isEmpty() ? hourStr : hourStr + " و " + minStr;
        if (totalTimeAsStr.isEmpty())
            totalTimeAsStr = "0 ساعت";
        return totalTimeAsStr;
    }

    private String getPlanRemainingTime() {
        Date start = PLAN_ID == -1 ? new Date(START_DATE_TIMESTAMP) : null;
        Date end = PLAN_ID == -1 ? new Date(END_DATE_TIMESTAMP) : null;
        int remainingTime = BoxController.getRemainingTimeInRange(this, PLAN_ID, -1, start, end);
        int hour = remainingTime / 60;
        int min = remainingTime % 60;
        String hourStr = hour > 0 ? hour + " ساعت" : "";
        String minStr = min > 0 ? min + " دقیقه" : "";
        String remainingTimeAsStr = hourStr.isEmpty() ? minStr : minStr.isEmpty() ? hourStr : hourStr + " و " + minStr;
        if (remainingTimeAsStr.isEmpty())
            remainingTimeAsStr = "0 ساعت";
        return remainingTimeAsStr;
    }

    private int getProgress() {
        return (int) (100 * BoxController.getCompletionRatio(this, PLAN_ID));
    }
}