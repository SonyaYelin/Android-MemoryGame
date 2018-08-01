package memoryGame.bl;

import android.location.Location;
import com.google.android.gms.maps.model.LatLng;

public class Score {

    private int     value;
    private String  name;
    private LatLng  location;
    private String  strLocation;

    public Score(int value, String name, Location location, String strLocation){
        this.value = value;
        this.name = name;
        this.strLocation = strLocation;

        if ( location != null )
            this.location = new LatLng(location.getLatitude(),location.getLongitude());
    }

    public Score(int value, String name, LatLng location, String strLocation){
        this.value = value;
        this.name = name;
        this.strLocation = strLocation;

        if ( location != null )
            this.location = location;
    }

    public int getValue(){
        return value;
    }

    public String getName(){ return name; }

    public LatLng getLocation(){
        return location;
    }

    public String getStrLocation(){
        return strLocation;
    }
}

