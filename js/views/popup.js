const PopUp = {
    init: function () {
    	$("#close-popup").addEventListener('click', () => {
    		PopUp.hide()
    	})
    },

    show_group_users: function (chat_id) {
        GetUsers_Popup.show(chat_id)
    },

    show_search_messages: function (chat_id) {
        SearchMessages_Popup.show(chat_id)
    },

    hide: function () {
    	$('pop-up-container').style.display = "none"
		$('pop-up').innerHTML = ""
    }
}

PopUp.init()