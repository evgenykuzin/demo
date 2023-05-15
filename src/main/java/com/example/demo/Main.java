package com.example.demo;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Main extends Application {
    int menuSize = 425;
    int menuSizeTwo = 135;
    int minimumSeconds = 0;
    int maximumSeconds = 60;
    int minimumMinutes = 0;
    int maximumMinutes = 60;
    int minimumHours = 0;
    int maximumHours = 24;
    Spinner<Integer> seconds = new Spinner<Integer>(minimumSeconds, maximumSeconds, 0, 1);
    Spinner<Integer> minutes = new Spinner<Integer>(minimumMinutes, maximumMinutes, 0, 1);
    Spinner<Integer> hours = new Spinner<Integer>(minimumHours, maximumHours, 0, 1);
    int delayamount = 1000;//how many ms between iterations?

    @Override
    public void start(Stage primaryStage) {
        setupSpinners();

        HBox holder = new HBox();
        HBox labeledSpinners = new HBox();
        VBox spinners = new VBox();
        VBox labels = new VBox();
        Button btJiggle = new Button("Jiggle the mouse pls");//button to jiggle the mouse
        btJiggle.setOnAction(new jigglehandler());//wire the button to jiggle the mouse
        btJiggle.setAlignment(Pos.CENTER);
        spinners.getChildren().add(seconds);
        spinners.getChildren().add(minutes);
        spinners.getChildren().add(hours);
        labels.getChildren().add(new Text("Seconds:"));
        labels.getChildren().add(new Text("Minutes:"));
        labels.getChildren().add(new Text("Hours: "));
        labeledSpinners.getChildren().add(labels);
        labeledSpinners.getChildren().add(spinners);
        holder.getChildren().add(labeledSpinners);
        holder.getChildren().add(btJiggle);
        labels.setSpacing(10);
        labeledSpinners.setSpacing(5);
        holder.setSpacing(10);
        Scene primscene = new Scene(holder, menuSize, menuSizeTwo);
        primaryStage.setScene(primscene);//make the scene with all the stuff in it and set it to the main window
        primaryStage.show();//show the main window
    }

    private void setupSpinners() {
        seconds.setEditable(false);
        minutes.setEditable(false);
        hours.setEditable(false);
        seconds.getValueFactory().setWrapAround(true);
        minutes.getValueFactory().setWrapAround(true);
        //hours doesn't wraparound because we don't want to set hours to 24 by lowering minutes without there being an hour in there
        seconds.valueProperty().addListener(new secondsIncrementHandler());
        minutes.valueProperty().addListener(new minutesChangeHandler());
        hours.valueProperty().addListener(new hoursChangeHandler());
    }

    class jigglehandler implements EventHandler<ActionEvent> {

        @Override
        public void handle(ActionEvent event) {
            int howlongnum = 0;
            try {
                PointerInfo a = MouseInfo.getPointerInfo();
                Point b = a.getLocation();
                int x = (int) b.getX();
                int y = (int) b.getY();
                Robot bot = new Robot();
                howlongnum = parseInput();
                bot.setAutoWaitForIdle(true);
                for (int i = 0; i < howlongnum; i++) {
                    a = MouseInfo.getPointerInfo();
                    b = a.getLocation();
                    x = (int) b.getX();
                    y = (int) b.getY();//get the location of the mouse each time the loop starts
                    if (i % 2 == 0) {
                        bot.mouseMove(x + 10, y - 10);
                        bot.delay(delayamount);
                    } else {
                        bot.mouseMove(x - 10, y + 10);
                        bot.delay(delayamount);
                    }//Wait a second, then if it's an even cycle of movement, move it to the right, otherwise, move it to the left
                }
            } catch (AWTException ex) {
                System.err.println("AWT Exception from the jigglehandler at " + DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss").format(LocalDateTime.now()));
                ex.printStackTrace();
                Stage ErrorStage = new Stage();
                HBox inside = new HBox();
                Scene ErrorScene = new Scene(inside, menuSize, menuSizeTwo);
                Text error = new Text("Oh dear, the bot has become angry.\n There's a log, in a logs folder.\nIt's in a folder called Glacier Nester, on your main drive.\n Send it to GlacierNester@gmail.com");
                inside.getChildren().add(error);
                ErrorStage.setScene(ErrorScene);
                ErrorStage.show();
            }
        }

        private Integer parseInput() {
            int numberOfSeconds = 0;
            numberOfSeconds += seconds.getValue();
            numberOfSeconds += minutes.getValue() * 60;
            numberOfSeconds += hours.getValue() * 3600;
            return numberOfSeconds;
        }

    }

    class secondsIncrementHandler implements ChangeListener<Integer> {

        @Override
        public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
            if (oldValue == maximumSeconds - 1 && newValue == maximumSeconds) {
                minutes.increment(1);
                //if they max out seconds, increment minutes
                //originally we were resetting the seconds manually here
                //but setting the wraparound property to true does that for us
            }
            if (oldValue == minimumSeconds + 1 && newValue == minimumSeconds) {
                minutes.decrement(1);
            }
            //essentially, if they max out seconds, increase the minutes, but if they zero out seconds, decrement minute
        }

    }

    class minutesChangeHandler implements ChangeListener<Integer> {

        @Override
        public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
            if (oldValue == maximumMinutes - 1 && newValue == maximumMinutes) {
                hours.increment(1);
                //originally we were setting the values manually here
                //but setting the wraparound property to true does that for us
            }
            if (oldValue == minimumMinutes + 1 && newValue == minimumMinutes) {
                hours.decrement(1);
            }
            //essentially, if they max out minutes, increase the hours, but if they zero out minutes, decrement hour
            if (oldValue == minimumMinutes && newValue == maximumMinutes) {
                minutes.getValueFactory().setValue(0);
                //we deal with minute underflow manually though because wraparound is needed in the minutes spinner
            }
        }

    }

    class hoursChangeHandler implements ChangeListener<Integer> {

        @Override
        public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
            if (oldValue == maximumHours - 1 && newValue == maximumHours) {
                seconds.getValueFactory().setValue(minimumSeconds);
                minutes.getValueFactory().setValue(minimumMinutes);
                hours.getValueFactory().setValue(minimumHours);
                //if they max out hours, it resets the whole schpiel
            }
        }

    }

    public static void main(String[] args) throws IOException {
//        File logFolder = new File("C:\\Glacier Nester\\logs");
//        File file = null;
//        if (!logFolder.exists()) {
//            logFolder.setWritable(true);
//            if (logFolder.mkdirs()) {
//                file = new File("C:\\Glacier Nester\\logs\\err.log");
//            }
//        } else {
//            file = new File("C:\\Glacier Nester\\logs\\err.log");
//        }
//        FileOutputStream fos = new FileOutputStream(file);
//        PrintStream ps = new PrintStream(fos);
//        System.setErr(ps);
        System.err.println("Started MouseJiggler at " + DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss").format(LocalDateTime.now()));
        launch(args);
    }


}
