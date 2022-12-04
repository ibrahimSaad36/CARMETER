package mainpkg;


import com.dlsc.gmapsfx.GoogleMapView;
import com.dlsc.gmapsfx.MapComponentInitializedListener;
import com.dlsc.gmapsfx.javascript.object.GoogleMap;
import com.dlsc.gmapsfx.javascript.object.LatLong;
import com.dlsc.gmapsfx.javascript.object.MapOptions;
import com.dlsc.gmapsfx.javascript.object.MapTypeIdEnum;
import com.dlsc.gmapsfx.javascript.object.Marker;
import com.dlsc.gmapsfx.javascript.object.MarkerOptions;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEvent;
import javafx.stage.Stage;

public class MapStage implements MapComponentInitializedListener{

    GoogleMapView mapView;
    GoogleMap map;
    MarkerOptions markerOptions;
    Marker marker;
    MapOptions mapOptions;
    private Stage stage;
    private Thread updateMarkerThread;
    
    public MapStage(){
        mapView = new GoogleMapView("en", "Your API KEY");
        mapView.setKey("Your API KEY");
        mapView.addMapInitializedListener(this);
        mapView.setDisableDoubleClick(true);
        mapView.getWebview().getEngine().setOnAlert(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(WebEvent event) {
            }
        });
    }
    
    public void showMapWindow(){
        stage = new Stage();
        stage.setTitle("Car Position");
        StackPane root = new StackPane(mapView);
        Scene scene = new Scene(root, 1500, 900);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
    public void closeMapWindow(){
        stage.close();
    }

    @Override
    public void mapInitialized() {
        MainClass.checkNoIntenet();
        if(!MainClass.NO_INTERNET){
            mapOptions = new MapOptions();
            mapOptions.center(new LatLong(30.0711, 31.0211))
                    .mapType(MapTypeIdEnum.ROADMAP)
                    .overviewMapControl(false)
                    .panControl(false)
                    .rotateControl(false)
                    .scaleControl(false)
                    .streetViewControl(false)
                    .zoomControl(false)
                    .mapMarker(true)
                    .zoom(30);

            map = mapView.createMap(mapOptions);
            markerOptions = new MarkerOptions();
            markerOptions.position(new LatLong(30.0711, 31.0211))
                    .visible(Boolean.TRUE)
                    .title("Position");
            marker = new Marker(markerOptions);
            map.addMarker(marker);
            updateMarkerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true){
                        try {
                        Thread.sleep(2000);
                        Platform.runLater(new Runnable(){
                        @Override
                        public void run() {
                            if(serialpkg.SerialCommunication.READY_FOR_LATLNG){
                                updateMarker(serialpkg.SerialCommunication.LATITUDE, serialpkg.SerialCommunication.LONGITUDE);
                            }
                        }
                    });
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MapStage.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    }
                }
            });
            updateMarkerThread.start();
        }
    }
    public void updateMarker(double lat, double lng){
        MainClass.checkNoIntenet();
        if(!MainClass.NO_INTERNET){
            //if(marker != null)
                map.removeMarker(marker);
            markerOptions.position(new LatLong(lat, lng));
            markerOptions = new MarkerOptions();
            markerOptions.position(new LatLong(lat, lng))
                .visible(Boolean.TRUE)
                .title("Position");
            marker = new Marker(markerOptions);
            map.addMarker(marker);
            map.setCenter(new LatLong(lat, lng));
        } 
    }
    public void closeUpdateMarkerThread(){
        if(updateMarkerThread.isAlive())
            updateMarkerThread.stop();
    }
}
