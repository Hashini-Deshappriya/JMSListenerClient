# Sample project to listen to any modifications in WSO2 API Manager 3.2.0

This is a sample project which can be used to connect to message broker in the WSO2 API Manager 3.2.0. This sample project conects to the 'notification' topic deployed in WSO2 API manager and prints a message on the terminal on any modification such as API creation, update, application creation, etc. 

## Supported events
Following are the list of events that can be captured by listing to 'notification' topic

    API_CREATE, API_UPDATE, API_DELETE, API_LIFECYCLE_CHANGE, APPLICATION_CREATE, APPLICATION_UPDATE, 
    APPLICATION_DELETE, APPLICATION_REGISTRATION_CREATE, POLICY_CREATE, POLICY_UPDATE, POLICY_DELETE, 
    SUBSCRIPTIONS_CREATE, SUBSCRIPTIONS_UPDATE, SUBSCRIPTIONS_DELETE, DEPLOY_API_IN_GATEWAY, 
    REMOVE_API_FROM_GATEWAY, REMOVE_APPLICATION_KEYMAPPING, SCOPE_CREATE, SCOPE_UPDATE, SCOPE_DELETE
    
## How to run

1. Open the EventReceiver.java file and change the username, password and the url of the API Manager 

2. Build the maven project

3. Execute the following command (Or run the EventReceiver main method).

    `mvn compile exec:java -Dexec.mainClass="com.chamila.sampleapp.EventReceiver"`
     
4. Log in the WSO2 API Manager and do any action (Ex: create an application)

5. You will see a message on the console




     

   
