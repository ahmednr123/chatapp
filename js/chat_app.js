const MessageType = {
	NEW: 0, OLD: 1, CURRENT: 2
}

const DashboardView = {
	data: {
		chats: []
	},

	html: {
		base: () => `
			<div id="top-bar" class="horizontal full">
				<input type="text" id="username" placeholder="Username" style="width: 100%;margin-right: 10px">
				<input type="button" value="Add User" id="create_chat">
			</div>
			<div id="app-body">
			</div>
		`,
		info_text: (str, getElement) => {
			if (!getElement) 
				return `<info-text>${str}</info-text>`;
			
			let el = document.createElement("info-text");
			el.innerHTML = str;
			return el;
		},
		add_chat: (id, username) => `
			<chat-user chat_id="${id}">${username}</chat-user>
		`
	},

	show: function () {
		$('chat-app').style.width = "400px"
		$('chat-app').innerHTML = this.html.base();
		$('#create_chat').addEventListener('click', this.createChat);
		this.updateChats();
		this.render();
	},

	createChat: function () {
		$xhrPost('/ChatApp/create_chat', {receiver:$('#username').value}, (res, _xhr) => {
			res = res.trim();
			if (res == "true") {
				DashboardView.updateChats(_xhr);
			}
		})
	},

	updateChats: function (_xhr) {
		$xhrRequest('/ChatApp/active_chats', (res) => {
			console.log("chat: " + res);
			res = JSON.parse(res);
			DashboardView.data.chats = res;
			DashboardView.render();
		}, _xhr)
	},

	render: function () {
		$('#app-body').innerHTML = "";

		if (this.data.chats.length == 0) 
		{
			$('#app-body').innerHTML = this.html.info_text("Add a user to get started chatting");
		} 
		else 
		{
			for (let json of this.data.chats)
				$('#app-body').innerHTML += this.html.add_chat(json.chat_id, json.username);

			$forEach('chat-user', (el) => {
				el.addEventListener('click', function () {
					console.log(`chat_id: ${el.getAttribute("chat_id")}, username: ${el.innerHTML}`);
					ChatView.show(el.getAttribute("chat_id"), el.innerHTML);
				})
			})

			$('#app-body').appendChild(this.html.info_text("Click on a user to chat", true));
		}
		
	}
}

const ChatView = {
	data: {
		// chatMessage Format: {(receiver: true,)msg_id:"", time: "", msg:""}
		chatMessages: [],
		receiver: "",
		chat_id: -1,
		old_msg_id: -1,
		new_msg_id: -1
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
		this.data.chat_id = chat_id;
		this.data.receiver = receiver_username;

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
				ChatView.data.new_msg_id = res[0].msg_id;

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
		$('chat-messages').innerHTML = this.html.show_more
		for (let message of this.data.chatMessages)
			$('chat-messages').innerHTML += this.html.message(message);

		$('show-more').addEventListener('click', this.getOldMessages)
		
		if (avoidScroll !== false)
			$('chat-messages').scrollTop = $('chat-messages').scrollHeight;
	}
}

DashboardView.show()