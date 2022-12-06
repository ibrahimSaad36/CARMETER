package serialpkg;

import gnu.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import javafx.application.Platform;
import mainpkg.MapStage;

public class SerialCommunication {

    SerialPort serialPort;
    InputStream in;
    Thread serialReadThread;
    public static boolean PORT_CONNECTED = false;
    public static boolean READY_FOR_LATLNG = false;
    private boolean readyForSpeed = false;
    public static double LATITUDE = 0, LONGITUDE = 0, SPEED = 0;
    
    public SerialCommunication(){
    }
    
    public void connect(){
        Enumeration e = CommPortIdentifier.getPortIdentifiers();
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
                        serialReadThread = new Thread(new SerialReader(in));
                        serialReadThread.setDaemon(true);
                        serialReadThread.start();
                        System.out.println("serial thread started");
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
                }
            }

        }
    }
    public void closePort() {
        if (serialReadThread.isAlive()) {
            serialReadThread.stop();
            serialPort.close();
            System.out.println("Serial Port Thread Stopped!");
            PORT_CONNECTED = false;
        }
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
            String recievedData;
            try{
                while ((len = in.read(buffer)) > -1) {
                    String str = new String(buffer, 0, len);
                    if(!str.equals("\n"))
                        strBuild.append(str);
                    else{
                        recievedData = new String(strBuild);
                        data = recievedData.split(" ");
                        READY_FOR_LATLNG = false;
                        readyForSpeed = false;
                        System.out.println(data[0] + ", " + data[1] + ", " + data[2]);
                        if(!data[0].equals("") && !data[1].equals("") && !data[2].equals(""))
                        {
                            try{
                                LATITUDE = Double.parseDouble(data[0]);
                                LONGITUDE = Double.parseDouble(data[1]);
                                System.out.println(LATITUDE);
                                System.out.println(LONGITUDE);
                                READY_FOR_LATLNG = true;
                            }catch(NumberFormatException ex){
                                System.err.println("Lat and Long Not a Number");
                                READY_FOR_LATLNG = false;
                            }
                            try{
                                SPEED = Double.parseDouble(data[2]);
                                System.out.println(SPEED);
                                readyForSpeed = true;
                            }catch(NumberFormatException ex){
                                System.err.println("Speed Not a Number");
                                readyForSpeed = false;
                            }
                            Platform.runLater(() -> {
                                if(READY_FOR_LATLNG){
                                    //map.updateMarker(LATITUDE, LONGITUDE);
                                    mainpkg.MainClass.updateLabels(LATITUDE, LONGITUDE);
                                }
                                if(readyForSpeed){
                                    mainpkg.MainClass.updateSpeedGauge(SPEED);
                                    mainpkg.MainClass.checkAlarm(SPEED);
                                }
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
