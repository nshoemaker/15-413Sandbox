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

public class MainActivity extends Activity {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    final G3MBuilder_Android builder = new G3MBuilder_Android(this);

    // final Geodetic2D lower = new Geodetic2D( //
    // Angle.fromDegrees(43.532822), //
    // Angle.fromDegrees(1.350360));
    // final Geodetic2D upper = new Geodetic2D( //
    // Angle.fromDegrees(43.668522), //
    // Angle.fromDegrees(1.515350));
    //
    // final Sector demSector = new Sector(lower, upper);
    //
    // final LayerSet layerSet = new LayerSet();
    // final WMSLayer franceRaster4000K = new WMSLayer("Raster4000k", new
    // URL("http://www.geosignal.org/cgi-bin/wmsmap?", false),
    // WMSServerVersion.WMS_1_1_0, demSector, "image/jpeg", "EPSG:4326", "",
    // false, new LevelTileCondition(0, 18),
    // TimeInterval.fromDays(30), true);

    // layerSet.addLayer(franceRaster4000K);
    // builder.getPlanetRendererBuilder().setLayerSet(layerSet);

    // final MapBoxLayer mboxTerrainLayer = new
    // MapBoxLayer("examples.map-qogxobv1", TimeInterval.fromDays(30), true,
    // 11);
    // layerSet.addLayer(mboxTerrainLayer);
    final TrailsRenderer trailsRenderer = new TrailsRenderer();
    final Trail trail = new Trail(Color.fromRGBA255(225, 10, 10, 255), 50000,
        15000);
    trailsRenderer.addTrail(trail);
    // final ShapesRenderer shapesRenderer = new ShapesRenderer();
    // marksRenderer.addMark(new Mark("MyMark", Geodetic3D.fromDegrees(0, 0, 0),
    // AltitudeMode.ABSOLUTE, 0));
    // marksRenderer
    // .addMark(new Mark(
    // new URL(
    // "http://upload.wikimedia.org/wikipedia/commons/thumb/5/5a/Wikipedia's_W.svg/50px-Wikipedia's_W.svg.png"),
    // Geodetic3D.fromDegrees(50, 0, 0), AltitudeMode.ABSOLUTE, 0));
    builder.addRenderer(trailsRenderer);

    builder.setInitializationTask(new GInitializationTask() {
      final AtomicBoolean loadedMarkers = new AtomicBoolean(false);

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
              List<String> names = new ArrayList<String>();
              List<Geodetic3D> coords = new ArrayList<Geodetic3D>();
              List<Double> times = new ArrayList<Double>();

              @Override
              public void runInBackground(G3MContext context) {
                IJSONParser parser = context.getJSONParser();
                JSONBaseObject data = parser.parse(buffer);
                JSONObject object = data.asObject();
                JSONArray features = object.getAsArray("features");
                for (int i = 0; i < features.size(); i++) {
                  JSONObject feature = features.getAsObject(i);
                  String name = feature.getAsString("name", "");
                  double lat = feature.getAsNumber("latitude", 0);
                  double lon = feature.getAsNumber("longitude", 0);
                  double alt = feature.getAsNumber("altitude", 0);
                  names.add(name);
                  coords.add(Geodetic3D.fromDegrees(lat, lon, alt));
                  times.add(feature.getAsNumber("timestamp", 0));
                }
              }

              @Override
              public void onPostExecute(G3MContext context) {
                for (int i = 0; i < times.size(); i++) {
                  trail.addPosition(coords.get(i));
                  // marksRenderer.addMark(new Mark(names.get(i), coords.get(i),
                  // AltitudeMode.ABSOLUTE, 0, 7));
                  // shapesRenderer.addShape(new BoxShape(coords.get(i),
                  // AltitudeMode.ABSOLUTE, new Vector3D(100000, 100000, pops
                  // .get(i)), 2, Color.red()));
                }
                loadedMarkers.set(true);
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
        // TODO Auto-generated method stub
        return loadedMarkers.get();
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