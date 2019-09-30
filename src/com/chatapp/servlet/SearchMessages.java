package com.chatapp.servlet;

import com.chatapp.format.ChatMessage;
import com.chatapp.util.DatabaseQuery;
import com.chatapp.util.ElasticManager;
import com.chatapp.util.XJSONObject;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.logging.Logger;

public class SearchMessages extends HttpServlet {
    private static Logger LOGGER = Logger.getLogger(SendMessage.class.getName());

    private static final int EXACT_TERM_SEARCH = 0;
    private static final int ANY_TERM_SEARCH = 1;

    public
    SearchMessages () {

    }

    protected
    void doGet (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        PrintWriter out = response.getWriter();
        String session_user = null;
        String search_term = request.getParameter("search_term");

        System.out.println("Search term: " + search_term);
        System.out.println("Type: " + request.getParameter("type"));
        System.out.println("Chat ID: " + request.getParameter("chat_id"));

        int chat_id, type, from_index = 0;

        HttpSession session = request.getSession(false);

        try {
            chat_id = Integer.parseInt(request.getParameter("chat_id"));
            type = Integer.parseInt(request.getParameter("type"));
            session_user = (String) session.getAttribute("username");
        } catch (NullPointerException e) {
            LOGGER.info(e.getMessage());
            out.println("{\"reply\":false}");
            out.close();
            return;
        }

        if ( !DatabaseQuery.isUserAuthorized(session_user, chat_id) ) {
            LOGGER.info("User: " + session_user + " accessed an unauthorized group");
            out.print("false");
            out.close();
            return;
        }

        if (request.getParameter("from") != null) {
            from_index = Integer.parseInt(request.getParameter("from"));
        }

        ArrayList<ChatMessage> chatMessages = getSearchResults(chat_id, type, search_term, from_index);

        out.print(ChatMessage.getJsonStringArray(chatMessages));
        out.close();
    }

    protected
    void doPost (HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.getWriter().println("false");
    }

    /**
     * Get messages that match the search result
     *
     * @param chat_id
     * @param type To search messages with all the terms or to messages with any terms
     * @param search_term Term that has to be searched
     * @param from
     * @return
     */
    private
    ArrayList<ChatMessage> getSearchResults (int chat_id, int type, String search_term, int from) {
        ArrayList<ChatMessage> chatMessages = new ArrayList<>();
        XJSONObject resObj;

        XJSONObject reqObj = new XJSONObject();
        reqObj.put("query", new JSONObject());
        reqObj.put("query.match", new JSONObject());
        reqObj.put("query.match.message", new JSONObject());
        reqObj.put("query.match.message.query", search_term);

        reqObj.put("size", 10);
        reqObj.put("from", from);

        if (type == EXACT_TERM_SEARCH) {
            reqObj.put("query.match.message.operator", "and");
        }

        try {
            resObj = new XJSONObject(ElasticManager.get("/" + chat_id + "_msgs/_search", reqObj.toString()));
            System.out.println(resObj.toString());
            JSONArray resArray = (JSONArray) resObj.get("hits.hits");
            for (Object obj : resArray) {
                XJSONObject data = new XJSONObject(((JSONObject)obj).getJSONObject("_source"));
                chatMessages.add(
                        new ChatMessage(
                                ((JSONObject)obj).getString("_id"),
                                (String) data.get("sender"),
                                (String) data.get("message"),
                                data.get("timestamp").toString()
                        )
                );
            }
        } catch (IOException | RuntimeException e) {
            LOGGER.severe(e.getMessage());
        }

        return chatMessages;
    }
}