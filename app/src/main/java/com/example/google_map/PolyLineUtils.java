package com.example.google_map;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PolyLineUtils {
    private static Polyline mPolyline;
    private static GoogleMap mMap;


    /**
     * Get poly line.
     *
     * @param aMap the a map
     * @param aURL the a url
     */
    public static void getPolyLine(GoogleMap aMap, String aURL){
        mMap = aMap;
        FetchUrl fetchUrl = new FetchUrl();
        fetchUrl.execute(aURL);
    }

    /**
     * The type Fetch url.
     */
    public static class FetchUrl extends AsyncTask<String, Void, String>
    {
        /**
         * The Ride book fragment.
         */
        MapsActivity mMapsActivity;


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask lParserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            lParserTask.execute(result);

        }

        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;

        }

        private String downloadUrl(String strUrl) throws IOException
        {
            String data = "";
            InputStream iStream = null;
            HttpURLConnection urlConnection = null;
            try
            {
                URL url = new URL(strUrl);

                // Creating an http connection to communicate with url
                urlConnection = (HttpURLConnection) url.openConnection();

                // Connecting to url
                urlConnection.connect();

                // Reading data from url
                iStream = urlConnection.getInputStream();

                BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                Log.d("downloadUrl", data);
                br.close();

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            } finally {
                if (iStream != null) {
                    iStream.close();
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return data;
        }
    }


    /**
     * The type Parser task.
     */
    public static class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        // Parsing the data in non-ui thread
        private PolylineOptions lineOptions = null;
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                Log.w("ParserTask", jsonData[0]);
                DataParser parser = new DataParser();
                Log.w("ParserTask", parser.toString());

                // Starts parsing data
                routes = parser.parse(jObject);
                Log.w("ParserTask", "Executing routes");
                Log.w("ParserTask", routes.toString());

            } catch (Exception e) {
                Log.w("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }


        // Executes in UI thread, after the parsing process
        @Override
        public void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;


            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(5);
                lineOptions.color(Color.BLACK);

                Log.w("onPostExecute", "onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route

            if (lineOptions != null) {
                if (mPolyline != null) {
                    mPolyline.remove();
                }

                //Add Polyline to map.
                mPolyline = mMap.addPolyline(lineOptions);
//                mMap.addPolyline(lineOptions);
            } else {
                Log.w("onPostExecute", "without Polylines drawn");
            }
        }
    }
}
