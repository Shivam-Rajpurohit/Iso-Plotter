import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.event.ActionEvent;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
//This class accepts input data for lines and arcs in a window
public class FreeL
{
    private Button infoBtn;
    private Stage sta;
    private String shapeType, arcMethod;
    private Scene scene;
    private GridPane pane;
    private HBox titleBar;
    private Label titleLabel, coord1Label, coord2Label, coord3Label;
    private Button LineB, ArcB;
    private TextField coord1, coord2, coord3;
    private ComboBox<String> option, Aoption;
    Main_Process MP = new Main_Process();
    public FreeL()
    {
        sta = new Stage();
        sta.setTitle("Lines Plotter");
        pane = new GridPane();
        pane.setPadding(new Insets(10, 10, 10, 10));
        pane.setMinSize(300, 300);
        pane.setVgap(10);   pane.setHgap(10);
        
        titleLabel = new Label("Here you can plot lines and arcs");
        coord1 = new TextField();
        coord2 = new TextField();
        coord3 = new TextField();
        coord1.setPrefColumnCount(24);
        coord2.setPrefColumnCount(24);
        coord3.setPrefColumnCount(24);
        
        option = new ComboBox<>();
        option.getItems().addAll("Lines", "Arcs");
        option.setPromptText("Select one");
        option.setEditable(false);
        option.setValue(null);
        
        infoBtn = new Button("ⓘ");
        infoBtn.getStyleClass().add("info-button");
        infoBtn.setOnAction(e -> showFreeLInfo());
        titleBar = new HBox();
        titleBar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(titleLabel, javafx.scene.layout.Priority.ALWAYS);
        titleBar.getChildren().addAll(titleLabel, infoBtn);
    
        pane.add(titleBar, 0, 0, 2, 1);
        pane.add(option, 0, 1, 2, 1);
        
        option.setOnAction(e -> {
            shapeType = option.getValue();
            if (shapeType == null) return;
            if(shapeType.equals("Lines")){ line_plot();}
            if(shapeType.equals("Arcs")){ arc_plot();}
        });
        scene = new Scene(pane, 400, 300);
        scene.getStylesheets().add(Iso_Input.getDarkTheme());
        sta.setScene(scene);
        sta.sizeToScene();
    }
    public void arc_plot()
    {
        pane.getChildren().retainAll(titleBar, option);
        titleLabel.setText("For plotting Arcs.\n Enter coordinates"
            + " of any 3 points, or 2 points and the centre\n"
            + "or any 3 coords for a free plane");
        
        Aoption = new ComboBox<>();
        Aoption.getItems().addAll("3 Points", "With Centre");
        Aoption.setPromptText("Method of plotting arc");
        Aoption.setEditable(false);  Aoption.setValue(null);
        pane.add(Aoption, 0, 2, 2, 1);
        
        coord1Label = new Label("1st Coordinate");
        coord2Label = new Label("2nd Coordinate");
        coord3Label = new Label("3rd Coordinate");
        pane.add(coord1Label, 0, 3);    pane.add(coord1, 1, 3);
        pane.add(coord2Label, 0, 4);    pane.add(coord2, 1, 4);
        pane.add(coord3Label, 0, 5);    pane.add(coord3, 1, 5);
        
        Aoption.setOnAction(e ->{
        arcMethod = Aoption.getValue();
        if(arcMethod != null && arcMethod.equals("3 Points")) {
            coord3Label.setText("3rd Coordinate");
        }else if(arcMethod!= null && arcMethod.equals("With Centre")) {
            coord3Label.setText("Centre Coordinate");
        }});
        ArcB = new Button("Plot");
        ArcB.setOnAction(this::A_data);
        pane.add(ArcB, 0, 6);
    }
    public void line_plot()
    {
        pane.getChildren().retainAll(titleBar, option);
        titleLabel.setText("For plotting Straight Lines.\n"
            + "Enter coordinates of end points,\n");
        
        coord1Label = new Label("1st Coordinate");
        coord2Label = new Label("2nd Coordinate");
        pane.add(coord1Label, 0, 2);    pane.add(coord1, 1, 2);
        pane.add(coord2Label, 0, 3);    pane.add(coord2, 1, 3);
        
        LineB = new Button("Plot");
        LineB.setOnAction(this::L_data);    
        pane.add(LineB, 0, 4);
    }
    //The next two methods forward data ahead
    public void L_data(ActionEvent event)
    {
        String a = coord1.getText(); String b = coord2.getText();
        if (a.isEmpty() || b.isEmpty()) {
            UserMessenger.show(ErrorCode.MISSING_INPUT);
            return;
        }
        Data_Distributor.getInstance().addRawData(a,b,null,null);
        coord1.clear(); coord2.clear();
        MP.iso_line(); 
    }
    public void A_data(ActionEvent event)
    {
        String a = coord1.getText().trim(); String b = coord2.getText().trim();
        String c = coord3.getText().trim();
        if (a.isEmpty() || b.isEmpty()) {
            UserMessenger.show(ErrorCode.MISSING_INPUT);
            return;
        }
        if(arcMethod != null && arcMethod.equals("3 Points")){ 
            Data_Distributor.getInstance().Cent_cond(false); 
        } else { 
            Data_Distributor.getInstance().Cent_cond(true); 
        }
        Data_Distributor.getInstance().addRawData(c,a,b,null);
        coord1.clear(); coord2.clear(); coord3.clear();
        MP.iso_arc();
    }
    private void showFreeLInfo() 
    {
        //For information in infoBtn
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("How to Use");
        alert.setHeaderText("Free Lines Plotter");
        alert.setContentText(
            "Welcome to the Free Lines Plotter!\n\n" +
            "📐 GETTING STARTED:\n" +
            "• Select 'Lines' or 'Arcs' from the dropdown\n" +
            "• Choose your plotting method from the second dropdown\n" +
            "• Enter coordinates in x,y,z format\n\n" +
            "📏 LINES:\n" +
            "• Enter the two endpoints of your line\n" +
            "• Lines can be plotted in any orientation\n\n" +
            "🔷 ARCS:\n" +
            "• Method 1: '3 Points' — enter three points on the arc\n" +
            "  (start, middle, and end of the curve)\n" +
            "• Method 2: 'With Centre' — enter the centre coordinate\n" +
            "  and two points on the arc\n" +
            "• The two points must NOT be opposite ends of a diameter\n" +
            "  (if they are, the arc cannot be determined)\n\n" +
            "💡 TIPS:\n" +
            "• Use coordinates between 0-10 for best results\n" +
            "• For arcs, pick points that clearly define your curve\n" +
            "• Click 'Plot' to draw on the chart"
        );
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }
    public void show(){ sta.show();}
}