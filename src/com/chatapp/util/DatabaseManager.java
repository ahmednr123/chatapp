package com.chatapp.util;

import java.util.logging.Logger;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * @author Ahmed Noor
 *
 * Establish a connection pool to MySQL database
 * and return connections from the connection pool
 */
public class DatabaseManager {
	private static DataSource dataSource;
	private static Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
	
	private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";  
	private static final String DB_URL = "jdbc:mysql://localhost/chat_app";
    
    //  Database credentials
	private static final String DB_USER = "root";
	private static final String DB_PASS = "root";
    
	/**
	 * Blocking Object Creation
	 */
	private DatabaseManager () {
    }
    
    /**
     * Initialize DataSource to get connections
     * from Connection Pool
     */
    public static 
    void initialize () 
    {
    	PoolProperties poolProps = new PoolProperties();
    	poolProps.setUrl(DB_URL);
        poolProps.setDriverClassName(JDBC_DRIVER);
        poolProps.setUsername(DB_USER);
        poolProps.setPassword(DB_PASS);

        dataSource = new DataSource();
        dataSource.setPoolProperties(poolProps);

        LOGGER.info("DatabaseManager Initialized");
	}
	
	/**
	 * @return DB Connection
	 * @exception SQLException
	 *					If a database access error occurs
	 */
	public static
	Connection getConnection()
		throws SQLException 
	{
		LOGGER.info("DBConnection Request");
		return dataSource.getConnection();
	}
}