import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.application.Platform;
//To copy selected series along a fixed axis and variable offset distance
public class CopyManager
{
    private double offset = 1.0;
    private int copyPlane = 0;
    private String[] planeNames = {"X-offset", "Y-offset", "Z-offset"};
    public CopyManager() { }
    public void cyclePlane(){ copyPlane = (copyPlane+1)%3; }
    public String getPlaneName(){ return planeNames[copyPlane]; }
    public void setOffset(double offset) { this.offset = offset; }
    public XYChart.Series<Number, Number> copySeries(
            XYChart.Series<Number, Number> original) 
    {
        if (original == null) return null;
        XYChart.Series<Number, Number> copy = new XYChart.Series<>();
        copy.setName(original.getName() + " (copy)");
        double dx = 0, dy = 0, dz = 0;
        switch(copyPlane) 
        {
            case 0: dx = offset; break;
            case 1: dy = offset; break;
            case 2: dz = offset; break;
        }
        double isoOffsetX = Math.cos(Math.toRadians(30)) * (dx - dy);
        double isoOffsetY = 0.5 * (dx + dy) + dz;
        for (XYChart.Data<Number, Number> data : original.getData()) 
        {
            double newX = data.getXValue().doubleValue() + isoOffsetX;
            double newY = data.getYValue().doubleValue() + isoOffsetY;
            copy.getData().add(new XYChart.Data<>(newX, newY));
        }
        return copy;
    }
}