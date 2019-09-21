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