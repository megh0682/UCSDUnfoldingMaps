package unfolding_choropleth;

import java.util.Comparator;

import de.fhpotsdam.unfolding.marker.Marker;
import de.fhpotsdam.unfolding.marker.SimpleLinesMarker;

public class IDComparator implements Comparator<Marker>{
	
    @Override
	public int compare(Marker SLM1, Marker SLM2) {
    	
    	if(Integer.parseInt(SLM1.getProperty("source").toString())< Integer.parseInt(SLM2.getProperty("source").toString())){
            return 1;
        } else {
            return -1;
        }

	} 

}
