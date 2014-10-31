package uk.ac.ebi.fgpt.sampletab.tools.myeq;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;
import uk.ac.ebi.fgpt.sampletab.AbstractDriver;
import uk.ac.ebi.fgpt.sampletab.sra.EraProManager;

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

    private EntityMappingManager emMgr;

    public static void main(String[] args) {
        new ERAsampleMapperDriver().doMain(args);
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
            emMgr = Resources.getInstance().getMyEqManagerFactory().newEntityMappingManager();
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
