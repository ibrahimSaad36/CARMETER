package serialpkg;

import eu.hansolo.medusa.Gauge;
import gnu.io.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.scene.control.Label;

public class SerialCommunication {

    SerialPort serialPort;
    InputStream in;
    Thread serialReadThread;
    BufferedReader buf;
    public static boolean PORT_CONNECTED = false;
    private Gauge speedGauge;
    private Label latLbl, longLbl;
    
    public SerialCommunication() {
        
    }
    
    public void connect(){
        Enumeration<?> e = CommPortIdentifier.getPortIdentifiers();
        System.out.println("There are any ports?" + e.hasMoreElements());
        while (e.hasMoreElements()) {
            CommPortIdentifier portIdentifier = (CommPortIdentifier) e.nextElement();

            if (portIdentifier.isCurrentlyOwned()) {
                System.out.println("Error: Port is currently in use");
            } else {
                //filter all ports for serial port only
                if (portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL){
                    PORT_CONNECTED = true;
                    String portName = portIdentifier.getName();
                    System.out.println(portName);
                    try{
                        serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(), 2000);
                        serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                    }catch (PortInUseException ex) {
                        System.err.println(ex.getMessage());
                        return;
                    }catch (UnsupportedCommOperationException ex) {
                        System.err.println(ex.getMessage());
                        return;
                    }
                    try{
                        in = serialPort.getInputStream();
                    }catch (IOException ex) {
                        System.err.println(ex.getMessage());
                    }

                    serialReadThread = new Thread(new SerialReader(in));
                    serialReadThread.setDaemon(true);
                    serialReadThread.start();
                    System.out.println("serial thread started");
                }
            }

        }
    }
    public void disconnect() {
        if (serialReadThread.isAlive()) {
            serialReadThread.stop();
            serialPort.close();
            System.out.println("Serial Port Thread Stopped!");
            PORT_CONNECTED = false;
        }
    }
    public void getSpeedGaugeControl(Gauge gauge){
        speedGauge = gauge;
    }
    public void getLatLabelControl(Label lat){
        latLbl = lat;
    }
    public void getLongLabelControl(Label Lon){
        longLbl = Lon;
    }
    
    class SerialReader implements Runnable{

        String[] data;
        
        private SerialReader(InputStream in) {
            
        }
        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int len = -1;
            StringBuilder strBuild = new StringBuilder("");
            String recievedData = new String();
            try{
                while ((len = in.read(buffer)) > -1) {
                    mainpkg.MainClass.READY = false;
                    String str = new String(buffer, 0, len);
                    if(!str.equals("\n"))
                        strBuild.append(str);
                    else{
                        recievedData = new String(strBuild);
                        data = recievedData.split(" ");
                        System.out.println(data[0] + ", " + data[1] + ", " + data[2]);
                        if(!data[0].equals("") && !data[1].equals("") && !data[2].equals(""))
                        {
                            mainpkg.MainClass.LATITUDE = Double.parseDouble(data[0]);
                            mainpkg.MainClass.LONGITUDE = Double.parseDouble(data[1]);
                            mainpkg.MainClass.SPEED = Double.parseDouble(data[2]);
                            System.out.println(mainpkg.MainClass.LATITUDE);
                            System.out.println(mainpkg.MainClass.LONGITUDE);
                            System.out.println(mainpkg.MainClass.SPEED);
                            mainpkg.MainClass.READY = true;
                            Platform.runLater(() -> {
                                speedGauge.setValue(mainpkg.MainClass.SPEED);
                                latLbl.setText("Latitude: " + mainpkg.MainClass.LATITUDE);
                                longLbl.setText("Longitude: " + mainpkg.MainClass.LONGITUDE);
                            });
                            
                            strBuild = new StringBuilder("");
                        }
                        
                    }
                }
            }catch (IOException e){
                System.err.println(e.getMessage());
            }
        }
    }
}
