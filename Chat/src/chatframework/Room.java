/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chatframework;

//import java.util.ArrayList;

/**
 *
 * @author Aantokhin
 */
public class Room {
    private String name;
    //private ArrayList<Integer> users = new ArrayList<>();

    public Room(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
}
