package uk.ac.ebi.fgpt.sampletab.tools.myeq;

import java.io.File;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fg.myequivalents.managers.interfaces.EntityMappingManager;
import uk.ac.ebi.fg.myequivalents.resources.Resources;

import uk.ac.ebi.fgpt.sampletab.AbstractInfileDriver;

public class SampleTabLoaderDriver extends AbstractInfileDriver<SampleTabLoaderTask> {

    @Argument(required=true, index=1, metaVar="USERNAME", usage = "username for myEquivalents")
    protected String username;

    @Argument(required=true, index=2, metaVar="PASSWORD", usage = "password for myEquivalents")
    protected String password;
    
    
    private EntityMappingManager emMgr = null;
    
    // logging
    private Logger log = LoggerFactory.getLogger(getClass());


    public static void main(String[] args) {
        new SampleTabLoaderDriver().doMain(args);
    }
    
    @Override
    public void doMain(String[] args) {
        super.doMain(args);
        
        emMgr = Resources.getInstance().getMyEqManagerFactory().newEntityMappingManager(username, password);
    }
    
    @Override
    protected SampleTabLoaderTask getNewTask(File inputFile) {
        return new SampleTabLoaderTask(inputFile, emMgr);
    }

}
