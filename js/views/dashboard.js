const ChatType = {
	USER: 0,
	GROUP: 1
}

const DashboardView = {
	data: {
		chats: []
	},

	html: {
		base: () => `
			<div id="top-bar" class="horizontal full">
				<input type="text" id="group_name" placeholder="Group name" style="width: 100%;margin-right: 10px">
				<input type="button" value="Create Group Chat" id="create_group_chat">
			</div>
			<div id="top-bar" class="horizontal full">
				<input type="text" id="username" placeholder="Username" style="width: 100%;margin-right: 10px">
				<input type="button" value="Create User Chat" id="create_user_chat">
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
		add_chat: (id, username, msg_key) => `
			<chat-user chat_id="${id}" msg_key="${msg_key}">${username}</chat-user>
		`
	},

	show: function () {
		$('chat-app').style.width = "400px"
		$('chat-app').innerHTML = this.html.base();
		$('#create_user_chat').addEventListener('click', this.createUserChat);
		$('#create_group_chat').addEventListener('click', this.createGroupChat);
		this.updateChats();
		this.render();
	},

	createGroupChat: function () {
		$xhrPost('/ChatApp/create_chat', {type: ChatType.GROUP, group_name:$('#group_name').value}, (res, _xhr) => {
			res = res.trim();
			if (res == "true") {
				DashboardView.updateChats(_xhr);
			} else if (res == "false") {
				alert("The chat with the user already exists");
			} else if (res == "err") {
				alert("User not found");
			}
		})
	},

	createUserChat: function () {
		$xhrPost('/ChatApp/create_chat', {type: ChatType.USER, receiver:$('#username').value}, (res, _xhr) => {
			res = res.trim();
			if (res == "true") {
				DashboardView.updateChats(_xhr);
			} else if (res == "false") {
				alert("The chat with the user already exists");
			} else if (res == "err") {
				alert("User not found");
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
			for (let json of this.data.chats){
			    if (json.group_name == "")
				    $('#app-body').innerHTML += this.html.add_chat(json.chat_id, json.username, json.message_key);
                else
                    $('#app-body').innerHTML += this.html.add_chat(json.chat_id, json.group_name + "<span style='color: lightgray;font-weight: 400;'> [Group]</span>", json.message_key);
            }
			/*$forEach('chat-user', (el) => {
				el.addEventListener('click', function () {
					console.log(`chat_id: ${el.getAttribute("chat_id")}, username: ${el.innerHTML}, 
									msg_key:${el.getAttribute("msg_key")}`);
					ChatView.show(el.getAttribute("chat_id"), el.innerHTML, el.getAttribute("msg_key"));
				})
			})*/

			$('#app-body').appendChild(this.html.info_text("Click on a user to chat", true));
		}
		
	}
}