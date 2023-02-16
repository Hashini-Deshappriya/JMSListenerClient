# Sample project to listen to JMS Topics




    
## How to run

1. Open the EventReceiver.java file and change the username, password and the url of the API Manager 
2. You need to provide a valid Access Token inside the EventReceiver.java file

3. Build the maven project

4. Execute the following command (Or run the EventReceiver main method).

    `mvn compile exec:java -Dexec.mainClass="com.chamila.sampleapp.EventReceiver"`
     
5. Log in the WSO2 API Manager and do any action (Ex: create an API and publish)

6. You will see a message on the console




     

   
