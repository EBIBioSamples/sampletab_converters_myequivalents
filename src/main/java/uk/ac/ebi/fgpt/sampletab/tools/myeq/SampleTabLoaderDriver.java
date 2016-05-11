package uk.ac.ebi.fgpt.sampletab.tools.myeq;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.impl.db.DbManagerFactory;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.managers.interfaces.ManagerFactory;
import uk.ac.ebi.fgpt.sampletab.AbstractInfileDriver;

public class SampleTabLoaderDriver extends AbstractInfileDriver<SampleTabLoaderTask> {

    
    @Option(required=true, name = "-u", aliases={"--username"}, metaVar="USERNAME", usage = "username for myEquivalents")
    protected String username;

    @Option(required=true, name = "-e", aliases={"--secret"}, metaVar="SECRET", usage = "secret for myEquivalents")
    protected String secret;

    private Logger log = LoggerFactory.getLogger(getClass());

	private ManagerFactory managerFactory = null;
	
    private EntityMappingManager emMgr = null;
    
    public static void main(String[] args) {
        new SampleTabLoaderDriver().doMain(args);
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
    protected SampleTabLoaderTask getNewTask(File inputFile) {
        log.info("connecting to the myequivalents database to store the mappings");
        if (emMgr == null) {
            //myEquivalents uses the secret for "minor" authorisation e.g. adding new mappings
            // the password is used for "major" authorisation e.g. adding new users
            emMgr = getManagerFactory().newEntityMappingManager(username, secret);
        }
        return new SampleTabLoaderTask(inputFile, emMgr);
    }

}
