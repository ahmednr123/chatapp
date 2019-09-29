package com.chatapp.listeners;

import com.chatapp.util.DatabaseManager;
import com.chatapp.util.ElasticManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {
	
	public 
	void contextInitialized(ServletContextEvent servletContextEvent) 
	{
		DatabaseManager.initialize();
		ElasticManager.setBaseUrl("localhost", 9200, "http");
	}
	
	public 
	void contextDestroyed(ServletContextEvent servletContextEvent) 
	{
	}
}