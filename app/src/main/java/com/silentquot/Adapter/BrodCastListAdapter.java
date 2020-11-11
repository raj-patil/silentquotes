package com.silentquot.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.silentquot.Model.Brodcast;
import com.silentquot.R;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrodCastListAdapter extends RecyclerView.Adapter<BrodCastListAdapter.ViewHolder> {

    private Context mContext;
    private List<Brodcast> mBrodcast;

    public BrodCastListAdapter(Context mContext, List<Brodcast> mBrodcast){
        this.mBrodcast = mBrodcast;
        this.mContext = mContext;

    }

    @NonNull
    @Override
    public BrodCastListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.brodcast_item, parent, false);
        return new BrodCastListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BrodCastListAdapter.ViewHolder holder, int position) {

        final Brodcast brodcast = mBrodcast.get(position);
        holder.username.setText(brodcast.getId());
        if (brodcast.getImageURL().equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(brodcast.getImageURL()).into(holder.profile_image);
        }

        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();


        DatabaseReference brodreference = FirebaseDatabase.getInstance().getReference("Brodcastlist").child(brodcast.getId()).child(fuser.getUid()).child("status");
        brodreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

               String status =dataSnapshot.getValue(String.class);
               if (("join").equals(status))
               {
                   holder.join_btn.setText("Exit Channel");
               }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
                 holder.join_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holder.join_btn.getText().equals("JOIN"))
                    {
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
                                        DatabaseReference brodreference = FirebaseDatabase.getInstance().getReference();
                                        Map map = new HashMap();
                                        map.put("Brodcastlist/"+ brodcast.getId() + "/" + fuser.getUid() + "/id",fuser.getUid());
                                        map.put("Brodcastlist/"+ brodcast.getId() + "/" + fuser.getUid() + "/status","join");
                                        map.put("Chatlist/"+ fuser.getUid() + "/" + brodcast.getId() + "/id",brodcast.getId());
                                        map.put("Chatlist/"+ fuser.getUid() + "/" + brodcast.getId() + "/chat_type","brodcast");
                                        map.put("Chatlist/"+ fuser.getUid() + "/" + brodcast.getId() + "/timestamp", ServerValue.TIMESTAMP);
                                        //map.put("notification/" + uid +"/" + key ,notificationData);
                                        brodreference.updateChildren(map, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                holder.join_btn.setText("Exit Channel");
                                            }

                                        });
                                        notifyDataSetChanged();
//                                        notifyItemRemoved(position);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage("Are you want to join This channel ").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();



                    }

                    else {



                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:

                                        FirebaseUser fuser = FirebaseAuth.getInstance().getCurrentUser();
                                        DatabaseReference brodreference = FirebaseDatabase.getInstance().getReference();
                                        Map map = new HashMap();
                                        map.put("Brodcastlist/"+ brodcast.getId() + "/" + fuser.getUid() + "/id",null);
                                        map.put("Brodcastlist/"+ brodcast.getId() + "/" + fuser.getUid() + "/status",null);
                                        map.put("Chatlist/"+ fuser.getUid() + "/" + brodcast.getId() + "/id",null);
                                        map.put("Chatlist/"+ fuser.getUid() + "/" + brodcast.getId() + "/chat_type",null);
                                        map.put("Chatlist/"+ fuser.getUid() + "/" + brodcast.getId() + "/timestamp", null);
                                        //map.put("notification/" + uid +"/" + key ,notificationData);
                                        brodreference.updateChildren(map, new DatabaseReference.CompletionListener() {
                                            @Override
                                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                                holder.join_btn.setText("JOIN");
                                            }

                                        });


                                        notifyDataSetChanged();
                                        notifyItemRemoved(position);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage("Are you sure to Exit This channel ").setPositiveButton("Yes", dialogClickListener)
                                .setNegativeButton("No", dialogClickListener).show();


                    }
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
        return mBrodcast.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder{

        public TextView username;
        public ImageView profile_image;
        public Button join_btn;

        public ViewHolder(View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.brodcast_profine_name);
            profile_image = itemView.findViewById(R.id.brodcast_profile_img);
            join_btn = itemView.findViewById(R.id.brodcast_join_btn);
          
        }
    }
    
}
