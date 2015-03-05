package com.example.myandroidapp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.glob3.mobile.generated.Color;
import org.glob3.mobile.generated.G3MContext;
import org.glob3.mobile.generated.GAsyncTask;
import org.glob3.mobile.generated.GInitializationTask;
import org.glob3.mobile.generated.GTask;
import org.glob3.mobile.generated.Geodetic3D;
import org.glob3.mobile.generated.IBufferDownloadListener;
import org.glob3.mobile.generated.IByteBuffer;
import org.glob3.mobile.generated.IJSONParser;
import org.glob3.mobile.generated.JSONArray;
import org.glob3.mobile.generated.JSONBaseObject;
import org.glob3.mobile.generated.JSONObject;
import org.glob3.mobile.generated.PeriodicalTask;
import org.glob3.mobile.generated.TimeInterval;
import org.glob3.mobile.generated.Trail;
import org.glob3.mobile.generated.TrailsRenderer;
import org.glob3.mobile.generated.URL;
import org.glob3.mobile.specific.G3MBuilder_Android;
import org.glob3.mobile.specific.G3MWidget_Android;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.LinearLayout;


// This is a small application for modelling a single orbit of the ISS as a Trail.
public class MainActivity extends Activity {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final G3MBuilder_Android builder = new G3MBuilder_Android(this);

    // add Trails Renderer so I can create a trail to represent the satellite
    final TrailsRenderer trailsRenderer = new TrailsRenderer();
    // add the trail and give it a large enough area that we will be able to
    // see it from a large distance
    final Trail trail = new Trail(Color.fromRGBA255(225, 10, 10, 255), 50000,
        15000);
    // add the trail to the renderer
    trailsRenderer.addTrail(trail);
    // add the renderer to the builer
    builder.addRenderer(trailsRenderer);

    builder.setInitializationTask(new GInitializationTask() {
      // create flag used in isDone() method to represent when all the 
      // positions in the trail have been loaded.
      final AtomicBoolean loadedTrail = new AtomicBoolean(false);

      @Override
      public void run(final G3MContext context) {
        IBufferDownloadListener listener = new IBufferDownloadListener() {

          @Override
          public void onError(URL url) {
            // TODO Auto-generated method stub

          }

          @Override
          public void onDownload(URL url, final IByteBuffer buffer,
              boolean expired) {
            context.getThreadUtils().invokeAsyncTask(new GAsyncTask() {
              // list of names of the satellites
              List<String> names = new ArrayList<String>();
              // list of locations of the satellite
              List<Geodetic3D> coords = new ArrayList<Geodetic3D>();
              // list of timestamps associated with each of the
              // locations of the satellite
              List<Double> times = new ArrayList<Double>();

              @Override
              public void runInBackground(G3MContext context) {
                // parse the json file with the satellite info
                IJSONParser parser = context.getJSONParser();
                JSONBaseObject data = parser.parse(buffer);
                JSONObject object = data.asObject();

                // get the feature array and itterate through it
                JSONArray features = object.getAsArray("features");

                for (int i = 0; i < features.size(); i++) {
                  JSONObject feature = features.getAsObject(i);
                  // get the name of the satellite
                  String name = feature.getAsString("name", "");
                  // get the latitude, longitude, and altitude (height)
                  // of the satellite
                  double lat = feature.getAsNumber("latitude", 0);
                  double lon = feature.getAsNumber("longitude", 0);
                  double alt = feature.getAsNumber("altitude", 0);

                  // add all the parsed data to their respective lists
                  names.add(name);
                  coords.add(Geodetic3D.fromDegrees(lat, lon, alt));
                  times.add(feature.getAsNumber("timestamp", 0));
                }
              }

              @Override
              public void onPostExecute(G3MContext context) {
                // loop through each of the satellite datapoints and 
                // add them as a position in the trail.
                for (int i = 0; i < times.size(); i++) {
                  trail.addPosition(coords.get(i));
                }

                // flag that all the positions in the trail are now loaded
                loadedTrail.set(true);
              }
            }, true);

          }

          @Override
          public void onCanceledDownload(URL url, IByteBuffer buffer,
              boolean expired) {
            // TODO Auto-generated method stub

          }

          @Override
          public void onCancel(URL url) {
            // TODO Auto-generated method stub

          }
        };
        long id = context.getDownloader().requestBuffer(
            new URL("file:///iss60_100.json"), 1, TimeInterval.fromDays(30),
            true, listener, true);
        // context.getDownloader().cancelRequest(id);
      }

      @Override
      public boolean isDone(G3MContext context) {
        // done when all the positions in the trail are loaded
        return loadedTrail.get();
      }
    });

    builder.addPeriodicalTask(new PeriodicalTask(TimeInterval.fromSeconds(5),
        new GTask() {

          @Override
          public void run(G3MContext context) {
            // TODO Auto-generated method stub

          }

        }));

    final G3MWidget_Android g3mWidget = builder.createWidget();

    // g3mWidget.setCameraPosition(new Geodetic3D(demSector.getCenter(),
    // 10000));

    final LinearLayout layout = (LinearLayout) findViewById(R.id.glob3);
    layout.addView(g3mWidget);

  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

}