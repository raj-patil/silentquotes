package com.silentquot.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.silentquot.MessageActivity;
import com.silentquot.Model.User;
import com.silentquot.Model.connections;
import com.silentquot.ProfileActivity;
import com.silentquot.R;

import java.util.List;

public class ConnectionsAdapter extends RecyclerView.Adapter<ConnectionsAdapter.ViewHolder> {

    private Context mContext;
    private List<connections> mconnectionss;
    private boolean ischat;

    String theLastMessage;
    private FirebaseUser currentUser;

    public ConnectionsAdapter(Context mContext, List<connections> mconnections){
        this.mconnectionss = mconnections;
        this.mContext = mContext;
        this.ischat = ischat;
    }

    @NonNull
    @Override
    public ConnectionsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.friend_item, parent, false);
        return new ConnectionsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectionsAdapter.ViewHolder holder, int position) {

        final connections con_user = mconnectionss.get(position);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String mCurrentUserId = currentUser.getUid();

        DatabaseReference reference1=FirebaseDatabase.getInstance().getReference("Users").child(con_user.getId());
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                    holder.username.setText(user.getUsername());
                if (user.getImageURL().equals("default")){
                    holder.profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
                    }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, ProfileActivity.class);
                intent.putExtra("userid", con_user.getId());

                mContext.startActivity(intent);
            }
        });

        holder.btn_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                        .child(mCurrentUserId)
                        .child(con_user.getId()).child("key");

                chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String chat_key;
                        if (!dataSnapshot.exists()) {

                            chat_key = "";
                        } else {
                            chat_key = dataSnapshot.getValue(String.class);
                        }


                        Intent messageeActivity = new Intent(mContext, MessageActivity.class);
                        messageeActivity.putExtra("userid", con_user.getId());
                        messageeActivity.putExtra("chatType", "userchat");
                        messageeActivity.putExtra("chat_key", chat_key);
                        mContext.startActivity(messageeActivity);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
            });



//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent(mContext, ProfileActivity.class);
//                intent.putExtra("userid", user.getId());
//                mContext.startActivity(intent);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mconnectionss.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_image;
        public Button btn_message;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            btn_message=itemView.findViewById(R.id.friend_item_btn_message);

        }
    }

}

