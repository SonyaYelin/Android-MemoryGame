

package memoryGame;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.sonya.myapplication.R;

import memoryGame.bl.Player;

public class EnterActivity extends AppCompatActivity implements IConstants {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter);

        checkLocationPrenissions();

        Button btnEnter = (Button)findViewById(R.id.btn_enter);
        btnEnter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                goToSelectLevel();
            }
        });
    }

    private void goToSelectLevel(){
        final EditText etName = (EditText) findViewById(R.id.et_name);
        final EditText etAge = (EditText) findViewById(R.id.et_age);

        Player player = new Player(getName(etName), getAge(etAge));

        Intent intent = new Intent(getBaseContext(), SelectLevelActivity.class);
        intent.putExtra(PLAYER, player);
        startActivity(intent);
        finish();
    }

    private String getName(EditText etName){
        Editable editableName = etName.getText();
        String name;
        if (TextUtils.isEmpty(editableName) )
            name = DEFAULT_NAME;
        else
            name = editableName.toString();
        return name;
    }

    private int getAge(EditText etAge){
        Editable editableAge = etAge.getText();
        int age;
        if (TextUtils.isEmpty(editableAge))
            age = DEFAULT_AGE;
        else
            age = Integer.parseInt(editableAge.toString());
        return age;
    }

    //location
    private void checkLocationPrenissions(){
        final int code = 1;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},1);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions,int[] grantResults){

    }
}

