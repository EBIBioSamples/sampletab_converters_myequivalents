package uk.ac.ebi.fgpt.sampletab.tools.myeq;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.ebi.fgpt.sampletab.AbstractDriver;

public class ERAsampleMapperCron extends AbstractDriver {

	    @Option(name = "--threads", aliases = { "-t" }, usage = "number of additional threads")
	    private int threads = 0;

	    @Option(name = "--no-conan", usage = "do not trigger conan loads")
	    private boolean noconan = false;

	    protected ExecutorService pool;
	    private List<Future<Void>> futures = new LinkedList<Future<Void>>();
	    
	    private Logger log = LoggerFactory.getLogger(getClass());

	    public ERAsampleMapperCron() {
	    	
	    }
	    
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	}

	
	
	@Override
    public void doMain(String[] args){
        super.doMain(args);

        pool = null;
        if (threads > 0) {
            pool = Executors.newFixedThreadPool(threads);
        }
	
        log.info("getting the mappings from the era-pro database");
        
	
	if (pool != null) {
        //wait for threading to finish
        for (Future<Void> f : futures) {
            try {
                f.get();
            } catch (Exception e) {
                //something went wrong
                log.error("problem processing update", e);
            }
        }
        
        // close the pool to tidy it all up
        // must synchronize on the pool object
        synchronized (pool) {
            log.info("shutting down pool");
            pool.shutdown();
            try {
                // allow 24h to execute. Rather too much, but meh
                pool.awaitTermination(1, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                log.error("Interuppted awaiting thread pool termination", e);
            }
        }
    }
	}
	
}