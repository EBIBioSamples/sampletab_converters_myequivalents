package uk.ac.ebi.fgpt.sampletab.tools.myeq;

import java.io.File;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

import uk.ac.ebi.fgpt.sampletab.AbstractInfileDriver;

public class SampleTabLoaderDriver extends AbstractInfileDriver<SampleTabLoaderTask> {

    
    @Option(required=true, name = "-u", aliases={"--username"}, metaVar="USERNAME", usage = "username for myEquivalents")
    protected String username;

    @Option(required=true, name = "-s", aliases={"--secret"}, metaVar="SECRET", usage = "secret for myEquivalents")
    protected String secret;
    
    private EntityMappingManager emMgr = null;
    
    // logging
    private Logger log = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        new SampleTabLoaderDriver().doMain(args);
    }
        
    @Override
    protected SampleTabLoaderTask getNewTask(File inputFile) {
        if (emMgr == null) {
            //myEquivalents uses the secret for "minor" authorisation e.g. adding new mappings
            // the password is used for "major" authorisation e.g. adding new users
            emMgr = Resources.getInstance().getMyEqManagerFactory().newEntityMappingManager(username, secret);    
        }
        return new SampleTabLoaderTask(inputFile, emMgr);
    }

}
