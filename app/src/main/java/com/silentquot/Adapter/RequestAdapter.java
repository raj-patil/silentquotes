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
import com.silentquot.Model.Chat;
import com.silentquot.Model.Request;
import com.silentquot.Model.User;
import com.silentquot.ProfileActivity;
import com.silentquot.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers;
   List<Request> mRequestLit;
    String theLastMessage;

    DatabaseReference reference;
    public RequestAdapter(Context mContext, List<Request> requestList){
      //  this.mUsers = mUsers;
        this.mContext = mContext;
        this.mRequestLit=requestList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.request_item, parent, false);
        return new RequestAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

       // final User user = mUsers.get(position);

        final Request request=mRequestLit.get(position);
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final String mCurrentUserId = firebaseUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(request.getId());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 final User user = dataSnapshot.getValue(User.class);
                    holder.username.setText(user.getUsername());
                    if (user.getImageURL().equals("default")){
                        holder.profile_image.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
                    }
                    if (request.getRequest_type().equals("received"))
                    {
                        holder.accept_btn.setVisibility(View.VISIBLE);
                        holder.cancel_btn.setVisibility(View.VISIBLE);

                        holder.accept_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                               acceptFriendRequest(mCurrentUserId, user.getId() , position);
                            }
                        });
                        holder.cancel_btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                cancelFriendRequest(mCurrentUserId, user.getId() , position);
                            }
                        });

                    }
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            Intent intent = new Intent(mContext, ProfileActivity.class);
                            intent.putExtra("userid", user.getId());
                            mContext.startActivity(intent);
                        }
                    });



                // requestList();
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

      //  holder.username.setText(user.getUsername());
//        if ()
//        {
//            holder.accept_btn.setVisibility(View.VISIBLE);
//        }
//        if (user.getImageURL().equals("default")){
//            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
//        } else {
//            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
//        }




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


    private void acceptFriendRequest(final String mCurrentUserId,final String uid , final  int position) {
        DatabaseReference mRootDatabase = FirebaseDatabase.getInstance().getReference();
        final String date = DateFormat.getDateTimeInstance().format(new Date());
        Map map = new HashMap();
        map.put("friends/"+ mCurrentUserId + "/" + uid + "/date",date);
        map.put("friends/"+ mCurrentUserId + "/" + uid + "/id",uid);
        map.put("friends/"+ uid + "/" + mCurrentUserId + "/date",date);
        map.put("friends/"+ uid + "/" + mCurrentUserId + "/id",mCurrentUserId);
        map.put("friend_req/"+mCurrentUserId + "/" + uid + "/id",null);
        map.put("friend_req/"+mCurrentUserId + "/" + uid + "/request_type",null);
        map.put("friend_req/"+uid + "/" + mCurrentUserId + "/id",null);
        map.put("friend_req/"+uid + "/" + mCurrentUserId + "/request_type",null);
        mRootDatabase.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
//                mRequestLit.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mRequestLit.size());
            }
        });
    }

    private void cancelFriendRequest(final String mCurrentUserId,final String uid , final  int position) {
        DatabaseReference mRootDatabase = FirebaseDatabase.getInstance().getReference();
        final String date = DateFormat.getDateTimeInstance().format(new Date());

        Map map = new HashMap();
        map.put("friend_req/"+mCurrentUserId + "/" + uid + "/id",null);
        map.put("friend_req/"+mCurrentUserId + "/" + uid + "/request_type",null);
        map.put("friend_req/"+uid + "/" + mCurrentUserId + "/id",null);
        map.put("friend_req/"+uid + "/" + mCurrentUserId + "/request_type",null);

        mRootDatabase.updateChildren(map, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                mRequestLit.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mRequestLit.size());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mRequestLit.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_image;
        private Button accept_btn  , cancel_btn;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            accept_btn=itemView.findViewById(R.id.btn_accept);
            cancel_btn=itemView.findViewById(R.id.btn_cancel);

        }
    }

    //check for last message
    private void lastMessage(final String userid, final TextView last_msg){
        theLastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if (firebaseUser != null && chat != null) {
                        if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userid) ||
                                chat.getReceiver().equals(userid) && chat.getSender().equals(firebaseUser.getUid())) {
                            theLastMessage = chat.getMessage();
                        }
                    }
                }

                switch (theLastMessage){
                    case  "default":
                        last_msg.setText("No Message");
                        break;

                    default:
                        last_msg.setText(theLastMessage);
                        break;
                }

                theLastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
