package unfolding_choropleth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.Feature;
import de.fhpotsdam.unfolding.data.GeoJSONReader;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.data.ShapeFeature;
import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;
import de.fhpotsdam.unfolding.marker.SimplePointMarker;
import de.fhpotsdam.unfolding.utils.MapUtils;
import de.fhpotsdam.unfolding.geo.Location;
import parsing.ParseFeed;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PFont;
import processing.core.PGraphics;

/**
 * An applet that shows airports statistics for each country in a Choropleth map.
 *  
 *  *******Data Statistics************
 *  -> Number of airports per country
 *  -> Total number of Inbound flights
 *  -> Total number of Outbound flights
 *  
 *  ******Map Behavior****************
 *  Density of country color directly correlates to the number of airports in that country.
 *  Denser color indicates more airports relative to other countries. 
 *  User hovers mouse over a country - Country gets highlighted and above data statistics is displayed in the key marker.
 *  User hovers mouse anywhere except a country - An informative message prompting user to hover over a country is displayed in the key marker.
 *  
 *   * @author Megha I.
 *
 */
public class AirportMap extends PApplet {
	
	UnfoldingMap map;
    List<Marker> airportList;
	List<Marker> routeList;
	List<Marker> CountryMarkers;
	Map<Object, Long> airportsDensityMap;
	Map<String,Integer[]> countryRoutes;
   	Marker lastselected;	
   	
public void setup() {
		// setting up PApplet
		size(800,600);
		
		// setting up map and default events
		map = new UnfoldingMap(this, 50, 50, 750, 550);
		map.zoomToLevel(2);
		map.setBackgroundColor(240);
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// Load country polygons and adds them as markers
		List<Feature> countries = GeoJSONReader.loadData(this, "countries.geo.json");
		CountryMarkers = MapUtils.createSimpleMarkers(countries);
			
		// get features from airport data
		List<PointFeature> features = ParseFeed.parseAirports(this, "airports.dat");
		airportList = new ArrayList<Marker>();
		// list for markers, hashmap for quicker access when matching with routes
		HashMap<Integer, Location> airports = new HashMap<Integer, Location>();
		HashMap<Integer, String> countryAirports = new HashMap<Integer,String>();
		
		// create markers from features
		for(PointFeature feature : features) {
			AirportMarker m = new AirportMarker(feature);
			m.setRadius(5);
			airportList.add(m);
			// put airport in hashmap with OpenFlights unique id for key
			airports.put(Integer.parseInt(feature.getId()), feature.getLocation());
		    countryAirports.put(Integer.parseInt(feature.getId()), feature.getProperty("country").toString());
			m.setProperty("AirportId", feature.getId());
			m.setProperty("AirportLocation",feature.getLocation());
		   
		}
				
		// parse route data
		List<ShapeFeature> routes = ParseFeed.parseRoutes(this, "routes.dat");
		routeList = new ArrayList<Marker>();
		for(ShapeFeature route : routes) {
						
			// get source and destination airportIds
			int source = Integer.parseInt((String)route.getProperty("source"));
			int dest = Integer.parseInt((String)route.getProperty("destination"));
			
			// get locations for airports on route
			if(airports.containsKey(source) && airports.containsKey(dest)) {
				route.addLocation(airports.get(source));
				route.addLocation(airports.get(dest));			
			}
			
			// get countries for airports on route
			if(countryAirports.containsKey(source)) {
			   route.addProperty("srcCountry", countryAirports.get(source));
			}
			if(countryAirports.containsKey(dest)){
			   route.addProperty("destCountry", countryAirports.get(dest));
			}
			if(!countryAirports.containsKey(source) && !countryAirports.containsKey("destination")){
				 route.addProperty("srcCountry", "NONE");
				 route.addProperty("destCountry", "NONE");
			}
			
			SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
		    routeList.add(sl);
		}
		
	 	//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
		//map.addMarkers(routeList);
		
		//Set the default stroke and color for country markers on choropleth map
		setCountryMarkerStroke(CountryMarkers);
		
		//Add country markers to map
		map.addMarkers(CountryMarkers);
		
		//Set Inbound and Outbound routes count per country in the CountryMarkers
		setRoutesCountPerCountry();
		
		//Generate a map of country as a key and total number of airports as value
		//Shade countries with color density been directly proportional to the number of airports.
        shadeCountries();
}
	
	public void draw() {
		background(240);
		map.draw();
		//Hover the mouse on a country and airports and routes statistics is seen on the key.
	    executeMouseEvent(CountryMarkers);
	}
	
 //shadeCountries function first creates an airportDensityMap to color each country per total number of airports count in a Country
   private void shadeCountries() {
	    long start_time = new Date().getTime();
	    
	    airportsDensityMap = airportList.stream().collect(Collectors.groupingBy(
		  		r -> r.getProperty("country"), Collectors.counting() ));
	    
	    Map<Object, Long> airportDensityOrderedMap = new LinkedHashMap<>();
		airportsDensityMap.entrySet().stream().sorted(Map.Entry.<Object,Long>comparingByValue().reversed()).forEachOrdered(e -> airportDensityOrderedMap.put(e.getKey(), e.getValue()));
	    //System.out.println(airportDensityOrderedMap);
	
		for (Marker marker : CountryMarkers) {
			// Find data for country of the current marker
			String countryName = "\""+ marker.getProperty("name").toString()+ "\"";
			if (airportDensityOrderedMap.containsKey(countryName)) {
				Long color_density = airportDensityOrderedMap.get(countryName);
			    // Encode value as brightness (values range: 1-350)
				int colorLevel = (int) map(color_density, 1,270, 162, 240);
				marker.setColor(color(255,100,100, colorLevel));
			}
			else {
				marker.setColor(color(100, 120));
			}
		}
		
	//Add airports data in country marker
		for(Marker marker : CountryMarkers){
			String countryName = "\""+ marker.getProperty("name").toString()+ "\"";
            if (airportsDensityMap.containsKey(countryName)){
				HashMap<String,Object> temp = marker.getProperties();
				temp.put("airportCount", airportsDensityMap.get(countryName));		
				marker.setProperties(temp);
				//System.out.println(marker.getProperties());
			}else{
				HashMap<String,Object> temp = marker.getProperties();
				temp.put("airportCount", 0);		
				marker.setProperties(temp);
			}
	    }
		
		long end_time = new Date().getTime();
	    System.out.println("shadeCountries function took " + (end_time-start_time)+" millisecs");
		
  }
  
  //setCountryMarkerStroke function to set default color and stroke to World choropleth map.
  public void setCountryMarkerStroke(List<Marker>CountryMarkers){
 	  for(Marker marker : CountryMarkers){
			   marker.setStrokeColor(200);
			   marker.setStrokeWeight(1);
	  }
  }
  
  //mousedMoved function override to add functionality when user hovers over a country
  @Override
  public void mouseMoved(){
  // clear the last selection
  	if (lastselected != null) {
  		lastselected.setStrokeColor(200);
  		lastselected.setStrokeWeight(1);
  		lastselected.setSelected(false);
  		lastselected = null;
  	}	  
  }
   
  //executeMouseEvent function helps control mouse events when user hovers over a country or outside the country. Sets a different color when user selects or hovers over a country.
   private void executeMouseEvent(List<Marker> markers)
	{
	  	// clear the last selection
	  	if (lastselected != null) {
	  		lastselected.setSelected(false);
	  		lastselected = null;
	  	}
	  	
		for(Marker marker : markers){
			boolean flag = marker.isInside(map,this.mouseX,this.mouseY);			
			if(flag){
			  lastselected = marker;
			  lastselected.setSelected(true);
			  lastselected.setStrokeColor(200);
			  lastselected.setStrokeWeight(1);
			  showTitle(marker);
		 	}else{
		 	  if(lastselected ==null){ 		
		 	  defaultKey();	
		 	  }
			}
		}	
	}
	
   private void defaultKey() {	
		// Remember you can use Processing's graphics methods here
	    PFont font;
        font = loadFont("Lato-Bold-14.vlw");
        int xbase = 925;
		int ybase = 50;
	    fill(255, 255, 255);
	    rect(xbase, ybase, 200, 100);
		textAlign(LEFT,CENTER);
		textSize(10);
		textFont(font);
		fill(100, 100, 100);
		text("Airport Statistics", xbase+20, ybase+20);
		text("Hover over a Country", xbase+20, ybase+40);
	}
   
   public void showTitle(Marker marker)
	{   
		int xbase = 925;
		int ybase = 50;
		String name =  marker.getStringProperty("name");
		PFont font;
		font = loadFont("Lato-Bold-14.vlw");
		fill(255, 255, 255);
	    rect(xbase, ybase, 300, 100);
	    fill(100, 100, 100);
		textAlign(PConstants.LEFT, PConstants.CENTER);
		textSize(10);
		textFont(font);
		text("Country:", xbase+5, ybase+10);
		text(name, xbase+100, ybase+10);
	    text("Number of Airports:", xbase+5, ybase+30);
	    text(marker.getProperty("airportCount").toString(), xbase+160, ybase+30);
	    text("Number of Inbound Routes:", xbase+5, ybase+50);
	    text(marker.getProperty("Inbound").toString(), xbase+210 ,ybase+50);
	    text("Number of Outbound Routes:", xbase+5, ybase+70);
	    text(marker.getProperty("Outbound").toString(), xbase+210, ybase+70);
	}
  
   /**
    * setRoutesCountPerCountry - This function creates a map of country and corresponding 
    * inbound and outbound route counts.Key is the country and Value is an array which consists 
    * of count of inbound and outbound routes.
    */
   public void setRoutesCountPerCountry(){
        countryRoutes = new HashMap<String,Integer[]>();
        long start_time = new Date().getTime();
        
        Collections.sort(airportList, new Comparator<Marker>() {
	        public int compare(Marker m1, Marker m2) {
	        	if(Integer.parseInt(m1.getProperty("AirportId").toString())< Integer.parseInt(m2.getProperty("AirportId").toString())){
	                return -1;
	            } else {
	                return 1;
	            }
	        }
	    });
        
        Collections.sort(routeList, new Comparator<Marker>() {
	        public int compare(Marker m1, Marker m2) {
	        	HashMap<String,Object> temp1 = m1.getProperties();
	        	HashMap<String,Object> temp2 = m2.getProperties();
	          	if(Integer.parseInt(temp1.get("source").toString())< Integer.parseInt(temp2.get("source").toString())){
	          		//System.out.println("I am inside");
	                return -1;
	            } else if (Integer.parseInt(temp1.get("source").toString())>Integer.parseInt(temp2.get("source").toString())){
	            	//System.out.println("I am inside");
	                return 1;
	            }else{
	            	//System.out.println("I am nowhere");
	                return 0;
	            }
	        }
	    });
        
       
		for(Marker rmarker: routeList){
		   String srcCountry = rmarker.getProperties().get("srcCountry").toString();
		   String destCountry = rmarker.getProperties().get("destCountry").toString();
		   //calculate outgoing flight route counts
		   if(countryRoutes.containsKey(srcCountry)){
			  Integer[] route_num = countryRoutes.get(srcCountry);
			  route_num[0] = route_num[0] + 1;
			  route_num[1] = route_num[1];
			  countryRoutes.put(srcCountry, route_num);
		   }else{
			  Integer[] route_num = new Integer[2];
			  route_num[0] = 1;
			  route_num[1] = 0;
			  countryRoutes.put(srcCountry, route_num);
		   }
		  //calculate incoming flight route counts  
		   if(countryRoutes.containsKey(destCountry)){
			  Integer[] route_num = countryRoutes.get(destCountry);
			  route_num[1] = route_num[1] + 1;
			  route_num[0] = route_num[0];
			  countryRoutes.put(destCountry, route_num);
		   }else{
			  Integer[] route_num = new Integer[2];
			  route_num[1] = 1;
			  route_num[0] = 0;
			  countryRoutes.put(destCountry, route_num);
		   } 
			
		   }		 
        
      //Add inbound and outbound routes data in country marker
  		for(Marker marker : CountryMarkers){
  			String countryName = "\""+ marker.getProperty("name").toString()+ "\"";
              if (countryRoutes.containsKey(countryName)){
  				HashMap<String,Object> temp = marker.getProperties();
  				temp.put("Outbound", countryRoutes.get(countryName)[0]);
  				temp.put("Inbound", countryRoutes.get(countryName)[1]);
  				marker.setProperties(temp);
  				//System.out.println(marker.getProperties());
  			}else{
  				HashMap<String,Object> temp = marker.getProperties();
  				temp.put("Outbound", 0);
  				temp.put("Inbound", 0);		
  				marker.setProperties(temp);
  			}
  	    }
  		
  		 long end_time = new Date().getTime();
         System.out.println("setRoutesCountPerCountry took " + (end_time-start_time)+" millisecs");
   }	   
 
/******************************Useful functions************************************************************************************************
   private boolean isRouteBidirectional(String srcCity,String destCity){
		Integer srcCityId = 0;
		Integer destCityId = 0;
		boolean is2way = false;
				
		for(Marker airportMarker : airportList){
			
			//System.out.println(airportMarker.getProperty("city"));
			
			if((airportMarker.getProperty("city").toString().contains(srcCity))){
			   srcCityId = Integer.parseInt(airportMarker.getId());
			}
			
			if((airportMarker.getProperty("city").toString().contains(destCity))){
				   destCityId = Integer.parseInt(airportMarker.getId());
				}
			
			if(srcCityId !=0 && destCityId !=0){
				break;
			}
		 }
		
		for(Marker routeMarker : routeList){
			if((Integer.parseInt(routeMarker.getProperty("source").toString())==srcCityId) && (Integer.parseInt(routeMarker.getProperty("destination").toString())==destCityId)){
				if((Integer.parseInt(routeMarker.getProperty("source").toString())==destCityId) && (Integer.parseInt(routeMarker.getProperty("destination").toString())==srcCityId)){ 
				    is2way = true;
				    break;
				}
			}
		}
		 
		 return is2way;
	}
	
	private void showRoutesbetween2Cities(int srcCityId,int destCityId){
			
			for(Marker routeMarker : routeList){
			   System.out.println("srcCity: " + routeMarker.getProperty("source"));
			   System.out.println("destCity: " + routeMarker.getProperty("destination"));
			   if((Integer.parseInt(routeMarker.getProperty("source").toString())==srcCityId) && (Integer.parseInt(routeMarker.getProperty("destination").toString())==destCityId)){
				   System.out.println("I am inside********************************************************************");
				   routeMarker.setHidden(false);
				}else{
					routeMarker.setHidden(true);
				}
			}
				
			}
	
	private void showAirportsForCountry(String country){
		
		for(Marker airportMarker : airportList){
			//System.out.println(airportMarker.getProperty("country").toString());
			if((airportMarker.getProperty("country").toString().contains(country))){
				airportMarker.setHidden(false);
				
				
			}else{
				airportMarker.setHidden(true);
			}
		}
				
		}
		
		private void showAirportsForSourceAndDestinationCities(String srcCity,String destCity){
			
			for(Marker airportMarker : airportList){
				//System.out.println(airportMarker.getProperty("city").toString());
				if((airportMarker.getProperty("city").toString().contains(srcCity)) || (airportMarker.getProperty("city").toString().contains(destCity))){
					airportMarker.setHidden(false);
				}else{
					airportMarker.setHidden(true);
				}
			}
				
			}
			
   //Create an airport density map.In Java 8 we have Collectors to group objects based on common key to get the count of objects in a group
    private Map<String, Integer> airportDensityMap(){
    	long start_time = new Date().getTime();
		Map<String,Integer> airport_density = new HashMap<String,Integer>();
		String str1 = "";
		String str2 = "";
		for (Marker countryMarker : CountryMarkers){
			for(Marker airportMarker : airportList){
		    	str1 = airportMarker.getProperty("country" ).toString().toUpperCase().trim() ;
		    	str2 = "\""+countryMarker.getProperty("name").toString().toUpperCase().trim()+ "\"";
		    	if(str2.contains(str1)){
					//System.out.println(str2);
		    	   if(airport_density.containsKey(airportMarker.getProperty("country"))){
		    		  int temp  =  airport_density.get(airportMarker.getProperty("country"));
			    	  temp= temp + 1;
			    	  airport_density.put(airportMarker.getProperty("country").toString(),temp);
			    	}else{
			    	  airport_density.put(airportMarker.getProperty("country").toString(),1);
		    	    }
		    	 }
			 }
		}
		
		long end_time = new Date().getTime();
	    System.out.println("airportDensityMap function took " + (end_time-start_time)+" millisecs");
		return airport_density;
	}
	
        
         //showAirportsForCountry("Cambodia");
		//showAirportsForSourceAndDestinationCities("San Diego","Chicago");	
		//showRoutesbetween2Cities(345,1056);
		//boolean is2way = isRouteBidirectional("London","Newyork");
		//System.out.println("Flight route is 2 way ? " + is2way);
		//showRoutesbetween2Countries(Jordan, USA);
******************************************************************************************************/
			

}
