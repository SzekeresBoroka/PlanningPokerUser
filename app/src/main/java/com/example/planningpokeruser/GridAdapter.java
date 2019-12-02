package com.example.planningpokeruser;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class GridAdapter extends RecyclerView.Adapter<GridAdapter.MyViewHolder>{
    private ArrayList<String> gridDataList;
    private Context context;
    private int selectedPosition = -1;

    GridAdapter(ArrayList<String> gridDataList, Context context) {
        this.gridDataList = gridDataList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.grid_item_view, viewGroup, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder viewHolder, final int i) {
        final String data = gridDataList.get(i);
        viewHolder.btn_value.setText(data);

        if(selectedPosition == i){
            //highlight selected button
            viewHolder.itemView.setBackgroundColor(Color.parseColor("#FF009688"));
            //save vote in shared preferences
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(context.getString(R.string.user_vote), data);
            editor.apply();
        }
        else{
            viewHolder.itemView.setBackgroundColor(android.R.drawable.btn_default);}

        viewHolder.itemView.setClickable(true);

        viewHolder.btn_value.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPosition = i;
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return gridDataList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        Button btn_value;
        MyViewHolder(View itemView) {
            super(itemView);
            btn_value = itemView.findViewById((R.id.btn_value));
        }
    }
}
