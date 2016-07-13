package module3;

//Java utilities libraries
import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Comparator;
import java.util.List;

//Processing library
import processing.core.PApplet;
import processing.core.PFont;
//Unfolding libraries
import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.providers.Google;
import de.fhpotsdam.unfolding.providers.MBTilesMapProvider;
import de.fhpotsdam.unfolding.utils.MapUtils;

//Parsing library
import parsing.ParseFeed;

/** EarthquakeCityMap
 * An application with an interactive map displaying earthquake data.
 * Author: UC San Diego Intermediate Software Development MOOC team
 * @author Megha Iyer
 * Date: July 17, 2015
 * */
public class EarthquakeCityMap extends PApplet {

	// You can ignore this.  It's to keep eclipse from generating a warning.
	private static final long serialVersionUID = 1L;

	// IF YOU ARE WORKING OFFLINE, change the value of this variable to true
	private static final boolean offline = false;
	
	// Less than this threshold is a light earthquake
	public static final float THRESHOLD_MODERATE = 5;
	// Less than this threshold is a minor earthquake
	public static final float THRESHOLD_LIGHT = 4;

	/** This is where to find the local tiles, for working without an Internet connection */
	public static String mbTilesString = "blankLight-1-3.mbtiles";
	
	// The map
	private UnfoldingMap map;
	
	// font
	PFont f;
	
	//Initialize features and markers
	List<Marker> markers;
	List<PointFeature> earthquakes;
	final int yellow = color(255, 255, 0);
    final int blue = color(0,255,255);
    final int red = color(255,0,0);
    final int gray = color(150,150,150);
	//feed with magnitude 2.5+ Earthquakes
	private String earthquakesURL = "http://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/2.5_week.atom";

	
	public void setup() {
		size(1200, 500);

		if (offline) {
		    map = new UnfoldingMap(this,20,200,700,500, new MBTilesMapProvider(mbTilesString));
		    earthquakesURL = "2.5_week.atom"; 	// Same feed, saved Aug 7, 2015, for working offline
		}
		else {
			//map = new UnfoldingMap(this,200,50,900,500,new Google.GoogleMapProvider());
	    map = new UnfoldingMap(this, 200, 50, 700, 500, new Google.GoogleMapProvider());
			// IF YOU WANT TO TEST WITH A LOCAL FILE, uncomment the next line
			//earthquakesURL = "2.5_week.atom";
		}
		
		//create a font
		f = createFont("Arial",12,true);
		
		//zoom to level 2		
	    map.zoomToLevel(2);
	    MapUtils.createDefaultEventDispatcher(this, map);	
			
	    // The List you will populate with new SimplePointMarkers
	    markers = new ArrayList<Marker>();

	    //Use provided parser to collect properties for each earthquake
	    //PointFeatures have a getLocation method
	    earthquakes = ParseFeed.parseEarthquake(this, earthquakesURL);
	    
	    // These print statements show you (1) all of the relevant properties 
	    // in the features, and (2) how to get one property and use it
	    
	    for (int i=0; i<earthquakes.size(); i++){
	        PointFeature f = earthquakes.get(i);
	    	System.out.println(f.getProperties());
	    	SimplePointMarker spm = createMarker(f);	    	
	    	markers.add(spm);	
	      }
	    
	   //Add markers to map    
	    map.addMarkers(markers);
	    
	 
	    
	    //TODO: Add code here as appropriate
	}
	
		
	// A suggested helper method that takes in an earthquake feature and 
	// returns a SimplePointMarker for that earthquake
	// TODO: Implement this method and call it from setUp, if it helps
	private SimplePointMarker createMarker(PointFeature feature)
	{
		Location location = feature.getLocation();
    	Object magObj = feature.getProperty("magnitude");
    	Float magnitude = Float.parseFloat(magObj.toString());
    	SimplePointMarker spm = new SimplePointMarker(location);
    	int[] colradArray = getIntensityProp(magnitude);
    	spm.setColor(colradArray[0]);
    	spm.setRadius((float)colradArray[1]);    	
		return spm;
	}
	
	private int[]  getIntensityProp(Float magnitude){
		
		int[] intarr = new int[2];
		
		if(magnitude<4.0){
			System.out.println("Minor earthquake. Set color as blue and radius as 4");
			intarr[0]=blue;
			intarr[1]= 6;			
		}else if(magnitude>=4.0 && magnitude<=4.9){
			System.out.println("Light earthquake. Set color as yellow and radius as 6");
			intarr[0]=yellow;
			intarr[1]= 8;
		}else if(magnitude>5.0){
			System.out.println("Severe earthquake. Set color as red and radius as 10");
			intarr[0]=red;
			intarr[1]= 12;
		}else{
			System.out.println("out of range earthquake. Set color as gray and radius as 2");
			intarr[0]=gray;
			intarr[1]= 4;
		}
		
		return intarr;
		
	}
		
	public void draw() {
	    background(10);
	    map.draw();
	    addKey();
	}


	// helper method to draw key in GUI
	// TODO: Implement this method to draw the key
	private void addKey() 
	{	
		textFont(f);       
		fill(255,255,255);
		text("Below 4.0",1000,200);  
		text("4.0+ Magnitude",1000,150);  
		text("5.0+ Magnitude",1000,100);  
		//blue
		fill(0,255,255);
	    ellipse(950,200,8,8);
	    
	  //yellow
	    fill(255,255,0);
	  	ellipse(950,150,10,10);
				
		//red
		fill(255,0,0);
		ellipse(950,100,16,16);

		
	
	}
}
