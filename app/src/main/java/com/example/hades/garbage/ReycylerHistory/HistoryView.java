package com.example.hades.garbage.ReycylerHistory;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.hades.garbage.HistorySingle;
import com.example.hades.garbage.R;
import com.mikhaellopez.circularimageview.CircularImageView;

public class HistoryView extends RecyclerView.ViewHolder implements View.OnClickListener {
    public TextView rideId;
    public TextView time;
    public TextView distance;
    public TextView cost_total;
    public TextView status;
    public CircularImageView mImage;
    public HistoryView(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);

        rideId = (TextView) itemView.findViewById(R.id.rideId);
        time = (TextView) itemView.findViewById(R.id.time);
        distance=(TextView) itemView.findViewById(R.id.distance);
        cost_total=(TextView) itemView.findViewById(R.id.cost);
        status=(TextView) itemView.findViewById(R.id.transaksi_status);
        mImage=(CircularImageView) itemView.findViewById(R.id.circularimage);

    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent(v.getContext(), HistorySingle.class);
        Bundle b = new Bundle();
        b.putString("rideId", rideId.getText().toString());
        intent.putExtras(b);
        v.getContext().startActivity(intent);

    }
}
