package gui;

import io.MPSReader;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import solver.Solver;

import java.io.File;
import java.io.IOException;

/**
 * Created by Lennart on 6/07/16.
 */
public class BPSolverGuiController {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private TextField crossConstraintParamTextField;
    @FXML
    private TextField graphSearchDepthTextField;
    @FXML
    private TextField lateAcceptanceFitnessTextField;
    @FXML
    private TextField runtimeTextField;
    @FXML
    private Label inputfileLabel;
    @FXML
    private Button chooseFileButton;
    @FXML
    private Button solveButton;
    @FXML
    private CheckBox runTimeUnlimitedCheckBox;
    @FXML
    private CheckBox cycleOperatorsCheckbox;
    @FXML
    private CheckBox minimizeCheckbox;

    private File inputFile;
    private boolean minimize;
    private int lateAcceptanceFitness, crossConstraintParam, conflictGraphDepth, orderedCyclicShift, unorderedCyclicShift;
    private long runtime;

    public void chooseFile(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        Stage stage = (Stage) rootPane.getScene().getWindow();
        inputFile = fileChooser.showOpenDialog(stage);
        inputfileLabel.setText(inputFile.getName());
    }

    public void solveProblem(ActionEvent actionEvent) throws IOException {
        minimize = minimizeCheckbox.isSelected();

        if (lateAcceptanceFitnessTextField.getText() == null || lateAcceptanceFitnessTextField.getText().trim().isEmpty()) {
            lateAcceptanceFitness = -1;
        } else {
            lateAcceptanceFitness = Integer.parseInt(lateAcceptanceFitnessTextField.getText().trim());
        }

        if (runTimeUnlimitedCheckBox.isSelected()) {
            runtime = -1;
        } else if (runtimeTextField.getText() == null || runtimeTextField.getText().trim().isEmpty()) {
            runtime = -2;
        } else {
            runtime = Integer.parseInt(runtimeTextField.getText().trim());
        }

        if (cycleOperatorsCheckbox.isSelected()) {
            orderedCyclicShift = 1;
            unorderedCyclicShift = 1;
        } else {
            orderedCyclicShift = 0;
            unorderedCyclicShift = 0;
        }

        if (crossConstraintParamTextField.getText() == null || crossConstraintParamTextField.getText().trim().isEmpty()) {
            crossConstraintParam = -1;
        } else {
            crossConstraintParam = Integer.parseInt(crossConstraintParamTextField.getText().trim());
        }

        if (graphSearchDepthTextField.getText() == null || graphSearchDepthTextField.getText().trim().isEmpty()) {
            conflictGraphDepth = -1;
        } else {
            conflictGraphDepth = Integer.parseInt(graphSearchDepthTextField.getText().intern());
        }

        if (lateAcceptanceFitness == -1 || runtime == -2 || crossConstraintParam == -1 || conflictGraphDepth == -1 || inputFile == null) {
            // data laden in nieuwe controller en deze initialiseren in deze scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BPSolverGui.fxml"));
            Parent parent = loader.load();
            BPSolverGuiController controller = loader.getController();
            controller.initData(new Object[]{minimize, lateAcceptanceFitness, runtime, orderedCyclicShift, unorderedCyclicShift, crossConstraintParam, conflictGraphDepth, inputFile});
            Scene scene = new Scene(parent);
            Stage stage = (Stage) rootPane.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } else {
            try {
                Solver solver = new Solver(new MPSReader(), minimize, lateAcceptanceFitness, crossConstraintParam, conflictGraphDepth, orderedCyclicShift, unorderedCyclicShift);
                solver.readProblem(inputFile);
                solver.solve(runtime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void initialize() {
        minimizeCheckbox.setSelected(minimize);

        if (lateAcceptanceFitness == -1) {
            // Add in red
        } else {
            lateAcceptanceFitnessTextField.setText(Integer.toString(lateAcceptanceFitness));
        }

        if (runtime == -2) {
            // Add red
        } else if (runtime == -1) {
            runTimeUnlimitedCheckBox.setSelected(true);
        } else {
            runtimeTextField.setText(Long.toString(runtime));
        }

        cycleOperatorsCheckbox.setSelected(orderedCyclicShift == 1);
        if (crossConstraintParam == -1) {
            // Add red
        } else {
            crossConstraintParamTextField.setText(Integer.toString(crossConstraintParam));
        }
        if (conflictGraphDepth == -1) {
            // Add red
        } else {
            graphSearchDepthTextField.setText(Integer.toString(conflictGraphDepth));
        }
        if (inputFile == null) {
            // Add selected file as red
        } else {
            inputfileLabel.setText(inputFile.getName());
        }

    }

    void initData(Object[] data) {
        minimize = (boolean) data[0];
        lateAcceptanceFitness = (int) data[1];
        runtime = (long) data[2];
        orderedCyclicShift = (int) data[3];
        unorderedCyclicShift = (int) data[4];
        crossConstraintParam = (int) data[5];
        conflictGraphDepth = (int) data[6];
        inputFile = (File) data[7];
    }
}

//    public void backToCustomerCreation(ActionEvent actionEvent) throws IOException {
//
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("caseCreationCustomer.fxml"));
//        Parent parent = (Parent) loader.load();
//        CaseCreationCustomerController caseCreationCustomerController = loader.<CaseCreationCustomerController>getController();
//        Customer customer = GuiMain.aCase.getCustomer();
//        caseCreationCustomerController.initData(customer);
//        Scene scene = new Scene(parent);
//        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
//        stage.setScene(scene);
//        stage.show();
//    }