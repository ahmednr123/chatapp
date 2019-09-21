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
				$('#app-body').innerHTML += this.html.add_chat(json.id, json.username);

			$forEach('chat-user', (el) => {
				el.addEventListener('click', function () {
					ChatView.show(el.innerHTML);
				})
			})

			$('#app-body').appendChild(this.html.info_text("Click on a user to chat", true));
		}
		
	}
}

const ChatView = {
	data: {
		chats: [{time: "2019-09-01 05:23:22", msg:"Where are you??"},
				{receiver: true, time: "2019-09-01 05:49:67", msg:"Hello!!!"}],
		receiver: "",
	},

	html: {
		base: () => `
			<div id="top-bar" class="horizontal full">
				<input type="button" value="Go Back" id="go_back">
			</div>
			<div id="app-body">
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
				<div id="msg-info" class="vertical">
					<span id="username">${(msg_data.receiver)?ChatView.data.receiver:"You"}</span>
					<span id="time">${msg_data.time}</span>
				</div>
				<div id="message">
					${msg_data.msg}
				</div>
			</message-bubble>
		`,
		show_more: `<show-more>Show more</show-more>`
	},

	show: function (receiver_username) { 
		this.data.receiver = receiver_username;
		$('chat-app').style.width = "600px";
		$('chat-app').innerHTML = this.html.base();
		$('#go_back').addEventListener('click', () => {
			DashboardView.show();
		})
		$('#send_msg').addEventListener('click', this.sendMessage);
		this.render();
	},

	sendMessage: function () {
		alert("sent message to:" + this.data.receiver);
	},

	render: function () {
		$('chat-messages').innerHTML = this.html.show_more
		for (let message of this.data.chats)
			$('chat-messages').innerHTML += this.html.message(message);
	}
}

DashboardView.show()