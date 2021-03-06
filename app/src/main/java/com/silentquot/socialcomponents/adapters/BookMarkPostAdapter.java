package com.silentquot.socialcomponents.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.silentquot.R;
import com.silentquot.socialcomponents.adapters.holders.BookMarkPostViewHolder;
import com.silentquot.socialcomponents.adapters.holders.PostViewHolder;
import com.silentquot.socialcomponents.controllers.BookMarkController;
import com.silentquot.socialcomponents.controllers.LikeController;
import com.silentquot.socialcomponents.main.base.BaseActivity;
import com.silentquot.socialcomponents.model.BookMark;

import java.util.ArrayList;
import java.util.List;

public class BookMarkPostAdapter  extends RecyclerView.Adapter<BookMarkPostViewHolder>  {




    private List<BookMark> itemsList = new ArrayList<>();
    private BookMarkPostAdapter.CallBack callBack;
    private int TEXT_POST=1;
    private  int IMAGE_POST=2;
    private int COLLAB_POST=3;
    private BaseActivity activity;

    private int selectedPostPosition = RecyclerView.NO_POSITION;

    public BookMarkPostAdapter(BaseActivity activity) {
        this.activity = activity;
    }

    public void setCallBack(BookMarkPostAdapter.CallBack callBack) {
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public BookMarkPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view;

        int POST_TYPE=1;
        if (viewType==IMAGE_POST) {
            view = inflater.inflate(R.layout.post_item_list_view, parent, false );
            POST_TYPE=IMAGE_POST;
        }else if (viewType==TEXT_POST){
            view = inflater.inflate(R.layout.text_post_item_list, parent, false);
            POST_TYPE=TEXT_POST;

        }
        else if (viewType==COLLAB_POST)
        {
            view = inflater.inflate(R.layout.collab_post_item_list, parent, false);
            POST_TYPE=COLLAB_POST;
        }
        else {
            view =null;
        }

        return new BookMarkPostViewHolder(view, createOnClickListener(), activity, true , POST_TYPE);
    }

    @Override
    public void onBindViewHolder(@NonNull BookMarkPostViewHolder holder, int position) {
        int POST_TYPE = getItemViewType(position);
        if (POST_TYPE == IMAGE_POST) {
            holder.bindData(itemsList.get(position));
           // selectedPostPosition = position;
          //  updateSelectedItem();

        } else if(POST_TYPE == TEXT_POST)
        {
            holder.bindDataWithText(itemsList.get(position));
            //selectedPostPosition = position;
            //updateSelectedItem();
        }
        else {
            holder.bindDataWithCollab(itemsList.get(position));
          //  selectedPostPosition = position;
        //    updateSelectedItem();
        }

    }

    @Override
    public int getItemCount() {
        return itemsList.size();
    }

    public int getItemViewType(int position) {


        String POST_TYPE= getItemByPosition(position).getPostType();
        if (POST_TYPE != null && POST_TYPE.equals("TEXT")) {
            return TEXT_POST;
        } else if (POST_TYPE != null && POST_TYPE.equals("IMAGE")) {
            return IMAGE_POST;
        } else if (POST_TYPE != null && POST_TYPE.equals("COLLAB")) {
            return COLLAB_POST;
        }
        return -1;
    }


    public void setList(List<BookMark> list) {
        itemsList.clear();


        itemsList.addAll(list);
        notifyDataSetChanged();
    }


    private PostViewHolder.OnClickListener createOnClickListener() {
        return new PostViewHolder.OnClickListener() {
            @Override
            public void onItemClick(int position, View view) {
                if (callBack != null) {
                    callBack.onItemClick(getItemByPosition(position), view);
                    selectedPostPosition = position;
                }
            }

            @Override
            public void onLikeClick(LikeController likeController, int position) {
                BookMark followingPost = getItemByPosition(position);
                likeController.handleLikeClickAction(activity, followingPost.getId());
            }

            @Override
            public void onAuthorClick(int position, View view) {
                if (callBack != null) {
                    callBack.onAuthorClick(position, view);
                }
            }

            @Override
            public void onCopyClick(int position, View view) {
                if (callBack !=null)
                {
                    callBack.onCopyClick(position , view);
                }
            }

            @Override
            public void onShareClick(int position, ImageView view , View mainView) {
                if (callBack !=null)
                {
                    callBack.onShareClick(position , view , mainView);
                }
            }

            @Override
            public void onCollabClick(int position, View view) {
                if (callBack !=null)
                {
                    callBack.onCollabClick(position , view );
                }
            }

            @Override
            public void onCollabContainerClick(int position, View view) {
                if (callBack!=null) {
                    callBack.onCollabContainerClick(position, view);
                    selectedPostPosition=position;
                }
            }

            @Override
            public void onBookMarkClick(BookMarkController bookMarkController, int position) {
                BookMark followingPost = getItemByPosition(position);
                bookMarkController.handleBookmarkAction(activity, followingPost.getId());
            }

            @Override
            public void onCollabAutherClick(int adapterPosition, View v) {
                if (callBack != null) {
                    callBack.onCollabAutherClick(adapterPosition, v);
                }
            }

        };
    }

    public BookMark getItemByPosition(int position) {

        return  itemsList.get(position);
    }

    public void updateSelectedItem() {
        if (selectedPostPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(selectedPostPosition);
        }
    }

    public interface CallBack {
        void onItemClick(BookMark followingPost, View view);

        void onAuthorClick(int position, View view);

        void onCopyClick(int position , View view);

        void onShareClick(int position , ImageView view , View mainView);

        void onCollabClick(int position , View view);

        void  onCollabContainerClick(int position , View view);

        void onCollabAutherClick(int adapterPosition, View v);
    }
    
    
}
