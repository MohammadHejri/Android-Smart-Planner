package com.sharif.project.view;


import android.annotation.SuppressLint;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewHolder;
import com.sharif.project.R;
import com.sharif.project.controller.BoxController;
import com.sharif.project.controller.TopicController;
import com.sharif.project.model.Box;
import com.sharif.project.model.StudyNode;
import com.sharif.project.model.Topic;

public class StudyBoxViewHolder extends TreeViewHolder {

    private TextView topicNameTextview;
    private TextView startDateTextview;
    private TextView totalTimeTextview;
    private TextView durationTextview;
    private TextView efficiencyTextview;
    private ImageView finishedStateImageview;
    private ImageView expansiontateImageview;

    public StudyBoxViewHolder(@NonNull View itemView) {
        super(itemView);
        initViews();
    }

    private void initViews() {
        topicNameTextview = itemView.findViewById(R.id.topic_name_textview);
        startDateTextview = itemView.findViewById(R.id.start_date_textview);
        totalTimeTextview = itemView.findViewById(R.id.total_time_textview);
        durationTextview = itemView.findViewById(R.id.duration_textview);
        efficiencyTextview = itemView.findViewById(R.id.efficiency_textview);
        finishedStateImageview = itemView.findViewById(R.id.finished_state_imageview);
        expansiontateImageview = itemView.findViewById(R.id.expansion_state_imageview);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void bindTreeNode(TreeNode node) {
        super.bindTreeNode(node);

        StudyNode studyNode = (StudyNode) node.getValue();

        Box box = BoxController.getBoxById(itemView.getContext(), studyNode.boxId);

        if (topicNameTextview != null)
            topicNameTextview.setText(studyNode.name);

        if (startDateTextview != null)
            startDateTextview.setText(studyNode.name);

        if (totalTimeTextview != null) {
            int totalTime = BoxController.getTotalTimeInRange(itemView.getContext(), studyNode.planId, studyNode.topicId, studyNode.start, studyNode.end);
            int hour = totalTime / 60;
            int min = totalTime % 60;
            String hourStr = hour > 0 ? hour + " ساعت" : "";
            String minStr = min > 0 ? min + " دقیقه" : "";
            String totalTimeAsStr = hourStr.isEmpty() ? minStr : minStr.isEmpty() ? hourStr : hourStr + " و " + minStr;
            if (totalTimeAsStr.isEmpty())
                totalTimeAsStr = "هنوز مطالعه نشده است";
            totalTimeTextview.setText(totalTimeAsStr);
        }

        if (durationTextview != null) {
            int hour = box.duration / 60;
            int min = box.duration % 60;
            String hourStr = hour > 0 ? hour + " ساعت" : "";
            String minStr = min > 0 ? min + " دقیقه" : "";
            durationTextview.setText(hourStr.isEmpty() ? minStr : minStr.isEmpty() ? hourStr : hourStr + "\n" + minStr);
            durationTextview.getBackground().setLevel(10000 * box.timeSpent / box.duration);
        }

        if (efficiencyTextview != null) {
            double efficiency = BoxController.getEfficiency(itemView.getContext(), studyNode.planId, studyNode.topicId, studyNode.start, studyNode.end);
            String efficiencyAsStr = (int) (100 * efficiency) + "%";
            if (efficiency == -1)
                efficiencyAsStr = "تازه";
            efficiencyTextview.setText(efficiencyAsStr);
        }

        if (finishedStateImageview != null)
            finishedStateImageview.setImageResource(box.finished ? R.drawable.ic_box_done : R.drawable.ic_box_pending);

        if (studyNode.topicId != -1) {
            if (node.getChildren().isEmpty()) {
                expansiontateImageview.setVisibility(View.GONE);
            } else {
                expansiontateImageview.setVisibility(View.VISIBLE);
                int stateIcon = node.isExpanded() ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_right;
                expansiontateImageview.setImageResource(stateIcon);
            }
        }
    }
}
