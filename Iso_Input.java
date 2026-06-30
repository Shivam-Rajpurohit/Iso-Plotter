import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Alert;
//This class makes the window for primary drawing selector
public class Iso_Input
{
    private String selectedOption;
    public void showMainWindow(Stage stage)
    {
        GridPane pane = new GridPane();
        pane.setPadding(new Insets(10, 10, 10, 10));
        pane.setMinSize(300, 200);
        pane.setVgap(10);
        pane.setHgap(10);
        
        Label L1 = new Label("Isometric plotter of Points");
        Label L2 = new Label("Choose your method of Plotting");
        ComboBox<String> option = new ComboBox<>();
        option.getItems().addAll("Fixed shapes", "Free lines");
        option.setPromptText("Select one");
        option.setEditable(false);
        option.setValue(null);
        option.setOnAction(e -> {selectedOption = option.getValue();});
        
        Button go = new Button("Start Plotting");
        Button infoBtn = new Button("ⓘ");
        infoBtn.getStyleClass().add("info-button");
        infoBtn.setOnAction(e -> showIsoInputInfo());
        go.setOnAction(this::Toggler);
        
        pane.add(L1, 0, 0);
        pane.add(L2, 0, 1);
        pane.add(option, 0, 2);
        pane.add(go, 0, 3);
        pane.add(infoBtn, 1, 0);
        GridPane.setHalignment(infoBtn, javafx.geometry.HPos.RIGHT);
        
        Scene sc = new Scene(pane, 450, 200);
        sc.getStylesheets().add(Iso_Input.getDarkTheme());
        stage.setScene(sc);
        stage.setTitle("Free Iso-plotter");
        stage.show();
        stage.sizeToScene();
    }
    public static String getDarkTheme() {
        return Iso_Input.class.getResource("DarkMode.css").toExternalForm();
    }
    private void Toggler(ActionEvent event)
    {
        if (selectedOption == null) return;
        if (selectedOption.equals("Fixed shapes")) { 
            Shapes shp = new Shapes();
            shp.show();
        } else if (selectedOption.equals("Free lines")) {
            FreeL FL = new FreeL();
            FL.show();
        }
    }
    private void showIsoInputInfo() 
    {
        //For information in infoBtn
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("How to Use");
        alert.setHeaderText("Isometric Plotter");
        alert.setContentText(
            "Welcome to the Isometric Plotter!\n\n" +
            "📐 GETTING STARTED:\n" +
            "• Select 'Fixed shapes' from the dropdown for rectangles, circles, and arcs\n" +
            "• Select 'Free lines' for drawing lines and arcs\n" +
            "• Click 'Start Plotting' to open the drawing window\n\n" +
            "🎯 SHAPES AVAILABLE:\n" +
            "• Fixed shapes: Rectangle, Circle\n" +
            "• Free lines: Line, Arc\n\n" +
            "🖱️ ON THE CHART:\n" +
            "• Click shapes to select them\n" +
            "• Use Undo/Redo to fix mistakes\n" +
            "• Copy duplicates selected shapes\n" +
            "• Trim removes unwanted segments\n" +
            "• Hover over points for 3D coordinates\n\n" +
            "💡 TIP:\n" +
            "Use coordinates between 0-10 for the best view!"
        );
        alert.getDialogPane().setPrefWidth(500);
        alert.showAndWait();
    }
}