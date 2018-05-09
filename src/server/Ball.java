/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.awt.Color;
import java.io.Serializable;

/**
 *
 * @author Pedro Vivaldi
 */
public class Ball implements Serializable {

    private String name;
    private Color color;
    private int x;
    private int y;
    private int radius;

    public Ball(int x, int y, int radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void move(int dx, int dy) {
        this.x += dx;
        this.y += dy;
    }
}
