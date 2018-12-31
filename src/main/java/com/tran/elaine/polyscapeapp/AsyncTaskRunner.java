package com.tran.elaine.polyscapeapp;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


/**
 * Created by Elaine on 7/10/2017.
 */


public class AsyncTaskRunner extends AsyncTask<String, String, String>{

    private OnTaskCompleted taskCompleted;
    private String fileName;

    public AsyncTaskRunner(OnTaskCompleted callback, String fn){
        taskCompleted = callback;
        fileName = fn;
    }

    @Override
    protected String doInBackground(String... param){
        String uri = "http://elainetran.com/polyscape/shapes/index.xml";
        return request(uri);
    }

    @Override
    protected void onPostExecute(String result) {
      //  Log.d("Test", result);
        String x = "";
    //    try {
           // XmlPullParser parser = Xml.newPullParser();
            //parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

           // InputStream stream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
         //   parser.setInput(stream, null);
           // parser.next();

          //  x = readFile(parser);
    //    }

     //   catch (XmlPullParserException e) {
     //      e.printStackTrace();
     //   }

      //  catch(IOException e) {
      //  }

     //   URL url = null;

        Bitmap imshpBitmapFilter = null;
        try {
           // url = new URL("http://elainetran.com/polyscape/shapes/fgfiltertriangle.png");
String url = "http://elainetran.com/polyscape/shapes/" + fileName;

            InputStream in = new java.net.URL(url).openStream();
            imshpBitmapFilter = BitmapFactory.decodeStream(in);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
         catch (Exception e){
            // Log.d("Test", e.getMessage());
             Log.d("Test", "5");
             e.printStackTrace();
         }


        taskCompleted.OnTaskCompleted(imshpBitmapFilter);

    }


    @Override
    protected void onPreExecute(){

    }

    @Override
    protected void onProgressUpdate(String... text){

    }

    public String request(String uri){

        StringBuilder sb = new StringBuilder();

        HttpURLConnection urlConnection = null;

        try{

            URL url = new URL(uri);
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(5000);
            InputStream in = urlConnection.getInputStream();


            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String inputLine;

            while((inputLine = r.readLine()) != null){
                sb.append(inputLine);
            }
           // Log.d("Test", sb.toString());

        }
        catch (Exception e){
            e.printStackTrace();
            Log.d("Test", e.getMessage());
        }
        finally{
            urlConnection.disconnect();
        }
        return sb.toString();
    }

    private String readFile(XmlPullParser parser) throws IOException, XmlPullParserException {
        Log.d("Test", "readFile");
        String fileName = "";

        if(parser.getEventType()==parser.START_DOCUMENT){
            Log.d("Test", "start doc");
        }

        int eventType = parser.next();


        //parser.next();
        if(eventType==parser.TEXT){
            Log.d("Test", "text");
        }

        parser.require(XmlPullParser.START_TAG, null, "shapes");
        Log.d("Test","shapes");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("shape")) {
                Log.d("Test","shape");
                parser.require(XmlPullParser.END_TAG, null, "file");
                Log.d("Test","file");
                fileName = readText(parser);
                parser.require(XmlPullParser.END_TAG, null, "file");
                Log.d("Test", fileName);

            }
            else{
                skip(parser);
            }
        }

        return "";

    }

    private String readText(XmlPullParser parser) throws IOException, XmlPullParserException {
        String result = "";
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        return result;
    }

    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }



}

