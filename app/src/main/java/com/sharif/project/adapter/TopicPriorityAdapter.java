package com.sharif.project.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.sharif.project.R;
import com.sharif.project.controller.TopicController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TopicPriorityAdapter extends RecyclerView.Adapter<TopicPriorityAdapter.ViewHolder> {

    public ArrayList<Integer> mTopicsId;
    public Map<Integer, Double> priorityDict;

    public TopicPriorityAdapter(ArrayList<Integer> topicsId) {
        mTopicsId = topicsId;
        priorityDict = new HashMap<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_topic_priority, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0) {
            ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(holder.itemLayout.getLayoutParams());
            marginLayoutParams.setMargins(0, 0, 0, 0);
            holder.itemLayout.setLayoutParams(marginLayoutParams);
        }

        int topicId = mTopicsId.get(position);
        holder.nameTextView.setText(TopicController.getFullNameById(holder.context, topicId));
        holder.priorityTextView.setText(String.valueOf(holder.prioritySeekbar.getProgress()));
        priorityDict.put(topicId, (double) holder.prioritySeekbar.getProgress());
        holder.prioritySeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                holder.priorityTextView.setText(String.valueOf(holder.prioritySeekbar.getProgress()));
                priorityDict.put(topicId, (double) holder.prioritySeekbar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return mTopicsId.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ConstraintLayout itemLayout;
        public TextView nameTextView;
        public TextView priorityTextView;
        public SeekBar prioritySeekbar;
        public Context context;

        public ViewHolder(View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.item_layout);
            nameTextView = itemView.findViewById(R.id.name_textview);
            priorityTextView = itemView.findViewById(R.id.priority_textview);
            prioritySeekbar = itemView.findViewById(R.id.priority_seekbar);
            context = itemView.getContext();
        }
    }
}

