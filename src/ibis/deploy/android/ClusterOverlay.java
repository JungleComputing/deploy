package ibis.deploy.android;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.RemoteException;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class ClusterOverlay extends Overlay {

    private static final int MAX_RETRY = 20;

    private DeployService mDeployService;

    private String mSelectedCluster;

    private int mResourceCount;

    private int mZoomLevel = -1;

    // private Map<String, Point> mOffsets = new HashMap<String, Point>();
    private Map<Integer, Map<String, Point>> mOffsetTable = new HashMap<Integer, Map<String, Point>>();

    public ClusterOverlay() {
        super();
    }

    public void setDeployService(DeployService deployService) {
        mDeployService = deployService;
    }

    public void setSelectedCluster(String selectedCluster) {
        mSelectedCluster = selectedCluster;
    }

    public String getSelectedCluster() {
        return mSelectedCluster;
    }

    public void addResourceCount(int resourceCount) {
        mResourceCount += resourceCount;
    }

    public void setResourceCount(int resourceCount) {
        mResourceCount = resourceCount;
    }

    public int getResourceCount() {
        return mResourceCount;
    }

    // deployService should be null
    public Map<String, Point> calculateOffsets(MapView mapView)
            throws RemoteException {

        Projection projection = mapView.getProjection();
        List<String> clusterNames = mDeployService.getClusterNames();
        Map<String, Point> offsets = new HashMap<String, Point>();
        for (String clusterName : clusterNames) {
            offsets.put(clusterName, new Point());
        }

        int retry = 0;
        for (int i = 0; i < clusterNames.size(); i++) {
            Point p1 = new Point();
            projection.toPixels(
                    new GeoPoint((int) (mDeployService.getLatitude(clusterNames
                            .get(i)) * 1000000), (int) (mDeployService
                            .getLongitude(clusterNames.get(i)) * 1000000)), p1);
            final int nodes = mDeployService.getNodes(clusterNames.get(i));
            final int radius = nodes / 2;
            for (String otherClusterName : offsets.keySet()) {
                if (clusterNames.get(i).equals(otherClusterName)) {
                    continue;
                }
                Point p2 = new Point();
                projection
                        .toPixels(
                                new GeoPoint(
                                        (int) (mDeployService
                                                .getLatitude(otherClusterName) * 1000000),
                                        (int) (mDeployService
                                                .getLongitude(otherClusterName) * 1000000)),
                                p2);
                final int otherNodes = mDeployService
                        .getNodes(otherClusterName);
                final int otherRadius = otherNodes / 2;

                Point p1WithOffset = new Point(p1);
                p1WithOffset.offset(offsets.get(clusterNames.get(i)).x, offsets
                        .get(clusterNames.get(i)).y);

                Point p2WithOffset = new Point(p2);
                p2WithOffset.offset(offsets.get(otherClusterName).x, offsets
                        .get(otherClusterName).y);

                final double distance = distance(p1WithOffset, p2WithOffset);
                if (distance < radius + otherRadius) {
                    Point deltaOffset = new Point();
                    if (distance == 0) {
                        deltaOffset.offset(radius + otherRadius, radius
                                + otherRadius);
                    } else {
                        double deltaX = (p1WithOffset.x - p2WithOffset.x)
                                * (distance - radius - otherRadius) / distance;
                        double deltaY = (p1WithOffset.y - p2WithOffset.y)
                                * (distance - radius - otherRadius) / distance;
                        deltaOffset.offset((int) -deltaX, (int) -deltaY);
                    }
                    // now decide who's going to move: p1 or p2
                    // if ((deltaOffset.x < 0 &&
                    // offsets.get(clusterNames.get(i)).x > 0)
                    // || (deltaOffset.x > 0 && offsets.get(clusterNames
                    // .get(i)).x < 0)) {
                    // offsets.get(clusterNames.get(i)).x += deltaOffset.x;
                    // } else {
                    // offsets.get(otherClusterName).x -= deltaOffset.x;
                    // }
                    // if ((deltaOffset.y < 0 &&
                    // offsets.get(clusterNames.get(i)).y > 0)
                    // || (deltaOffset.y > 0 && offsets.get(clusterNames
                    // .get(i)).y < 0)) {
                    // offsets.get(clusterNames.get(i)).y += deltaOffset.y;
                    // } else {
                    // offsets.get(otherClusterName).y -= deltaOffset.y;
                    // }

                    double randX = Math.random();
                    double randY = Math.random();
                    offsets.get(clusterNames.get(i)).x += randX
                            * (deltaOffset.x + retry);
                    offsets.get(otherClusterName).x -= (1.0 - randX)
                            * (deltaOffset.x + retry);
                    offsets.get(clusterNames.get(i)).y += randY
                            * (deltaOffset.y + retry);
                    offsets.get(otherClusterName).y -= (1.0 - randY)
                            * (deltaOffset.y + retry);

                    // restart the loop from the beginning!
                    if (retry < MAX_RETRY
                            && (Math.abs(deltaOffset.x) > 4 || Math
                                    .abs(deltaOffset.y) > 4)) {
                        retry++;
                        i = 0;
                        break;
                    } else {
                    }

                }
            }
        }
        return offsets;
    }

    public double distance(Point p1, Point p2) {
        return Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    public void drawCluster(Canvas canvas, MapView mapView, String clusterName,
            boolean selected) throws RemoteException {
        // this should be guaranteed to exist
        Point point = new Point();
        mapView
                .getProjection()
                .toPixels(
                        new GeoPoint(
                                (int) (mDeployService.getLatitude(clusterName) * 1000000),
                                (int) (mDeployService.getLongitude(clusterName) * 1000000)),
                        point);
        if (!mOffsetTable.get(mZoomLevel).containsKey(clusterName)) {
            mOffsetTable.put(mZoomLevel, calculateOffsets(mapView));
        }
        point.offset(mOffsetTable.get(mZoomLevel).get(clusterName).x,
                mOffsetTable.get(mZoomLevel).get(clusterName).y);

        final int nodes = mDeployService.getNodes(clusterName);
        final int radius = nodes / 2;

        int textColor = Color.WHITE;
        int foreGroundColor = (selected) ? Color.argb(200, 255, 100, 100)
                : Color.argb(200, 100, 100, 255);
        int backGroundColor = (selected) ? Color.argb(80, 255, 100, 100)
                : Color.argb(80, 100, 100, 255);

        Paint p = new Paint();
        p.setStrokeWidth(2);
        p.setTextAlign(Paint.Align.CENTER);
        p.setAntiAlias(true);
        p.setShadowLayer(5, 0, 0, backGroundColor);

        p.setStyle(Paint.Style.FILL);

        if (selected) {
            p.setColor(foreGroundColor);
            int arc = (mResourceCount * 360) / Math.max(1, nodes);

            canvas.drawArc(new RectF(point.x - radius, point.y - radius,
                    point.x + radius, point.y + radius), -90, arc, true, p);
            p.setColor(backGroundColor);
            canvas.drawArc(new RectF(point.x - radius, point.y - radius,
                    point.x + radius, point.y + radius), -90 + arc, 360 - arc,
                    true, p);
        } else {
            p.setColor(backGroundColor);
            canvas.drawCircle(point.x, point.y, radius, p);
        }
        p.setColor(foreGroundColor);
        p.setStyle(Paint.Style.STROKE);
        canvas.drawCircle(point.x, point.y, radius, p);

        p.setStyle(Paint.Style.FILL);
        p.setColor(textColor);
        if (selected && mResourceCount > 0) {
            canvas.drawText(clusterName, point.x, point.y, p);
            canvas.drawText(mResourceCount + "/"
                    + mDeployService.getNodes(clusterName), point.x, point.y
                    + p.getTextSize() + 2, p);
        } else {
            canvas.drawText(clusterName, point.x, point.y, p);
        }
    }

    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
            long when) {
        if (mDeployService == null) {
            return false;
        }
        // has the zoom level been changed after the previous draw?
        if (mapView.getZoomLevel() != mZoomLevel) {
            // update the zoom level
            mZoomLevel = mapView.getZoomLevel();
            // do we have the offsets for this zoom level in our cache?
            if (!mOffsetTable.containsKey(mZoomLevel)) {
                // calculate offsets for this zoom level
                try {
                    mOffsetTable.put(mZoomLevel, calculateOffsets(mapView));
                } catch (RemoteException e) {
                    // TODO fail in some reasonable way...
                    e.printStackTrace();
                }
            }
        }
        // now we know the offsets, let's draw the circles
        try {
            for (String clusterName : mDeployService.getClusterNames()) {
                drawCluster(canvas, mapView, clusterName, clusterName
                        .equals(mSelectedCluster));
            }
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public String getClosestCluster(MapView mapView, Point touchPoint) {
        double minDistance = Double.MAX_VALUE;
        String closestCluster = null;
        try {
            for (String clusterName : mDeployService.getClusterNames()) {
                final int nodes = mDeployService.getNodes(clusterName);
                final int radius = nodes / 2;

                Point point = new Point();
                mapView
                        .getProjection()
                        .toPixels(
                                new GeoPoint(
                                        (int) (mDeployService
                                                .getLatitude(clusterName) * 1000000),
                                        (int) (mDeployService
                                                .getLongitude(clusterName) * 1000000)),
                                point);
                point.offset(mOffsetTable.get(mZoomLevel).get(clusterName).x,
                        mOffsetTable.get(mZoomLevel).get(clusterName).y);
                double distance = distance(point, touchPoint) - radius;
                if (distance < minDistance) {
                    minDistance = distance;
                    closestCluster = clusterName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
        return closestCluster;

    }
}
