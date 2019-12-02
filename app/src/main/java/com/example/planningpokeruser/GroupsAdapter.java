package com.example.planningpokeruser;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder> {
    private ArrayList<Group>  mDataset;
    private Context context;

    GroupsAdapter(ArrayList<Group> groups, Context context) {
        this.context = context;
        mDataset = groups;
    }

    @NonNull
    @Override
    public GroupsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_groups_recyclerview, parent, false);
        return new GroupsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GroupsViewHolder holder, int position) {
        final Group group = mDataset.get(position);
        final String groupCode = group.getGroupCode();
        final String status = group.getStatus();

        holder.btn_group.setText(groupCode);
        holder.tv_status.setText(status);

        holder.itemView.setClickable(true);

        holder.btn_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(status.equals("Inactive")){
                    Toast.makeText(context, "Group is inactive!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(status.equals("Voted")){
                    Toast.makeText(context, "Group is voted!", Toast.LENGTH_SHORT).show();
                    return;
                }

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(context.getString(R.string.active_group), groupCode);
                editor.apply();

                QuestionFragment questionFragment = new QuestionFragment();
                FragmentTransaction frag_trans =((MainActivity) context).getSupportFragmentManager().beginTransaction();
                frag_trans.replace(R.id.fragment_container, questionFragment);
                frag_trans.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    class GroupsViewHolder extends RecyclerView.ViewHolder {
        Button btn_group;
        TextView tv_status;
        GroupsViewHolder(View itemView) {
            super(itemView);
            btn_group = itemView.findViewById(R.id.btn_group);
            tv_status = itemView.findViewById(R.id.tv_status);
        }
    }
}

