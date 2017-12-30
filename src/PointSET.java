import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.SET;

import java.util.ArrayList;
import java.util.List;

public class PointSET {
    private final SET<Point2D> points;

    public PointSET() {
        this.points = new SET<>();
    }

    public boolean isEmpty() {
        return this.points.isEmpty();
    }

    public int size() {
        return this.points.size();
    }

    public void insert(final Point2D p) {
        this.points.add(p);
    }

    public boolean contains(final Point2D p) {
        return this.points.contains(p);
    }

    public void draw() {
        this.points.forEach(Point2D::draw);
    }

    public Iterable<Point2D> range(final RectHV rect) {
        final List<Point2D> filteredPoints = new ArrayList<>();
        for (final Point2D point : this.points)
            if (rect.contains(point))
                filteredPoints.add(point);
        return filteredPoints;
    }


    public Point2D nearest(final Point2D p) {
        if (p == null)
            throw new IllegalArgumentException();
        if (isEmpty())
            return null;
        double smallDistance = Double.POSITIVE_INFINITY;
        Point2D targetPoint = null;
        for (final Point2D point : this.points) {
            if (p.distanceSquaredTo(point) < smallDistance) {
                smallDistance = p.distanceSquaredTo(point);
                targetPoint = point;
            }
        }
        return targetPoint;
    }


    public static void main(final String[] args) {
        final PointSET pointSET = new PointSET();
        pointSET.insert(new Point2D(1, 1));
        pointSET.insert(new Point2D(1, 2));
        pointSET.insert(new Point2D(2, 1));
        pointSET.insert(new Point2D(2, 2));
        System.out.println("Intersects 2x2: " + pointSET.range(new RectHV(1, 1, 2, 2)));
        System.out.println("Nearest to (0,0) = " + pointSET.nearest(new Point2D(0, 0)));
    }
}
