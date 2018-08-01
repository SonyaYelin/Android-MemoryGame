package memoryGame;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.sonya.myapplication.R;

import java.util.ArrayList;

import memoryGame.bl.Score;
import memoryGame.ui.ScoreAdapter;

public class RecordTableFragment extends Fragment {

    private ListView                        scoreList;
    private ScoreAdapter                    scoreAdapter;

    private ArrayList<Score>                scores = new ArrayList<>();

    public RecordTableFragment(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        scoreList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.getFocusables(position);
                view.setSelected(true);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view  = inflater.inflate(R.layout.fragment_record_table, container, false);

        scoreList = view.findViewById(R.id.score_list);
        scoreAdapter = new ScoreAdapter(getContext(),scores);
        scoreList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        scoreList.setAdapter(scoreAdapter);
        return view;
    }

    public void showTable(ArrayList<Score>scores){
        this.scores.clear();
        if(scores.size()>0)
            this.scores.addAll(scores);
        if (scoreAdapter != null )
            scoreAdapter.notifyDataSetChanged();
    }
}

