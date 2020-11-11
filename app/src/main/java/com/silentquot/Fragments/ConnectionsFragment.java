package com.silentquot.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.silentquot.Adapter.ConnectionsAdapter;
import com.silentquot.Model.connections;
import com.silentquot.R;

import java.util.ArrayList;
import java.util.List;


public class ConnectionsFragment extends Fragment {


    private RecyclerView recyclerView;

    private ConnectionsAdapter connectionsAdapter;
    private List<connections> mUsers;
    private  List<connections> connections;
    private FirebaseAuth mauth;
        private String fuser;
    public ConnectionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_connections, container, false);



        recyclerView = view.findViewById(R.id.connections_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        connections = new ArrayList<>();

        readUsers();

        mauth = FirebaseAuth.getInstance();
        fuser = mauth.getCurrentUser().getUid();



        return  view;
    }


    private void readUsers() {

        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("friends").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    connections.clear();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        connections con_user = snapshot.getValue(connections.class);
                        connections.add(con_user);
                    }

                    connectionsAdapter = new ConnectionsAdapter(getContext(), connections );
                    recyclerView.setAdapter(connectionsAdapter);
                }
                @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
