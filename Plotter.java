  import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import java.util.regex.*;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import java.util.Stack;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.control.Tooltip;
import javafx.scene.Node;
import javafx.scene.image.WritableImage;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import javafx.print.PrinterJob;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Alert;
//Puts all data points received onto the chart, and implements various
//Quality-of-Life measures.
public class Plotter
{
    private static Plotter instance;
    private CopyManager copyMan;
    private Trimmer trimMan;
    private LineChart<Number, Number> mainChart;
    private XYChart.Series<Number, Number> selectedSeries = null;
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private Stage chartStage;
    private Double[][] points;
    private boolean trimMode = false;
    private Button trimBtn;
    private ArrayList<String[]> rawPoints = new ArrayList<>();
    private Stack<List<XYChart.Series<Number, Number>>> undoStack = new Stack<>();
    private Stack<List<XYChart.Series<Number, Number>>> redoStack = new Stack<>();
    private double xPadding = 43;
    private double yPadding = 38;
    private boolean paddingMeasured = false;
    public Plotter()
    {
        //Prepares the chart, buttons for features and their listeners
        xAxis = new NumberAxis();
        yAxis = new NumberAxis();
        xAxis.setAutoRanging(true);
        yAxis.setAutoRanging(true);
        xAxis.setTickUnit(1);
        yAxis.setTickUnit(1);
        xAxis.setLabel("X");
        yAxis.setLabel("Y");
        
        mainChart = new BlueLineChart(xAxis, yAxis);
        mainChart.setAxisSortingPolicy(LineChart.SortingPolicy.NONE);
        mainChart.setAnimated(false);
        mainChart.setTitle("Isometric Plot");
        mainChart.setPrefSize(700, 600);
        mainChart.setLegendVisible(false);

        chartStage = new Stage();
        chartStage.setTitle("Plot");
        points = new Double[10000][2];
        copyMan = new CopyManager();
        trimMan = new Trimmer(mainChart);
        trimMan.setUndoStacks(undoStack, redoStack);
        Data_Distributor DD = Data_Distributor.getInstance();
        
        Button undoBtn = new Button("↩");
        Button redoBtn = new Button("↪");
        Button copyBtn = new Button("📋");
        Button planeBtn = new Button(copyMan.getPlaneName());
        TextField offsetField = new TextField("1.0");
        offsetField.setPrefColumnCount(7);
        offsetField.setPromptText("Offset");
        Button saveBtn = new Button("💾");
        Button printBtn = new Button("🖨");
        trimBtn = new Button("\u2702");
        Button infoBtn = new Button("ⓘ");
        Button deleteBtn = new Button("🗑");
        Button clearBtn = new Button("\u2716");
        
        undoBtn.setTooltip(new Tooltip("Undo"));
        redoBtn.setTooltip(new Tooltip("Redo"));
        copyBtn.setTooltip(new Tooltip("Copy"));
        deleteBtn.setTooltip(new Tooltip("Delete Series"));
        clearBtn.setTooltip(new Tooltip("Clear Chart "));
        saveBtn.setTooltip(new Tooltip("Save as PNG"));
        printBtn.setTooltip(new Tooltip("Print"));
        trimBtn.setTooltip(new Tooltip("Trim"));
        infoBtn.setTooltip(new Tooltip("Help"));
        
        HBox toolbar = new HBox(15, saveBtn, printBtn, undoBtn, redoBtn, 
                                deleteBtn, clearBtn, copyBtn, planeBtn, 
                                offsetField, trimBtn, infoBtn);
        
        planeBtn.setOnAction(e -> {
            copyMan.cyclePlane();
            planeBtn.setText(copyMan.getPlaneName());
        });
        copyBtn.setOnAction(e -> {
        if (selectedSeries == null) {
            UserMessenger.show(ErrorCode.NOTHING_TO_COPY);
            return;
        }
        XYChart.Series<Number, Number> copy = copyMan.copySeries(selectedSeries);
        if (copy != null) {
            addSeries(copy);
            selectedSeries = copy;
            highlightSeries(copy);
            ArrayList<Double[]> points3D = Data_Distributor.getInstance().get3DPoints();
            if (!points3D.isEmpty()) {
                installPointTooltip(copy);
            }
        }});
        offsetField.textProperty().addListener((obs, old, newVal) -> {
            try {
                copyMan.setOffset(Double.parseDouble(newVal));
            } catch (NumberFormatException ex) { }
        });
        mainChart.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.DELETE) {
                deleteSelectedSeries();
        }});
        clearBtn.setOnAction(e -> {
        if (!mainChart.getData().isEmpty()) {
            List<XYChart.Series<Number, Number>> allSeries = new ArrayList<>(mainChart.getData());
            undoStack.push(allSeries);
            redoStack.clear();
            mainChart.getData().clear();
            selectedSeries = null;
        }});
        
        mainChart.setFocusTraversable(true);
        deleteBtn.setOnAction(e -> deleteSelectedSeries());
        saveBtn.setOnAction(e -> saveChart());
        printBtn.setOnAction(e -> printChart());
        trimBtn.setOnAction(e -> toggleTrimMode());
        infoBtn.getStyleClass().add("info-button");
        infoBtn.setOnAction(e -> showPlotterInfo());
        toolbar.setAlignment(Pos.CENTER_RIGHT);
        toolbar.setPadding(new Insets(5));
        undoBtn.setOnAction(e -> undo());
        redoBtn.setOnAction(e -> redo());
        
        VBox root = new VBox(5, toolbar, mainChart);
        root.setPadding(new Insets(5));
        VBox.setVgrow(mainChart, javafx.scene.layout.Priority.ALWAYS);
        root.setAlignment(Pos.CENTER);
        
        chartStage.setScene(new Scene(root, 750, 700));
        chartStage.getScene().getStylesheets().add(Iso_Input.getDarkTheme());
        chartStage.show();
        
    
        Platform.runLater(() -> measurePadding());
        mainChart.setOnMouseClicked(e -> {
            if (!paddingMeasured) measurePadding();
            double clickX = xAxis.getValueForDisplay(e.getX() - xPadding).doubleValue();
            double clickY = yAxis.getValueForDisplay(e.getY() - yPadding).doubleValue();

            if (trimMode) {
                trimMan.trim(clickX, clickY);
                toggleTrimMode();
            } else {
                XYChart.Series<Number, Number> closest = null;
                double minDist = Double.MAX_VALUE;
                for (XYChart.Series<Number, Number> series : mainChart.getData()) {
                    for (XYChart.Data<Number, Number> data : series.getData()) {
                        double dx = data.getXValue().doubleValue() - clickX;
                        double dy = data.getYValue().doubleValue() - clickY;
                        double dist = Math.sqrt(dx*dx + dy*dy);
                        if (dist < minDist) {
                            minDist = dist;
                            closest = series;
                        }
                    }
                }
                highlightSeries(closest);
                selectedSeries = closest;
            }
            Platform.runLater(() -> mainChart.requestFocus());
        });
    }
    private void measurePadding() 
    {
        //Measures widths of X and Y axes of the chart for localisation of coordinates
        Node plotArea = mainChart.lookup(".chart-plot-background");
        if (plotArea != null) {
            javafx.geometry.Bounds plotBounds = plotArea.localToScene(plotArea.getBoundsInLocal());
            javafx.geometry.Bounds chartBounds = mainChart.localToScene(mainChart.getBoundsInLocal());
            double measuredX = plotBounds.getMinX() - chartBounds.getMinX();
            double measuredY = plotBounds.getMinY() - chartBounds.getMinY();
        
            if (measuredX > 0 && measuredY > 0) {
                xPadding = measuredX;
                yPadding = measuredY;
                paddingMeasured = true;
            }
        }
    }
    public static Plotter getInstance() 
    {
        if (instance == null) {
            instance = new Plotter();
        }
        return instance;
    }//The next two methods add points onto the chart
    public void addSeries(XYChart.Series<Number, Number> series) 
    {
        mainChart.getData().add(series);
        List<XYChart.Series<Number, Number>> group = new ArrayList<>();
        group.add(series);
        undoStack.push(group);
        redoStack.clear();
    }
    public void addSeriesGroup(List<XYChart.Series<Number, Number>> group) 
    {
        //For rectangles 
        mainChart.getData().addAll(group);
        undoStack.push(group);
        redoStack.clear();
    }
    public void undo() 
    {
        if (!undoStack.isEmpty()) 
        {
            List<XYChart.Series<Number, Number>> group = undoStack.pop();
            if (group.size() > 1 && !mainChart.getData().contains(group.get(0))) {
                List<XYChart.Series<Number, Number>> pieces = new ArrayList<>(group.subList(1, group.size()));
                mainChart.getData().removeAll(pieces);
                mainChart.getData().add(group.get(0));
            } else {
                mainChart.getData().removeAll(group);
            }
            redoStack.push(group);
        }
    }
    public void redo() 
    {
        if (!redoStack.isEmpty()) 
        {
            List<XYChart.Series<Number, Number>> group = redoStack.pop();
            if (group.size() > 1 && mainChart.getData().contains(group.get(0))) {
                List<XYChart.Series<Number, Number>> pieces = new ArrayList<>(group.subList(1, group.size()));
                mainChart.getData().remove(group.get(0));
                mainChart.getData().addAll(pieces);
            } else {
            mainChart.getData().addAll(group);
            }
            undoStack.push(group);
        }
    }
    public void highlightSeries(XYChart.Series<Number, Number> series) 
    {
        for (XYChart.Series<Number, Number> s : mainChart.getData()) {
            if (s.getNode() != null) {
                s.getNode().setStyle("-fx-stroke-width: 1.5px;");
            }
        }
        if (series != null && series.getNode() != null) {
            series.getNode().setStyle("-fx-stroke: #b9e7ff; -fx-stroke-width: 1.5px;");
        }
    }
    private void saveChart() 
    {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Chart");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("PNG Image", "*.png"));
        chooser.setInitialFileName("isometric_plot.png");
        File file = chooser.showSaveDialog(chartStage);
        if (file != null) 
        {
            try {
                WritableImage image = mainChart.snapshot(null, null);
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
            } catch (IOException ex) {
                UserMessenger.show(ErrorCode.SAVE_FAILED);
            }
        }
    }
    private void printChart() 
    {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(chartStage)) 
        {
            boolean success = job.printPage(mainChart);
            if (success) {
                job.endJob();
            }
            else{
                job.endJob();
                UserMessenger.show(ErrorCode.PRINT_FAILED);
            }
        }
    }
    private void toggleTrimMode() 
    {
        trimMode = !trimMode;
        if (trimMode) {
            trimBtn.setStyle("-fx-background-color: #ffb6c1;");
        } else {
            trimBtn.setStyle("");
        }
    }
    private void deleteSelectedSeries() 
    {
        if (selectedSeries != null) 
        {
            List<XYChart.Series<Number, Number>> group = new ArrayList<>();
            group.add(selectedSeries);
            mainChart.getData().remove(selectedSeries);
            undoStack.push(group);
            redoStack.clear();
            selectedSeries = null;
        }
        else {
            UserMessenger.show(ErrorCode.NOTHING_TO_DELETE);
            return;
        }
    }
    public void coord_splitter() 
    {
        //Splits Iso coordinates
        for (int i = 0; i < points.length; i++) 
        {
            points[i][0] = null;
            points[i][1] = null;
        }
        Pattern pat = Pattern.compile("(-?\\d+\\.?\\d*)\\s(-?\\d+\\.?\\d*)");
        int row = 0;
        for(String[] point : rawPoints) 
        {
            for(int idx = 0; idx < point.length; idx++) 
            {
                String pt = point[idx];
                if (pt == null) continue;
                Matcher mat = pat.matcher(pt);
                if(mat.matches()) {
                    points[row][0] = Double.parseDouble(mat.group(1));
                    points[row][1] = Double.parseDouble(mat.group(2));
                    row++;
                }
            }
        }
    }
    public void RectLinePlot() 
    {
        rawPoints = Data_Distributor.getInstance().getPlotPoints();
        coord_splitter(); 
    
        XYChart.Series<Number, Number> edge1 = new XYChart.Series<>();
        edge1.setName("Rect Edge 1");
        edge1.getData().add(new XYChart.Data<>(points[0][0], points[0][1]));
        edge1.getData().add(new XYChart.Data<>(points[2][0], points[2][1]));

        XYChart.Series<Number, Number> edge2 = new XYChart.Series<>();
        edge2.setName("Rect Edge 2");
        edge2.getData().add(new XYChart.Data<>(points[2][0], points[2][1]));
        edge2.getData().add(new XYChart.Data<>(points[1][0], points[1][1]));

        XYChart.Series<Number, Number> edge3 = new XYChart.Series<>();
        edge3.setName("Rect Edge 3");
        edge3.getData().add(new XYChart.Data<>(points[1][0], points[1][1]));
        edge3.getData().add(new XYChart.Data<>(points[3][0], points[3][1]));

        XYChart.Series<Number, Number> edge4 = new XYChart.Series<>();
        edge4.setName("Rect Edge 4");
        edge4.getData().add(new XYChart.Data<>(points[3][0], points[3][1]));
        edge4.getData().add(new XYChart.Data<>(points[0][0], points[0][1]));

        List<XYChart.Series<Number, Number>> rectGroup = new ArrayList<>();
        rectGroup.add(edge1);
        rectGroup.add(edge2);
        rectGroup.add(edge3);
        rectGroup.add(edge4);
        addSeriesGroup(rectGroup);

        installEdgeTooltip(edge1, 0, 1);
        installEdgeTooltip(edge2, 1, 2);
        installEdgeTooltip(edge3, 2, 3);
        installEdgeTooltip(edge4, 3, 0);
        Data_Distributor.getInstance().clear();
    }
    public void CirclePlot() {
        rawPoints = Data_Distributor.getInstance().getPlotPoints();
        coord_splitter(); int i;
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Circle");
    
        for(i=0;;i++) {
            if(points[i][0] != null) {
                series.getData().add(new XYChart.Data<>(points[i][0],points[i][1])); 
            } else { break; }
        }
    
        series.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                for (XYChart.Data<Number, Number> d : series.getData()) {
                    if (d.getNode() != null) {
                        d.getNode().setStyle("-fx-background-color: transparent;");
                    } else {
                        d.nodeProperty().addListener((o, old, n) -> {
                            if (n != null) n.setStyle("-fx-background-color: transparent;");
                        });
                    }
                }
            }
        });
        addSeries(series);
        plotCentre();
        Data_Distributor.getInstance().clear();
    }
    public void LinePlot() 
    {
        rawPoints = Data_Distributor.getInstance().getPlotPoints();
        coord_splitter();
    
        XYChart.Series<Number, Number> e1 = new XYChart.Series<>();
        e1.getData().add(new XYChart.Data<>(points[0][0], points[0][1]));
        e1.getData().add(new XYChart.Data<>(points[1][0], points[1][1]));
        e1.setName("Line");

        addSeries(e1);  
        installPointTooltip(e1);
        Data_Distributor.getInstance().clear();
    }
    public void ArcPlot() 
    {
        rawPoints = Data_Distributor.getInstance().getPlotPoints();
        coord_splitter(); int i;
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName("Arc");
    
        for(i=0;;i++) {
            if(points[i][0] != null) {
                series.getData().add(new XYChart.Data<>(points[i][0],points[i][1])); 
            } else { break; }
        }
    
        series.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                for (XYChart.Data<Number, Number> d : series.getData()) {
                    if (d.getNode() != null) {
                        d.getNode().setStyle("-fx-background-color: transparent;");
                    } else {
                        d.nodeProperty().addListener((o, old, n) -> {
                            if (n != null) n.setStyle("-fx-background-color: transparent;");
                        });
                    }
                }
            }
        });
        addSeries(series);
        plotCentre();
        Data_Distributor.getInstance().clear();
    }//The next 3 methods add Tooltips to addes series
    private void installPointTooltip(XYChart.Series<Number, Number> series) {
        ArrayList<Double[]> points3D = Data_Distributor.getInstance().get3DPoints();
        if (points3D.isEmpty()) return;
        
        for (int i = 0; i < series.getData().size() && i < points3D.size(); i++) 
        {
            XYChart.Data<Number, Number> data = series.getData().get(i);
            Double[] pt = points3D.get(i);
            Node node = data.getNode();
            if (node != null) {
                Tooltip tip = new Tooltip(String.format("(%.2f, %.2f, %.2f)", pt[0], pt[1], pt[2]));
                Tooltip.install(node, tip);
            } else 
            {
                final int idx = i;
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if(newNode != null) {
                        Tooltip tip = new Tooltip(String.format("(%.2f, %.2f, %.2f)", pt[0], pt[1], pt[2]));
                        Tooltip.install(newNode, tip);
                    }
                });
            }
        }
    }
    private void installEdgeTooltip(XYChart.Series<Number, Number> series, 
                                    int idx3D_A, int idx3D_B) 
    {
        ArrayList<Double[]> points3D = Data_Distributor.getInstance().get3DPoints();
        if (points3D.isEmpty() || points3D.size() < 4) return;
        installSingleTooltip(series.getData().get(0), points3D.get(idx3D_A));
        installSingleTooltip(series.getData().get(1), points3D.get(idx3D_B));
    }
    private void installSingleTooltip(XYChart.Data<Number, Number> data, Double[] pt3D) 
    {
        Node node = data.getNode();
        Tooltip tip = new Tooltip(String.format("(%.2f, %.2f, %.2f)", pt3D[0], pt3D[1], pt3D[2]));
        if (node != null) {
            Tooltip.install(node, tip);
        } else {
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, tip);
                }
            });
        }
    }
    public void plotCentre() 
    {
        Double[] centre = Data_Distributor.getInstance().getCentre3D();
        if (centre == null) return;
        double isoX = Math.cos(Math.toRadians(30)) * (centre[0] - centre[1]);
        double isoY = 0.5 * (centre[0] + centre[1]) + centre[2];
    
        XYChart.Series<Number, Number> centreSeries = new XYChart.Series<>();
        centreSeries.setName("Centre");
        XYChart.Data<Number, Number> centreData = new XYChart.Data<>(isoX, isoY);
        centreSeries.getData().add(centreData);
    
        centreData.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                Tooltip tip = new Tooltip(
                    String.format("Centre: (%.2f, %.2f, %.2f)", centre[0], centre[1], centre[2]));
                Tooltip.install(newNode, tip);
            }
        });
        
        centreSeries.nodeProperty().addListener((obs, oldNode, newNode) -> {
            if (newNode != null) {
                newNode.setStyle("-fx-stroke: transparent;");
            }
        });
        mainChart.getData().add(centreSeries);
    }
    private void showPlotterInfo() 
    {
        //For information shown in infoBtn
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Chart Help");
        alert.setHeaderText("Chart Controls");
        alert.setContentText(
            "🖱️ INTERACTING WITH SHAPES:\n" +
            "• Click any shape to select it (highlights pink)\n" +
            "• Selected shapes can be copied or trimmed\n\n" +
        
            "✂️ TRIM:\n" +
            "• Click the 'Trim' button to enter trim mode\n" +
            "• Click on the segment you want to delete\n" +
            "• Works best when clicking near intersection points\n" +
            "• The segment between two intersections will be removed\n\n" +
            "📋 COPY:\n" +
            "• Select a shape, then click 'Copy'\n" +
            "• Use the plane toggle (X/Y/Z offset) to choose direction\n" +
            "• Enter a custom offset distance in the text field\n\n" +
            "↩ UNDO / REDO:\n" +
            "• Undo removes the last shape added\n" +
            "• Redo brings it back\n" +
            "• Works for plots, copies, and trims\n\n" +
            "💾 SAVE / PRINT:\n" +
            "• Save exports the chart as a PNG image\n" +
            "• Print sends the chart directly to your printer\n\n" +
            "🔍 TOOLTIPS:\n" +
            "• Hover over rectangle/line vertices for 3D coordinates\n" +
            "• Circles and arcs show a centre dot with coordinates\n\n" +
            "💡 TIPS:\n" +
            "• Trim empty space does nothing — click near intersections\n" +
            "• You can trim already-trimmed pieces\n" +
            "• Copy a trimmed piece to reuse it"
        );
        alert.getDialogPane().setPrefWidth(520);
        alert.showAndWait();
    }
}