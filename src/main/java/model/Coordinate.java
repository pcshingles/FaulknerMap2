package model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.awt.*;

@Entity
@Table(name = "COORDINATES")
public class Coordinate {
    private Long id;

    private double x, y;
    private int floor;

    public Coordinate() {
        // Left empty for hibernate
    }

    public Coordinate(double x, double y, int floor) {
        this.x = x;
        this.y = y;
        this.floor = floor;
    }

    @Id
    @GeneratedValue(generator="increment")
    @GenericGenerator(name="increment", strategy = "increment")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "COORD_X")
    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    @Column(name = "COORD_Y")
    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Column(name = "COORD_FLOOR")
    public int getFloor() {
        return floor;
    }

    public void setFloor(int floor) {
        this.floor = floor;
    }

    public String toString(){
        return "Node Location: [" + x + "," + y + "," + floor + "]";
    }

    @Transient
    public Point.Double getPoint() {
        return new Point.Double(x, y);
    }
}
