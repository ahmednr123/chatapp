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
		msg_key:"",
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

	show: function (chat_id, receiver_username, msg_key) { 
		this.data = {
			// chatMessage Format: {(receiver: true,)msg_id:"", time: "", msg:""}
			chatMessages: [],
			receiver: receiver_username,
			chat_id: chat_id,
			old_msg_id: -1,
			new_msg_id: -1,
			msg_key: msg_key,
			more: false,
			avoidScroll: false
		}

		$('chat-app').style.width = "600px";
		$('chat-app').innerHTML = this.html.base(receiver_username);

		$('#go_back').addEventListener('click', () => DashboardView.show())
		$('#send_msg').addEventListener('click', this.sendMessage);
		$('#refresh_btn').addEventListener('click', () => 
				this.getMessages(MessageType.NEW, this.data.new_msg_id)
		);
		
		this.getMessages(MessageType.CURRENT);
		this.render();
	},

	getMessages: function (type, msg_id) {
		let api = `/ChatApp/messages
				?type=${type}
				&chat_id=${ChatView.data.chat_id}`

		if (msg_id) 
			api += `&msg_id=${msg_id}`
		
		$xhrRequest(api,
			(res) => {
				console.log("GET[/messages] : " + res);
				res = res.trim();
				res = JSON.parse(res);
				console.log("Cleaning JSON");

				if (type == MessageType.CURRENT) {
					ChatView.data.chatMessages = []
					if (res[0])
						ChatView.data.new_msg_id = res[0].msg_id;
				}

				if (type == MessageType.CURRENT || type == MessageType.OLD) {
					if (res.length >= 10)
						ChatView.data.more = true;
					else
						ChatView.data.more = false;
				}

				for (let json of res) {
					let chatMessage = 
						{
							msg_id: json.msg_id, 
							msg: Decrypt(json.message, ChatView.data.msg_key), 
							time: json.time
						}

					if (ChatView.data.receiver == json.sender)
						chatMessage["receiver"] = true;

					if (type == MessageType.CURRENT || type == MessageType.OLD) {
						ChatView.data.chatMessages.unshift(chatMessage);
						ChatView.data.old_msg_id = json.msg_id;
					} else {
						ChatView.data.chatMessages.push(chatMessage);
						ChatView.data.new_msg_id = json.msg_id;
					}
					
				}

				if (type == MessageType.OLD)
					ChatView.data.scroll = false;
				else
					ChatView.data.scroll = true;

				ChatView.render();
			}
		)
	},

	sendMessage: function () {
		$xhrPost('/ChatApp/message', 
			{
				chat_id: ChatView.data.chat_id,
				message: Encrypt($("#message").value, ChatView.data.msg_key)
			}, 
			(res) => {
				console.log("POST[/message] :" + res);
				$('#message').value = "";
				res = res.trim();
				res = JSON.parse(res);
				if (res.reply == true) {
					let json = 
						{
							msg: Decrypt(res.message, ChatView.data.msg_key),
							time: res.time
						};
					let msgHTML = ChatView.html.message(json);
					$('chat-messages').innerHTML += msgHTML;
					
					setTimeout(function () {
						$('chat-messages').scrollTop = $('chat-messages').scrollHeight;
					}, 200)
				}
			}
		)
	},

	render: function () {
		$('chat-messages').innerHTML = "";

		if(this.data.more) {
			$('chat-messages').innerHTML = this.html.show_more;
			setTimeout(function () {
				$('show-more').addEventListener('click', () => {
					ChatView.getMessages(MessageType.OLD, ChatView.data.old_msg_id)
				});
			}, 200)
		}
		
		for (let message of this.data.chatMessages)
			$('chat-messages').innerHTML += this.html.message(message);
		
		if (this.data.scroll)
			$('chat-messages').scrollTop = $('chat-messages').scrollHeight;
	}
}

// ====================================================== //
// SimpleCrypto.js======================================= //

function Encrypt (content, passcode) {
	console.log(typeof(passcode))
	var result = []; var passLen = passcode.length ;
	for(var i = 0  ; i < content.length ; i++) {
		var passOffset = i%passLen ;
		var calAscii = (content.charCodeAt(i)+passcode.charCodeAt(passOffset));
		result.push(calAscii);
	}
	return JSON.stringify(result);
}

function Decrypt (content, passcode) {
	var result = [];var str = '';
	var codesArr = JSON.parse(content);var passLen = passcode.length ;
	for(var i = 0  ; i < codesArr.length ; i++) {
		var passOffset = i%passLen ;
		var calAscii = (codesArr[i]-passcode.charCodeAt(passOffset));
		result.push(calAscii) ;
	}
	for(var i = 0 ; i < result.length ; i++) {
		var ch = String.fromCharCode(result[i]); str += ch ;
	}
	return str;
}

// ======================================================= //