package sample;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

public class Controller implements Initializable {

    @FXML private TextArea newTaskFXML;
    @FXML private ListView taskListViewFXML;
    @FXML public Label currentTaskFXML;
    @FXML private Button startStopButtonFXML;
    @FXML private Button chooseTaskButtonFXML;

    private List<HBox> taskHBoxList = new ArrayList<>();
    private static List<Label> taskLabelList = new ArrayList<>();
    private static List<CheckBox> taskCheckBoxList = new ArrayList<>();
    private static List<Long> timeLeftList = new ArrayList<>();
    private int currentTaskNumber = 0;
    private boolean clockIsOn = false;
    private boolean autoSelect = true;
    private long timeLeft = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.newTaskFXML.setOnKeyReleased(event -> {
            if (event.getCode().equals(KeyCode.ENTER)){
                String s = this.newTaskFXML.getText();
                this.newTaskFXML.setText(s.substring(0, s.length()-1));
                addTask();
            }
        });
        /** read last tasks if exists */
        try {
            Scanner in = new Scanner(new File("lastTasks.txt"));
            while (in.hasNext()) {
                CheckBox checkBox = new CheckBox();
                checkBox.selectedProperty()
                        .addListener((observable, oldValue, newValue) -> {
                            checkBox.setSelected(newValue);
                            int index = this.taskCheckBoxList.indexOf(checkBox);
                            if (oldValue) {
                                this.taskLabelList
                                        .get(index)
                                        .setTextFill(Color.DIMGRAY);
                            } else {
                                setTime(index);
                                this.taskLabelList
                                        .get(index)
                                        .setTextFill(Color.GREEN);
                                if (this.currentTaskNumber == index && !this.autoSelect) {
                                    this.autoSelect = true;
                                }
                            }
                            if (!this.clockIsOn && this.autoSelect) {
                                changeCurrentTask();
                            }
                        });
                this.taskCheckBoxList.add(checkBox);

                String isSelected = in.nextLine();

                Label newTask = new Label(in.nextLine());
                newTask.setMaxWidth(260);
                newTask.setWrapText(true);

                int elementNumber = this.taskHBoxList.size();

                this.taskLabelList.add(newTask);

                HBox hBox = new HBox(5);
                hBox.getChildren().addAll(
                        this.taskCheckBoxList.get(elementNumber),
                        this.taskLabelList.get(elementNumber));
                this.taskHBoxList.add(hBox);


                this.timeLeftList.add(Long.parseLong(in.nextLine()));

                if (isSelected.equals("true")){
                    newTask.setTextFill(Color.GREEN);
                    checkBox.setSelected(true);
                    setTime(this.taskLabelList.size()-1);
                }else {
                    newTask.setTextFill(Color.DIMGRAY);
                }
                changeCurrentTask();
                this.taskListViewFXML.setItems(FXCollections.observableArrayList(this.taskHBoxList));
            }
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
        }
        /**-------------------*/
    }

    public static void saveTaskOnClose() throws FileNotFoundException {
        if (taskLabelList.size()>0) {
            PrintWriter write = new PrintWriter("lastTasks.txt");
            for (int i = 0; i < taskLabelList.size(); i++) {
                if (taskCheckBoxList.get(i).isSelected()) {
                    write.println("true");
                } else {
                    write.println("false");
                }
                taskLabelList.get(i).setText(taskLabelList.get(i).getText() + "  (");
                write.println(taskLabelList.get(i)
                        .getText()
                        .substring(0, taskLabelList.get(i).getText().indexOf("  (")));
                write.println(timeLeftList.get(i));
            }
            write.close();
        }
    }

    public void addTask() {
        if (Pattern.matches("[\\w\\W]+[\\w\\W ]*", this.newTaskFXML.getText())) {
            CheckBox checkBox = new CheckBox();
            checkBox.selectedProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        checkBox.setSelected(newValue);
                        int index = this.taskCheckBoxList.indexOf(checkBox);
                        if (oldValue) {
                            this.taskLabelList
                                    .get(index)
                                    .setTextFill(Color.DIMGRAY);
                        } else {
                            setTime(index);
                            this.taskLabelList
                                    .get(index)
                                    .setTextFill(Color.GREEN);
                            if (this.currentTaskNumber == index && !this.autoSelect) {
                                this.autoSelect = true;
                            }
                        }
                        if (!this.clockIsOn && this.autoSelect) {
                            changeCurrentTask();
                        }
                    });
            this.taskCheckBoxList.add(checkBox);

            int elementNumber = this.taskHBoxList.size();

            Label newTask = new Label(this.newTaskFXML.getText());
            newTask.setMaxWidth(260);
            newTask.setWrapText(true);
            if (elementNumber == 0) {
                newTask.setTextFill(Color.BLACK);
            } else {
                newTask.setTextFill(Color.DIMGRAY);
            }
            this.taskLabelList.add(newTask);

            HBox hBox = new HBox(5);
            hBox.getChildren().addAll(
                    this.taskCheckBoxList.get(elementNumber),
                    this.taskLabelList.get(elementNumber));
            this.taskHBoxList.add(hBox);

            this.taskListViewFXML.setItems(FXCollections.observableArrayList(this.taskHBoxList));

            this.timeLeftList.add((long) 0);

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
            this.chooseTaskButtonFXML.setDisable(true);
            start();
        } else {
            this.startStopButtonFXML.setText("Start");
            this.chooseTaskButtonFXML.setDisable(false);
            this.autoSelect = true;
            stop();
            taskListViewFXML.scrollTo(this.currentTaskNumber);
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

        setTime(this.currentTaskNumber);

        this.taskCheckBoxList.get(this.currentTaskNumber).setDisable(false);
        this.taskCheckBoxList.get(this.currentTaskNumber).setSelected(true);
    }

    public void changeCurrentTask(){
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
    }

    public void changeCurrentTask(int index){
        if (!this.taskLabelList.get(this.currentTaskNumber)
                .getTextFill().equals(Color.GREEN)) {

            this.taskLabelList
                    .get(this.currentTaskNumber)
                    .setTextFill(Color.DIMGRAY);
        }

        this.currentTaskNumber = index;

        String s = this.taskLabelList
                .get(this.currentTaskNumber)
                .getText();
        this.currentTaskFXML.setText(s);

        this.taskCheckBoxList
                .get(this.currentTaskNumber)
                .setSelected(false);

        this.taskListViewFXML
                .getSelectionModel()
                .select(this.currentTaskNumber);
        this.taskLabelList
                .get(this.currentTaskNumber)
                .setTextFill(Color.BLACK);
    }

    public void chooseTask(ActionEvent actionEvent) throws InterruptedException {
        this.startStopButtonFXML.setDisable(false);
        this.autoSelect = false;
        changeCurrentTask(this.taskListViewFXML.getSelectionModel().getSelectedIndex());
    }

    public void setTime(int index){
        this.taskLabelList
                .get(index)
                .setText(this.taskLabelList.get(index).getText()+"  (");
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

    public void clear(ActionEvent actionEvent) {
        PrintWriter clear = null;
        try {
            clear = new PrintWriter("lastTasks.txt");
            clear.print("");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        clear.close();

        while (this.taskHBoxList.size() - 1 >= 0) {
            this.taskLabelList.remove(this.taskHBoxList.size() - 1);
            this.taskCheckBoxList.remove(this.taskHBoxList.size() - 1);
            this.timeLeftList.remove(this.taskHBoxList.size() - 1);
            this.taskHBoxList.remove(this.taskHBoxList.size() - 1);
        }
        this.taskListViewFXML.setItems(FXCollections.observableArrayList(this.taskHBoxList));
    }

    public void moveUp(ActionEvent actionEvent) {
        int index = this.taskListViewFXML.getSelectionModel().getSelectedIndex();
        if (index > 0)
            moveElement(index, --index);
    }

    public void moveDown(ActionEvent actionEvent) {
        int index = this.taskListViewFXML.getSelectionModel().getSelectedIndex();
        if (index+1 != this.taskHBoxList.size())
            moveElement(index, ++index);
    }

    public void moveElement(int index1, int index2){
        HBox hBox = this.taskHBoxList.get(index1);
        this.taskHBoxList.set(index1, this.taskHBoxList.get(index2));
        this.taskHBoxList.set(index2, hBox);

        Label label = this.taskLabelList.get(index1);
        this.taskLabelList.set(index1, this.taskLabelList.get(index2));
        this.taskLabelList.set(index2, label);

        CheckBox checkBox = this.taskCheckBoxList.get(index1);
        this.taskCheckBoxList.set(index1, this.taskCheckBoxList.get(index2));
        this.taskCheckBoxList.set(index2, checkBox);

        long t = this.timeLeftList.get(index1);
        this.timeLeftList.set(index1, this.timeLeftList.get(index2));
        this.timeLeftList.set(index2, t);

        for (int i = 0; i < this.taskLabelList.size(); i++) {
            if (this.taskLabelList.get(i).getTextFill().equals(Color.BLACK)||
                    this.taskLabelList.get(i).getTextFill().equals(Color.CORAL)) {
                this.currentTaskNumber = i;
                break;
            }
        }
        this.taskListViewFXML.setItems(FXCollections.observableArrayList(this.taskHBoxList));
    }
}
