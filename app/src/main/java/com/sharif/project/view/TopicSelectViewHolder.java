package com.sharif.project.view;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewHolder;
import com.sharif.project.R;
import com.sharif.project.controller.TopicController;
import com.sharif.project.model.Topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TopicSelectViewHolder extends TreeViewHolder {

    @SuppressLint("StaticFieldLeak")
    public static Context context;
    public static Map<Integer, Boolean> selectionStateById;

    private TextView topic_name_textview;
    private ImageView expansion_state_imageview;
    private CheckBox selection_state_checkbox;

    public TopicSelectViewHolder(@NonNull View itemView) {
        super(itemView);
        initViews();
    }

    private void initViews() {
        topic_name_textview = itemView.findViewById(R.id.topic_name);
        expansion_state_imageview = itemView.findViewById(R.id.expansion_state);
        selection_state_checkbox = itemView.findViewById(R.id.selection_state);
    }

    @Override
    public void bindTreeNode(TreeNode node) {
        super.bindTreeNode(node);

        Topic topic = TopicController.getTopicById(itemView.getContext(), (Integer) node.getValue());
        topic_name_textview.setText(topic.name);

        selectionStateById.putIfAbsent(topic.id, false);
        selection_state_checkbox.setChecked(selectionStateById.get(topic.id));
        selection_state_checkbox.setOnClickListener(v -> {
            selectionStateById.put(topic.id, selection_state_checkbox.isChecked());
        });

        if (node.getChildren().isEmpty()) {
            expansion_state_imageview.setVisibility(View.INVISIBLE);
        } else {
            expansion_state_imageview.setVisibility(View.VISIBLE);
            int stateIcon = node.isExpanded() ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_right;
            expansion_state_imageview.setImageResource(stateIcon);
            if (!node.isExpanded())
                unselectChildren(topic.id, TopicController.getChildrenDict(context));
        }
    }

    private void unselectChildren(int id, Map<Integer, ArrayList<Integer>> childrenDict) {
        for (Integer childId: childrenDict.get(id)) {
            selectionStateById.putIfAbsent(childId, false);
            selectionStateById.put(childId, false);
            unselectChildren(childId, childrenDict);
        }
    }
}
