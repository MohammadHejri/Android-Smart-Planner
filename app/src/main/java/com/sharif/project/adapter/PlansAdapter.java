package com.sharif.project.adapter;

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;

import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewAdapter;
import com.amrdeveloper.treeview.TreeViewHolderFactory;
import com.sharif.project.MainActivity;
import com.sharif.project.R;
import com.sharif.project.StudyActivity;
import com.sharif.project.controller.BoxController;
import com.sharif.project.controller.PlanController;
import com.sharif.project.controller.TopicController;
import com.sharif.project.model.Plan;
import com.sharif.project.model.Topic;
import com.sharif.project.util.PersianDateUtil;
import com.sharif.project.view.PlansFragment;
import com.sharif.project.view.TopicSelectViewHolder;
import com.zerobranch.layout.SwipeLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.hamsaa.persiandatepicker.PersianDatePickerDialog;
import ir.hamsaa.persiandatepicker.api.PersianPickerDate;
import ir.hamsaa.persiandatepicker.api.PersianPickerListener;

public class PlansAdapter extends RecyclerView.Adapter<PlansAdapter.ViewHolder> {

    public ArrayList<Plan> mPlans;

    private Date startDate;
    private Date endDate;
    private EditText startDateEdittext;
    private EditText endDateEdittext;

    public PlansAdapter(ArrayList<Plan> plans) {
        mPlans = plans;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_plans_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Plan plan = mPlans.get(position);
        holder.planNameTextView.setText(plan.name);
        holder.planStartDateTextView.setText(PersianDateUtil.getDateString(new Date(plan.startDateTimeStamp)));
        holder.planEndDateTextView.setText(PersianDateUtil.getDateString(new Date(plan.endDateTimeStamp)));

        holder.swipeLayout.setOnActionsListener(new SwipeLayout.SwipeActionsListener() {
            @Override
            public void onOpen(int direction, boolean isContinuous) {
                holder.editImageView.setClickable(true);
                holder.deleteImageView.setClickable(true);
            }

            @Override
            public void onClose() {
                holder.editImageView.setClickable(false);
                holder.deleteImageView.setClickable(false);
            }
        });

        holder.editImageView.setOnClickListener(v -> {
            showEditPlanDialog(holder, position, v.getContext(), plan);
        });

        holder.deleteImageView.setOnClickListener(v -> {
            showDeletePlanDialog(holder, position, v.getContext(), plan);
        });

        holder.editImageView.setClickable(false);
        holder.deleteImageView.setClickable(false);

        holder.progressBar.setProgress((int) (100 * BoxController.getCompletionRatio(holder.context, plan.id)));
    }

    private void showEditPlanDialog(ViewHolder holder, int position, Context context, Plan plan) {
        holder.swipeLayout.close();
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialog);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_add_plan, null);
        EditText planNameEdittext = view.findViewById(R.id.plan_name);
        planNameEdittext.setText(plan.name);

        startDateEdittext = view.findViewById(R.id.plan_start_date);
        startDateEdittext.setOnClickListener(v -> getDate("start", context));
        startDate = new Date(plan.startDateTimeStamp);
        startDateEdittext.setText(PersianDateUtil.getDateString(startDate));

        endDateEdittext = view.findViewById(R.id.plan_end_date);
        endDateEdittext.setOnClickListener(v -> getDate("end", context));
        endDate = new Date(plan.endDateTimeStamp);
        endDateEdittext.setText(PersianDateUtil.getDateString(endDate));

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setNestedScrollingEnabled(false);

        TopicSelectViewHolder.context = context;
        TopicSelectViewHolder.selectionStateById = new HashMap<>();
        TreeViewHolderFactory factory = (v, layout) -> new TopicSelectViewHolder(v);

        TreeViewAdapter treeViewAdapter = new TreeViewAdapter(factory);
        recyclerView.setAdapter(treeViewAdapter);

        ArrayList<Integer> planTopicsId = PlanController.getPlanTopicsID(context, plan.id);
        for (Integer topicId: planTopicsId)
            TopicSelectViewHolder.selectionStateById.put(topicId, true);
        updateTreeNodes(treeViewAdapter, context, planTopicsId);

        builder.setView(view);
        builder.setTitle("ویرایش برنامه");
        builder.setIcon(R.drawable.ic_edit);
        builder.setMessage("نام برنامه و تاریخ شروع و پایان آن را وارد کرده و دروس مورد پوشش را انتخاب کنید");
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ویرایش", (dialog, which) -> {});
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "انصراف", (dialog, which) -> {});
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String planName = planNameEdittext.getText().toString().trim();
            if (planName.isEmpty()) {
                planNameEdittext.setError("نام درس نمی\u200Cتواند خالی باشد");
            } else if (startDate.getTime() > endDate.getTime()) {
                Toast.makeText(context, "تاریخ پایان نمی\u200Cتواند قبل از تاریخ شروع باشد", Toast.LENGTH_LONG).show();
            } else {
                ArrayList<Integer> selectedTopicsId = new ArrayList<>();
                for (Integer id: TopicSelectViewHolder.selectionStateById.keySet())
                    if (TopicSelectViewHolder.selectionStateById.get(id))
                        selectedTopicsId.add(id);
                Plan newPlan = new Plan(plan.id, planName, startDate.getTime(), endDate.getTime());
                PlanController.updatePlan(context, newPlan, String.valueOf(plan.id));
                PlanController.updatePlanTopics(context, plan.id, selectedTopicsId);
                mPlans.set(position, newPlan);
                notifyItemChanged(position);
                alertDialog.dismiss();
                Toast.makeText(context, "ویرایش برنامه با موفقیت انجام شد", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDeletePlanDialog(ViewHolder holder, int position, Context context, Plan plan) {
        holder.swipeLayout.close();
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialog);
        builder.setTitle("حذف برنامه");
        builder.setIcon(R.drawable.ic_delete);
        builder.setMessage("آیا از حذف " + plan.name + " مطمئن هستید؟");
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "حذف", (dialog, which) -> {});
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "انصراف", (dialog, which) -> {});
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
            PlanController.deletePlan(v1.getContext(), String.valueOf(plan.id));
            alertDialog.dismiss();
            mPlans.remove(position);
            notifyItemRemoved(position);
            Toast.makeText(v1.getContext(), "حذف برنامه با موفقیت انجام شد", Toast.LENGTH_LONG).show();
        });
    }

    private void updateTreeNodes(TreeViewAdapter treeViewAdapter, Context context, ArrayList<Integer> expandedTopicsId) {
        List<TreeNode> roots = new ArrayList<>();
        Map<Integer, TreeNode> nodesById = new HashMap<>();
        List<Topic> topics = new ArrayList<>(TopicController.getAllTopics(context).values());
        Map<Integer, Boolean> expansionDict = TopicController.getExpansionDict(context, expandedTopicsId);
        for (Topic topic: topics) {
            TreeNode node = new TreeNode(topic.id, R.layout.layout_topics_selection_item);
            nodesById.put(topic.id, node);
        }
        for (Topic topic: topics) {
            TreeNode node = nodesById.get(topic.id);
            if (topic.parentId == 0)
                roots.add(node);
            else nodesById.get(topic.parentId).addChild(node);
        }
        treeViewAdapter.updateTreeNodes(roots);
        for (Integer topicId: expansionDict.keySet())
            if (expansionDict.get(topicId))
                treeViewAdapter.expandNode(nodesById.get(topicId));
    }

    private void getDate(String when, Context context){
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true);
        int color = typedValue.data;

        PersianDatePickerDialog picker = new PersianDatePickerDialog(context)
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
                            startDateEdittext.setText(dateAsStr);
                            startDate = date;
                        }
                        else if (when.equals("end")) {
                            endDateEdittext.setText(dateAsStr);
                            endDate = date;
                        }
                    }

                    @Override
                    public void onDismissed() {
                    }
                });
        picker.show();
    }

    @Override
    public int getItemCount() {
        return mPlans.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public SwipeLayout swipeLayout;
        public TextView planNameTextView;
        public TextView planStartDateTextView;
        public TextView planEndDateTextView;
        public ImageView editImageView;
        public ImageView deleteImageView;
        public ProgressBar progressBar;
        public Context context;

        public ViewHolder(View itemView) {
            super(itemView);
            swipeLayout = itemView.findViewById(R.id.swipe_layout);
            planNameTextView = itemView.findViewById(R.id.plan_name);
            planStartDateTextView = itemView.findViewById(R.id.plan_start_date);
            planEndDateTextView = itemView.findViewById(R.id.plan_end_date);
            editImageView = itemView.findViewById(R.id.edit_imageview);
            deleteImageView = itemView.findViewById(R.id.delete_imageview);
            progressBar = itemView.findViewById(R.id.progressBar);
            context = itemView.getContext();
        }
    }
}

