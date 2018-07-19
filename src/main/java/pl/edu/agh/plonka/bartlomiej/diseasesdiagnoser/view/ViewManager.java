package pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.model.Entity;
import pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.model.Patient;
import pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.service.PatientsService;
import pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.utils.SystemDefaults;
import pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.view.controller.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;

public class ViewManager {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private Stage primaryStage;
    private BorderPane rootLayout;

    public ViewManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout(PatientsService patientsService, String baseUrl) throws IOException {
        LOG.info("Loading root layout from FXML file");
        FXMLLoader loader = getFXMLLoader("fxml/RootLayout.fxml");
        rootLayout = loader.load();

        Scene scene = new Scene(rootLayout);
        primaryStage.setScene(scene);

        RootLayoutController controller = loader.getController();
        controller.init(this, patientsService, baseUrl);

        primaryStage.show();
    }

    /**
     * Shows the patient overview inside the root layout.
     */
    public void showPatientOverview(PatientsService patientsService) throws IOException {
        LOG.info("Loading patient overview layout from FXML file");
        FXMLLoader loader = getFXMLLoader("fxml/PatientOverview.fxml");
        AnchorPane patientOverview = loader.load();

        rootLayout.setCenter(patientOverview);

        PatientOverviewController controller = loader.getController();
        controller.init(this, patientsService);
    }

    public void setTitle(String title) {
        primaryStage.setTitle(title);
    }

    public boolean showEntityEditDialog(Entity entity, PatientsService patientsService) {
        try {
            // Load the fxml file and create a new stage for the popup dialog.
            FXMLLoader loader = getFXMLLoader("fxml/EntityEditDialog.fxml");
            AnchorPane page = loader.load();

            Stage dialogStage = createDialogStage(page, "Edit Entity");

            EntityEditDialogController controller = loader.getController();
            controller.init(this, dialogStage, patientsService);
            controller.setEntity(entity);

            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean showPatientEditDialog(Patient patient, PatientsService patientsService) throws IOException {
        LOG.info("Loading patient edit dialog layout from FXML file");
        FXMLLoader loader = getFXMLLoader("fxml/PatientEditDialog.fxml");
        AnchorPane page = loader.load();

        Stage dialogStage = createDialogStage(page, "Edit Patient");

        PatientEditDialogController controller = loader.getController();
        controller.init(this, dialogStage, patientsService);
        controller.setPatient(patient);

        dialogStage.showAndWait();

        return controller.isOkClicked();
    }

    public boolean showEntitiesEditDialog(Entity rootEntity, Collection<Entity> currentEntities, Collection<Entity> results, PatientsService patientsService) {
        try {
            FXMLLoader loader = getFXMLLoader("fxml/EntitiesEditDialog.fxml");
            AnchorPane page = loader.load();

            Stage dialogStage = createDialogStage(page, "Edit Patient");

            EntitiesEditDialogController controller = loader.getController();
            controller.init(this, dialogStage, patientsService);
            controller.setResultsContainer(results);
            controller.setEntities(rootEntity, currentEntities);

            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean showRulesEditDialog(PatientsService patientsService) {
        try {
            FXMLLoader loader = getFXMLLoader("fxml/RulesEditDialog.fxml");
            AnchorPane page = loader.load();

            Stage dialogStage = createDialogStage(page, "Edit Rules");

            RulesEditDialogController controller = loader.getController();
            controller.init(dialogStage, patientsService);

            dialogStage.showAndWait();

            return controller.isOkClicked();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void errorDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(primaryStage);
        if (title != null)
            alert.setTitle(title);
        if (header != null)
            alert.setHeaderText(header);
        if (content != null)
            alert.setContentText(content);
        alert.showAndWait();
    }

    public void warningDialog(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(primaryStage);
        if (title != null)
            alert.setTitle(title);
        if (header != null)
            alert.setHeaderText(header);
        if (content != null)
            alert.setContentText(content);
        alert.showAndWait();
    }

    public void errorExceptionDialog(String title, String header, String content,
                                     Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(primaryStage);
        if (title != null)
            alert.setTitle(title);
        if (header != null)
            alert.setHeaderText(header);
        if (content != null)
            alert.setContentText(content);

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        // Set expandable Exception into the dialog pane.
        alert.getDialogPane().setExpandableContent(expContent);

        alert.showAndWait();
    }

    public File showOpenDialog(final String description, final String... extensions) {
        FileChooser fileChooser = fileChooser(description, extensions);

        return fileChooser.showOpenDialog(primaryStage);
    }

    public File showSaveDialog(final String description, final String... extensions) {
        FileChooser fileChooser = fileChooser(description, extensions);

        return fileChooser.showSaveDialog(primaryStage);
    }

    public void informationAlert(String title, String header, String cntent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(cntent);

        alert.showAndWait();
    }

    private FileChooser fileChooser(final String description, final String... extensions) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(description, extensions);
        fileChooser.getExtensionFilters().add(extFilter);

        LOG.debug("Fetching default directory.");
        File defaultDirectory = SystemDefaults.getDefaultDirectoryFile();
        if (defaultDirectory != null) {
            LOG.debug("Found default directory: " + defaultDirectory.getPath());
            fileChooser.setInitialDirectory(defaultDirectory);
        }

        return fileChooser;
    }

    private FXMLLoader getFXMLLoader(String resourcePath) {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource(resourcePath));
        return loader;
    }

    private Stage createDialogStage(Parent parent, String title) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(primaryStage);
        Scene scene = new Scene(parent);
        dialogStage.setScene(scene);

        return dialogStage;
    }
}
