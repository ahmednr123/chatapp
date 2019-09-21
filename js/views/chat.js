const MessageType = {
	NEW: 0, OLD: 1, CURRENT: 2
}

const ChatView = {
	data: {
		// chatMessage Format: {(receiver: true,)msg_id:"", time: "", msg:""}
		chatMessages: [],
		receiver: "",
		chat_id: -1,
		old_msg_id: -1,
		new_msg_id: -1,
		more: false
	},

	html: {
		base: (username) => `
			<div id="top-bar" class="horizontal full">
				<input type="button" value="Go Back" id="go_back">
				<div id="refresh_btn">Refresh</div>
			</div>
			<div id="app-body">
				<div id="receiver-user">${username}</div>
				<chat-messages>
				</chat-messages>
				<div class="horizontal full">
					<input type="text" id="message" autocomplete="off" placeholder="Type message" style="width: 100%;margin-right: 10px">
					<input type="button" value="Send Message" id="send_msg">
				</div>
			</div>
		`,
		message: (msg_data) => `
			<message-bubble ${(!msg_data.receiver)?'class="flip"':''}>
				<!--<div class="vertical msg-info">
					<span class="username">${(msg_data.receiver)?ChatView.data.receiver:"You"}</span>
					<span class="time">${msg_data.time}</span>
				</div>-->
				<div class="message" id="msg_${msg_data.msg_id}">
					${msg_data.msg}
				</div>
			</message-bubble>
		`,
		show_more: `<show-more>Show more</show-more>`
	},

	show: function (chat_id, receiver_username) { 
		this.data = {
			// chatMessage Format: {(receiver: true,)msg_id:"", time: "", msg:""}
			chatMessages: [],
			receiver: receiver_username,
			chat_id: chat_id,
			old_msg_id: -1,
			new_msg_id: -1,
			more: false
		}

		$('chat-app').style.width = "600px";
		$('chat-app').innerHTML = this.html.base(receiver_username);

		$('#go_back').addEventListener('click', () => DashboardView.show())
		$('#send_msg').addEventListener('click', this.sendMessage);
		$('#refresh_btn').addEventListener('click', this.getNewMessages);
		
		this.getMessages();
		this.render();
	},

	getMessages: function () {
		$xhrRequest(
			`/ChatApp/messages?type=${MessageType.CURRENT}&chat_id=${ChatView.data.chat_id}`,
			(res) => {
				console.log("GET[/messages] : " + res);
				res = res.trim();
				res = JSON.parse(res);
				console.log("Cleaning JSON");

				ChatView.data.chatMessages = []
				if (res[0])
					ChatView.data.new_msg_id = res[0].msg_id;

				if (res.length >= 20)
					ChatView.data.more = true;
				else
					ChatView.data.more = false;

				for (let json of res) {
					let chatMessage = {msg_id:json.msg_id, msg:json.message, time:json.time}

					if (ChatView.data.receiver == json.sender)
						chatMessage["receiver"] = true;

					ChatView.data.chatMessages.unshift(chatMessage);
					ChatView.data.old_msg_id = json.msg_id;
				}
				ChatView.render();
				$('chat-messages').scrollTop = $('chat-messages').scrollHeight;
			}
		)
	},

	getOldMessages: function () {
		$xhrRequest(
			`/ChatApp/messages
				?type=${MessageType.OLD}
				&chat_id=${ChatView.data.chat_id}
				&msg_id=${ChatView.data.old_msg_id}`,
			(res) => {
				console.log("GET[/messages] : " + res);
				res = res.trim();
				res = JSON.parse(res);
				console.log("Cleaning JSON");

				if (res.length >= 20)
					ChatView.data.more = true;
				else
					ChatView.data.more = false;

				for (let json of res) {
					let chatMessage = {msg_id:json.msg_id, msg:json.message, time:json.time}

					if (ChatView.data.receiver == json.sender)
						chatMessage["receiver"] = true;

					ChatView.data.chatMessages.unshift(chatMessage);
					ChatView.data.old_msg_id = json.msg_id;
				}
				ChatView.render(false);
			}
		)
	},

	getNewMessages: function () {
		$xhrRequest(
			`/ChatApp/messages
				?type=${MessageType.NEW}
				&chat_id=${ChatView.data.chat_id}
				&msg_id=${ChatView.data.new_msg_id}`,
			(res) => {
				console.log("GET[/messages] : " + res);
				res = res.trim();
				res = JSON.parse(res);
				console.log("Cleaning JSON");

				for (let json of res) {
					let chatMessage = {msg_id:json.msg_id, msg:json.message, time:json.time}

					if (ChatView.data.receiver == json.sender)
						chatMessage["receiver"] = true;

					ChatView.data.chatMessages.push(chatMessage);
					ChatView.data.new_msg_id = json.msg_id;
				}
				ChatView.render();
			}
		)
	},

	sendMessage: function () {
		$xhrPost('/ChatApp/message', 
			{
				chat_id: ChatView.data.chat_id,
				message: $("#message").value
			}, 
			(res) => {
				console.log("POST[/message] :" + res);
				$('#message').value = "";
				res = res.trim();
				res = JSON.parse(res);
				if (res.reply == true) {
					let json = {msg: res.message, time: res.time};
					let msgHTML = ChatView.html.message(json);
					$('chat-messages').innerHTML += msgHTML;
				}
			}
		)
	},

	render: function (avoidScroll) {
		$('chat-messages').innerHTML = "";

		if(this.data.more) {
			$('chat-messages').innerHTML = this.html.show_more;
			$('show-more').addEventListener('click', this.getOldMessages);
		}
		
		for (let message of this.data.chatMessages)
			$('chat-messages').innerHTML += this.html.message(message);
		
		if (avoidScroll !== false)
			$('chat-messages').scrollTop = $('chat-messages').scrollHeight;
	}
}