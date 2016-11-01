package module7;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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

/** An applet that shows airports (and routes)
 * on a world map.  
 * @author Adam Setters and the UC San Diego Intermediate Software Development
 * MOOC team
 *
 */
public class AirportMap extends PApplet {
	
	UnfoldingMap map;
    List<Marker> airportList;
	List<Marker> routeList;
	List<Marker> countryMarkers;
	Map<String, Integer> airportsDensityMap;
	
	public void setup() {
		// setting up PAppler
		size(800,600);
		
		// setting up map and default events
		map = new UnfoldingMap(this, 50, 50, 750, 550);
		map.zoomToLevel(2);
		map.setBackgroundColor(240);
		MapUtils.createDefaultEventDispatcher(this, map);
		
		// Load country polygons and adds them as markers
		List<Feature> countries = GeoJSONReader.loadData(this, "countries.geo.json");
		countryMarkers = MapUtils.createSimpleMarkers(countries);
        map.addMarkers(countryMarkers);
       		
		// get features from airport data
		List<PointFeature> features = ParseFeed.parseAirports(this, "airports.dat");
		
		// list for markers, hashmap for quicker access when matching with routes
		airportList = new ArrayList<Marker>();
		HashMap<Integer, Location> airports = new HashMap<Integer, Location>();
		
		// create markers from features
		for(PointFeature feature : features) {
			AirportMarker m = new AirportMarker(feature);
			//System.out.println(feature.getLocation());
			//System.out.println(m.getProperties());
			m.setRadius(5);
			airportList.add(m);
			
			// put airport in hashmap with OpenFlights unique id for key
			airports.put(Integer.parseInt(feature.getId()), feature.getLocation());
		
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
				//System.out.println(route.getLocations());
			}
			
			SimpleLinesMarker sl = new SimpleLinesMarker(route.getLocations(), route.getProperties());
		    //System.out.println(sl.getProperties());
		    //System.out.println(sl.getLocations());
			
			//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
			routeList.add(sl);
		}
		
		
		
		//UNCOMMENT IF YOU WANT TO SEE ALL ROUTES
		//map.addMarkers(routeList);
		
         airportsDensityMap =  airportDensityMap();
         //System.out.println(airportsDensityMap.values());
         //shadeCountries();
         
         
		//showAirportsForCountry("Cambodia");
		//showAirportsForSourceAndDestinationCities("San Diego","Chicago");	
		//showRoutesbetween2Cities(345,1056);
		//boolean is2way = isRouteBidirectional("London","Newyork");
		//System.out.println("Flight route is 2 way ? " + is2way);
		//showRoutesbetween2Countries(Jordan, USA);
		
		
	}
	
	public void draw() {
		background(240);
		map.draw();
		
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
	
  private Map<String, Integer> airportDensityMap(){
		Map<String,Integer> airport_density = new HashMap<String,Integer>();
		String str1 = "";
		String str2 = "";
		for (Marker countryMarker : countryMarkers){
			for(Marker airportMarker : airportList){
		    	str1 = airportMarker.getProperty("country").toString().toUpperCase().trim() ;
		    	//System.out.println(str1);
		    	//str2 = "\""+countryMarker.getProperty("name").toString().toUpperCase().trim()+ "\"";
		    	str2 = countryMarker.getProperty("name").toString().toUpperCase().trim();
		    	//System.out.println(str2 );
		    	if(str2.contains(str1)|| str1.contains(str2)){
		    		System.out.println(str2);
					System.out.println(str1);
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
		
		for(Map.Entry<String, Integer> entry :  airport_density.entrySet()){
			//System.out.println(entry.getKey() + "=" +entry.getValue());
		}
		
		
		return airport_density;
	}
  
  private void shadeCountries() {
		for (Marker marker : countryMarkers) {
			// Find data for country of the current marker
			String countryName = "\""+ marker.getProperty("name").toString()+ "\"";
			System.out.println(airportsDensityMap.containsKey(countryName));
			if (airportsDensityMap.containsKey(countryName)) {
				int color_density = airportsDensityMap.get(countryName);
				System.out.println(countryName + ":" + color_density);
				// Encode value as brightness (values range: 1-350)
				int colorLevel = (int) map(color_density, 1,300, 10, 255);
				marker.setColor(color(255,0,0, colorLevel));
			}
			else {
				marker.setColor(color(100, 120));
			}
		}
	}
	
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
	
	

}
