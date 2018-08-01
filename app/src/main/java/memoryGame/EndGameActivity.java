package memoryGame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.sonya.myapplication.R;

import memoryGame.bl.Player;

public class EndGameActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end);

        Button btnNewGame = (Button)findViewById(R.id.btn_new_game);
        Button btnExit = (Button)findViewById(R.id.btn_exit);

        Intent intent =  getIntent();

        TextView text = (TextView)findViewById(R.id.tv_msg);
        text.setText(intent.getStringExtra("msg"));

        final Player player = (Player) intent.getSerializableExtra("player");

        btnNewGame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getBaseContext(), SelectLevelActivity.class);
                i.putExtra("player", player);
                startActivity(i);
                finish();
            }
        });

        btnExit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
}




