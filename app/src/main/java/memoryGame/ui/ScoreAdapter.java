package memoryGame.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.sonya.myapplication.R;

import java.util.ArrayList;

import memoryGame.bl.Score;

public class ScoreAdapter extends BaseAdapter {

    private Context             context;
    private ArrayList<Score>    scoreList;
    private LayoutInflater      inflater;
    private ViewHolder          viewHolder;

    public ScoreAdapter(Context context) {
        this.context = context;
    }

    public ScoreAdapter(Context context, ArrayList<Score> scoreList) {
        this.context = context;
        this.scoreList = scoreList;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return scoreList.size();
    }

    @Override
    public Object getItem(int position) {
        return scoreList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate((R.layout.list_item), parent, false);
            viewHolder = new ViewHolder();
            viewHolder.nameTextView = convertView.findViewById(R.id.col_name);
            viewHolder.scoreTextView = convertView.findViewById(R.id.col_score);
            viewHolder.LocationTextView = convertView.findViewById(R.id.col_location);
            convertView.setTag(viewHolder);
        } else
            viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.nameTextView.setText(scoreList.get(position).getName());
        viewHolder.scoreTextView.setText(scoreList.get(position).getValue() + "");
        viewHolder.LocationTextView.setText(scoreList.get(position).getStrLocation().toString());
        return convertView;
    }

    static class ViewHolder {
        private TextView nameTextView;
        private TextView scoreTextView;
        private TextView LocationTextView;
    }
}