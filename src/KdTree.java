import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.Queue;
import edu.princeton.cs.algs4.RectHV;
import edu.princeton.cs.algs4.StdDraw;

import java.util.Comparator;

public class KdTree {
    private int size;
    private final Node rootNode;
    private static final RectHV UNIT_SQUARE = new RectHV(0.0, 0.0, 1.0, 1.0);

    private enum InsertStrategy {
        COMPARE_BY_X,
        COMPARE_BY_Y
    }

    public KdTree() {
        this.rootNode = new Node();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int size() {
        return this.size;
    }

    public void insert(final Point2D p) {
        if (p == null)
            throw new IllegalArgumentException();
        this.rootNode.insert(p, InsertStrategy.COMPARE_BY_X);
        this.size++;
    }

    public boolean contains(final Point2D p) {
        if (p == null)
            throw new IllegalArgumentException();
        return this.rootNode.contains(p);
    }

    public void draw() {
        if (isEmpty())
            return;
        final boolean dir = true;
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.line(0, 0, 0, 1);
        StdDraw.line(0, 0, 1, 0);
        StdDraw.line(1, 1, 0, 1);
        StdDraw.line(1, 1, 1, 0);
        internalDraw(this.rootNode, dir, 0, 1, 0, 1);
    }


    private void internalDraw(final Node node, final boolean dir, final double xmin, final double xmax, final double ymin, final double ymax) {
        if (node == null)
            return;
        // OY vertical line, x fixed
        if (dir) {
            StdDraw.setPenColor(StdDraw.RED);
            StdDraw.line(node.point.x(), ymin, node.point.x(), ymax);
            internalDraw(node.left, !dir, xmin, node.point.x(), ymin, ymax);
            internalDraw(node.right, !dir, node.point.x(), xmax, ymin, ymax);
        } else {
            StdDraw.setPenColor(StdDraw.BLUE);
            StdDraw.line(xmin, node.point.y(), xmax, node.point.y());
            internalDraw(node.left, !dir, xmin, xmax, ymin, node.point.y());
            internalDraw(node.right, !dir, xmin, xmax, node.point.y(), ymax);
        }
        StdDraw.setPenColor(StdDraw.BLACK);
        StdDraw.setPenRadius(.01);
        node.point.draw();
    }


    public Iterable<Point2D> range(final RectHV rect) {
        if (rect == null)
            throw new IllegalArgumentException();
        final Queue<Point2D> pointsInRect = new Queue<>();
        findRange(this.rootNode, UNIT_SQUARE, rect, pointsInRect);
        return pointsInRect;

    }

    private void findRange(final Node node, final RectHV nodeRect, final RectHV queryRect, final Queue<Point2D> rectPoints) {
        if (node == null) {
            return;
        }
        if (!queryRect.intersects(nodeRect))
            return;

        if (queryRect.contains(node.point)) {
            rectPoints.enqueue(node.point);
        }

        findRange(node.left, createLeftSubtreeRect(node, nodeRect), queryRect, rectPoints);
        findRange(node.right, createRightSubtreeRect(node, nodeRect), queryRect, rectPoints);

    }

    private RectHV createLeftSubtreeRect(final Node node, final RectHV nodeRect) {
        return node.isVertical
                ? new RectHV(nodeRect.xmin(), nodeRect.ymin(), node.point.x(), nodeRect.ymax())
                : new RectHV(nodeRect.xmin(), nodeRect.ymin(), nodeRect.xmax(), node.point.y());
    }

    private RectHV createRightSubtreeRect(final Node node, final RectHV nodeRect) {
        return node.isVertical
                ? new RectHV(node.point.x(), nodeRect.ymin(), nodeRect.xmax(), nodeRect.ymax())
                : new RectHV(nodeRect.xmin(), node.point.y(), nodeRect.xmax(), nodeRect.ymax());
    }


    public Point2D nearest(final Point2D p) {
        if (p == null)
            throw new IllegalArgumentException();
        return this.rootNode.nearestPoint(p);
    }

    private class Node {
        Node left, right;
        Point2D point;
        boolean isVertical;

        Node() {
            this.isVertical = true;
        }

        void insert(final Point2D p, final InsertStrategy insertStrategy) {
            if (this.point == null) {
                this.point = p;
                if (insertStrategy == InsertStrategy.COMPARE_BY_X)
                    this.isVertical = true;
                else
                    this.isVertical = false;
                return;
            }
            if (insertStrategy == InsertStrategy.COMPARE_BY_X) {
                insert(p, Comparator.comparingDouble(Point2D::x), InsertStrategy.COMPARE_BY_Y);
            } else if (insertStrategy == InsertStrategy.COMPARE_BY_Y) {
                insert(p, Comparator.comparingDouble(Point2D::y), InsertStrategy.COMPARE_BY_X);
            }
        }

        private void insert(final Point2D p, final Comparator<Point2D> comparator, final InsertStrategy insertStrategy) {
            if (comparator.compare(p, this.point) < 0) {
                if (this.left == null)
                    this.left = new Node();
                this.left.insert(p, insertStrategy);
                return;
            }
            if (this.right == null)
                this.right = new Node();
            this.right.insert(p, insertStrategy);
        }

        public boolean contains(final Point2D p) {
            if (this.point == null)
                return false;
            if (p.equals(this.point))
                return true;
            if (this.point.x() > p.x()) {
                return this.left != null ? this.left.contains(p) : false;
            }
            return this.right != null ? this.right.contains(p) : false;
        }

        @Override
        public String toString() {
            return this.point != null ? this.point.toString() : "null";
        }

        public Point2D nearestPoint(final Point2D p) {
            return doNearest(KdTree.this.rootNode, UNIT_SQUARE, p, null);
        }

        private Point2D doNearest(final Node node, final RectHV nodeRect, final Point2D queryPoint, final Point2D nearestPoint) {
            if (node == null) {
                return nearestPoint;
            }

            Point2D nearestPointCandidate = nearestPoint;
            final double nearestDist = (nearestPointCandidate != null)
                    ? queryPoint.distanceSquaredTo(nearestPointCandidate)
                    : Double.POSITIVE_INFINITY;

            if (nearestDist > nodeRect.distanceSquaredTo(queryPoint)) {
                final double dist = queryPoint.distanceSquaredTo(node.point);
                if (dist < nearestDist) {
                    nearestPointCandidate = node.point;
                }

                final RectHV leftNodeRect = createLeftSubtreeRect(node, nodeRect);
                final RectHV rightNodeRect = createRightSubtreeRect(node, nodeRect);

                if (isSmallerThanPointInNode(queryPoint, node)) {
                    // explore left subtree first
                    nearestPointCandidate = doNearest(node.left, leftNodeRect, queryPoint, nearestPointCandidate);
                    nearestPointCandidate = doNearest(node.right, rightNodeRect, queryPoint, nearestPointCandidate);
                } else {
                    // explore right subtree first
                    nearestPointCandidate = doNearest(node.right, rightNodeRect, queryPoint, nearestPointCandidate);
                    nearestPointCandidate = doNearest(node.left, leftNodeRect, queryPoint, nearestPointCandidate);
                }
            }

            return nearestPointCandidate;
        }

        private boolean isSmallerThanPointInNode(final Point2D p, final Node node) {
            final int cmp = node.isVertical ? Point2D.X_ORDER.compare(p, node.point) : Point2D.Y_ORDER.compare(p, node.point);
            return (cmp < 0);
        }


    }


    public static void main(final String[] args) {
        final KdTree kdTree = new KdTree();
        System.out.println(kdTree.size());
        kdTree.insert(new Point2D(0.7, 0.2));
        kdTree.insert(new Point2D(0.5, 0.4));
        kdTree.insert(new Point2D(0.2, 0.3));
        kdTree.insert(new Point2D(0.4, 0.7));
        kdTree.insert(new Point2D(0.9, 0.6));
        //  kdTree.draw();
        System.out.println("Size: " + kdTree.size());

        System.out.println("Intersects 2x2: " + kdTree.range(new RectHV(0, 0, 2, 2)));
        System.out.println("Nearest to (0,0) = " + kdTree.nearest(new Point2D(0, 0)));
    }
}
