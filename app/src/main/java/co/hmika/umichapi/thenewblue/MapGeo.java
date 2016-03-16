package co.hmika.umichapi.thenewblue;

import android.graphics.Point;
import android.graphics.PointF;

public class MapGeo {

    /**
     * Returns closest point on segment to point
     *
     * @param ss
     *            segment start point
     * @param se
     *            segment end point
     * @param p
     *            point to found closest point on segment
     * @return closest point on segment to p
     */
    public static PointF getClosestPointOnSegment(PointF ss, PointF se, PointF p)
    {
        return getClosestPointOnSegment(ss.x, ss.y, se.x, se.y, p.x, p.y);
    }

    /**
     * Returns closest point on segment to point
     *
     * @param sx1
     *            segment x coord 1
     * @param sy1
     *            segment y coord 1
     * @param sx2
     *            segment x coord 2
     * @param sy2
     *            segment y coord 2
     * @param px
     *            point x coord
     * @param py
     *            point y coord
     * @return closets point on segment to point
     */
    public static PointF getClosestPointOnSegment(float sx1, float sy1, float sx2, float sy2, float px, float py)
    {
        double xDelta = sx2 - sx1;
        double yDelta = sy2 - sy1;

        if ((xDelta == 0) && (yDelta == 0)) {
            throw new IllegalArgumentException("Segment start equals segment end");
        }

        double u = ((px - sx1) * xDelta + (py - sy1) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

        final PointF closestPoint;
        if (u < 0)
        {
            closestPoint = new PointF(sx1, sy1);
        }
        else if (u > 1)
        {
            closestPoint = new PointF(sx2, sy2);
        }
        else
        {
            closestPoint = new PointF((float) (sx1 + u * xDelta), (float) (sy1 + u * yDelta));
        }

        return closestPoint;
    }
}

