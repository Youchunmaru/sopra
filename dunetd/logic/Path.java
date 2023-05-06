package io.swapastack.dunetd.logic;

import java.util.ArrayList;
import java.util.List;

/**
 * calculates the way the enemy's have to go.
 *
 * @author Samuel Gröner
 * */
public class Path {

  private static final Path INSTANCE = new Path();
  private static Point START;
  private static Point END;
  public static int[][] map;
  public List<Point> path;

  private Path() {
    map = new int[DuneTDMap.HEIGHT][DuneTDMap.WIDTH];
  }

  public static Path getINSTANCE(Point start, Point end){
    START = start;
    END = end;
    return INSTANCE;
  }
  /**
   * updates the path
   * */
  public void update(){
    path = findPath(map, START, END);
    if (path != null) {
        //System.out.println("Path found");
      path.add(0,DuneTDMap.START);
    }
    else {
      //System.err.println("No path found");
    }
  }
  /**
   * finds the path
   * */
  public List<Point> findPath(int[][] map, Point start, Point end) {
    boolean finished = false;
    List<Point> used = new ArrayList<>();
    used.add(start);
    while (!finished) {
      List<Point> open = new ArrayList<>();
      for(int i = 0; i < used.size(); ++i){
        Point point = used.get(i);
        for (Point neighbor : findNeighbors(map, point)) {
          if (!used.contains(neighbor) && !open.contains(neighbor)) {
            open.add(neighbor);
          }
        }
      }
      for(Point point : open) {
        used.add(point);
        if (end.equals(point)) {
          finished = true;
          break;
        }
      }
      if (!finished && open.isEmpty())
        return null;
    }
    List<Point> path = new ArrayList<>();
    Point point = used.get(used.size() - 1);
    while(point.previous != null) {
      path.add(0, point);
      point = point.previous;
    }
    return path;
  }
  /**
   * looks if the next tile is passable
   * */
  public boolean isPassable(int[][] map, Point point) {
    if (point.y < 0 || point.y > map.length - 1) {
      return false;
    }
    if (point.x < 0 || point.x > map[0].length - 1){
      return false;
    }
    return map[point.y][point.x] == 0;
  }
  /**
   * looks which tiles are next to each other
   * */
  public List<Point> findNeighbors(int[][] map, Point point) {
    List<Point> neighbors = new ArrayList<>();
    Point up = point.offset(0,  1);
    Point down = point.offset(0,  -1);
    Point left = point.offset(-1, 0);
    Point right = point.offset(1, 0);

    if (isPassable(map, up)) {
      neighbors.add(up);
    }
    if (isPassable(map, down)) {
      neighbors.add(down);
    }
    if (isPassable(map, left)) {
      neighbors.add(left);
    }
    if (isPassable(map, right)) {
      neighbors.add(right);
    }
    return neighbors;
  }
}
