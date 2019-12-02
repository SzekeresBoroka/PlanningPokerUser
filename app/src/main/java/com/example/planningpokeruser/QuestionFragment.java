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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class QuestionFragment extends Fragment {
    private Context context;
    private ArrayList<Question> questions;
    private String groupCode;
    private View v;
    private FirebaseDatabase db;

    @Override
    public void onCreate (Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        context = getActivity();

        // get the active group's groupcode(ID)
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        groupCode = sharedPref.getString(getString(R.string.active_group),"Active Group");

        db = FirebaseDatabase.getInstance();

        //if the groups status is voted (no more active questions), go back to GroupsFragment
        DatabaseReference groupRef = db.getReference().child("groups").child(groupCode);
        groupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                if(group.getStatus().equals("Voted")){
                    FragmentTransaction frag_trans = ((MainActivity) context).getSupportFragmentManager().beginTransaction();
                    frag_trans.replace(R.id.fragment_container, new GroupsFragment());
                    frag_trans.commit();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_question,container,false);

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        //when the active question's status becomes "voted", go to VotesFragment
        final ValueEventListener questionStatusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Question question = dataSnapshot.getValue(Question.class);
                if(question.getStatus().equals("Voted")){
                    VotesFragment votesFragment= new VotesFragment();
                    FragmentTransaction frag_trans = ((MainActivity) context).getSupportFragmentManager().beginTransaction();
                    frag_trans.replace(R.id.fragment_container, votesFragment);
                    frag_trans.commit();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        //get the active question and set the questionStatusListener
        questions = new ArrayList<>();
        DatabaseReference questionsRef = db.getReference().child("groups").child(groupCode).child("questions");
        questionsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                questions.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren())
                {
                    Question question = ds.getValue(Question.class);
                    questions.add(question);
                }

                for(Question q : questions){
                    if(q.getStatus().equals("Active")){
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString(context.getString(R.string.active_question), q.getQuestion());
                        editor.putString(context.getString(R.string.user_vote), "User Vote");
                        editor.apply();

                        TextView tv_task = v.findViewById(R.id.tv_task);
                        tv_task.setText(q.getQuestion());

                        DatabaseReference activeQuestionRef = db.getReference().child("groups").child(groupCode).child("questions").child(q.getQuestion());
                        activeQuestionRef.addValueEventListener(questionStatusListener);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //when user clicks "vote", save the vote, remove questionStatusListener and go to VotesFragment
        Button btn_vote = v.findViewById(R.id.btn_vote);
        btn_vote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = sharedPref.getString(context.getString(R.string.active_user),"Active User");
                String vote = sharedPref.getString(context.getString(R.string.user_vote),"User Vote");
                String question = sharedPref.getString(context.getString(R.string.active_question),"Active Question");

                DatabaseReference activeQuestionRef = db.getReference().child("groups").child(groupCode).child("questions").child(question);
                activeQuestionRef.removeEventListener(questionStatusListener);

                if(vote.equals("User Vote")){
                    Toast.makeText(context, "Nothing selected!", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(question.equals("Active Question")){
                    Toast.makeText(context, "Wrong question!", Toast.LENGTH_SHORT).show();
                    return;
                }

                //insert vote into firebase
                DatabaseReference groupRef = db.getReference().child("groups").child(groupCode);
                groupRef.child("questions").child(question).child("votes").child(user).setValue(vote);

                VotesFragment fragment = new VotesFragment();
                FragmentTransaction frag_trans = ((MainActivity) context).getSupportFragmentManager().beginTransaction();
                frag_trans.replace(R.id.fragment_container, fragment);
                frag_trans.commit();
            }
        });

        //set the recyclerview that will contain the vote options
        RecyclerView recyclerView = v.findViewById(R.id.grid_recyclerView);
        RecyclerView.LayoutManager manager = new GridLayoutManager(context, 3);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, LinearLayoutManager.VERTICAL));

        ArrayList<String> gridDataList = new ArrayList<>();
        gridDataList.add("\u2615");
        gridDataList.add("1");
        gridDataList.add("2");
        gridDataList.add("3");
        gridDataList.add("5");
        gridDataList.add("8");
        gridDataList.add("13");
        gridDataList.add("20");
        gridDataList.add("?");

        RecyclerView.Adapter gridAdapter = new GridAdapter(gridDataList, context);
        recyclerView.setAdapter(gridAdapter);

        return v;
    }
}
