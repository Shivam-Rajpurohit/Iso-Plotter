import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
//This class finds intersections of a selected series with other series,
//Locates the nearest ones, and deletes the segment
public class Trimmer
{
    private LineChart<Number, Number> chart;
    private XYChart.Series<Number, Number> points;
    private XYChart.Series<Number, Number> trimmedSeries;
    private List<XYChart.Series<Number, Number>> allSeriesList = new ArrayList<>();
    private List<double[]> walkablePoints = new ArrayList<>();
    private Stack<List<XYChart.Series<Number, Number>>> undoStack;
    private Stack<List<XYChart.Series<Number, Number>>> redoStack;
    private int clickedIndex; 
    private Integer matches[] = new Integer[2];
    private static final double THRESHOLD_COARSE = 0.8;
    private static final double THRESHOLD_FINE = 0.08;
    private static final double POINTS_PER_UNIT = 50;
    public Trimmer(LineChart<Number, Number> chart)
    {
        this.chart = chart;
    }
    public void setUndoStacks(Stack<List<XYChart.Series<Number, Number>>> undoStack,
                              Stack<List<XYChart.Series<Number, Number>>> redoStack) 
    {
        this.undoStack = undoStack;
        this.redoStack = redoStack;
    }
    //This method receives the clicked point on the chart
    public XYChart.Series<Number, Number> trim(double clickX, double clickY) 
    {
        points = null;
        clickedIndex = -1;
        trimmedSeries = null;
        double minDist = Double.MAX_VALUE;
        double[] clickPt = {clickX, clickY};
    
        for (XYChart.Series<Number, Number> series : chart.getData()) {
            if (series.getData() == null || series.getData().isEmpty()) continue;
            if (series.getName() != null && series.getName().contains("(trim")) {
                if (series.getData().size() < 2) continue;
            }
            double dist;
            if (series.getData().size() == 2) 
            {
                dist = distanceToLineSegmentValue(clickPt, series);
            } else 
            {
                double minD = Double.MAX_VALUE;
                int dataSize = series.getData().size();
                if (dataSize < 2) continue;
                for (int i = 0; i < dataSize - 1; i++) 
                {
                    double x0 = series.getData().get(i).getXValue().doubleValue();
                    double y0 = series.getData().get(i).getYValue().doubleValue();
                    double x1 = series.getData().get(i+1).getXValue().doubleValue();
                    double y1 = series.getData().get(i+1).getYValue().doubleValue();
                    double d = perpendicularDistance(clickX, clickY, x0, y0, x1, y1);
                    if (d < minD) minD = d;
                }
                dist = minD;
            }
            if (dist < minDist) 
            {
                minDist = dist;
                points = series;
            }
        }
        if (points == null) return null;
        
        walkablePoints.clear();
        if (points.getData().size() == 2) 
        {
            generateWalkablePoints(points);
        } else 
        {
            for (XYChart.Data<Number, Number> data : points.getData()) 
            {
                walkablePoints.add(new double[]{
                    data.getXValue().doubleValue(), 
                    data.getYValue().doubleValue()
                });
            }
        }
        if (walkablePoints.isEmpty()) return null;
    
        clickedIndex = 0;
        minDist = Double.MAX_VALUE;
        for (int i = 0; i < walkablePoints.size(); i++) {
            double[] pt = walkablePoints.get(i);
            double dx = pt[0] - clickX;
            double dy = pt[1] - clickY;
            double dist = Math.sqrt(dx*dx + dy*dy);
            if (dist < minDist) {
                minDist = dist;
                clickedIndex = i;
            }
        }
        
        if (points.getData().size() == 2 && clickedIndex == 1) {
            clickedIndex = walkablePoints.size() - 1;
        }
        buildAllSeriesList();
        walkAndFindMatches();
        return trimmedSeries;
    }
    //Finds the perpendicular distance of a line from the clicked point
    private double perpendicularDistance(double px, double py, 
        double x0, double y0, double x1, double y1) 
    {
        double dx = x1 - x0;
        double dy = y1 - y0;
        double lengthSq = dx*dx + dy*dy;
        if (lengthSq == 0) {
            return Math.sqrt((px-x0)*(px-x0) + (py-y0)*(py-y0));
        }
        
        double t = ((px-x0)*dx + (py-y0)*dy) / lengthSq;
        t = Math.max(0, Math.min(1, t));
        double nearX = x0 + t*dx;
        double nearY = y0 + t*dy;
        return Math.sqrt((px-nearX)*(px-nearX) + (py-nearY)*(py-nearY));
    }
    //Lines on chart have only 2 points, which prevents points-based matching
    //This method generates points on the entire line
    private void generateWalkablePoints(XYChart.Series<Number, Number> series) 
    {
        double x0 = series.getData().get(0).getXValue().doubleValue();
        double y0 = series.getData().get(0).getYValue().doubleValue();
        double x1 = series.getData().get(1).getXValue().doubleValue();
        double y1 = series.getData().get(1).getYValue().doubleValue();
        int steps = getStepCount(x0, y0, x1, y1);
        
        for (int i = 0; i <= steps; i++) 
        {
            double t = (double) i / steps;
            walkablePoints.add(new double[]{
                x0 + t * (x1 - x0), 
                y0 + t * (y1 - y0)
            });
        }
    }
    //Helps find how many points are needed on a line
    private int getStepCount(double x0, double y0, double x1, double y1)
    {
        double length = Math.sqrt((x1-x0)*(x1-x0) + (y1-y0)*(y1-y0));
        int steps = (int) (length * POINTS_PER_UNIT);
        return Math.max(20, Math.min(steps, 500));
    }
    //Builds a list of qualifiabe series which are then walked on
    private void buildAllSeriesList() 
    {
        allSeriesList.clear();
        for (XYChart.Series<Number, Number> series : chart.getData()) 
        {
            if (series.equals(points)) continue;
            if (series.getData() == null || series.getData().isEmpty()) continue;
            allSeriesList.add(series);
        }
    }
    //The next three methods do the walking
    private void walkAndFindMatches() 
    {
        if (points.getData().size() == 2) {
            walkLineMatches();
        } else {
            walkCurveMatches();
        }
    }
    private void walkLineMatches() 
    {
        int totalPoints = walkablePoints.size();
        int matchCount = 0;
        matches[0] = -1;
        matches[1] = -1;
        XYChart.Series<Number, Number> leftLockedSeries = null;
        int leftMatch = -1;
        
        for (int step = 1; (clickedIndex - step) >= 0; step++) 
        {
            int idx = clickedIndex - step;
            double[] pt = walkablePoints.get(idx);
            
            if (leftLockedSeries == null) 
            {
                double closestDist = THRESHOLD_COARSE;
                for (XYChart.Series<Number, Number> series : allSeriesList) 
                {
                    double dist = distanceToSeries(pt, series);
                    if (dist < closestDist) 
                    {
                        closestDist = dist;
                        leftLockedSeries = series;
                    }
                }
            }
            
            if (leftLockedSeries != null && leftMatch == -1) 
            {
                if (distanceToSeries(pt, leftLockedSeries) < THRESHOLD_FINE) {
                    leftMatch = idx;
                }
            }
        }
        if (leftMatch >= 0) leftMatch = refineMatch(leftMatch, leftLockedSeries, -1);
        
        XYChart.Series<Number, Number> rightLockedSeries = null;
        int rightMatch = -1;
        for (int step = 1; (clickedIndex + step) < totalPoints; step++) 
        {
            int idx = clickedIndex + step;
            double[] pt = walkablePoints.get(idx);
            
            if (rightLockedSeries == null) 
            {
                double closestDist = THRESHOLD_COARSE;
                for (XYChart.Series<Number, Number> series : allSeriesList)
                {
                    double dist = distanceToSeries(pt, series);
                    if (dist < closestDist) 
                    {
                        closestDist = dist;
                        rightLockedSeries = series;
                    }
                }
            }
            
            if (rightLockedSeries != null && rightMatch == -1) 
            {
                if (distanceToSeries(pt, rightLockedSeries) < THRESHOLD_FINE) {
                    rightMatch = idx;
                }
            }
        }
        
        if (rightMatch >= 0) rightMatch = refineMatch(rightMatch, rightLockedSeries, 1);
        if (leftMatch >= 0) { matches[matchCount] = leftMatch; matchCount++; }
        if (rightMatch >= 0) { matches[matchCount] = rightMatch; matchCount++; }
        finalizeMatches(matchCount);
    }
    private void walkCurveMatches() 
    {
        int totalPoints = walkablePoints.size();
        int matchCount = 0;
        matches[0] = -1;
        matches[1] = -1;
        int maxSteps = totalPoints * 6 / 11;
        XYChart.Series<Number, Number> leftLockedSeries = null;
        int leftMatch = -1;
        
        for (int step = 1; step <= maxSteps; step++) 
        {
            int idx = (clickedIndex - step + totalPoints) % totalPoints;
            double[] pt = walkablePoints.get(idx);
            
            if (leftLockedSeries == null) 
            {
                double closestDist = THRESHOLD_COARSE;
                for (XYChart.Series<Number, Number> series : allSeriesList) 
                {
                    double dist = distanceToSeries(pt, series);
                    if (dist < closestDist) {
                        closestDist = dist;
                        leftLockedSeries = series;
                    }
                }
            }
            
            if (leftLockedSeries != null && leftMatch == -1) 
            {
                double dist = distanceToSeries(pt, leftLockedSeries);
                if (dist < THRESHOLD_FINE) {
                    boolean isMinimum = true;
                    int range = Math.min(5, totalPoints / 10);
                    for (int i = -range; i <= range; i++) 
                    {
                        if (i == 0) continue;
                        int checkIdx = (idx + i + totalPoints) % totalPoints;
                        double checkDist = distanceToSeries(walkablePoints.get(checkIdx), leftLockedSeries);
                        if (checkDist < dist) 
                        {
                            isMinimum = false;
                            break;
                        }
                    }
                    if (isMinimum) {
                        leftMatch = idx;
                    }
                }
            }
        }
        
        if (leftMatch >= 0) leftMatch = refineMatch(leftMatch, leftLockedSeries, -1);
        List<XYChart.Series<Number, Number>> rightSeriesList = new ArrayList<>(allSeriesList);
        if (leftLockedSeries != null && leftLockedSeries.getData().size() != points.getData().size()) 
        {
            rightSeriesList.remove(leftLockedSeries);
        }
        
        XYChart.Series<Number, Number> rightLockedSeries = null;
        int rightMatch = -1;
        for (int step = 1; step <= maxSteps; step++) 
        {
            int idx = (clickedIndex + step) % totalPoints;
            double[] pt = walkablePoints.get(idx);
            
            if (rightLockedSeries == null) 
            {
                double closestDist = THRESHOLD_COARSE;
                for (XYChart.Series<Number, Number> series : rightSeriesList) 
                {
                    double dist = distanceToSeries(pt, series);
                    if (dist < closestDist)
                    {
                        closestDist = dist;
                        rightLockedSeries = series;
                    }
                }
            }
            
            if (rightLockedSeries != null && rightMatch == -1) 
            {
                double dist = distanceToSeries(pt, rightLockedSeries);
                if (dist < THRESHOLD_FINE) 
                {
                    boolean isMinimum = true;
                    int range = Math.min(5, totalPoints / 10);
                    for (int i = -range; i <= range; i++) 
                    {
                        if (i == 0) continue;
                        int checkIdx = (idx + i + totalPoints) % totalPoints;
                        double checkDist = distanceToSeries(walkablePoints.get(checkIdx), rightLockedSeries);
                        if (checkDist < dist) 
                        {
                            isMinimum = false;
                            break;
                        }
                    }
                    if (isMinimum) {
                        rightMatch = idx;
                    }
                }
            }
        }
        
        if (rightMatch >= 0) rightMatch = refineMatch(rightMatch, rightLockedSeries, 1);
        if (leftMatch >= 0) { matches[matchCount] = leftMatch; matchCount++; }
        if (rightMatch >= 0) { matches[matchCount] = rightMatch; matchCount++; }
        finalizeMatches(matchCount);
    }
    //This method validates matches
    private void finalizeMatches(int matchCount) {
        if (matchCount >= 2) {} 
        else if (matchCount == 1) {
            matches[1] = matches[0];
        } else 
        {
            double[] clickPt = walkablePoints.get(clickedIndex);
            boolean nearAnySeries = false;
            for (XYChart.Series<Number, Number> s : allSeriesList) 
            {
                if (distanceToSeries(clickPt, s) < 1.0) 
                {
                    nearAnySeries = true;
                    break;
                }
            }
            if (nearAnySeries) 
            {
                UserMessenger.show(ErrorCode.CANNOT_TRIM);
            }
            trimmedSeries = points;
            return;
        }
        deleter();
    }
    //This method perfects intersections 
    private int refineMatch(int matchIdx, XYChart.Series<Number, Number> lockedSeries, int direction) {
        int totalPoints = walkablePoints.size();
        int bestIdx = matchIdx;
        double bestDist = distanceToSeries(walkablePoints.get(matchIdx), lockedSeries);
        
        for (int i = 1; i <= 10; i++) 
        {
            int checkIdx;
            if (points.getData().size() == 2) 
            {
                checkIdx = matchIdx + direction * i;
                if (checkIdx < 0 || checkIdx >= totalPoints) break;
            } else 
            {
                checkIdx = (matchIdx + direction * i + totalPoints) % totalPoints;
            }
            double d = distanceToSeries(walkablePoints.get(checkIdx), lockedSeries);
            if (d < bestDist) 
            {
                bestDist = d;
                bestIdx = checkIdx;
            } else {
                break;
            }
        }
        return bestIdx;
    }
    //This method selects only those series which come close to the selected
    //series for walking, if the close series is a curve
    private double distanceToSeries(double[] point, XYChart.Series<Number, Number> series) 
    {
        if (series.getData().size() == 2) 
        {
            return distanceToLineSegmentValue(point, series);
        } else 
        {
            double minDist = Double.MAX_VALUE;
            for (XYChart.Data<Number, Number> data : series.getData()) 
            {
                double dx = point[0] - data.getXValue().doubleValue();
                double dy = point[1] - data.getYValue().doubleValue();
                double dist = Math.sqrt(dx*dx + dy*dy);
                if (dist < minDist) minDist = dist;
            }
            return minDist;
        }
    }
    //This method selects only those series which come close to the selected
    //series for walking, if the close series is a line
    private double distanceToLineSegmentValue(double[] point, XYChart.Series<Number, Number> series) 
    {
        double x0 = series.getData().get(0).getXValue().doubleValue();
        double y0 = series.getData().get(0).getYValue().doubleValue();
        double x1 = series.getData().get(1).getXValue().doubleValue();
        double y1 = series.getData().get(1).getYValue().doubleValue();
        double dx = x1 - x0;
        double dy = y1 - y0;
        double lengthSq = dx*dx + dy*dy;
        
        if (lengthSq == 0) {
            return Math.sqrt((point[0]-x0)*(point[0]-x0) + (point[1]-y0)*(point[1]-y0));
        }
        double t = ((point[0]-x0)*dx + (point[1]-y0)*dy) / lengthSq;
        t = Math.max(0, Math.min(1, t));
        
        return Math.sqrt((point[0]-(x0+t*dx))*(point[0]-(x0+t*dx)) + 
                         (point[1]-(y0+t*dy))*(point[1]-(y0+t*dy)));
    }
    //Deletes the part of a series which was selected by the user
    //if it's a curve
    private void deleter() 
    {
        if (trimmedSeries == points) return;
        if (points.getData().size() == 2) {
            int left = Math.min(matches[0], matches[1]);
            int right = Math.max(matches[0], matches[1]);
            splitLineSegment(left, right);
            return;
        }

        int left = matches[0];
        int right = matches[1];
        int sortedLeft = Math.min(left, right);
        int sortedRight = Math.max(left, right);
        List<XYChart.Series<Number, Number>> pieces = new ArrayList<>();

        if (left <= right) 
        {
            if (sortedLeft > 0) 
            {
                XYChart.Series<Number, Number> piece1 = new XYChart.Series<>();
                piece1.setName(points.getName() + " (trim1)");
                for (int i = 0; i < sortedLeft; i++) {
                    XYChart.Data<Number, Number> pt = points.getData().get(i);
                    piece1.getData().add(new XYChart.Data<>(pt.getXValue(), pt.getYValue()));
                }
                pieces.add(piece1);
            }
            if (sortedRight < points.getData().size() - 1) 
            {
                XYChart.Series<Number, Number> piece2 = new XYChart.Series<>();
                piece2.setName(points.getName() + " (trim2)");
                for (int i = sortedRight + 1; i < points.getData().size(); i++) {
                    XYChart.Data<Number, Number> pt = points.getData().get(i);
                    piece2.getData().add(new XYChart.Data<>(pt.getXValue(), pt.getYValue()));
                }
                pieces.add(piece2);
            }
        } else 
        {
            boolean clickedIndexInside = (clickedIndex > right && clickedIndex < left);
            if (clickedIndexInside) 
            {
                if (right >= 0) 
                {
                    XYChart.Series<Number, Number> piece1 = new XYChart.Series<>();
                    piece1.setName(points.getName() + " (trim1)");
                    for (int i = 0; i <= right; i++) {
                        XYChart.Data<Number, Number> pt = points.getData().get(i);
                        piece1.getData().add(new XYChart.Data<>(pt.getXValue(), pt.getYValue()));
                    }
                    pieces.add(piece1);
                }
                if (left < points.getData().size()) 
                {
                    XYChart.Series<Number, Number> piece2 = new XYChart.Series<>();
                    piece2.setName(points.getName() + " (trim2)");
                    for (int i = left; i < points.getData().size(); i++) {
                        XYChart.Data<Number, Number> pt = points.getData().get(i);
                        piece2.getData().add(new XYChart.Data<>(pt.getXValue(), pt.getYValue()));
                    }
                    pieces.add(piece2);
                }
            } else 
            {
                if (left - right > 1) 
                {
                    XYChart.Series<Number, Number> piece1 = new XYChart.Series<>();
                    piece1.setName(points.getName() + " (trimmed)");
                    for (int i = right + 1; i < left; i++) {
                        XYChart.Data<Number, Number> pt = points.getData().get(i);
                        piece1.getData().add(new XYChart.Data<>(pt.getXValue(), pt.getYValue()));
                    }
                    pieces.add(piece1);
                }
            }
        }

        chart.getData().remove(points);
        if (!pieces.isEmpty()) 
        {
            chart.getData().addAll(pieces);
            if (undoStack != null) 
            {
                List<XYChart.Series<Number, Number>> group = new ArrayList<>();
                group.add(points);          
                group.addAll(pieces);       
                undoStack.push(group);
                if (redoStack != null) redoStack.clear();
            }
        }
        trimmedSeries = pieces.isEmpty() ? null : pieces.get(0);
    }
    //Deletes the part of a series which was selected by the user
    //if it's a line
    private void splitLineSegment(int left, int right) 
    {
        double tLeft = (double) left / walkablePoints.size();
        double tRight = (double) right / walkablePoints.size();

        double x0 = points.getData().get(0).getXValue().doubleValue();
        double y0 = points.getData().get(0).getYValue().doubleValue();
        double x1 = points.getData().get(1).getXValue().doubleValue();
        double y1 = points.getData().get(1).getYValue().doubleValue();

        double intLeftX = x0 + tLeft * (x1 - x0);
        double intLeftY = y0 + tLeft * (y1 - y0);
        double intRightX = x0 + tRight * (x1 - x0);
        double intRightY = y0 + tRight * (y1 - y0);

        if (left == right) 
        {
            trimmedSeries = new XYChart.Series<>();
            trimmedSeries.setName(points.getName() + " (trimmed)");
    
            if (clickedIndex < left) 
            {
                trimmedSeries.getData().add(new XYChart.Data<>(intRightX, intRightY));
                trimmedSeries.getData().add(new XYChart.Data<>(x1, y1));
            } else 
            {
                trimmedSeries.getData().add(new XYChart.Data<>(x0, y0));
                trimmedSeries.getData().add(new XYChart.Data<>(intLeftX, intLeftY));
            }
    
            chart.getData().remove(points);
            if (!trimmedSeries.getData().isEmpty()) 
            {
                chart.getData().add(trimmedSeries);
                if (undoStack != null) 
                {
                    List<XYChart.Series<Number, Number>> group = new ArrayList<>();
                    group.add(points);
                    group.add(trimmedSeries);
                    undoStack.push(group);
                    if (redoStack != null) redoStack.clear();
                }
            }
            return;
        }

        List<XYChart.Series<Number, Number>> pieces = new ArrayList<>();
        double len1 = Math.sqrt((intLeftX-x0)*(intLeftX-x0) + (intLeftY-y0)*(intLeftY-y0));
        if (len1 > 0.01) 
        {
            XYChart.Series<Number, Number> piece1 = new XYChart.Series<>();
            piece1.setName(points.getName() + " (trim1)");
            piece1.getData().add(new XYChart.Data<>(x0, y0));
            piece1.getData().add(new XYChart.Data<>(intLeftX, intLeftY));
            pieces.add(piece1);
        }

        double len2 = Math.sqrt((x1-intRightX)*(x1-intRightX) + (y1-intRightY)*(y1-intRightY));
        if (len2 > 0.01) 
        {
            XYChart.Series<Number, Number> piece2 = new XYChart.Series<>();
            piece2.setName(points.getName() + " (trim2)");
            piece2.getData().add(new XYChart.Data<>(intRightX, intRightY));
            piece2.getData().add(new XYChart.Data<>(x1, y1));
            pieces.add(piece2);
        }

        chart.getData().remove(points);
        if (!pieces.isEmpty()) 
        {
            chart.getData().addAll(pieces);
            if (undoStack != null) 
            {
                List<XYChart.Series<Number, Number>> group = new ArrayList<>();
                group.add(points);
                group.addAll(pieces);
                undoStack.push(group);
                if (redoStack != null) redoStack.clear();
            }
        }
        trimmedSeries = pieces.isEmpty() ? null : pieces.get(0);
    }
}