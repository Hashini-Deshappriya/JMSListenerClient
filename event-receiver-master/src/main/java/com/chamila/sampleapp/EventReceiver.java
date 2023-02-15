package com.chamila.sampleapp;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.codec.binary.Base64;
import org.wso2.andes.client.message.AMQPEncodedMapMessage;
import java.util.Properties;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import org.json.JSONObject;
import org.json.*;
import org.json.JSONException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;



import java.io.IOException;
import java.net.URL;
import java.lang.*;

import java.net.HttpURLConnection;

import java.net.MalformedURLException;
import java.net.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.HttpsURLConnection;

import java.security.KeyStore;
import java.io.FileInputStream;
import java.util.*;
public class EventReceiver implements MessageListener {
    String topicName = "notification";
    TopicConnection topicConnection;
    TopicSession topicSession;


    //String brokerUrl = "tcp://0.0.0.0:5672";
    String brokerUrl = "tcp://localhost:5672";
    String username = "admin";
    String password = "admin";

    private static final Log log = LogFactory.getLog(EventReceiver.class);
    private boolean debugEnabled = log.isDebugEnabled();


    public static void main(String[] args) throws NamingException, JMSException {
        EventReceiver eventReceiver = new EventReceiver();
        eventReceiver.start();
    }

    private SSLContext createSSLContext(){
        try{
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("/home/hashini/setup/wso2am-3.2.0+1668439626961.full/wso2am-3.2.0_node1/repository/resources/security/wso2carbon.jks"),"wso2carbon".toCharArray());

            // Create key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, "wso2carbon".toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();

            // Create trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();

            // Initialize SSLContext
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(km,  tm, null);

            return sslContext;
        } catch (Exception ex){
            ex.printStackTrace();
        }

        return null;
    }

    public void start() throws NamingException, JMSException {
        Properties properties = new Properties();
        properties.put("java.naming.factory.initial", "org.wso2.andes.jndi.PropertiesFileInitialContextFactory");
        properties.put("transport.jms.DestinationType", "topic");
        properties.put("connectionfactory.TopicConnectionFactory",
                "amqp://" + username + ":" + password + "@clientid/carbon?brokerlist='" + brokerUrl + "'");
        properties.put("transport.jms.ConnectionFactoryJNDIName", "TopicConnectionFactory");

        InitialContext ctx = new InitialContext(properties);
        TopicConnectionFactory connFactory = (TopicConnectionFactory) ctx.lookup("TopicConnectionFactory");

        topicConnection = connFactory.createTopicConnection();
        topicConnection.start();
        topicSession = topicConnection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);

        Topic topic = topicSession.createTopic(topicName);
        MessageConsumer consumer = topicSession.createConsumer(topic);
        consumer.setMessageListener(this);
        System.out.println("\n\nEvent receiver started and Listening to " + brokerUrl);
    }



    @Override
    public void onMessage(Message message) {

        AMQPEncodedMapMessage msg = (AMQPEncodedMapMessage) message;


        try {
            String eventType = msg.getString("eventType");
            if(eventType.equals("DEPLOY_API_IN_GATEWAY")) {
                System.out.println("Inside the IF part");
                String encodedEvent = new String(Base64.decodeBase64(msg.getString("event").getBytes()));
                System.out.println(encodedEvent);
                JSONObject json = new JSONObject(encodedEvent);



                try {

                    SSLContext sslContext = this.createSSLContext();
                    try{
                        // Create socket factory
                        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                        // Create socket
                        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket("localhost", 9443);

                        System.out.println("SSL client started");

                    } catch (Exception ex){
                        ex.printStackTrace();
                    }

                    URL url = new URL("https://localhost:9443/api/am/publisher/v1/apis/cf077cd4-6492-46b0-9773-c9102308fe8e");
                    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                    conn.setSSLSocketFactory(sslContext.getSocketFactory());
                    conn.setRequestMethod("GET");

                    conn.setRequestProperty("Accept", "application/json");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("Authorization", "Bearer eyJ4NXQiOiJNell4TW1Ga09HWXdNV0kwWldObU5EY3hOR1l3WW1NNFpUQTNNV0kyTkRBelpHUXpOR00wWkdSbE5qSmtPREZrWkRSaU9URmtNV0ZoTXpVMlpHVmxOZyIsImtpZCI6Ik16WXhNbUZrT0dZd01XSTBaV05tTkRjeE5HWXdZbU00WlRBM01XSTJOREF6WkdRek5HTTBaR1JsTmpKa09ERmtaRFJpT1RGa01XRmhNelUyWkdWbE5nX1JTMjU2IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJhZG1pbiIsImF1dCI6IkFQUExJQ0FUSU9OX1VTRVIiLCJhdWQiOiJ6bG9FSzh6YTVSZmJiUExwbkpxMHV1MG43R0FhIiwibmJmIjoxNjc2NDY5MTU3LCJhenAiOiJ6bG9FSzh6YTVSZmJiUExwbkpxMHV1MG43R0FhIiwic2NvcGUiOiJhcGltOmFwaV9jcmVhdGUgYXBpbTphcGlfdmlldyIsImlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3Q6OTQ0M1wvb2F1dGgyXC90b2tlbiIsImV4cCI6MTY3NjQ3Mjc1NywiaWF0IjoxNjc2NDY5MTU3LCJqdGkiOiJlNzA3YjVmNi0wY2I4LTQwNmUtOWM2Ni0xZTc3NzQyNTgwYzQifQ.BeVD54w2sy-YIvezVy8hZqCSiaXzRSzW4cgIVmHl1BIZ6zCl2I3Z4sjAifnuLzWaA8iI7W9IhOgAsjl0deHxUmRrQtMO4eN7z6XazFuWeyLTZx0menVojwJZJm_7gtjfzs5sQM-u8sa3Xa8E-q6dJjbHC6Kj7o_R6LkrtADiSY-eLWE3wqBRe5R1IBJBB_N7nn-GMh2eCt9P_m8X5O_P2nyZpsSkQ-xTxbPbTE9ZwvD0ZSz7C43bvOPq9B3tHI0WnmZZLyh8HeqU2sFqxdGqECLhb8rJnIbu036ij4nOpa7L3bhXql7dlHPt-yBTJ7qLMlE80zgHdfeILqD1CUWMqA");




                    if (conn.getResponseCode() != 200) {
                        throw new RuntimeException("Failed : HTTP error code : "
                                + conn.getResponseCode());
                    }

                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            (conn.getInputStream())));
                    StringBuilder content = new StringBuilder();
                    String output;

                    System.out.println("Output from Server .... \n");
                    while ((output = br.readLine()) != null) {
                        content.append(output);
                        //System.out.println(output);

                    }
                    String test = content.toString();
                    System.out.println(test);

                    System.out.println(test);

                    JSONObject json1 = new JSONObject(test);


                    System.out.println(json1.toString());
                    String context = json1.getString("context");
                    System.out.println(context);

                    JSONArray ja_data = json1.getJSONArray("labels");
                    System.out.println(ja_data);

                    conn.disconnect();

                } catch (MalformedURLException e){

                }catch (IOException e) {

                    e.printStackTrace();

                }
            }
            //System.out.println("Event     : " + new String(Base64.decodeBase64(msg.getString("event").getBytes())));

// Generated by curl-to-java: https://fivesmallq.github.io/curl-to-java

//            Request request = Request.Get("https://localhost:9443/api/am/publisher/v1/apis/cf077cd4-6492-46b0-9773-c9102308fe8e");
//            HttpResponse httpResponse = request.execute().returnResponse();
//            System.out.println(httpResponse.getStatusLine());
//            request.setHeader("Authorization", "Bearer eyJ4NXQiOiJNell4TW1Ga09HWXdNV0kwWldObU5EY3hOR1l3WW1NNFpUQTNNV0kyTkRBelpHUXpOR00wWkdSbE5qSmtPREZrWkRSaU9URmtNV0ZoTXpVMlpHVmxOZyIsImtpZCI6Ik16WXhNbUZrT0dZd01XSTBaV05tTkRjeE5HWXdZbU00WlRBM01XSTJOREF6WkdRek5HTTBaR1JsTmpKa09ERmtaRFJpT1RGa01XRmhNelUyWkdWbE5nX1JTMjU2IiwiYWxnIjoiUlMyNTYifQ.eyJzdWIiOiJhZG1pbiIsImF1dCI6IkFQUExJQ0FUSU9OX1VTRVIiLCJhdWQiOiJ6bG9FSzh6YTVSZmJiUExwbkpxMHV1MG43R0FhIiwibmJmIjoxNjc2NDM5OTI4LCJhenAiOiJ6bG9FSzh6YTVSZmJiUExwbkpxMHV1MG43R0FhIiwic2NvcGUiOiJhcGltOmFwaV9jcmVhdGUgYXBpbTphcGlfdmlldyIsImlzcyI6Imh0dHBzOlwvXC9sb2NhbGhvc3Q6OTQ0M1wvb2F1dGgyXC90b2tlbiIsImV4cCI6MTY3NjQ0MzUyOCwiaWF0IjoxNjc2NDM5OTI4LCJqdGkiOiI4MTMwNDFlMS04ODBmLTRjMTYtODdmZC0wNWZjNWExNTFmOTQifQ.dK62xB4vl3PLYjA8NwEQbwLyLo0PSk8Fy_j9k4kpMNFw0HN4T1d42ckmr4qs2jz3SkhVBQamGfCze9ChQpyzLRRL9z38jZZwjgWUVcSD_-kIFboI-TTjT5mW7JH9Ouh99MzyK_IU7i-bTYIu5P9-Cbc5A3gVSQu_V4Y-h3rZpgLgjm05f18ZxyU94xE89U0_PVbw998AERPGOvT8OKoKrgUHgmkLStkiB3auIxByxbbW3bzV6a_zAK45H5efS9DHsXmaP6UVb74-DyOtAbo9hI2xXcAlY3RpOppuKu7Hms9iAIX0V1-vdOdFRX_Mj6svog0GYCXlI_v5Gd8gx7nXWQ");
//            HttpResponse httpResponse = request.execute().returnResponse();
//            System.out.println(httpResponse.getStatusLine());
//            if (httpResponse.getEntity() != null) {
//                String html = EntityUtils.toString(httpResponse.getEntity());
//                System.out.println(html);
//            }




        } catch (JMSException e) {
            System.out.print("Error whline reading the jms message " + e.getMessage());
        }
    }

}
