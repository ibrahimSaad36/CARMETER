package mainpkg;


import com.dlsc.gmapsfx.GoogleMapView;
import com.dlsc.gmapsfx.MapComponentInitializedListener;
import com.dlsc.gmapsfx.javascript.object.GoogleMap;
import com.dlsc.gmapsfx.javascript.object.LatLong;
import com.dlsc.gmapsfx.javascript.object.MapOptions;
import com.dlsc.gmapsfx.javascript.object.MapTypeIdEnum;
import com.dlsc.gmapsfx.javascript.object.Marker;
import com.dlsc.gmapsfx.javascript.object.MarkerOptions;
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
    
    public MapStage(){
        mapView = new GoogleMapView("en", "AIzaSyD7lZzbkOXqLFLzZnGvb7Kd9rnTZO8wGko");
        mapView.setKey("AIzaSyD7lZzbkOXqLFLzZnGvb7Kd9rnTZO8wGko");
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
        mapOptions = new MapOptions();
        mapOptions.center(new LatLong(30.033333, 31.233334))
                .mapType(MapTypeIdEnum.ROADMAP)
                .overviewMapControl(false)
                .panControl(false)
                .rotateControl(false)
                .scaleControl(false)
                .streetViewControl(false)
                .zoomControl(false)
                .zoom(13);

        map = mapView.createMap(mapOptions);
        markerOptions = new MarkerOptions();
        markerOptions.position(new LatLong(30.033333, 30.033333))
                .visible(Boolean.TRUE)
                .title("My Marker");

        marker = new Marker(markerOptions);
    }
    
}
