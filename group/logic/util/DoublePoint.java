package logic.util;

import logic.game.map.Field;
import messages.util.Tile;

public class DoublePoint {
    public double x;
    public double y;

    public DoublePoint(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public DoublePoint(Field field){
        this.x = (double)field.getXCoordinate();
        this.y = (double)field.getYCoordinate();
    }
    public boolean hasDiagonalDistance(DoublePoint other){
        return Math.abs(Math.abs(this.x-other.x)-Math.abs(this.y-other.y))<MathUtil.eps;
    }
    public DoublePoint swapPoint(DoublePoint other){
        DoublePoint temp = this;
        this.x = other.x;
        this.y = other.y;
        return temp;
    }
    @Override
    public boolean equals(Object obj){
        if(obj.getClass()==DoublePoint.class){
            DoublePoint other = (DoublePoint) obj;
            if(other.x==this.x&&other.y==this.y){
                return true;
            }else return false;
        }else return false;
    }
    @Override
    public String toString(){
        return "("+x +", " + y +")";
    }
}
