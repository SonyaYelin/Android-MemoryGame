package memoryGame;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.sonya.myapplication.R;

import memoryGame.bl.Player;

public class SelectLevelActivity  extends AppCompatActivity implements IConstants {

        private Player   player;

        private Button  btnEasy;
        private Button  btnNormal;
        private Button  btnHard;
        private Button  btnRecords;

        @Override
        protected void onCreate(Bundle savedInstanceState){
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_select_level);

                Intent intent = getIntent();
                player = (Player) intent.getSerializableExtra(PLAYER);

                TextView tvDetails = (TextView) findViewById(R.id.tv_details);
                tvDetails.setText(NAME + player.getName() +", " + AGE + player.getAge());

                btnEasy = (Button)findViewById(R.id.btn_easy);
                btnNormal = (Button)findViewById(R.id.btn_normal);
                btnHard = (Button)findViewById(R.id.btn_hard);
                btnRecords = (Button)findViewById(R.id.btn_records);
                setListeners();
        }

        private void setListeners(){
            btnEasy.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            startGame(LEVEL_1);
                        }
                });

            btnNormal.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {startGame(LEVEL_2);
                        }
                });

            btnHard.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {startGame(LEVEL_3);
                        }
                });

            btnRecords.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Intent intent = new Intent(getBaseContext(), RecordsActivity.class);
                    startActivity(intent);
                }
            });
        }

        private void startGame(int level){
                Intent intent = new Intent(getBaseContext(), GameActivity.class);
                intent.putExtra(LEVEL,level);
                intent.putExtra(PLAYER,player);
                startActivity(intent);
                finish();
        }
}
