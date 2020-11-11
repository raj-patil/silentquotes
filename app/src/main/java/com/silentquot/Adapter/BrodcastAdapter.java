package com.silentquot.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;
import com.silentquot.Model.BrodcastChat;
import com.silentquot.R;

import java.util.List;

public class BrodcastAdapter extends RecyclerView.Adapter<BrodcastAdapter.ViewHolder> {


    private Context mContext;
    private List<BrodcastChat> bChat;
    private String imageurl;


    FirebaseUser fuser;
    RelativeLayout right_chat_txt;
    public BrodcastAdapter(Context mContext, List<BrodcastChat> bChat, String imageurl){
        this.bChat = bChat;
        this.mContext = mContext;
        this.imageurl = imageurl;
    }

    @NonNull
    @Override
    public BrodcastAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
         View view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false);
            return new BrodcastAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final BrodcastAdapter.ViewHolder holder, int position) {

        BrodcastChat chat = bChat.get(position);
        String msgt=chat.getMsgtype();

        if(msgt!=null && msgt.equals("Text")) {
            holder.show_message.setText(chat.getMessage());
            holder.imageView.setVisibility(View.INVISIBLE);
            holder.show_message.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Toast.makeText(mContext, "Long Click", Toast.LENGTH_SHORT).show();
                    //  holder.chat_right_rel_layout.setBackgroundResource(R.drawable.common_google_signin_btn_icon_dark_normal_background);
                    return false;
                }
            });
        }
        else {
            holder.show_message.setVisibility(View.INVISIBLE);
            Glide.with(mContext).load(chat.getMessage()).into(holder.imageView);
        }

        if (imageurl.equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(imageurl).into(holder.profile_image);
        }

            holder.txt_seen.setVisibility(View.GONE);
    }


    @Override
    public int getItemCount() {
        return bChat.size();
    }

    public  class ViewHolder extends RecyclerView.ViewHolder {

        public TextView show_message;
        public ImageView profile_image;
        public TextView txt_seen;
        public ImageView imageView;
        public RelativeLayout chat_right_rel_layout;
        public ViewHolder(View itemView) {
            super(itemView);

            show_message = itemView.findViewById(R.id.show_message);
            profile_image = itemView.findViewById(R.id.profile_image);
            txt_seen = itemView.findViewById(R.id.txt_seen);
            imageView=itemView.findViewById(R.id.show_image);
            chat_right_rel_layout=itemView.findViewById(R.id.chat_right_relative_layout);
        }
    }

}