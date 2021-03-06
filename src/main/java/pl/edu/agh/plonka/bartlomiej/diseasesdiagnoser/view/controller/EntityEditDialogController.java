package pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.model.Entity;
import pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.service.PatientsService;
import pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.view.ViewManager;

import static pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.utils.NameUtils.generateName;
import static pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.utils.binding.ObservableResourceFactory.getStringBinding;
import static pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.utils.binding.ObservableResourceFactory.getTranslation;

public class EntityEditDialogController {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private PatientsService patientsService;
    private ViewManager viewManager;
    private Stage dialogStage;
    private Entity entity;
    private boolean okClicked = false;

    @FXML
    private Label nameLabel;
    @FXML
    private Label descriptionLabel;

    @FXML
    private Button okButton;
    @FXML
    private Button cancelButton;

    @FXML
    private TextField entityName;
    @FXML
    private TextArea entityDescription;

    public void init(ViewManager viewManager, Stage dialogStage, PatientsService patientsService) {
        this.viewManager = viewManager;
        this.dialogStage = dialogStage;
        this.patientsService = patientsService;
    }

    /**
     * Initializes the controller class. This method is automatically called
     * after the fxml file has been loaded.
     */
    @FXML
    private void initialize() {
        bindTranslations();
    }

    private void bindTranslations() {
        nameLabel.textProperty().bind(getStringBinding("NAME"));
        descriptionLabel.textProperty().bind(getStringBinding("DESCRIPTION"));
        okButton.textProperty().bind(getStringBinding("OK"));
        cancelButton.textProperty().bind(getStringBinding("CANCEL"));
    }

    /**
     * Returns true if the user clicked OK, false otherwise.
     *
     * @return
     */
    public boolean isOkClicked() {
        return okClicked;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
        entityName.setText(entity.getLabel());
        entityDescription.setText(entity.getComment());
    }

    @FXML
    private void handleOK() {
        if (isInputValid()) {
            String name = entityName.getText();
            if (entity.getID() == null) {
                String newID = generateName(name);
                if (patientsService.getOntology().containsID(newID)) {
                    int i = 1;
                    newID = generateName(name, Integer.toString(i));
                    while (patientsService.getOntology().containsID(newID)) {
                        newID = generateName(name, Integer.toString(++i));
                    }
                    entity.setID(newID);
                } else
                    entity.setID(newID);
            }
            entity.setLabel(name);
            entity.setComment(entityDescription.getText());
            okClicked = true;
            dialogStage.close();
        }
    }

    @FXML
    private void handleCancel() {
        dialogStage.close();
    }

    private boolean isInputValid() {
        if (entityName.getText() == null || entityName.getText().length() == 0) {
            viewManager.errorDialog(getTranslation("ERROR_CREATING_ENTITY"), null, getTranslation("MISSING_ENTITY_NAME"));
            return false;
        } else
            return true;
    }
}
