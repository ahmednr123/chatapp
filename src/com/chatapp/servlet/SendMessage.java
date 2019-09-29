package com.chatapp.servlet;

import com.chatapp.util.ElasticManager;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 *  - Route: /message
 *	- POST
 *		[chat_id] Chat ID corresponding to the users chat
 *		[username] (FROM SESSION)
 *		(json reply) message with timestamp
 *		(onFail reply) false
 */
public class SendMessage extends HttpServlet {
	private static Logger LOGGER = Logger.getLogger(SendMessage.class.getName());

	public SendMessage () {
		super();
	}

	protected
	void doGet (HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
			response.getWriter().println("false");
	}

	protected
	void doPost (HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException 
	{
		PrintWriter out = response.getWriter();
		String sender = null;
		String chat_id_string = request.getParameter("chat_id");
		String message = request.getParameter("message");

		HttpSession session = request.getSession(false);

		try {
			sender = (String) session.getAttribute("username");
		} catch (NullPointerException e) {
			LOGGER.info("User session not found");
			out.println("{\"reply\":false}");
			out.close();
			return;
		}

		// Check if appropriate parameters are passed with the request
		if (chat_id_string == null || message == null) {
			LOGGER.info("Not enough parameter passed");
			out.println("{\"reply\":false}");
			return;
		}

		if (message == "") {
			LOGGER.info("Empty message parameter received");
			out.println("{\"reply\":false}");
			return;
		}

		int chat_id = Integer.parseInt(chat_id_string);

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		Date datetime = new Date();
		boolean isMessageSent = sendMessage(chat_id, sender, message, datetime);

		if (!isMessageSent) {
			out.println("{\"reply\":false}");
			out.close();
			return;
		}

		out.println("{\"reply\":true,\"message\": \"" + message 
						+ "\", \"timestamp\": \"" + formatter.format(datetime) + "\"}");
		out.close();
	}

	/**
	 * Add message to index of chat
	 *
	 * @param chat_id
	 * @param sender
	 * @param message
	 * @param dt Date and time when the message was received
	 * @return
	 */
	private
	boolean sendMessage (int chat_id, String sender, String message, Date dt) {
		boolean isIndexInserted = false;
		JSONObject reqObject = new JSONObject();
		JSONObject resObject = null;

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

		reqObject.put("sender", sender);
		reqObject.put("message", message);
		reqObject.put("timestamp", formatter.format(dt));

		LOGGER.info("POST DATA: " + reqObject.toString());

		try{
			resObject = ElasticManager.post("/" + chat_id + "_msgs/_doc/?refresh", reqObject.toString());
			isIndexInserted = true;
			if (resObject.get("error") != null) {
				throw new RuntimeException();
			}
		} catch (IOException e) {
			LOGGER.severe(e.getMessage());
		} catch (RuntimeException e) {
			LOGGER.severe(resObject.toString());
		}
		return isIndexInserted;
	}
}