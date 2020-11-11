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
import com.google.firebase.iid.FirebaseInstanceId;
import com.silentquot.Adapter.RequestAdapter;
import com.silentquot.Model.Request;
import com.silentquot.Model.User;
import com.silentquot.Notifications.Token;
import com.silentquot.R;

import java.util.ArrayList;
import java.util.List;


public class RequestFragment extends Fragment {

    private RecyclerView recyclerView;

    private RequestAdapter RequestAdapter;
    private List<User> mUsers;

    FirebaseUser fuser;
    DatabaseReference reference;

    private List<Request> requestList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser = FirebaseAuth.getInstance().getCurrentUser();

        requestList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("friend_req").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Request RequestList = snapshot.getValue(Request.class);

                    if("received".equalsIgnoreCase(RequestList.getRequest_type()))
                    {
                        requestList.add(RequestList);
                    }

                }

                RequestAdapter = new RequestAdapter(getContext(),requestList);
                recyclerView.setAdapter(RequestAdapter);

               // requestList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        updateToken(FirebaseInstanceId.getInstance().getToken());


        return view;
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(fuser.getUid()).setValue(token1);
    }

    private void requestList() {
        mUsers = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    User user = snapshot.getValue(User.class);
                    for (Request RequestList : requestList){
                        if(user.getId() != null && user.getId().equals(RequestList.getId()) && "received".equalsIgnoreCase(RequestList.getRequest_type()))
                        {
                            mUsers.add(user);
                        }
                    }
                }
              //  RequestAdapter = new RequestAdapter(getContext(), mUsers ,requestList);
                recyclerView.setAdapter(RequestAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
