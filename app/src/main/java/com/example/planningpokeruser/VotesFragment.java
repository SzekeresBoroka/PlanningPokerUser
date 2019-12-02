package com.example.planningpokeruser;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class VotesFragment extends Fragment {
    private Context context;
    private String groupCode;
    private String question;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private ArrayList<String> votes;

    @Override
    public void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();

        //get the active group and question
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        groupCode = sharedPref.getString(getString(R.string.active_group),"Active Group");
        question = sharedPref.getString(getString(R.string.active_question),"Active Question");

        //reset user_vote
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.user_vote), "User Vote");
        editor.apply();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_votes, container, false);

        //set the recyclerview that contains the votes on the active question

        recyclerView = v.findViewById(R.id.votes_recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        votes = new ArrayList<>();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference myRef = db.getReference().child("groups").child(groupCode).child("questions").child(question).child("votes");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                votes.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    //key = user, value = vote
                    votes.add(ds.getKey() + " - " + ds.getValue());
                }
                mAdapter = new VotesAdapter(votes, context);
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //put the active group and question on the screen
        TextView tv_group_code = v.findViewById(R.id.tv_group_code);
        TextView tv_question = v.findViewById(R.id.tv_question);
        tv_group_code.setText(groupCode);
        tv_question.setText(question);

        //if the current questions time is over, go to the next question (QuestionFragment)
        DatabaseReference questionRef = db.getReference().child("groups").child(groupCode).child("questions").child(question);
        questionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Question question = dataSnapshot.getValue(Question.class);
                if(question.getStatus().equals("Voted")){
                    FragmentTransaction frag_trans = ((MainActivity) context).getSupportFragmentManager().beginTransaction();
                    frag_trans.replace(R.id.fragment_container, new QuestionFragment());
                    frag_trans.commit();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();

        View v = getView();

        recyclerView = v.findViewById(R.id.votes_recyclerView);
        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);

        votes = new ArrayList<>();

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference myRef = db.getReference().child("groups").child(groupCode).child("questions").child(question).child("votes");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                votes.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    //key = user, value = vote
                    votes.add(ds.getKey() + " - " + ds.getValue());
                }
                mAdapter = new VotesAdapter(votes, context);
                recyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}

