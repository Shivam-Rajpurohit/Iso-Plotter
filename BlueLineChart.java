import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.application.Platform;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
//This class overrides JavaFX's in-built node colouring cycles and adds custom 
//styling and glow to added series
public class BlueLineChart extends LineChart<Number, Number> 
{
    public BlueLineChart(NumberAxis xAxis, NumberAxis yAxis) {
        super(xAxis, yAxis);
    }
    @Override
    protected void seriesAdded(XYChart.Series<Number, Number> series, int seriesIndex) 
    {
        super.seriesAdded(series, seriesIndex);
        if (series.getNode() != null) 
        {
            series.getNode().setStyle("-fx-stroke: #b9e7ff; -fx-stroke-width: 1px;");
        }
        series.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                newNode.setStyle("-fx-stroke: #b9e7ff; -fx-stroke-width: 1px;");
                if (newNode instanceof javafx.scene.shape.Shape) {
                    javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
                    glow.setColor(javafx.scene.paint.Color.web("#b9e7ff"));
                    glow.setRadius(20);
                    glow.setSpread(0.8);
                    ((javafx.scene.shape.Shape) newNode).setEffect(glow);
                }
            }
        });
        if (series.getData().size() > 2) 
        {
            for (XYChart.Data<Number, Number> d : series.getData()) 
            {
                if (d.getNode() != null) 
                {
                    d.getNode().setStyle("-fx-background-color: transparent;");
                    if (d.getNode() instanceof javafx.scene.shape.Shape) 
                    {
                        javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
                        glow.setColor(javafx.scene.paint.Color.web("#b9e7ff"));
                        glow.setRadius(10);
                        glow.setSpread(0.4);
                        ((javafx.scene.shape.Shape) d.getNode()).setEffect(glow);
                    }
                } else 
                {
                    d.nodeProperty().addListener((o, old, n) -> {
                        if (n != null) {
                            n.setStyle("-fx-background-color: transparent;");
                            if (n instanceof javafx.scene.shape.Shape) 
                            {
                                javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
                                glow.setColor(javafx.scene.paint.Color.web("#b9e7ff"));
                                glow.setRadius(10);
                                glow.setSpread(0.4);
                                ((javafx.scene.shape.Shape) n).setEffect(glow);
                            }
                        }
                    }); 
                }
            }
        }
        Platform.runLater(() -> {
            Platform.runLater(() -> {
                if (series.getNode() != null) 
                {
                    series.getNode().setStyle("-fx-stroke: #b9e7ff; -fx-stroke-width: 1px;");
                    if (series.getNode() instanceof javafx.scene.shape.Shape) 
                    {
                        DropShadow glow = new DropShadow();
                        glow.setColor(Color.web("#b9e7ff"));
                        glow.setRadius(12);
                        glow.setSpread(0.5);
                        ((javafx.scene.shape.Shape) series.getNode()).setEffect(glow);
                    }
                }
            });
        });
    }
}