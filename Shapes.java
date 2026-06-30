import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
// This class takes input data for rectangles and circles in a window
public class Shapes
{
    private String shapeType;
    private int plane;
    private Button infoBtn;
    private Stage sta;
    private GridPane pane;
    private Label titleLabel, coord1Label, coord2Label, coord3Label, distLabel;
    private TextField coord1, coord2, coord3, Dists;
    private ComboBox<String> option;
    private HBox titleBar;
    Main_Process MP = new Main_Process();
    public Shapes()
    {
        sta = new Stage();
        sta.setTitle("Shapes Plotter");
        pane = new GridPane();
        pane.setPadding(new Insets(10, 10, 10, 10));
        pane.setMinSize(300, 200);
        pane.setVgap(10);
        pane.setHgap(10);
    
        titleLabel = new Label("Here you can plot some pre-defined shapes");
        coord1 = new TextField(); coord1.setPrefColumnCount(20);
        coord2 = new TextField(); coord2.setPrefColumnCount(20);
        coord3 = new TextField(); coord3.setPrefColumnCount(20);
        Dists = new TextField(); Dists.setPrefColumnCount(7);
    
        coord1Label = new Label();
        coord2Label = new Label();
        coord3Label = new Label();
        distLabel = new Label("Radius");
    
        option = new ComboBox<>();
        option.getItems().addAll("Rectangle", "Circle");
        option.setPromptText("Select one");
        option.setEditable(false);
        option.setValue(null);
    
        infoBtn = new Button("ⓘ");
        infoBtn.getStyleClass().add("info-button");
        infoBtn.setOnAction(e -> showShapesInfo());
    
        titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);
        titleBar.getChildren().addAll(titleLabel, infoBtn);
        pane.add(titleBar, 0, 0, 2, 1);
        pane.add(option, 0, 1, 2, 1);
    
        option.setOnAction(e -> {
        shapeType = option.getValue();
        if (shapeType == null) {
            UserMessenger.show(ErrorCode.NO_SHAPE_SELECTED);
            return;
        }
        if ("Rectangle".equals(shapeType)) {
                buildRectInput();
        } else if ("Circle".equals(shapeType)) {
                buildCircleInput();
            }});
        Scene scene = new Scene(pane, 420, 300);
        scene.getStylesheets().add(Iso_Input.getDarkTheme());
        sta.setScene(scene);
        sta.sizeToScene();
    }
    private void buildRectInput()
    {
        pane.getChildren().retainAll(titleBar, option);
        titleLabel.setText("For plotting rectangles and Squares.\n"
            + "Enter coordinates of a diagonal's edges in fixed planes,\n"
            + "or any 3 coords for a free plane");
        
        ComboBox<String> planeSelector = new ComboBox<>();
        planeSelector.getItems().addAll("Free Plane", "Iso-top", "Iso-left", "Iso-right");
        planeSelector.setPromptText("Select one");
        planeSelector.setEditable(false);
        planeSelector.setValue(null);
        pane.add(planeSelector, 0, 2, 2, 1);
        
        coord1Label.setText("1st Diagonal Coordinate");
        coord2Label.setText("2nd Diagonal Coordinate");
        coord3Label.setText("3rd Vertex Coordinate");
        pane.add(coord1Label, 0, 3); pane.add(coord1, 1, 3);
        pane.add(coord2Label, 0, 4); pane.add(coord2, 1, 4);
        
        planeSelector.setOnAction(e -> {
            String val = planeSelector.getValue();
            if (val != null && val.equals("Free Plane")) 
            {
                pane.getChildren().removeAll(coord3Label, coord3);
                pane.add(coord3Label, 0, 5);
                pane.add(coord3, 1, 5);
            } else {
                pane.getChildren().removeAll(coord3Label, coord3);
            }
        });
        
        Button plotBtn = new Button("Plot");
        pane.add(plotBtn, 0, 6, 2, 1);
        plotBtn.setOnAction(this::R_data);
    }
    private void buildCircleInput()
    {
        pane.getChildren().retainAll(titleBar, option);
        titleLabel.setText("For plotting circles. Enter coordinates\n"
            + "of the centre, and any two points on circle for free plane\n"
            + "or centre and radius in any fixed plane");
        
        ComboBox<String> planeSelector = new ComboBox<>();
        planeSelector.getItems().addAll("Free Plane", "Iso-top", 
                                         "Iso-left", "Iso-right", "3 Points");
        planeSelector.setPromptText("Select one");
        planeSelector.setEditable(false);
        planeSelector.setValue(null);
        pane.add(planeSelector, 0, 2, 2, 1);
        pane.add(coord1Label, 0, 3); pane.add(coord1, 1, 3);
        
        planeSelector.setOnAction(e -> {
            String val = planeSelector.getValue();
            if ("Free Plane".equals(val)) { plane = 0; }
            else if ("Iso-top".equals(val)) { plane = 1; }
            else if ("Iso-left".equals(val)) { plane = 2; }
            else if ("Iso-right".equals(val)) { plane = 3; }
            else if ("3 Points".equals(val)) { plane = 4; }
            pane.getChildren().removeAll(coord2Label, coord2, coord3Label, coord3, distLabel, Dists);
            if (val != null && (val.equals("Free Plane") || val.equals("3 Points"))) 
            {
                coord1Label.setText(val.equals("Free Plane") ? "Centre Coordinate" : "1st Coordinate");
                coord2Label.setText("2nd Coordinate");
                coord3Label.setText("3rd Coordinate");
                pane.add(coord2Label, 0, 4); pane.add(coord2, 1, 4);
                pane.add(coord3Label, 0, 5); pane.add(coord3, 1, 5);
            } else if (val != null) {
                coord1Label.setText("Centre Coordinate");
                pane.add(distLabel, 0, 4); pane.add(Dists, 1, 4);
            }
        });
        Button plotBtn = new Button("Plot");
        pane.add(plotBtn, 0, 6, 2, 1);
        plotBtn.setOnAction(this::C_data);
    }//The next two methods forward data ahead
    public void R_data(ActionEvent event)
    {
        String a = coord1.getText().trim();
        String b = coord2.getText().trim();
        String c = coord3.getText().trim();
        if (a.isEmpty() || b.isEmpty()) {
            UserMessenger.show(ErrorCode.MISSING_INPUT);
            return;
        }
        Data_Distributor.getInstance().addRawData(a, b, c, null);
        coord1.clear(); coord2.clear(); coord3.clear();
        MP.iso_rect();
    }
    public void C_data(ActionEvent event)
    {
        String a = coord1.getText().trim();
        String b = coord2.getText().trim();
        String c = coord3.getText().trim();
        if (a.isEmpty()) {
            UserMessenger.show(ErrorCode.MISSING_INPUT);
            return;
        }
        String distText = (Dists != null) ? Dists.getText().trim() : "";
        Double d;
        try {
            d = distText.isEmpty() ? 0.0 : Double.parseDouble(distText);
        } catch (NumberFormatException ex) {
            UserMessenger.show(ErrorCode.INVALID_NUMBER);
            return;
        }
        Data_Distributor.getInstance().addRawData(a, b, c, d);
        Data_Distributor.getInstance().Plane(plane);
        coord1.clear(); coord2.clear(); coord3.clear(); Dists.clear();
        MP.iso_circle();
    }
    private void showShapesInfo() 
    {
        //For information in infoBtn
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("How to Use");
        alert.setHeaderText("Shapes Plotter");
        alert.setContentText(
            "Welcome to the Shapes Plotter!\n\n" +
            "📐 GETTING STARTED:\n" +
            "• Select 'Rectangle' or 'Circle' from the dropdown\n" +
            "• Choose your plane or method from the second dropdown\n" +
            "• Enter coordinates in x,y,z format\n" +
            "• Click 'Plot' to draw on the chart\n\n" +
            "📦 RECTANGLES:\n" +
            "• Fixed planes: Enter the two endpoints of a diagonal\n" +
            "• Free plane: Enter three vertices of the rectangle\n" +
            "  (the fourth vertex is computed automatically)\n\n" +
            "⭕ CIRCLES:\n" +
            "• Fixed planes: Enter the centre coordinate and radius\n" +
            "• Free plane: Enter the centre and two points on the circle\n" +
            "  OR three points on the circumference (without centre)\n" +
            "• The two points must NOT be opposite ends of a diameter\n" +
            "  (if they are, the circle cannot be uniquely determined)\n\n" +
            "💡 TIPS:\n" +
            "• Use coordinates between 0-10 for best results\n" +
            "• For free plane shapes, pick points that clearly define your shape\n" +
            "• The chart window updates automatically with each plot"
        );
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }
    public void show() { sta.show(); }
}