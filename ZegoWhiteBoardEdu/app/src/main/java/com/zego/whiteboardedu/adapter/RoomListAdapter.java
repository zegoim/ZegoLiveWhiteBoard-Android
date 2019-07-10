package com.zego.whiteboardedu.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zego.whiteboardedu.R;
import com.zego.whiteboardedu.entity.RoomInfo;

import java.util.List;

public class RoomListAdapter extends RecyclerView.Adapter<RoomListAdapter.ViewHolder> {

    private List<RoomInfo> mRoomInfoList;

    private OnRoomInfoItemClickListener mOnRoomInfoItemClickListener;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_room_layout, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        RoomInfo roomInfo = mRoomInfoList.get(position);
        viewHolder.mTvRoomName.setText(roomInfo.roomName);
        viewHolder.mTvRoomName.setTag(roomInfo);
    }

    @Override
    public int getItemCount() {
        return mRoomInfoList == null ? 0 : mRoomInfoList.size();
    }

    public void setRoomInfoList(List<RoomInfo> roomInfoList) {
        this.mRoomInfoList = roomInfoList;
        notifyDataSetChanged();
    }

    public void setOnRoomInfoItemClickListener(OnRoomInfoItemClickListener onRoomInfoItemClickListener) {
        this.mOnRoomInfoItemClickListener = onRoomInfoItemClickListener;
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView mTvRoomName;

        private ViewHolder(View view) {
            super(view);
            mTvRoomName = view.findViewById(R.id.tv_room_name);
            mTvRoomName.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            RoomInfo roomInfo = (RoomInfo) v.getTag();
            if (mOnRoomInfoItemClickListener != null) {
                mOnRoomInfoItemClickListener.onRoomInfoItemClick(roomInfo);
            }
        }
    }

    public interface OnRoomInfoItemClickListener {
        void onRoomInfoItemClick(RoomInfo roomInfo);
    }
}
