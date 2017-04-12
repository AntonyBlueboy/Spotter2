package ua.com.spottertest.spotter2.frontend;

import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ua.com.spottertest.spotter2.R;

/**
 * Created by Rudolf on 21.02.2017.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder>{

    ArrayList<String> arrayList = new ArrayList<>();
    View.OnClickListener listener;

    public RecyclerAdapter(ArrayList<String> arrayList, View.OnClickListener listener) {
        this.arrayList = arrayList;
        this.listener = listener;
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_theory_field, parent, false);
        RecyclerViewHolder viewHolder = new RecyclerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerViewHolder holder, int position) {
        holder.textView.setText(arrayList.get(position));
        holder.textView.setOnClickListener(listener);
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public static class RecyclerViewHolder extends RecyclerView.ViewHolder{
        TextView textView;

        public RecyclerViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tx_item);
        }
    }
}
