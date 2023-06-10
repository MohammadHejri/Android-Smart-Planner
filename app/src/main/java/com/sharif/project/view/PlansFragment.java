package com.sharif.project.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewAdapter;
import com.amrdeveloper.treeview.TreeViewHolderFactory;
import com.sharif.project.MainActivity;
import com.sharif.project.R;
import com.sharif.project.StudyActivity;
import com.sharif.project.adapter.PlansAdapter;
import com.sharif.project.adapter.RecyclerItemClickListener;
import com.sharif.project.controller.PlanController;
import com.sharif.project.controller.TopicController;
import com.sharif.project.databinding.FragmentPlansBinding;
import com.sharif.project.model.Plan;
import com.sharif.project.model.Topic;
import com.sharif.project.util.PersianDateUtil;
import com.sharif.project.util.PersianNumberConverter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.hamsaa.persiandatepicker.PersianDatePickerDialog;
import ir.hamsaa.persiandatepicker.api.PersianPickerDate;
import ir.hamsaa.persiandatepicker.api.PersianPickerListener;
import ir.hamsaa.persiandatepicker.util.PersianCalendar;
import saman.zamani.persiandate.PersianDate;
import saman.zamani.persiandate.PersianDateFormat;

public class PlansFragment extends Fragment {

    private FragmentPlansBinding binding;

    private Date startDate;
    private Date endDate;
    private EditText startDateEdittext;
    private EditText endDateEdittext;

    public PlansAdapter plansAdapter;

    private String TAG;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.plans_fragment_actionbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlansBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ArrayList<Plan> plans = new ArrayList<>(PlanController.getAllPlans(getContext()).values());
        plansAdapter = new PlansAdapter(plans);
        recyclerView.setAdapter(plansAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener
                (getContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        Intent intent = new Intent(getActivity(), StudyActivity.class);
                        intent.putExtra("TITLE", plansAdapter.mPlans.get(position).name);
                        intent.putExtra("PLAN_ID", plansAdapter.mPlans.get(position).id);
                        intent.putExtra("START_DATE_TIMESTAMP", plansAdapter.mPlans.get(position).startDateTimeStamp);
                        intent.putExtra("END_DATE_TIMESTAMP", plansAdapter.mPlans.get(position).endDateTimeStamp);
                        startActivity(intent);
                    }
                }));
        return root;
    }

    private void showAddPlanDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.MyAlertDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_plan, null);
        EditText planNameEdittext = view.findViewById(R.id.plan_name);

        startDateEdittext = view.findViewById(R.id.plan_start_date);
        startDateEdittext.setOnClickListener(v -> getDate("start"));
        startDate = PersianDateUtil.today();
        startDateEdittext.setText(PersianDateUtil.getDateString(startDate));

        endDateEdittext = view.findViewById(R.id.plan_end_date);
        endDateEdittext.setOnClickListener(v -> getDate("end"));
        endDate = PersianDateUtil.today();
        endDateEdittext.setText(PersianDateUtil.getDateString(endDate));

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setNestedScrollingEnabled(false);

        TopicSelectViewHolder.context = getContext();
        TopicSelectViewHolder.selectionStateById = new HashMap<>();
        TreeViewHolderFactory factory = (v, layout) -> new TopicSelectViewHolder(v);

        TreeViewAdapter treeViewAdapter = new TreeViewAdapter(factory);
        recyclerView.setAdapter(treeViewAdapter);
        updateTreeNodes(treeViewAdapter);


        builder.setView(view);
        builder.setTitle("افزودن برنامه جدید");
        builder.setIcon(R.drawable.ic_add);
        builder.setMessage("نام برنامه و تاریخ شروع و پایان آن را وارد کرده و دروس مورد پوشش را انتخاب کنید");
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "افزودن", (dialog, which) -> {});
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "انصراف", (dialog, which) -> {});
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String planName = planNameEdittext.getText().toString().trim();
            if (planName.isEmpty()) {
                planNameEdittext.setError(getResources().getString(R.string.error_blank_plan_name));
            } else if (startDate.getTime() > endDate.getTime()) {
                Toast.makeText(getActivity(), "تاریخ پایان نمی\u200Cتواند قبل از تاریخ شروع باشد", Toast.LENGTH_LONG).show();
            } else {
                ArrayList<Integer> selectedTopicsId = new ArrayList<>();
                for (Integer id: TopicSelectViewHolder.selectionStateById.keySet())
                    if (TopicSelectViewHolder.selectionStateById.get(id))
                        selectedTopicsId.add(id);
                Plan plan = new Plan(null, planName, startDate.getTime(), endDate.getTime());
                int planId = PlanController.addPlan(getContext(), plan);
                PlanController.addPlanTopics(getContext(), planId, selectedTopicsId);
                plan = PlanController.getPlanById(getContext(), planId);
                plansAdapter.mPlans.add(plan);
                plansAdapter.notifyItemInserted(plansAdapter.getItemCount() - 1);
                alertDialog.dismiss();
                Toast.makeText(getActivity(), "افزودن برنامه با موفقیت انجام شد", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateTreeNodes(TreeViewAdapter treeViewAdapter) {
        List<TreeNode> roots = new ArrayList<>();
        Map<Integer, TreeNode> nodesById = new HashMap<>();
        List<Topic> topics = new ArrayList<>(TopicController.getAllTopics(getContext()).values());
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int menuId = item.getItemId();
        if (menuId == R.id.add_plan)
            showAddPlanDialog();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}