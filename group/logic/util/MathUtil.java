package logic.util;


import logic.game.GameInstance;
import logic.game.map.Field;
import messages.util.Tile;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * X * * *
 * * * - *
 * * | * |
 * * * - *
 * spickzettel_8.pdf
 */
public class MathUtil {
    DoublePoint start;
    DoublePoint end;
    static final double eps = 0.0000001d;
    public static Random random = new Random();

    public List<DoublePoint> getIntersectingFieldCoordinates(Field originField, Field targetField, GameInstance gameInstance){
        List<DoublePoint> intersections = new LinkedList<DoublePoint>();
        start = new DoublePoint(originField);
        end = new DoublePoint(targetField);

        List<DoublePoint> sandStormCoordinates = new LinkedList<DoublePoint>();

        for (int i = 0; i < gameInstance.getGameMap().getXSize(); i++) {
            for (int j = 0; j < gameInstance.getGameMap().getYSize(); j++) {
                Tile tile = gameInstance.getGameMap().getField(i, j).fieldToTile(gameInstance);
                if (tile.isInSandstorm) {
                    sandStormCoordinates.add(new DoublePoint(i, j));
                }
            }
        }
          if(!start.hasDiagonalDistance(end)&&start.x!=end.x&&start.y!=end.y) {

            for (DoublePoint sandStCoord : sandStormCoordinates) {
                //Create 4 borders
                DoublePoint bUpl = new DoublePoint(sandStCoord.x - 0.5d, sandStCoord.y - 0.5d);
                DoublePoint bLup = bUpl;
                DoublePoint bUpr = new DoublePoint(sandStCoord.x + 0.5d, sandStCoord.y - 0.5d);
                DoublePoint bRup = bUpr;
                DoublePoint bRlo = new DoublePoint(sandStCoord.x + 0.5d, sandStCoord.y + 0.5d);
                DoublePoint bLor = bRlo;
                DoublePoint bLol = new DoublePoint(sandStCoord.x - 0.5d, sandStCoord.y + 0.5d);
                DoublePoint bLlo = bLol;

                if (isSegmentIntersect(start, end, bUpl, bUpr) == 1) {
                    //Upper
                    intersections.add(sandStCoord);
                } else if (isSegmentIntersect(start, end, bRup, bRlo) == 1) {
                    //Right
                    intersections.add(sandStCoord);
                } else if (isSegmentIntersect(start, end, bLol, bLor) == 1) {
                    //Down
                    intersections.add(sandStCoord);
                } else if (isSegmentIntersect(start, end, bLup, bLlo) == 1) {
                    //Left
                    intersections.add(sandStCoord);
                }
            }
            return intersections;
        }else if(start!=end){
            if(start.hasDiagonalDistance(end)){
                if(start.x<end.x){
                    end = start.swapPoint(end);
                }
                for (int i = 0; i <= (int)Math.abs(start.x-end.x); i++) {
                    if(sandStormCoordinates.contains(new DoublePoint(start.x-i,start.y-i))) intersections.add(new DoublePoint(start.x-i,start.y-i));
                }
            }else if(start.x==end.x){
                if(start.y<end.y){
                    end = start.swapPoint(end);
                }
                for (int i = 0; i < (int)Math.abs(start.y-end.y); i++) {
                    if(sandStormCoordinates.contains(new DoublePoint(start.x, start.y-i))) intersections.add(new DoublePoint(start.x, start.y-i));
                }
            }else{
                if(start.x<end.x){
                    end = start.swapPoint(end);
                }
                for (int i = 0; i < (int)Math.abs(start.x-end.x); i++) {
                    if(sandStormCoordinates.contains(new DoublePoint(start.x-i, start.y))) intersections.add(new DoublePoint(start.x-i, start.y));
                }
            }
            return  intersections;
        }else{
            return null;
        }

    }
    private int CounterClockWiseTest(DoublePoint p0,DoublePoint p1,DoublePoint p2){
            double d1 =(p1.x-p0.x)*(p2.y-p0.y);
            double d2 =(p2.x-p0.x)*(p1.y-p0.y);
            if((d1-d2>eps)&&(d2-d1>eps)) return 0;
            if((d1-d2>eps)&&!(d2-d1>eps)) return 1;
            if(!(d1-d2>eps)&&(d2-d1>eps)) return -1;
            if(!(d1-d2>eps)&&!(d2-d1>eps)) return 0;
            return 0;
    }
    private int isPointOnSegment(DoublePoint p, DoublePoint a0, DoublePoint a1){
        if(CounterClockWiseTest(a0,a1,p)!=0) return 0;
        double cx = (p.x-a0.x)*(p.x-a1.x);
        double cy = (p.y-a0.y)*(p.y-a1.y);
        if(cx > eps || cy > eps) return 0;
        if(cx < -eps || cy < -eps) return 2;
        return 1;
    }
    private int isSegmentIntersect(DoublePoint a0, DoublePoint a1, DoublePoint b0, DoublePoint b1){

        int c1 = CounterClockWiseTest(a0, a1, b0);
        int c2 = CounterClockWiseTest(a0, a1, b1);
        int c3 = CounterClockWiseTest(b0, b1, a0);
        int c4 = CounterClockWiseTest(b0, b1, a1);
        if (c1*c2>0 || c3*c4>0) return 0;
        if (c1==0 && c2==0 && c3==0 && c4==0) {
            c1 = isPointOnSegment(a0,b0,b1);
            c2 = isPointOnSegment(a1,b0,b1);
            c3 = isPointOnSegment(b0,a0,a1);
            c4 = isPointOnSegment(b1,a0,a1);
            if (c1>=1 && c2>=1 && c3>=1 && c4>=1) {
                if ((a0.x != a1.x || a0.y != a1.y)) return 2;
                return 1;
            }
            if (c1 + c2 + c3 + c4 == 0) {
                return 0;
            }
            return 3 - Math.max(Math.max(c1, c2), Math.max(c3, c4));
        }
        if(c1 == 0 || c2 == 0 || c3 == 0 || c4 == 0) return 2;
        return 1;
    }

}
