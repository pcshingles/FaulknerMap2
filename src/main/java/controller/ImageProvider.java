package controller;

import java.util.HashMap;
import java.util.Map;

import Singleton.LoginStatusSingleton;
import javafx.scene.image.Image;

public class ImageProvider {
    private static final Map<String, Image> images = new HashMap<>();

    public static Image getImage(String name) {
        if (!images.containsKey(name)) {
            Image newImage = new Image(name);
            images.put(name, newImage);
            return newImage;
        }
        return images.get(name);
    }

    public static Image getEmployeeImageByFloor(int i){
        String ImageName = "";
        switch (i){
            case 1:
                ImageName = "images/floor1.png";
                break;
            case 2:
                ImageName = "images/floor2.png";
                break;
            case 3:
                ImageName = "images/floor3.png";
                break;
            case 4:
                ImageName = "images/floor4.png";
                break;
            case 5:
                ImageName = "images/floor5.png";
                break;
            case 6:
                ImageName = "images/floor6.png";
                break;
            case 7:
                ImageName = "images/floor7.png";
                break;
        }
        return getImage(ImageName);
    }

    public static Image getImageByFloor(int i){
        // if admin, use the employee
        if(LoginStatusSingleton.getInstance().getMemento().getStatus()){
            getEmployeeImageByFloor(i);
        }

        String ImageName ="";
        switch(i) {
            case 1:
                ImageName = "images/1_thefirstfloor.png";
                break;
            case 2:
                ImageName = "images/2_thesecondfloor.png";
                break;
            case 3:
                ImageName = "images/3_thethirdfloor.png";
                break;
            case 4:
                ImageName = "images/4_thefourthfloor.png";
                break;
            case 5:
                ImageName = "images/5_thefifthfloor.png";
                break;
            case 6:
                ImageName = "images/6_thesixthfloor.png";
                break;
            case 7:
                ImageName = "images/7_theseventhfloor.png";
                break;
        }
        return getImage(ImageName);
    }
}
