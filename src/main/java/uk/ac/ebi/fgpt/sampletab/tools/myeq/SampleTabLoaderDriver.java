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

    @Option(required=true, name = "-p", aliases={"--password"}, metaVar="PASSWORD", usage = "password for myEquivalents")
    protected String password;
    
    private EntityMappingManager emMgr = null;
    
    // logging
    private Logger log = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        new SampleTabLoaderDriver().doMain(args);
    }
        
    @Override
    protected SampleTabLoaderTask getNewTask(File inputFile) {
        if (emMgr == null) {
            emMgr = Resources.getInstance().getMyEqManagerFactory().newEntityMappingManager(username, password);    
        }
        return new SampleTabLoaderTask(inputFile, emMgr);
    }

}
