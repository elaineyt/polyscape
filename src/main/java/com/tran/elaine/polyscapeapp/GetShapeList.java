package com.tran.elaine.polyscapeapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;


/**
 * Created by Elaine on 7/10/2017.
 */


public class GetShapeList extends AsyncTask<String, String, String> {

    private GetShapeListCompleted taskCompleted;

    public GetShapeList(GetShapeListCompleted callback) {
        taskCompleted = callback;
    }

    @Override
    protected String doInBackground(String... param) {
        String uri = "http://elainetran.com/polyscape/shapes/index.xml";
        return request(uri);
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("Test", result);
        ArrayList<String> shapeList = new ArrayList<String>();
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);

            InputStream stream = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
            parser.setInput(stream, null);
          //  parser.next();

            shapeList = readFile(parser);

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
        }

        ArrayList<Bitmap> imshpBitmapFilterList = new ArrayList<Bitmap>();
        ArrayList<String> imshpNameList = new ArrayList<String>();



        try {
            Bitmap imshpBitmapFilter = null;
            Log.d("Test", ""+shapeList.size());
            for(int i = 0; i < shapeList.size(); i++) {
                String url = "http://elainetran.com/polyscape/shapes/tn/" + shapeList.get(i);
                Log.d("Test", url);
                imshpNameList.add(shapeList.get(i));
                InputStream in = new java.net.URL(url).openStream();
                imshpBitmapFilter = BitmapFactory.decodeStream(in);
                imshpBitmapFilterList.add(imshpBitmapFilter);


            }


        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            // Log.d("Test", e.getMessage());
            Log.d("Test", "5");
            e.printStackTrace();
        }


        taskCompleted.GetShapeListCompleted(imshpBitmapFilterList, imshpNameList);

    }


    @Override
    protected void onPreExecute() {

    }

    @Override
    protected void onProgressUpdate(String... text) {

    }

    public String request(String uri) {

        StringBuilder sb = new StringBuilder();

        HttpURLConnection urlConnection = null;

        try {

            URL url = new URL(uri);
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setReadTimeout(5000);
            urlConnection.setConnectTimeout(5000);
            InputStream in = urlConnection.getInputStream();


            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String inputLine;

            while ((inputLine = r.readLine()) != null) {
                sb.append(inputLine);
            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.d("Test", e.getMessage());
        } finally {
            urlConnection.disconnect();
        }
        return sb.toString();
    }

    private ArrayList<String> readFile(XmlPullParser parser) throws IOException, XmlPullParserException {

        ArrayList<String> shapeList = new ArrayList<String>();
        parser.next();
        parser.require(XmlPullParser.START_TAG, null, "shapes");

        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals("shape")) {
                shapeList.add(parser.getAttributeValue(null, "file"));

            }
        }

        return shapeList;

    }
}
