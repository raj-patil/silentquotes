package com.silentquot.Adapter;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class  ChatAdapter extends FirebaseRecyclerAdapter<Chatlist, ChatAdapter.ViewHolder> {

    private Context mContext;
    private List<User> mUsers=null;
    private boolean ischat;
    private  List<Brodcast> mBrodcast;
    final int VIEW_TYPE_MESSAGE = 0;
    final int VIEW_TYPE_BRODCAST= 1;
    ConversationDAO conversationDAO ;
    String theLastMessage;
//
//    public ChatAdapter(Context mContext, List<User> mUsers, boolean ischat, List<Brodcast> mBrodcast){
//        this.mUsers = mUsers;
//        this.mContext = mContext;
//        this.ischat = ischat;
//        this.mBrodcast=mBrodcast;
//    }


    public ChatAdapter(@NonNull FirebaseRecyclerOptions<Chatlist> options) {
        super(options);

    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

//        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);

     View   view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);

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
//        if(position < mUsers.size()){
//            return VIEW_TYPE_MESSAGE;
//        }
//
//        if(position - mUsers.size() < mBrodcast.size()){
//            return VIEW_TYPE_BRODCAST;
//        }
//        return -1;
        if (getItem(position).getChat_type().equals("userchat"))
        {
            return VIEW_TYPE_MESSAGE;
        }
        if(getItem(position).getChat_type().equals("brodcast"))
        {
            return VIEW_TYPE_BRODCAST;
        }
        
        return  -1;
    }


    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Chatlist chatItem) {
        conversationDAO= new ConversationDAOImplSQLiteOpenHelper(mContext);
        if (getItemViewType(position)==VIEW_TYPE_MESSAGE)
        {
            boolean isExist=false;
         Chatlist chat=conversationDAO.get(chatItem.getId());
            if ( chat==null) {
                isExist = false;
            }
            else {
                isExist=true;
            }


            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(chatItem.getId());
            boolean finalIsExist = isExist;
            reference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);
                    chatItem.setUsername(user.getUsername());
                    chatItem.setFimgurl(user.getImageURL());
                    holder.username.setText(user.getUsername());
                    if (user.getImageURL()!=null)
                        if (user.getImageURL().equals("default")) {
                            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
                        } else {
                            Glide.with(mContext).load(user.getImageURL()).listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    Bitmap bitmap = getBitmapFromView(holder.profile_image);
                                    String path=saveToInternalStorage(bitmap);
                                    chatItem.setLimgurl(path);
                                    return false;
                                }
                            }).into(holder.profile_image);

                        }

                    if (ischat) {
                        lastMessage(user.getId(), holder.last_msg);
                    } else {
                        holder.last_msg.setVisibility(View.GONE);
                    }

                    if (ischat) {
                        if (user.getStatus().equals("online")) {
                            holder.img_on.setVisibility(View.VISIBLE);
                            holder.img_off.setVisibility(View.GONE);
                        } else {
                            holder.img_on.setVisibility(View.GONE);
                            holder.img_off.setVisibility(View.VISIBLE);
                        }
                    } else {
                        holder.img_on.setVisibility(View.GONE);
                        holder.img_off.setVisibility(View.GONE);
                    }

                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(mContext, MessagePersistanceActivity.class);
                            intent.putExtra("userid", user.getId());
                            intent.putExtra("chatType" , "userchat");
                            intent.putExtra("chat_key" , chatItem.getKey());
                            mContext.startActivity(intent);
                        }
                    });

                    if (!finalIsExist)
                    {
                        conversationDAO.insert(chatItem);
                    }
                    else
                    {
                        String Fimg= conversationDAO.getFimgurl(chatItem.getId());
                        if (!Fimg.equals(chatItem.getFimgurl()))
                        {
                            conversationDAO.updateLimg(chatItem.getLimgurl() , chatItem.getFimgurl() , chatItem.getId());
                        }


                    }




                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });

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


    private Bitmap getBitmapFromView(View view)
    {
        Bitmap returnedBitmap=Bitmap.createBitmap(view.getWidth() , view.getHeight() ,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(returnedBitmap);
        Drawable bgDrawable=view.getBackground();
        if(bgDrawable!=null)
        {
            bgDrawable.draw(canvas);
        }else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(mContext);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath=new File(directory,"profile.jpg");

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return directory.getAbsolutePath();
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
