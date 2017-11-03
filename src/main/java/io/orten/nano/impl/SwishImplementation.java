package io.orten.nano.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.orten.nano.model.SwishPaymentRequestObject;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;


public class SwishImplementation {

    public static URLConnection createURL(String url) throws Exception{
        URL myURL = new URL(url);
        URLConnection connection = myURL.openConnection();
        connection.setDoOutput(true);
        return connection;
    }

    public static void writeObject(InputStream input) throws IOException {
        BufferedReader in = null;
        try {

            in = new BufferedReader(new InputStreamReader(input));

            String decodingString;
            while ((decodingString = in.readLine()) != null) {
                System.out.println(decodingString);
            }
        } finally {
            if (in != null)
                in.close();
        }
    }
    public  static  void  saveData(URLConnection con, String data) throws IOException{
        if(con !=null) {
            OutputStreamWriter out = new OutputStreamWriter(con.getOutputStream());
            out.write(data);
            out.close();
        }else{
            System.out.println("Connetion problem !!!");
        }
    }



    // GET Method
    public static  void sendObject()throws Exception{

        writeObject(createURL("http://18.221.31.52:8080/nano.se/api/projects/1").getInputStream());///change to projects
    }
    private static TrustManager[] trustCertificate(){
        TrustManager[] trustAllCerts= new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(
                            X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(
                            X509Certificate[] certs, String authType) {
                    }
                }
        };
        return trustAllCerts;
    }

    private void loadKeyStore()throws Exception{
        KeyStore clientStore = KeyStore.getInstance("PKCS12");
        InputStream clientStoreInputStream = getClass().getResourceAsStream("/1231181189.p12");
        clientStore.load(clientStoreInputStream, "swish".toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientStore, "swish".toCharArray());
        KeyManager[] kms = kmf.getKeyManagers();

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kms, trustCertificate(), new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

    }
  /*  private static SwishPaymentRequestObject loadPaymentObject(){

        String payeePaymentReference= "0123456789";
        String  callbackUrl = "https://mss.swicpc.bankgirot.se/swish-cpcapi/api/v1/paymentrequests";
        String payerAlias ="4671234768";
        String payeeAlias = "1231181189";
        String amount = "100";
        String currency = "SEK";
        String  message = "test amount";
        SwishPaymentRequestObject swishPaymentRequestObject=null;
        try {
     //       swishPaymentRequestObject= new SwishPaymentRequestObject(payeePaymentReference,callbackUrl, payerAlias, payeeAlias, amount, currency, message);

        }catch (Exception e){
            e.printStackTrace();
        }
        return swishPaymentRequestObject;
    }*/
    // POST Method
    public Map sendPOST(SwishPaymentRequestObject paymentRequest) throws IOException {


        try{
            loadKeyStore();
        }catch (Exception e){ System.out.println("failed to load key store");e.printStackTrace(); throw new RuntimeException(e);}

            System.setProperty("https.protocols", "TLSv1.2");
            String SwishBaseUrl = "https://mss.swicpc.bankgirot.se/swish-cpcapi/api/v1/paymentrequests/";
            URL requestURL = new URL(SwishBaseUrl);
            HttpsURLConnection httpsURLConnection = (HttpsURLConnection) requestURL.openConnection();

            httpsURLConnection.setRequestMethod("POST");
            httpsURLConnection.setRequestProperty("Content-Type", "application/json");
            httpsURLConnection.setDoOutput(true);

            httpsURLConnection.connect();


            ObjectMapper objectMapper = new ObjectMapper();
            String swishJson = objectMapper.writeValueAsString(paymentRequest);

            System.out.println(swishJson);


            try{
                saveData(httpsURLConnection, swishJson);

                int responseCode = httpsURLConnection.getResponseCode();
                System.out.println(responseCode);
                System.out.println(httpsURLConnection.getResponseMessage());



                if (responseCode == 201) {
                    String entityUrlString = httpsURLConnection.getHeaderField("Location");
                    URL entityUrl = new URL(entityUrlString);
                    HttpsURLConnection entityUrlConnection = (HttpsURLConnection) entityUrl.openConnection();
                    entityUrlConnection.setRequestMethod("GET");
                    entityUrlConnection.setRequestProperty("Content-Type", "application/json");

                    entityUrlConnection.connect();

                    return objectMapper.readValue(entityUrlConnection.getInputStream(), Map.class);

                }else {
                    throw new RuntimeException("bad response from swish");
                }

            } catch (IOException ex) {
                ex.printStackTrace();
                writeObject(httpsURLConnection.getErrorStream());
                throw new RuntimeException(ex);
            }


    }

}
