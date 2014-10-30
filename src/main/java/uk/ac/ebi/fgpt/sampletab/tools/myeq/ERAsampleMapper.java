/**
 * 
 */
package uk.ac.ebi.fgpt.sampletab.tools.myeq;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jolbox.bonecp.BoneCPDataSource;

/**
 * This class interacts with the ENA-pro database and gets all the Biosamples
 * IDs - Sample IDs pair to be pushed into the myequivalents database
 * 
 * @author drashtti
 * 
 */
public class ERAsampleMapper {

	private Logger log = LoggerFactory.getLogger(getClass());
	private BoneCPDataSource ds = null;
	private String username = null;
	private String password = null;

	private static ERAsampleMapper instance = null;

	/**
	 * Private constructor, use getInstance() method to ensure singleton pattern
	 * is followed
	 */
	private ERAsampleMapper() {

	}

	public synchronized static ERAsampleMapper getInstance() {
		if (instance == null) {
			instance = new ERAsampleMapper();
		}
		return instance;
	}

	protected synchronized BoneCPDataSource getDataSource()
			throws ClassNotFoundException {
		if (ds == null) {
			synchronized (getClass()) {
				// load defaults
				Properties properties = new Properties();
				try {
					InputStream is = getClass().getResourceAsStream(
							"/era-pro.properties");
					properties.load(is);
				} catch (IOException e) {
					log.error("Unable to read resource era-pro.properties", e);
				}
				String hostname = properties.getProperty("hostname");
				Integer port = new Integer(properties.getProperty("port"));
				String database = properties.getProperty("database");
				username = properties.getProperty("username");
				password = properties.getProperty("password");

				try {
					Class.forName("oracle.jdbc.driver.OracleDriver");
				} catch (ClassNotFoundException e) {
					log.error("Unable to find oracle.jdbc.driver.OracleDriver",
							e);
					throw e;
				}
				String jdbc = "jdbc:oracle:thin:@" + hostname + ":" + port
						+ ":" + database;
				log.trace("JDBC URL = " + jdbc);
				log.trace("USER = " + username);
				log.trace("PW = " + password);

				ds = new BoneCPDataSource();
				ds.setJdbcUrl(jdbc);
				ds.setUsername(username);
				ds.setPassword(password);

				ds.setPartitionCount(1);
				ds.setMaxConnectionsPerPartition(10);
				ds.setAcquireIncrement(2);
			}
		}
		return ds;
	}

	public Map<String, String> getPublicSampleIds(Date minDate, Date maxDate) {
		PreparedStatement statement = null;
		Connection connection = null;
		ResultSet resultset = null;
		log.info("Getting all public sample - biosample ids");
		String query = "SELECT SAMPLE_ID,BIOSAMPLE_ID FROM SAMPLE WHERE STATUS_ID = 4 AND EGA_ID IS NULL " +
				"AND ((LAST_UPDATED BETWEEN ? AND ?) OR (FIRST_PUBLIC BETWEEN ? AND ?))";

		Map<String, String> sampleIds = new HashMap<String, String>();

		try {
			BoneCPDataSource datasource = getDataSource();
			connection = datasource.getConnection();
			statement = connection.prepareStatement(query);
			statement.setDate(1, new java.sql.Date(minDate.getTime()));
			statement.setDate(2, new java.sql.Date(maxDate.getTime()));
			statement.setDate(3, new java.sql.Date(minDate.getTime()));
			statement.setDate(4, new java.sql.Date(maxDate.getTime()));
			resultset = statement.executeQuery();
			if (resultset == null) {
				log.info("No Updates for sample mapping during the time period provided");
			} else {
				while (resultset.next()) {
					String sampleId = resultset.getString(1); // result sets are one-indexed,
					String biosampleId = resultset.getString(2);
					sampleIds.put(sampleId, biosampleId);
				}
			}
		} catch (SQLException e) {
			log.error("Problem acessing database", e);
		} catch (ClassNotFoundException e) {
			log.error(
					"The BoneCPDatasouce class for connection to the database cannot be found",
					e);
		} finally {
			// close each of these separately in case of errors
			if (resultset != null) {
				try {
					resultset.close();
				} catch (SQLException e) {
					// do nothing
				}
			}
			if (statement != null) {
				try {
					statement.close();
				} catch (SQLException e) {
					// do nothing
				}
			}
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException e) {
					// do nothing
				}
			}
		}

		log.info("Got " + sampleIds.size() + " public sample ids");

		return sampleIds;

	}

}
