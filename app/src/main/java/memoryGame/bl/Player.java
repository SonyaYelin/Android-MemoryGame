package memoryGame.bl;


import java.io.Serializable;

public class Player implements Serializable {

    private String  name;
    private int     age;

    public Player(String name, int age){
        this.name = name;
        this.age = age;
    }

    public String getName(){
        return name;
    }

    public int getAge(){
        return age;
    }


}

