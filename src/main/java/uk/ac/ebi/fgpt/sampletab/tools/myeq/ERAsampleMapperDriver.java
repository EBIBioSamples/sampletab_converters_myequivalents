package uk.ac.ebi.fgpt.sampletab.tools.myeq;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.kohsuke.args4j.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;


/**
 * This class will collect all the 
 * mappings from the era pro database and update 
 * the myequivalents database
 * @author drashtti
 *
 */

public class ERAsampleMapperDriver extends ERAsampleMapperCron{
	
	 @Argument(required = true, index = 1, metaVar = "STARTDATE", usage = "Start date as YYYY/MM/DD")
	    protected String minDateString;

	    @Argument(required = false, index = 2, metaVar = "ENDDATE", usage = "End date as YYYY/MM/DD")
	    protected String maxDateString;
	   
	    private Logger log = LoggerFactory.getLogger(getClass());
	    
	    private EntityMappingManager emMgr;

	    public static void main(String[] args) {
	        new ERAsampleMapperDriver().doMain(args);
	    }
	    
	    public void getSamples() {
	        log.info("Getting sample ids from ERA-PRO");
	        
	        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
	        Date minDate = null;
	        Date maxDate = null;
	        try {
	            minDate = formatter.parse(minDateString);
	            if (maxDateString != null) { 
	                maxDate = formatter.parse(maxDateString);
	            }
	        } catch (ParseException e) {
	            throw new RuntimeException(e);
	        }
	        
	        Map<String, String> sampleIds = ERAsampleMapper.getInstance().getPublicSampleIds(minDate, maxDate);        
	                
	        log.info("Finishing getting sample ids from ERA-PRO"); 
	        
	        //call the method to store mappings in the myequivalents database
	        storeMappings(sampleIds);
	    }

	    
	    	public void storeMappings(Map<String,String> sampleIds){
	    		log.info("connecting to the myequivalents database to store the mappings");
	    		if(emMgr == null){
	    		emMgr = Resources.getInstance ().getMyEqManagerFactory ().newEntityMappingManager();}
	    		// Store a pair of entities that are linked together (i.e., are equivalent)
	    		for (Map.Entry<String,String> entry : sampleIds.entrySet()){
	    			String erasampleid = entry.getKey();
	    			String biosampleid = entry.getValue();
	    		emMgr.storeMappings ( 
	    		  MyEqProperties.getENASamplesService() + ":" +erasampleid, MyEqProperties.getBioSamplesSamplesService() + ":"+biosampleid
	    				);
	    		}
	    	}
}
