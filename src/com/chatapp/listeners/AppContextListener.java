package com.chatapp.listeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import com.chatapp.util.DatabaseManager;

@WebListener
public class AppContextListener implements ServletContextListener {
	
	public 
	void contextInitialized(ServletContextEvent servletContextEvent) 
	{
		DatabaseManager.initialize();
	}
	
	public 
	void contextDestroyed(ServletContextEvent servletContextEvent) 
	{
	}
}