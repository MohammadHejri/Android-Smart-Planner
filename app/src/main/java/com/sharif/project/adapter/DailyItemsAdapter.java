package com.sharif.project.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sharif.project.R;
import com.sharif.project.controller.BoxController;
import com.sharif.project.controller.LimitationController;
import com.sharif.project.controller.TopicController;
import com.sharif.project.model.Box;
import com.sharif.project.model.DailyItem;
import com.sharif.project.model.DailyItemType;
import com.sharif.project.model.Limitation;
import com.sharif.project.util.PersianDateUtil;

import java.util.ArrayList;

public class DailyItemsAdapter extends RecyclerView.Adapter<DailyItemsAdapter.ViewHolder> {

    public ArrayList<DailyItem> mDailyItems;

    public DailyItemsAdapter(ArrayList<DailyItem> dailyItems) {
        mDailyItems = dailyItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.layout_daily_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position == 0) {
            ViewGroup.MarginLayoutParams marginLayoutParams = new ViewGroup.MarginLayoutParams(holder.itemLayout.getLayoutParams());
            marginLayoutParams.setMargins(0, 0, 0, 0);
            holder.itemLayout.setLayoutParams(marginLayoutParams);
        }

        DailyItem dailyItem = mDailyItems.get(position);

        if (!dailyItem.availablilty)
            holder.descriptionTextView.setPaintFlags(holder.descriptionTextView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
            holder.descriptionTextView.setPaintFlags(holder.descriptionTextView.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);

        holder.startTimeTextView.setText(PersianDateUtil.getTimeString(dailyItem.startTime));
        holder.endTimeTextView.setText(PersianDateUtil.getTimeString(dailyItem.endTime));

        if (dailyItem.type == DailyItemType.FREE) {
            holder.typeImageView.setImageResource(R.drawable.ic_free_time);
            holder.descriptionTextView.setTextColor(ContextCompat.getColor(holder.context, R.color.blue_900));
            holder.descriptionTextView.setText("استراحت");
        } else if (dailyItem.type == DailyItemType.LIMITATION) {
            Limitation limitation = LimitationController.getLimitationById(holder.context, dailyItem.id);
            holder.typeImageView.setImageResource(R.drawable.ic_limitation);
            holder.descriptionTextView.setTextColor(ContextCompat.getColor(holder.context, R.color.red_900));
            holder.descriptionTextView.setText(limitation.name);
        } else if (dailyItem.type == DailyItemType.STUDY) {
            Box box = BoxController.getBoxById(holder.context, dailyItem.id);
            if (box.finished) {
                holder.typeImageView.setImageResource(R.drawable.ic_daily_box_done);
                holder.descriptionTextView.setTextColor(ContextCompat.getColor(holder.context, R.color.green_900));
            } else {
                holder.typeImageView.setImageResource(R.drawable.ic_daily_box_pending);
                holder.descriptionTextView.setTextColor(ContextCompat.getColor(holder.context, R.color.yellow_900));
            }
            holder.descriptionTextView.setText(TopicController.getFullNameById(holder.context, box.topicId));
        }

    }

    @Override
    public int getItemCount() {
        return mDailyItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ConstraintLayout itemLayout;
        public TextView startTimeTextView;
        public TextView endTimeTextView;
        public TextView descriptionTextView;
        public ImageView typeImageView;
        public Context context;

        public ViewHolder(View itemView) {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.item_layout);
            startTimeTextView = itemView.findViewById(R.id.start_time_textview);
            endTimeTextView = itemView.findViewById(R.id.end_time_textview);
            descriptionTextView = itemView.findViewById(R.id.description_textview);
            typeImageView = itemView.findViewById(R.id.type_imageview);
            context = itemView.getContext();
        }
    }
}

