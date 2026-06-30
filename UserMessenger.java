import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
//Displays error messages when edge cases are triggered
public class UserMessenger 
{
    public static void show(ErrorCode code) 
    {
        switch (code) 
        {
            case POINTS_NOT_ON_CIRCLE:
                showError("Invalid Circle", 
                    "The two points are not at equal distance from the centre.");
                break;
            case POINTS_COLLINEAR:
                showError("Invalid Input", 
                    "The three points are collinear and cannot define a circle.");
                break;
            case MISSING_INPUT:
                showWarning("Missing Input", 
                    "Please fill in all required coordinate fields.");
                break;
            case INVALID_NUMBER:
                showWarning("Invalid Number", 
                    "Please enter valid numeric values.");
                break;
            case INVALID_COORDINATE_FORMAT:
                showWarning("Invalid Format", 
                    "Coordinates must be in x,y,z format (e.g., 2,3,5).");
                break;
            case DIAMETER_ENDPOINTS:
                showWarning("Invalid Points", 
                    "The two points are opposite ends of a diameter.\nPlease provide points that are not directly opposite each other.");
                break;
            case NO_SHAPE_SELECTED:
                showWarning("No Shape Selected", 
                    "Please select a shape type before plotting.");
                break;
            case CANNOT_TRIM:
                showWarning("Trim Failed", 
                    "No intersection found near the click point.\nTry clicking closer to where two shapes cross.");
                break;
            case NOTHING_TO_COPY:
                showWarning("Nothing to Copy", 
                    "Please select a shape on the chart before copying.");
                break;
            case NOTHING_TO_DELETE:
                showWarning("Nothing to Delete", 
                    "Please select a shape on the chart before deleting.");
                break;
            case SAVE_FAILED:
                showError("Save Failed", 
                    "The chart could not be saved. Please try again.");
                break;
            case PRINT_FAILED:
                showError("Print Failed", 
                    "The chart could not be printed. Please check your printer.");
                break;
        }
    }
    private static void showError(String header, String message) 
    {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private static void showWarning(String header, String message) 
    {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static void showInfo(String header, String message) 
    {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Info");
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }
}