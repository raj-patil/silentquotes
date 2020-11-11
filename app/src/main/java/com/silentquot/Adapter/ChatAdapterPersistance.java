package com.silentquot.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.silentquot.MessagePersistanceActivity;
import com.silentquot.Model.Brodcast;
import com.silentquot.Model.Chat;
import com.silentquot.Model.Chatlist;
import com.silentquot.Model.User;
import com.silentquot.R;
import com.silentquot.persistence.ConversationDAO;
import com.silentquot.persistence.ConversationDAOImplSQLiteOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

public class ChatAdapterPersistance  extends RecyclerView.Adapter<ChatAdapterPersistance.ViewHolder> {

        private Context mContext;
        private List<User> mUsers=null;
        private boolean ischat;
        private  List<Brodcast> mBrodcast;
    private  List<Chatlist> mChatlist;
        final int VIEW_TYPE_MESSAGE = 0;
        final int VIEW_TYPE_BRODCAST= 1;
        ConversationDAO conversationDAO ;
        String theLastMessage;
//
    public ChatAdapterPersistance(Context mContext, List<Chatlist> chatlist){

        this.mContext = mContext;

        mChatlist=chatlist;
    }


//        public ChatAdapter(@NonNull FirebaseRecyclerOptions<Chatlist> options) {
//            super(options);
//
//        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

//        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);

//      RecyclerView.ViewHolder viewHolder = null;
//        if(viewType == VIEW_TYPE_MESSAGE){
//            viewHolder =new MessageViewHolder(view);
//        }
//        if(viewType == VIEW_TYPE_BRODCAST){
//            viewHolder =new BrodcastViewHolder(view);
//        }

            mContext=parent.getContext();
            return new ViewHolder(view);
        }

        //
        @Override
        public int getItemViewType(int position) {

            if (mChatlist.get(position).getChat_type().equals("userchat"))
            {
                return VIEW_TYPE_MESSAGE;
            }
            if(mChatlist.get(position).getChat_type().equals("brodcast"))
            {
                return VIEW_TYPE_BRODCAST;
            }

            return  -1;
        }

    @Override
    public int getItemCount() {
        return mChatlist.size();
    }


    @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            conversationDAO= new ConversationDAOImplSQLiteOpenHelper(mContext);
            Chatlist chatItem=mChatlist.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MessagePersistanceActivity.class);
                intent.putExtra("userid", chatItem.getId());
                intent.putExtra("chatType" , "userchat");
                intent.putExtra("chat_key" , chatItem.getKey());
                intent.putExtra("userName" , chatItem.getUsername());
                mContext.startActivity(intent);
            }
        });

        if (getItemViewType(position)==VIEW_TYPE_MESSAGE)
            {
//                Chatlist chat=   conversationDAO.get(chatItem.getId());
//                if (chat==null)
//                    conversationDAO.insert(chatItem);


                holder.username.setText(chatItem.getUsername());
                if (chatItem.getLimgurl()!=null)
                    if (chatItem.getLimgurl().equals("default")) {
                        holder.profile_image.setImageResource(R.mipmap.ic_launcher);
                    } else {
                        Glide.with(mContext).load(chatItem.getLimgurl()).into(holder.profile_image);
                    }

//                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(chatItem.getId());
//                reference.addValueEventListener(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        User user = dataSnapshot.getValue(User.class);
//                        holder.username.setText(user.getUsername());
//
//                        if (user.getImageURL()!=null)
//                            if (user.getImageURL().equals("default")) {
//                                holder.profile_image.setImageResource(R.mipmap.ic_launcher);
//                            } else {
//                                Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
//                            }
//
//                        if (ischat) {
//                            lastMessage(user.getId(), holder.last_msg);
//                        } else {
//                            holder.last_msg.setVisibility(View.GONE);
//                        }
//
//                        if (ischat) {
//                            if (user.getStatus().equals("online")) {
//                                holder.img_on.setVisibility(View.VISIBLE);
//                                holder.img_off.setVisibility(View.GONE);
//                            } else {
//                                holder.img_on.setVisibility(View.GONE);
//                                holder.img_off.setVisibility(View.VISIBLE);
//                            }
//                        } else {
//                            holder.img_on.setVisibility(View.GONE);
//                            holder.img_off.setVisibility(View.GONE);
//                        }
//
//                        holder.itemView.setOnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View view) {
//                                Intent intent = new Intent(mContext, MessagePersistanceActivity.class);
//                                intent.putExtra("userid", user.getId());
//                                intent.putExtra("chatType" , "userchat");
//                                intent.putExtra("chat_key" , chatItem.getKey());
//                                mContext.startActivity(intent);
//                            }
//                        });



//                    }
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                    }
//                });

            }


            if (getItemViewType(position)==VIEW_TYPE_BRODCAST)
            {
                DatabaseReference brodref = FirebaseDatabase.getInstance().getReference("Brodcast").child(chatItem.getId()) ;
                brodref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Brodcast brodcast=dataSnapshot.getValue(Brodcast.class);


                        holder.username.setText(brodcast.getId());
                        if (brodcast.getImageURL().equals("default")){
                            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
                        } else {
                            Glide.with(mContext).load(brodcast.getImageURL()).into(holder.profile_image);
                        }
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(mContext, MessageActivity.class);
                                intent.putExtra("userid", brodcast.getId());
                                intent.putExtra("chatType" , "brodcast");

                                mContext.startActivity(intent);
                            }
                        });

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
        }

    private void loadImageFromStorage(String path)
    {

        try {
            File f=new File(path, "profile.jpg");
            Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
           // ImageView img=(ImageView)findViewById(R.id.imgPicker);
           // img.setImageBitmap(b);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }

    }

        public  class BrodcastViewHolder extends RecyclerView.ViewHolder{

            public TextView brodcastname;
            public ImageView profile_image;
            //        private ImageView img_on;
//        private ImageView img_off;
//        private TextView last_msg;
            public BrodcastViewHolder(@NonNull View itemView) {
                super(itemView);

                brodcastname = itemView.findViewById(R.id.username);
                profile_image = itemView.findViewById(R.id.profile_image);
            }
            public void populate(final Brodcast brodcast) {
                brodcastname.setText(brodcast.getId() + "  BrodCast");
                if (brodcast.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher);
                } else {
                    Glide.with(mContext).load(brodcast.getImageURL()).into(profile_image);
                }
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, MessageActivity.class);
                        intent.putExtra("userid", brodcast.getId());
                        intent.putExtra("chatType" , "brodcast");

                        mContext.startActivity(intent);
                    }
                });

            }
        }


        public  class  ViewHolder extends  RecyclerView.ViewHolder{

            public TextView username;
            public ImageView profile_image;
            private ImageView img_on;
            private ImageView img_off;
            private TextView last_msg;
            public TextView brodcastname;
            public ImageView profile_image_brodcast;
            public ViewHolder(@NonNull View itemView) {
                super(itemView);


                username = itemView.findViewById(R.id.username);
                profile_image = itemView.findViewById(R.id.profile_image);
                img_on = itemView.findViewById(R.id.img_on);
                img_off = itemView.findViewById(R.id.img_off);
                last_msg = itemView.findViewById(R.id.last_msg);
                brodcastname = itemView.findViewById(R.id.username);
                profile_image = itemView.findViewById(R.id.profile_image);

            }
        }

        public  class MessageViewHolder extends RecyclerView.ViewHolder{

            public TextView username;
            public ImageView profile_image;
            private ImageView img_on;
            private ImageView img_off;
            private TextView last_msg;

            public MessageViewHolder(View itemView) {
                super(itemView);

                username = itemView.findViewById(R.id.username);
                profile_image = itemView.findViewById(R.id.profile_image);
                img_on = itemView.findViewById(R.id.img_on);
                img_off = itemView.findViewById(R.id.img_off);
                last_msg = itemView.findViewById(R.id.last_msg);
            }



        }

        //check for last message
        private void lastMessage(final String brodcastid, final TextView last_msg){
            theLastMessage = "default";
            final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                        Chat chat = snapshot.getValue(Chat.class);
                        if (firebaseUser != null && chat != null) {
                            if (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(brodcastid) ||
                                    chat.getReceiver().equals(brodcastid) && chat.getSender().equals(firebaseUser.getUid())) {
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










