package com.sharif.project.view;

import android.os.Bundle;
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
import com.sharif.project.R;
import com.sharif.project.controller.TopicController;
import com.sharif.project.databinding.FragmentTopicsBinding;
import com.sharif.project.model.Topic;
import com.sharif.project.model.TopicSearchModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.mirrajabi.searchdialog.SimpleSearchDialogCompat;
import ir.mirrajabi.searchdialog.core.SearchResultListener;

public class TopicsFragment extends Fragment {

    private FragmentTopicsBinding binding;

    private TreeViewAdapter treeViewAdapter;

    private EditText parentEdittext;
    private int selectedParentId = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.topics_fragment_actionbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentTopicsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setNestedScrollingEnabled(false);

        TreeViewHolderFactory factory = (v, layout) -> new TopicViewHolder(v);

        treeViewAdapter = new TreeViewAdapter(factory);
        recyclerView.setAdapter(treeViewAdapter);

        updateTreeNodes();

        treeViewAdapter.setTreeNodeLongClickListener((treeNode, nodeView) -> {
            int topicId = (int) treeNode.getValue();
            showModifyTopicDialog(topicId);
            return true;
        });

        return root;
    }

    private void updateTreeNodes() {
        List<TreeNode> roots = new ArrayList<>();
        Map<Integer, TreeNode> nodesById = new HashMap<>();
        List<Topic> topics = new ArrayList<>(TopicController.getAllTopics(getContext()).values());
        for (Topic topic: topics) {
            TreeNode node = new TreeNode(topic.id, R.layout.layout_topics_item);
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

    private ArrayList<TopicSearchModel> getTopicSearchItems(int exception) {
        ArrayList<TopicSearchModel> topicSearchModels = new ArrayList<>();
        HashMap<Integer, String> hierarchicalInfoDict = (HashMap<Integer, String>) TopicController.getHierarchicalInfoDict(getContext(), exception);
        for (Integer id: hierarchicalInfoDict.keySet())
            topicSearchModels.add(new TopicSearchModel(id, hierarchicalInfoDict.get(id)));
        return topicSearchModels;
    }

    private void showSearchParentDialog(int exception) {
        parentEdittext.setText("");
        selectedParentId = 0;
        SimpleSearchDialogCompat simpleSearchDialogCompat = new SimpleSearchDialogCompat(
                getContext(), "اطلاعات سلسله مراتبی", "جستجو کنید...", null, getTopicSearchItems(exception),
                (SearchResultListener<TopicSearchModel>) (dialog, item, position) -> {
                    selectedParentId = item.mTopicId;
                    parentEdittext.setText(item.getTitle());
                    dialog.dismiss();
                });
        simpleSearchDialogCompat.show();
        EditText parentEditText = simpleSearchDialogCompat.getSearchBox();
        parentEditText.setSingleLine();
    }

    private void showAddTopicDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.MyAlertDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_topic_management, null);
        EditText topicNameEdittext = view.findViewById(R.id.topic_name);
        parentEdittext = view.findViewById(R.id.topic_parent);
        parentEdittext.setOnClickListener(v -> showSearchParentDialog(-1));
        selectedParentId = 0;

        builder.setView(view);
        builder.setTitle("افزودن درس جدید");
        builder.setIcon(R.drawable.ic_add);
        builder.setMessage("نام درس و اطلاعات سلسله مراتبی آن را وارد کنید");
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "افزودن", (dialog, which) -> {});
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "انصراف", (dialog, which) -> {});
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String topicName = topicNameEdittext.getText().toString().trim();
            if (topicName.isEmpty()) {
                topicNameEdittext.setError(getResources().getString(R.string.error_blank_topic_name));
            } else {
                Topic topic = new Topic(null, topicName, selectedParentId);
                TopicController.addTopic(getContext(), topic);
                updateTreeNodes();
                alertDialog.dismiss();
                Toast.makeText(getActivity(), "افزودن درس با موفقیت انجام شد", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showModifyTopicDialog(int topic_id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.MyAlertDialog);
        View view = getLayoutInflater().inflate(R.layout.dialog_topic_management, null);
        EditText topicNameEdittext = view.findViewById(R.id.topic_name);
        Topic topic = TopicController.getTopicById(getContext(), topic_id);
        topicNameEdittext.setText(topic.name);
        parentEdittext = view.findViewById(R.id.topic_parent);
        parentEdittext.setOnClickListener(v -> showSearchParentDialog(topic_id));
        String hierarchicalInfo = TopicController.getHierarchicalInfoDict(getContext(), -1).getOrDefault(topic.parentId, "");
        parentEdittext.setText(hierarchicalInfo);
        selectedParentId = topic.parentId;

        builder.setView(view);
        builder.setTitle("ویرایش یا حذف درس");
        builder.setIcon(R.drawable.ic_edit);
        builder.setMessage("این درس را حذف و یا اطلاعات آن را ویرایش کنید");
        AlertDialog alertDialog = builder.create();
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ویرایش", (dialog, which) -> {});
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "حذف", (dialog, which) -> {});
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "انصراف", (dialog, which) -> {});
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String newTopicName = topicNameEdittext.getText().toString().trim();
            if (newTopicName.isEmpty()) {
                topicNameEdittext.setError(getResources().getString(R.string.error_blank_topic_name));
            } else {
                Topic updatedTopic = new Topic(topic.id, newTopicName, selectedParentId);
                TopicController.updateTopic(getContext(), updatedTopic, String.valueOf(topic.id));
                updateTreeNodes();
                alertDialog.dismiss();
                Toast.makeText(getActivity(), "ویرایش درس با موفقیت انجام شد", Toast.LENGTH_LONG).show();
            }
        });
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(v -> {
            TopicController.deleteTopic(getContext(), topic_id);
            updateTreeNodes();
            alertDialog.dismiss();
            Toast.makeText(getActivity(), "حذف درس با موفقیت انجام شد", Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        final int menuId = item.getItemId();
        if (menuId == R.id.add_topic)
            showAddTopicDialog();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}