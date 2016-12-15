package sample;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

public class Controller implements Initializable {

    @FXML private TextArea newTaskFXML;
    @FXML private ListView taskListViewFXML;
    @FXML public Label currentTaskFXML;
    @FXML private Button startStopButtonFXML;
    @FXML private Button chooseTaskButtonFXML;
    @FXML private Pane about;

    private List<HBox> taskList = new ArrayList<>();
    private List<Label> taskLabelList = new ArrayList<>();
    private List<CheckBox> taskCheckBoxList = new ArrayList<>();
    private List<Long> timeLeftList = new ArrayList<>();
    private int currentTaskNumber = 0;
    private int nextTaskNumber = -1;
    private int newElementNumber = 0;
    private static boolean clockIsOn = false;
    private boolean autoSelect = true;
    private static long timeLeft = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        newTaskFXML.setOnKeyReleased(event -> {
            if (event.getCode().equals(KeyCode.ENTER)){
                addTask();
            }
        });
    }

    public void addTask() {
        if (Pattern.matches("[\\w\\W]+[\\w\\W ]*", this.newTaskFXML.getText())) {
            CheckBox checkBox = new CheckBox();
            checkBox.selectedProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        checkBox.setSelected(newValue);
                        if (oldValue) {
                            this.taskLabelList
                                    .get(this.taskCheckBoxList.indexOf(checkBox))
                                    .setTextFill(Color.DIMGRAY);
                        }
                        else {
                            setTime(this.taskCheckBoxList.indexOf(checkBox));
                            this.taskLabelList
                                    .get(this.taskCheckBoxList.indexOf(checkBox))
                                    .setTextFill(Color.GREEN);
                        }

                        if (!this.clockIsOn && this.autoSelect) {
                            changeCurrentTask();
                        }
                    });
            this.taskCheckBoxList.add(checkBox);

            Label newTask = new Label('\b'+this.newTaskFXML.getText());
            newTask.setMaxWidth(260);
            newTask.setWrapText(true);
            if (newElementNumber == 0)
                newTask.setTextFill(Color.BLACK);
            else
                newTask.setTextFill(Color.DIMGRAY);
            this.taskLabelList.add(newTask);

            HBox hBox = new HBox(5);
            hBox.getChildren().addAll(
                    this.taskCheckBoxList.get(this.newElementNumber),
                    this.taskLabelList.get(this.newElementNumber++));

            this.taskList.add(hBox);
            this.newTaskFXML.setText("");

            this.taskListViewFXML.setItems(FXCollections.observableArrayList(this.taskList));

            this.timeLeftList.add((long)0);

            if (!this.clockIsOn && this.autoSelect) {
                changeCurrentTask();
            }
        }
        this.newTaskFXML.setText("");
    }

    public void startStop(ActionEvent actionEvent) throws InterruptedException {
        if (!this.clockIsOn) {
            taskListViewFXML.scrollTo(this.currentTaskNumber);
            this.startStopButtonFXML.setText("Stop");
            this.chooseTaskButtonFXML.setText("Choose next task");
            start();
        } else {
            this.startStopButtonFXML.setText("Start");
            this.chooseTaskButtonFXML.setText("Choose current task");
            this.autoSelect = true;
            stop();
        }
    }

    public void start() throws InterruptedException {

        this.timeLeft = System.currentTimeMillis();

        this.clockIsOn = true;

        this.taskLabelList.get(this.currentTaskNumber).setTextFill(Color.CORAL);
        this.taskLabelList.get(this.currentTaskNumber).setUnderline(true);
        this.taskLabelList.get(this.currentTaskNumber).setStyle("-fx-font-weight: bold;");
        this.taskCheckBoxList.get(this.currentTaskNumber).setDisable(true);
    }

    public void stop() {

        this.clockIsOn = false;

        this.timeLeft = (System.currentTimeMillis() - this.timeLeft) / 1000;
        this.timeLeftList
                .set(this.currentTaskNumber,
                        this.timeLeftList.get(this.currentTaskNumber) + this.timeLeft);
        this.taskLabelList.get(this.currentTaskNumber).setUnderline(false);
        this.taskLabelList.get(this.currentTaskNumber).setStyle("-fx-font-weight: normal;");
        this.timeLeft = 0;

        if (nextTaskNumber != -1)
            taskCheckBoxList
                    .get(nextTaskNumber)
                    .setDisable(false);

        setTime(this.currentTaskNumber);

        this.taskCheckBoxList.get(this.currentTaskNumber).setDisable(false);
        this.taskCheckBoxList.get(this.currentTaskNumber).setSelected(true);
    }

    public void changeCurrentTask(){
        if (this.nextTaskNumber == -1 && this.autoSelect) {
            for (int i = 0; i < this.taskCheckBoxList.size(); i++) {
                if (!this.taskCheckBoxList.get(i).isSelected()) {

                    if (!this.taskLabelList.get(this.currentTaskNumber).getTextFill().equals(Color.GREEN)) {
                        this.taskLabelList
                                .get(this.currentTaskNumber)
                                .setTextFill(Color.DIMGRAY);
                    }

                    String s = this.taskLabelList
                            .get(i)
                            .getText();
                    this.currentTaskFXML.setText(s);
                    this.taskListViewFXML.getSelectionModel().select(i);
                    this.taskLabelList
                            .get(i)
                            .setTextFill(Color.BLACK);
                    this.currentTaskNumber = i;
                    this.startStopButtonFXML.setDisable(false);
                    break;
                } else {
                    this.startStopButtonFXML.setDisable(true);
                    this.currentTaskFXML.setText("");
                }
            }
        } else if (this.nextTaskNumber != -1) {
            this.currentTaskNumber = this.nextTaskNumber;
            this.nextTaskNumber = -1;
            this.taskListViewFXML.getSelectionModel().select(this.currentTaskNumber);
            this.taskLabelList
                    .get(this.currentTaskNumber)
                    .setTextFill(Color.BLACK);
        }
    }

    public void changeCurrentTask(int index){

        if (!this.taskLabelList.get(index).getTextFill().equals(Color.GREEN)) {
            this.taskLabelList
                    .get(index)
                    .setTextFill(Color.DIMGRAY);
        }
            this.currentTaskNumber =
                    this.taskList
                            .indexOf(this.taskListViewFXML.getSelectionModel().getSelectedItem());
            String s = this.taskLabelList
                    .get(currentTaskNumber)
                    .getText();
            this.taskCheckBoxList
                    .get(currentTaskNumber)
                    .setSelected(false);
            this.currentTaskFXML.setText(s);
            this.taskListViewFXML
                    .getSelectionModel()
                    .select(currentTaskNumber);
            this.taskLabelList
                    .get(currentTaskNumber)
                    .setTextFill(Color.BLACK);

    }

    public void chooseTask(ActionEvent actionEvent) throws InterruptedException {
        this.autoSelect = false;
        this.startStopButtonFXML.setDisable(false);

        int indexOfSelectedItem = this.taskList.indexOf(this.taskListViewFXML.getSelectionModel().getSelectedItem());
        if (!this.clockIsOn) {
            changeCurrentTask(this.currentTaskNumber);
        } else if (currentTaskNumber != indexOfSelectedItem){
            if (this.nextTaskNumber != -1) {
                this.taskCheckBoxList
                        .get(nextTaskNumber)
                        .setDisable(false);
                this.taskLabelList
                        .get(this.nextTaskNumber)
                        .setTextFill(Color.DIMGRAY);
            }
            this.nextTaskNumber = indexOfSelectedItem;

            this.taskListViewFXML
                    .getSelectionModel()
                    .select(this.currentTaskNumber);
            this.taskCheckBoxList
                    .get(this.nextTaskNumber)
                    .setDisable(true);
            this.taskCheckBoxList
                    .get(this.nextTaskNumber)
                    .setSelected(false);
            this.taskLabelList
                    .get(this.nextTaskNumber)
                    .setTextFill(Color.BLUE);
        } else if (this.nextTaskNumber == indexOfSelectedItem){
            this.taskLabelList
                    .get(this.nextTaskNumber)
                    .setTextFill(Color.DIMGRAY);
            this.nextTaskNumber = -1;
        }
    }

    public void setTime(int index){
        this.taskLabelList.get(index).setText(this.taskLabelList.get(index).getText()+"  (");
        this.taskLabelList.get(index)
                .setText(this.taskLabelList
                        .get(index)
                        .getText()
                        .substring(0, this.taskLabelList.get(index).getText().indexOf("  (")));
        String time = this.timeLeftList.get(index) < 60 ?
                "  (" + this.timeLeftList.get(index) + "sec)" :
                "  (" + this.timeLeftList.get(index) / 60 + "min "
                        + this.timeLeftList.get(index) % 60 % 60 + "sec)";
        this.taskLabelList
                .get(index)
                .setText(this.taskLabelList
                        .get(index)
                        .getText()+time);
    }
}
