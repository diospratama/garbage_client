package com.example.hades.garbage.ReycylerHistory;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.example.hades.garbage.R;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryView> {

    private List<HistoryObject> itemList;
    private Context context;

    public HistoryAdapter(List<HistoryObject> itemlist,Context context){
        this.itemList=itemlist;
        this.context=context;
    }
    @Override
    public HistoryView onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView= LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history,null,false);
        RecyclerView.LayoutParams lp= new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);
        HistoryView rcv = new HistoryView(layoutView);
        return rcv;
    }

    @Override
    public void onBindViewHolder(HistoryView holder, int position) {
        holder.rideId.setText(itemList.get(position).getRideId());
        if(itemList.get(position).getTime()!=null){
            holder.time.setText(itemList.get(position).getTime());

        }
        if(itemList.get(position).getDistance()!=null){
            holder.distance.setText(itemList.get(position).getDistance());
        }
        if(itemList.get(position).getCostTotal()!=null){
            holder.cost_total.setText(itemList.get(position).getCostTotal());
        }
        if(itemList.get(position).getStatus_transaksi()!=null){
            holder.status.setText(itemList.get(position).getStatus_transaksi());
        }

        if(itemList.get(position).getProfilImageUrl()!=null){
            Glide.with(context).load(itemList.get(position).getProfilImageUrl()).into(holder.mImage);
        }


    }

    @Override
    public int getItemCount() {
        return this.itemList.size();
    }
}
