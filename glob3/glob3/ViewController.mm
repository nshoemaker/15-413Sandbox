//
//  ViewController.m
//  glob3
//
//  Created by Pratik Prakash on 1/31/15.
//  Copyright (c) 2015 Pratik Prakash. All rights reserved.
//

#import "ViewController.h"
#include <G3MiOSSDK/G3MWidget_iOS.h>
#include <G3MiOSSDK/G3MBuilder_iOS.hpp>
#include <G3MiOSSDK/MarksRenderer.hpp>
#include <G3MiOSSDK/MeshRenderer.hpp>
#include <G3MiOSSDK/Mark.hpp>
#include <G3MiOSSDK/Planet.hpp>
#include <G3MiOSSDK/CoordinateSystem.hpp>
#include <G3MiOSSDK/Mesh.hpp>
#include <G3MiOSSDK/Geodetic3D.hpp>
#include <G3MiOSSDK/IThreadUtils.hpp>
#include <G3MiOSSDK/GInitializationTask.hpp>
#include <G3MiOSSDK/IDownloader.hpp>
#include <G3MiOSSDK/IBufferDownloadListener.hpp>
#include <G3MiOSSDK/IJSONParser.hpp>
#include <G3MiOSSDK/JSONParser_iOS.hpp>
#include <G3MiOSSDK/JSONBaseObject.hpp>
#include <G3MiOSSDK/JSONObject.hpp>
#include <G3MiOSSDK/JSONArray.hpp>
#include <G3MiOSSDK/JSONNumber.hpp>
#include <G3MiOSSDK/IDownloader.hpp>


#include <string>
#include <fstream>
#include <sstream>
#include <streambuf>

@interface ViewController ()
@end


class InitTask : GInitializationTask {
    
    void run(const G3MContext* context) {
        IBufferDownloadListener listener = new IBufferDownloadListener();
        
        URL* file = new URL("file:///data.json");
        long long priority = 1;
        TimeInterval timeToCache = TimeInterval::fromDays(30);
        bool readExpired = true;
        bool deleteListener = true;
        
        long id = context->getDownloader()->requestBuffer(file, priority, timeToCache, readExpired, listener, deleteListener);
        
    }
    
    bool isDone(const G3MContext* context) {
        
    }
};

@implementation ViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    
    G3MBuilder_iOS builder([self view]);
    const Planet* planet = Planet::createEarth();
    builder.setPlanet(planet);
    /*
    double lat = 28.96384553643802, lon = -13.60974902228918;
    CoordinateSystem sr = planet->getCoordinateSystemAt(Geodetic3D::fromDegrees(lat, lon, 0));
    Mesh* _mesh = sr.createMesh(1e4, Color::red(), Color::green(), Color::yellow());
    MeshRenderer* _meshrender = new MeshRenderer();
    _meshrender->addMesh(_mesh);
    
   
    builder.addRenderer(_meshrender);
    */

    //Creates marks renderer
    bool readyWhenMarksReady = true;
    MarksRenderer* mRenderer = new MarksRenderer::MarksRenderer(readyWhenMarksReady);
    builder.addRenderer(mRenderer);
    

    class DownloadListener : IBufferDownloadListener {
        
        
        
        void onDownload(const URL& url,IByteBuffer* buffer,bool expired) {
            
            //Create the JSON parser
            IJSONParser* jParser = new JSONParser_iOS();
            const JSONBaseObject* jsonBuildingData = jParser->parse(buffer);
            
            //Create a list of Geodetic
            std::vector<Geodetic3D*> coords;
            
            //Gets the type field from the JSON building data
            const JSONObject* buildings = jsonBuildingData->asObject();
            const JSONArray* features = buildings->getAsArray("features");
            for (int i=0; i < features->size(); i++) {
                const JSONObject* feature = features->getAsObject(i);
                const JSONObject* geometry = feature->getAsObject("geometry");
                const JSONArray* coordArray = geometry->getAsArray("coordinates");
                
                //TODO: get all the coordinates in geometry
                double lat = coordArray->getAsArray(0)->getAsArray(0)->getAsNumber(0, 0);
                double lon = coordArray->getAsArray(0)->getAsArray(0)->getAsNumber(1, 0);
                double height = 0;
                
                Geodetic3D tempCoord = Geodetic3D::fromDegrees(lat, lon, height);
                coords.push_back(&tempCoord);
                //std::string name = feature->getAsString("type", "");
                //TODO finish parsing all the other fields from building data
            }
            
            //TODO: Put a mark on all the coordinates in coords vector list
            for (int i = 0; i < coords.size(); i++) {
                
                URL iconurl = URL::URL("");
                double minDistanceToCamera = 0;
                MarkUserData* userData = new MarkUserData::MarkUserData();
                bool autoDeleteUserData = true;
                MarkTouchListener* marksListener = NULL;
                bool autoDeleteListener = true;
                Mark* mark = new Mark(iconurl, *coords.at(i), ABSOLUTE, minDistanceToCamera, userData, autoDeleteUserData, marksListener, autoDeleteListener);
                
                mRenderer->addMark(mark); //still gives errors about local variable mRenderer
            }
            
        }
        
    };

    
    builder.initializeWidget();
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

// Start animation when view has appeared
- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    // Start the glob3 render loop
    [self.view startAnimation];
}
// Stop the animation when view has disappeared
- (void)viewDidDisappear:(BOOL)animated
{
    // Stop the glob3 render loop
    [self.view stopAnimation];
    [super viewDidDisappear:animated];
}
// Release property
- (void)viewDidUnload
{
    self.view        = nil;
}

@end
