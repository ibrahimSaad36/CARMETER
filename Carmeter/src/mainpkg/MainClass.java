package mainpkg;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import serialpkg.SerialCommunication;

public class MainClass extends Application{
    
    private static MediaPlayer MEDIAPLAYER;
    private static double SETTING_SPEED = 30;
    private Slider setSpeedSlider;
    private static Gauge CAR_SPEED_GAUGE;
    private Gauge carSpeedSetting;
    private Button showMapStageBtn;
    private Button refreshInternet;
    private Button refreshGPS;
    public static boolean NO_INTERNET = false;
    private boolean noGps = false;
    private SerialCommunication serialComm;
    private static Label LAT_LABEL, LONG_LABEL;
    private MapStage mapStage;

    @Override
    public void init() throws Exception {
        super.init();
        serialComm = new SerialCommunication();
        Media media = new Media(new File("carAlarm.mp3").toURI().toString());
        MEDIAPLAYER = new MediaPlayer(media);
        
        CAR_SPEED_GAUGE = GaugeBuilder.create()
                .skinType(Gauge.SkinType.MODERN)
                .title("Current Speed")
                .subTitle("Speed Now")
                .unit("Km/h")
                .prefSize(400, 400)
                .maxValue(180)
                .build();
        CAR_SPEED_GAUGE.setAnimated(true);
        CAR_SPEED_GAUGE.setDecimals(0);
        CAR_SPEED_GAUGE.setAnimationDuration(250);
        CAR_SPEED_GAUGE.setThresholdColor(Color.RED);  //color will become red if it crosses threshold value
        CAR_SPEED_GAUGE.setThreshold(170);
        CAR_SPEED_GAUGE.setThresholdVisible(true);
        CAR_SPEED_GAUGE.setNeedleColor(Color.BLUE);
        CAR_SPEED_GAUGE.setValue(0);
        LAT_LABEL = new Label("Latitude: ");
        LAT_LABEL.setId("label");
        LONG_LABEL = new Label("Longitude: ");
        LONG_LABEL.setId("label");
        carSpeedSetting = GaugeBuilder.create()
                .title("Speed Limiter")
                .skinType(Gauge.SkinType.MODERN)
                .subTitle("Setting the speed")
                .unit("Km/h")
                .prefSize(300, 300)
                .maxValue(180)
                .build();
        carSpeedSetting.setAnimated(true);
        carSpeedSetting.setDecimals(0);
        carSpeedSetting.setAnimationDuration(250);
        carSpeedSetting.setNeedleColor(Color.BLUE);
        carSpeedSetting.setValue(SETTING_SPEED);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        checkNoIntenet();
        if(NO_INTERNET)
            showNoIntenetAlert();
        serialComm.connect();
        if(!SerialCommunication.PORT_CONNECTED){
            noGps = true;
            showNoGPSAlert();
        }
        
        showMapStageBtn = new Button("Show Map");
        refreshInternet = new Button("Internet Connected");
        refreshGPS = new Button("GPS Connected");
        showMapStageBtn.setId("btn");
        refreshInternet.setId("btn");
        refreshGPS.setId("btn");
        showMapStageBtn.setFont(new Font(Font.getDefault().getName(), 25));
        refreshInternet.setFont(new Font(Font.getDefault().getName(), 25));
        refreshGPS.setFont(new Font(Font.getDefault().getName(), 25));
        
        showMapStageBtn.setOnAction((ActionEvent event) -> {
            checkNoIntenet();
            if(!NO_INTERNET){
                mapStage = new MapStage();
                mapStage.showMapWindow();
            }else{
                showNoIntenetAlert();
            }
        });
        
        refreshInternet.setOnAction((ActionEvent event) -> {
            checkNoIntenet();
            if(NO_INTERNET){
                showNoIntenetAlert();
            }
        });
        
        refreshGPS.setOnAction((ActionEvent event) -> {
            if(noGps){
                serialComm.connect();
                if(!SerialCommunication.PORT_CONNECTED){
                    noGps = true;
                    showNoGPSAlert();
                } 
            }
        });
        
        setSpeedSlider = new Slider();
        setSpeedSlider.setMin(0);
        setSpeedSlider.setMax(180);
        // enable the marks
        setSpeedSlider.setShowTickMarks(true);
        // enable the Labels
        setSpeedSlider.setShowTickLabels(true);
        setSpeedSlider.setValue(SETTING_SPEED);
        setSpeedSlider.setId("slider");
        
        setSpeedSlider.valueProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                SETTING_SPEED = newValue.floatValue();
                carSpeedSetting.setValue(SETTING_SPEED);
            }
        });

        HBox guagesBox = new HBox();
        guagesBox.setPadding(new Insets(30));
        guagesBox.setAlignment(Pos.CENTER);
        guagesBox.setSpacing(250);
        guagesBox.getChildren().addAll(CAR_SPEED_GAUGE, carSpeedSetting);
        
        HBox btnBox = new HBox();
        btnBox.setPadding(new Insets(30));
        btnBox.setAlignment(Pos.CENTER);
        btnBox.getChildren().addAll(showMapStageBtn, refreshInternet, refreshGPS);
        VBox btnsAndLabelsContainer = new VBox();
        btnsAndLabelsContainer.setPadding(new Insets(30));
        btnsAndLabelsContainer.setAlignment(Pos.CENTER);
        btnsAndLabelsContainer.getChildren().addAll(btnBox, LAT_LABEL, LONG_LABEL);
        BorderPane root= new BorderPane();
        root.setId("root");
        root.setCenter(guagesBox);
        root.setTop(setSpeedSlider);
        root.setBottom(btnsAndLabelsContainer);
        Scene scene = new Scene(root, 1500, 700);
        scene.getStylesheets().add(getClass().getResource("../csspkg/styles.css").toString());
        primaryStage.setTitle("OUR CARMETER");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        //speedSettingThread.stop();
        MEDIAPLAYER.dispose();
        if(SerialCommunication.PORT_CONNECTED)
            serialComm.closePort();
        if(SerialCommunication.READY_FOR_LATLNG)
            mapStage.closeUpdateMarkerThread();
    }
    
    public static void main(String[] args){
        Application.launch(args);
    }
    
    public void showNoIntenetAlert(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("NO Internet Connection");
        alert.setHeaderText("Oops, We Couldn't Connect To a Network, Please Connect To Any Netwrok, Then Click Intenert Connected");
        alert.showAndWait();
    }
    public void showNoGPSAlert(){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("NO GPS");
        alert.setHeaderText("Oops, We Couldn't Find GPS, Reconnect The Module and Then Click GPS Connected");
        alert.showAndWait();
    }
    public static void checkNoIntenet(){
        try{
            URL url = new URL("https://www.google.com");
            URLConnection connection = url.openConnection();
            connection.connect();
            NO_INTERNET = false;
        }catch (MalformedURLException e) {
            NO_INTERNET = true;
        }catch (IOException e) {
            NO_INTERNET = true;
        }
    }
    public static void checkAlarm(double currentSpeed){
        if(currentSpeed >= SETTING_SPEED){
            if(MEDIAPLAYER.getStatus() == MediaPlayer.Status.STOPPED){
                MEDIAPLAYER.play();
            }else
                MEDIAPLAYER.play();
        }else{
            MEDIAPLAYER.stop();
        }
    }
    public static void updateSpeedGauge(double speed){
        CAR_SPEED_GAUGE.setValue(speed);
    }
    public static void updateLabels(double lat, double lng){
        LAT_LABEL.setText("Latitude: " + lat);
        LONG_LABEL.setText("Longitude: " + lng);
    }
}
