package mainpkg;

import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
    
    private MediaPlayer mediaPlayer;
    private Thread speedSettingThread;
    private Thread updateUiThread;
    private float currentSpeed = 0;
    private float settingSpeed = 0;
    private Slider setSpeedSlider;
    private Gauge carSpeedGauge;
    private Gauge carSpeedSetting;
    private Button showMapStageBtn;
    private Button refreshInternet;
    private Button refreshGPS;
    private boolean noInternet = false;
    private boolean noGps = false;
    private SerialCommunication serialComm;
    public static double LATITUDE = 0.0, LONGITUDE = 0.0, SPEED = 0.0;
    public static boolean READY = false;
    private Label latLabel, longLabel;

    @Override
    public void init() throws Exception {
        super.init();
        serialComm = new SerialCommunication();
        Media media = new Media(new File("carAlarm.mp3").toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setAutoPlay(true);
        speedSettingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //check if the speed exceeds the setting
                while(true){
                    if(settingSpeed >= currentSpeed){
                        if(mediaPlayer.getStatus() == MediaPlayer.Status.STOPPED){
                            mediaPlayer.play();
                        }else
                            mediaPlayer.play();
                    }else{
                        mediaPlayer.stop();
                    }
                }
            }
        });
        
        carSpeedGauge = GaugeBuilder.create()
                .skinType(Gauge.SkinType.MODERN)
                .title("Current Speed")
                .subTitle("Speed Now")
                .unit("RPM")
                .prefSize(400, 400)
                .maxValue(180)
                .build();
        carSpeedGauge.setAnimated(true);
        carSpeedGauge.setDecimals(0);
        carSpeedGauge.setAnimationDuration(250);
        carSpeedGauge.setThresholdColor(Color.RED);  //color will become red if it crosses threshold value
        carSpeedGauge.setThreshold(170);
        carSpeedGauge.setThresholdVisible(true);
        carSpeedGauge.setNeedleColor(Color.BLUE);
        carSpeedGauge.setValue(90);
        latLabel = new Label("Latitude: ");
        latLabel.setId("label");
        longLabel = new Label("Longitude: ");
        longLabel.setId("label");
        serialComm.getSpeedGaugeControl(carSpeedGauge);
        serialComm.getLatLabelControl(latLabel);
        serialComm.getLongLabelControl(longLabel);
        carSpeedSetting = GaugeBuilder.create()
                .title("Speed Limiter")
                .skinType(Gauge.SkinType.MODERN)
                .subTitle("Setting the speed")
                .unit("RPM")
                .prefSize(300, 300)
                .maxValue(180)
                .build();
        carSpeedSetting.setAnimated(true);
        carSpeedSetting.setDecimals(0);
        carSpeedSetting.setAnimationDuration(250);
        carSpeedSetting.setNeedleColor(Color.BLUE);
        carSpeedSetting.setValue(20);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        checkNoIntenet();
        if(noInternet)
            showNoIntenetAlert();
        serialComm.connect();
        if(!SerialCommunication.PORT_CONNECTED){
            noGps = true;
            showNoGPSAlert();
        }
        
        speedSettingThread.start();
        //startUpdateUiThread();
        
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
            if(!noInternet){
                MapStage mapStage = new MapStage();
                mapStage.showMapWindow();
            }else{
                showNoIntenetAlert();
            }
        });
        
        refreshInternet.setOnAction((ActionEvent event) -> {
            checkNoIntenet();
            if(noInternet){
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
        setSpeedSlider.setValue(20);
        setSpeedSlider.setId("slider");
        
        setSpeedSlider.valueProperty().addListener(new ChangeListener<Number>(){
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                settingSpeed = newValue.floatValue();
                carSpeedSetting.setValue(settingSpeed);
            }
        });

        HBox guagesBox = new HBox();
        guagesBox.setPadding(new Insets(30));
        guagesBox.setAlignment(Pos.CENTER);
        guagesBox.setSpacing(250);
        guagesBox.getChildren().addAll(carSpeedGauge, carSpeedSetting);
        //FlowPane guages = new FlowPane(carSpeedGauge, carSpeedSetting);
        
        HBox btnBox = new HBox();
        btnBox.setPadding(new Insets(30));
        btnBox.setAlignment(Pos.CENTER);
        btnBox.getChildren().addAll(showMapStageBtn, refreshInternet, refreshGPS);
        VBox btnsAndLabelsContainer = new VBox();
        btnsAndLabelsContainer.setPadding(new Insets(30));
        btnsAndLabelsContainer.setAlignment(Pos.CENTER);
        btnsAndLabelsContainer.getChildren().addAll(btnBox, latLabel, longLabel);
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
        speedSettingThread.stop();
        mediaPlayer.dispose();
        if(SerialCommunication.PORT_CONNECTED)
            serialComm.disconnect();
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
    public void checkNoIntenet(){
        try{
            URL url = new URL("https://www.google.com");
            URLConnection connection = url.openConnection();
            connection.connect();
            noInternet = false;
        }catch (MalformedURLException e) {
            noInternet = true;
        }catch (IOException e) {
            noInternet = true;
        }
    }
}
