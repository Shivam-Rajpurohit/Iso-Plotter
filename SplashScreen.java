import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import javafx.geometry.Pos;
//This class shows the openinng window with Title and Author name
public class SplashScreen extends Application
{
    @Override
    public void start(Stage primaryStage)
    {
        Stage splashStage = new Stage();
        splashStage.initStyle(StageStyle.UNDECORATED);
        
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #2d2d2d;");
        
        Label title = new Label("Isometric Plotter");
        title.setStyle("-fx-font-size: 32px; -fx-text-fill: #ffb6c1; -fx-font-weight: bold;");
        Label subtitle = new Label("• Isometric Projections •");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #e0e0e0;");
        
        Label author = new Label("Built by Shivam Rajpurohit");
        author.setStyle("-fx-font-size: 12px; -fx-text-fill: #888;");
        Label version = new Label("v1.0");
        version.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
        
        root.getChildren().addAll(title, subtitle, author, version);        
        Scene scene = new Scene(root, 500, 300);
        splashStage.setScene(scene);
        splashStage.show();
        
        FadeTransition fade = new FadeTransition(Duration.seconds(1), root);
        fade.setDelay(Duration.seconds(2));
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(e -> {
            splashStage.close();
            Platform.runLater(() -> {
                Iso_Input isoApp = new Iso_Input();
                isoApp.showMainWindow(primaryStage);
            });
        });
        fade.play();
    }
    public static void main(String[] args) {
        launch(args);
    }
}