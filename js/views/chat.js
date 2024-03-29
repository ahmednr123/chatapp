const MessageType = {
	NEW: 0, OLD: 1, CURRENT: 2
}

const ChatView = {
	data: null,

	html: {
		base: (username) => `
			<div id="top-bar" class="horizontal full">
				<input type="button" value="Go Back" id="go_back">
				${(ChatView.data.isGroup)?`<div id="group_users_btn">Users</div>`:``}
				<div id="search_messages_btn">Search Messages</div>
				<div id="refresh_btn">Refresh</div>
			</div>
			<div id="app-body">
				<!--<div id="receiver-user">${username}</div>-->
				<chat-messages>
				</chat-messages>
				<div class="horizontal full">
					<input type="text" id="message" autocomplete="off" placeholder="Type message" style="width: 100%;margin-right: 10px">
					<input type="button" value="Send Message" id="send_msg">
				</div>
			</div>
		`,
		message: (msg_data, display_name) => `
			<message-bubble ${(msg_data.isSessionUser)?'class="flip"':''}>
				${(display_name)?`<span class="username">${(!msg_data.isSessionUser)?msg_data.sender:"You"}</span>`:""}
				<!--<div class="vertical msg-info">
					<span class="time">${msg_data.time}</span>
				</div>-->
				<div class="message ${(display_name)?`first-bubble`:``}" id="msg_${msg_data.timestamp}">
					${msg_data.msg}
				</div>
			</message-bubble>
		`,
		show_more: `<show-more>Show more</show-more>`
	},

	show: function (chat_id, chat_name, msg_key, isGroup) {
		this.data = {
			// chatMessage Format: {(receiver: true,)timestamp:"", time: "", msg:""}
			chatMessages: [],
			receiver: chat_name,
			chat_id: chat_id,
			old_timestamp: -1,
			new_timestamp: -1,
			msg_key: msg_key,
			more: false,
			isGroup: isGroup,
			avoidScroll: false,
			temp_string: ""
		}

		$('chat-app').style.width = "600px";
		$('chat-app').innerHTML = this.html.base(chat_name);

		$('#go_back').addEventListener('click', () => DashboardView.show())
		$('#send_msg').addEventListener('click', this.sendMessage);
		
		$('#refresh_btn').addEventListener('click', () => 
				this.getMessages(MessageType.NEW, this.data.new_timestamp)
		);
		
		$('#search_messages_btn').addEventListener('click', () => {
			PopUp.show_search_messages(this.data.chat_id);
		});

		if (isGroup)
			$('#group_users_btn').addEventListener('click', () => {
				PopUp.show_group_users(this.data.chat_id);
			});

		this.getMessages(MessageType.CURRENT);
		this.render();
	},

	getMessages: function (type, timestamp) {
		if (timestamp == -1) {
			this.getMessages(MessageType.CURRENT)
			return
		}

		let api = `/ChatApp/messages
				?type=${type}
				&chat_id=${ChatView.data.chat_id}`

		if (timestamp) 
			api += `&timestamp=${timestamp}`

		this.data.temp_string = ""
		
		$xhrRequest(api,
			(res) => {
				console.log("GET[/messages] : " + res);
				res = res.trim();
				res = JSON.parse(res);
				console.log("Cleaning JSON");

				if (type == MessageType.CURRENT) {
					ChatView.data.chatMessages = []
					if (res[0])
						ChatView.data.new_timestamp = res[0].timestamp;
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
							timestamp: json.timestamp, 
							msg: Decrypt(json.message, ChatView.data.msg_key)
						}

					chatMessage["isSessionUser"] = (json.sender == $("#session-user").innerHTML)
					chatMessage["sender"] = json.sender;

					if (type == MessageType.CURRENT || type == MessageType.OLD) {
						ChatView.data.chatMessages.unshift(chatMessage);
						ChatView.data.old_timestamp = json.timestamp;
					} else {
						ChatView.data.chatMessages.push(chatMessage);
						ChatView.data.new_timestamp = json.timestamp;
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
		if ($("#message").value.length > 0) {
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
						setTimeout(() => {
							ChatView.getMessages(MessageType.NEW, ChatView.data.new_timestamp);
						}, 400)
					}
				}
			)
		}
	},

	render: function () {
		$('chat-messages').innerHTML = "";

		if(this.data.more) {
			$('chat-messages').innerHTML = this.html.show_more;
			setTimeout(function () {
				$('show-more').addEventListener('click', () => {
					ChatView.getMessages(MessageType.OLD, ChatView.data.old_timestamp)
				});
			}, 200)
		}
		
		for (let message of this.data.chatMessages) {
			$('chat-messages').innerHTML += this.html.message(message, (ChatView.data.temp_string != message.sender));
			ChatView.data.temp_string = message.sender;
		}
		
		if (this.data.scroll)
			$('chat-messages').scrollTop = $('chat-messages').scrollHeight;
	}
}

// ====================================================== //

function Encrypt (content, passcode) {
	/*console.log(typeof(passcode))
	var result = []; var passLen = passcode.length ;
	for(var i = 0  ; i < content.length ; i++) {
		var passOffset = i%passLen ;
		var calAscii = (content.charCodeAt(i)+passcode.charCodeAt(passOffset));
		result.push(calAscii);
	}
	return JSON.stringify(result);*/

	return content;
}

function Decrypt (content, passcode) {
	/*var result = [];var str = '';
	var codesArr = JSON.parse(content);var passLen = passcode.length ;
	for(var i = 0  ; i < codesArr.length ; i++) {
		var passOffset = i%passLen ;
		var calAscii = (codesArr[i]-passcode.charCodeAt(passOffset));
		result.push(calAscii) ;
	}
	for(var i = 0 ; i < result.length ; i++) {
		var ch = String.fromCharCode(result[i]); str += ch ;
	}
	return str;*/

	return content;
}

// ======================================================= //