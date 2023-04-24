# CARMETER
## The Project Idea: 
Making desktop application to monitor the speed of a car and track its position on the map, also there is a speed limit configured by the app, and if the speed got
increased above this limit, the alarm sound will be initiated.

## The Desktop App:
![gui](https://user-images.githubusercontent.com/118214245/205881067-c145d79a-8ee1-4a96-8fd9-dcb6d10dd57e.png)

In the app we have:

 * A slider changes the speed limit and it affects the speed limiter gauge.
 * The current speed gauge which is used to display the actual car speed in Km/h.
 * 'Show Map' Button which is used to open a new stage containing the map with a mark initially placed at ITI position and then it will be updated if the GPS module
 connected and the car is moving
 
 **Note**: If there is no internet connection, the map stage will be opened and the app will inform you with an alert that you're offline.
 
 * 'Internet Connected' Button: this is used to tell the program that you have connected to a network (and this is required only if you have lost your connection
 and then connected again), then you will be able to show the map and see the updates of your location.
 * 'GPS Connected' Button: In case you have your connection to the Arduino and the GPS module, you can reconnect it and press 'GPS Connected' button to establish the
 communication between the app and Arduino again.
 * The labels which are used for indicating the updates in latitude and longitude.

 ## Features of The App:
 
 * Responsive design: The components in the app fit themselves to keep the designed final positions in the stage when maximizing and minimizing or displaying the 
  the stage alongside other stages.
  
  ![res1](https://user-images.githubusercontent.com/118214245/205886882-72472f0b-e0b8-4f75-bb97-e4aea9209d19.png) 
  
  ![res2](https://user-images.githubusercontent.com/118214245/205886997-747086be-b33f-45df-9eb0-9dbe0ba998f8.png)
  
  * Check if there is no internet connection, and also check if the GPS module is not connected.
  
  ![no internet](https://user-images.githubusercontent.com/118214245/205888091-f5166426-94ff-45fd-a8f0-a4d6acec43e5.png)
  
  ![no gps](https://user-images.githubusercontent.com/118214245/205888130-b0ef1829-8995-4d4a-a4f1-790914528475.png)

 * Continuous listening and receiving data from Arduino (latitude and longitude and speed) via serial communication.
 * Continuous update for the position marker on the map.
 * The speed is continuously updated into the current speed gauge.
 * You can set the speed limit via the speed limit slider and it will be displayed on the speed limiter gauge, and if the current speed exceeded the speed 
 limit, an alarm will be initiated.
 
 **Note:** you can find a short demonstration video and actual running of the app in this [link](https://youtu.be/oRMRqPTpZlI)
 
  
