package pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.service;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.model.Patient;
import pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.model.ontology.OntologyWrapper;
import pl.edu.agh.plonka.bartlomiej.diseasesdiagnoser.model.rule.Rule;

import java.io.File;

public class PatientsService {

    private final Logger LOG = LoggerFactory.getLogger(getClass());

    private OntologyWrapper ontology;
    private ObservableList<Patient> patients = FXCollections.observableArrayList();
    private ObservableList<Rule> rules = FXCollections.observableArrayList();

    public PatientsService(String url) throws OWLOntologyCreationException {
        createKnowledgeBase(url);
    }

    public PatientsService(File file) throws OWLOntologyCreationException {
        createKnowledgeBase(file);
    }

    public void createKnowledgeBase(String url) throws OWLOntologyCreationException {
        ontology = new OntologyWrapper(url);
        patients.clear();
        rules.clear();
    }

    public void createKnowledgeBase(File file) throws OWLOntologyCreationException {
        ontology = new OntologyWrapper(file);
        patients.setAll(ontology.getPatients());
        rules.setAll(ontology.getRules());
    }

    public void saveKnowledgeBase(File file) throws OWLOntologyStorageException {
        ontology.saveOntologyToFile(file);
    }

    public ObservableList<Patient> getPatients() {
        return patients;
    }

    public void addPatient(Patient patient) {
        patients.add(patient);
        ontology.addPatient(patient);
    }

    public void deletePatient(Patient patient) {
        patients.remove(patient);
        ontology.deleteEntity(patient);
    }

    public ObservableList<Rule> getRules() {
        return rules;
    }

    public void editPatient(Patient patient) {
        ontology.updatePatient(patient);
    }

    public OntologyWrapper getOntology() {
        return ontology;
    }
}
