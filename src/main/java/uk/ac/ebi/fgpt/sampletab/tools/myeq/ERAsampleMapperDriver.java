package uk.ac.ebi.fgpt.sampletab.tools.myeq;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import org.kohsuke.args4j.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fgpt.sampletab.AbstractDriver;

/**
 * This class will collect all the
 * mappings from the era pro database and update
 * the myequivalents database
 * 
 * @author drashtti
 * 
 */

public class ERAsampleMapperDriver extends AbstractDriver {

    @Argument(required = true, index = 0, metaVar = "STARTDATE", usage = "Start date as YYYY/MM/DD")
    protected String minDateString;

    @Argument(required = false, index = 1, metaVar = "ENDDATE", usage = "End date as YYYY/MM/DD")
    protected String maxDateString;

    private Logger log = LoggerFactory.getLogger(getClass());

	
	private ManagerFactory managerFactory = null;
    private EntityMappingManager emMgr;

    public static void main(String[] args) {
        new ERAsampleMapperDriver().doMain(args);
    }

	public synchronized ManagerFactory getManagerFactory() {
		if (managerFactory == null) {
			//managerFactory = Resources.getInstance().getMyEqManagerFactory();

			Properties properties = new Properties();
			InputStream is = null;
			try {
				is = this.getClass().getResourceAsStream("/myeq.properties");
				if (is == null) {
					throw new RuntimeException("Unable to find myeq.properties");
				}
				properties.load(is);
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						//do nothing
					}
				}
			}
			return new DbManagerFactory(properties);
		}
		return managerFactory;
	}

    @Override
    public void doMain(String[] args) {
        super.doMain(args);

        //handle arguments into dates
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

        log.info("Getting sample ids from ERA-PRO");
        
        EraProManager manager = EraProManager.getInstance();
        
        Map<String, String> sampleIds = manager.getSRABioSampleMappingForDateRange(minDate, maxDate);

        log.info("Finishing getting sample ids from ERA-PRO");
        
        log.info("connecting to the myequivalents database to store the mappings");
        if (emMgr == null) {
            emMgr = getManagerFactory().newEntityMappingManager();
        }
        // Store a pair of entities that are linked together (i.e., are equivalent)
        for (Map.Entry<String, String> entry : sampleIds.entrySet()) {
            String erasampleid = entry.getKey();
            String biosampleid = entry.getValue();
            log.info("Mapping "+biosampleid+" to "+erasampleid);
            //ENA supports use of BioSample accessions, so do so
            emMgr.storeMappings(MyEqProperties.getENASamplesService() + ":" + biosampleid,
                    MyEqProperties.getBioSamplesSamplesService() + ":" + biosampleid);
        }
    }

}
