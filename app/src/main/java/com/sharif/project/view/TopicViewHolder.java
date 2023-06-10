package com.sharif.project.view;


import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Space;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.amrdeveloper.treeview.TreeNode;
import com.amrdeveloper.treeview.TreeViewHolder;
import com.sharif.project.MainActivity;
import com.sharif.project.R;
import com.sharif.project.controller.TopicController;
import com.sharif.project.model.Topic;

public class TopicViewHolder extends TreeViewHolder {

    private TextView topic_name_textview;
    private ImageView expansion_state_imageview;

    public TopicViewHolder(@NonNull View itemView) {
        super(itemView);
        initViews();
    }

    private void initViews() {
        topic_name_textview = itemView.findViewById(R.id.topic_name);
        expansion_state_imageview = itemView.findViewById(R.id.expansion_state);
    }

    @Override
    public void bindTreeNode(TreeNode node) {
        super.bindTreeNode(node);

        Topic topic = TopicController.getTopicById(itemView.getContext(), (Integer) node.getValue());
        topic_name_textview.setText(topic.name);

        if (node.getChildren().isEmpty()) {
            expansion_state_imageview.setVisibility(View.INVISIBLE);
        } else {
            expansion_state_imageview.setVisibility(View.VISIBLE);
            int stateIcon = node.isExpanded() ? R.drawable.ic_arrow_down : R.drawable.ic_arrow_right;
            expansion_state_imageview.setImageResource(stateIcon);
        }
    }
}
