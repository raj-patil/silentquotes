package com.silentquot.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.silentquot.Adapter.BrodCastListAdapter;
import com.silentquot.Adapter.ChatAdapter;
import com.silentquot.Adapter.ChatAdapterPersistance;
import com.silentquot.Model.Brodcast;
import com.silentquot.Model.Chatlist;
import com.silentquot.Model.User;
import com.silentquot.Notifications.Token;
import com.silentquot.R;
import com.silentquot.persistence.ConversationDAO;
import com.silentquot.persistence.ConversationDAOImplSQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {

    private RecyclerView recyclerView , brodcastRecycleView;

    private ChatAdapter chatAdapter;
    private List<User> mUsers;
    private  List<Brodcast> mBrodcast;
    private  List<Brodcast > brodcastList;
    FirebaseUser fuser;
    Query reference;
    Button allchannl;
    private List<Chatlist> chatList;
    private BrodCastListAdapter BrodCastListAdapter;
    private ChatAdapterPersistance chatAdapterPersistance;
    ConversationDAO conversationDAO;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        brodcastRecycleView=view.findViewById(R.id.brodcast_recycler_view);
        allchannl=view.findViewById(R.id.all_channel);
        recyclerView.setHasFixedSize(true);
        brodcastRecycleView.setHasFixedSize(true);
        LinearLayoutManager linearLayout= new LinearLayoutManager(getContext());
        linearLayout.setStackFromEnd(true);
        linearLayout.setReverseLayout(true);

        recyclerView.setLayoutManager(linearLayout);

        brodcastRecycleView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, true));
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        mUsers = new ArrayList<>();
        mBrodcast = new ArrayList<>();
        chatList = new ArrayList<>();
      //  chatAdapter = new ChatAdapter(getContext(), mUsers, true , mBrodcast);
        //recyclerView.setAdapter(chatAdapter);
//        chatList.clear();
//        mUsers.clear();
//        mBrodcast.clear();

    conversationDAO= new ConversationDAOImplSQLiteOpenHelper(getContext());
        String status = NetworkUtil.getConnectivityStatusString(getContext());
        if(status==null) {
            status="No Internet Connection";
            Toast.makeText(getContext() , status , Toast.LENGTH_SHORT).show();
            List<Chatlist> chatlist = conversationDAO.getAll();
            if (chatlist != null) {
                Log.d("TAG", chatlist.toString());
                chatAdapterPersistance = new ChatAdapterPersistance(getContext(), chatlist);
                recyclerView.setAdapter(chatAdapterPersistance);
            }
        }
        else {


            Query chatList = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid()).orderByChild("timestamp");
            //     com.google.firebase.firestore.Query firstQuery = firebaseFirestore.collection("SilentQuotPost").orderBy("TimeStamp", com.google.firebase.firestore.Query.Direction.DESCENDING);
            FirebaseRecyclerOptions<Chatlist> options = new FirebaseRecyclerOptions.Builder<Chatlist>()
                    .setQuery(chatList, Chatlist.class)
                    .build();
            chatAdapter = new ChatAdapter(options);
               recyclerView.setAdapter(chatAdapter);


        }

//        reference = FirebaseDatabase.getInstance().getReference("Chatlist").child(fuser.getUid()).orderByChild("timestamp");
//        reference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
//                    Chatlist chatlist = snapshot.getValue(Chatlist.class);
//                         if(("userchat").equals(chatlist.getChat_type())) {
//                             reference = FirebaseDatabase.getInstance().getReference("Users").child(chatlist.getId());
//                               reference.addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                                mUsers.clear();
//                                    User user = dataSnapshot.getValue(User.class);
//                                        if (("userchat").equals(chatlist.getChat_type())) {
//                                            if (user.getId() != null && user.getId().equals(chatlist.getId())) {
//                                                user.setChat_key(chatlist.getKey());
//                                                mUsers.add(0, user);
//                                            }
//                                        }
//                                chatAdapter.notifyDataSetChanged();
//                            }
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//                            }
//                        });
////                        chatList.add(0, chatlist);
////                        chatList();
//                    }
//                    else
//                    {
//                        DatabaseReference brodref = FirebaseDatabase.getInstance().getReference("Brodcast").child(chatlist.getId()) ;
//                        brodref.addValueEventListener(new ValueEventListener() {
//                            @Override
//                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//
//                                    Brodcast brodcast=dataSnapshot.getValue(Brodcast.class);
//                                    if (brodcast!=null)
//                                        if (brodcast.getId() != null && brodcast.getId().equals(chatlist.getId())) {
//                                            mBrodcast.add(brodcast);
//                                        }
//                                    chatAdapter.notifyDataSetChanged();
//                                 }
//                            @Override
//                            public void onCancelled(@NonNull DatabaseError databaseError) {
//                            }
//                        });
//                    }
//                }
//
//            }
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//            }
//        });

        updateToken(FirebaseInstanceId.getInstance().getToken());

        brodcastList=new ArrayList<>();
        BrodCastListAdapter = new BrodCastListAdapter(getContext(), brodcastList);
        brodcastRecycleView.setAdapter(BrodCastListAdapter);

        DatabaseReference brodreference = FirebaseDatabase.getInstance().getReference("Brodcast");
        brodreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                brodcastList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Brodcast brodcast = snapshot.getValue(Brodcast.class);
                    brodcastList.add(brodcast);
                }
                BrodCastListAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        allchannl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(brodcastRecycleView.isShown()){
                    //     Fx.slide_up(this, txt_help_gest);
                    brodcastRecycleView.setVisibility(View.GONE);

                }
                else{
                    brodcastRecycleView.setVisibility(View.VISIBLE);
                    //      Fx.slide_down(this, txt_help_gest);
                }
            }
        });
        return view;
    }

    private void updateToken(String token){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1 = new Token(token);
        reference.child(fuser.getUid()).setValue(token1);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (chatAdapter!=null)
        chatAdapter.startListening();
    }

    @Override
    public void onStop() {
        super.onStop();
     //   chatAdapter.stopListening();
    }


}
