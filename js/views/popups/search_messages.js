const SearchType = {
	EXACT_TERM:0, ANY_TERM:1
}

const SearchMessages_Popup = {
	data: {
		search_type: SearchType.EXACT_TERM,
		chat_id: -1,
		temp_string: "",

		chatMessages: [],
		searchTerm: "",
		fromIndex: 0
	},

	html: {
		base: () => `
			<div class="horizontal full" style="margin-bottom: 10px;font-size:12px;line-height:1.5">
                <input type="radio" value="${SearchType.EXACT_TERM}" name="search_type" checked/> All Exact Terms
                <input type="radio" value="${SearchType.ANY_TERM}" name="search_type" style="margin-left: 10px"> Any Term
            </div>
			<div class="horizontal full">
                <input type="text" class="full" placeholder="Search terms" id="search-term"/>
                <input type="button" value="Search" id="search-btn" style="margin-left: 6px">
            </div>
            <div class="vertical full" id="search-result">
            	<info-text>No messages</info-text>
            </div>
		`,
		message: (msg_data, display_name) => `
			<message-bubble ${(msg_data.isSessionUser)?'class="flip"':''}>
				${(display_name)?`<span style="margin-bottom:6px" class="username">${(!msg_data.isSessionUser)?msg_data.sender:"You"}</span>`:""}
				<span class="time" style="display: block; width: 100%;">${msg_data.time}</span>
				<div class="message first-bubble" id="msg_${msg_data.timestamp}">
					${msg_data.msg}
				</div>
			</message-bubble>
		`,
		show_more: `<show-more>Show more</show-more>`
	},

	search_terms: function () {
		//ChatApp/search_messages?chat_id=22&search_term=idea time&type=0
		let url = `/ChatApp/search_messages?chat_id=${this.data.chat_id}&search_term=${this.data.searchTerm}&type=${this.data.search_type}`
		console.log("Searching terms: " + url)
		
		this.data.chatMessages = []
		
		$xhrRequest(url, (res) => {
			res = res.trim();
			res = JSON.parse(res);

			for (let json of res) {
				let chatMessage = 
					{
						timestamp: json.timestamp, 
						msg: Decrypt(json.message, SearchMessages_Popup.data.msg_key),
						time: getCleanDate(json.timestamp)
					}

				chatMessage["isSessionUser"] = (json.sender == $("#session-user").innerHTML)
				chatMessage["sender"] = json.sender;

				SearchMessages_Popup.data.chatMessages.push(chatMessage);
				SearchMessages_Popup.data.old_timestamp = json.timestamp;
			}

			SearchMessages_Popup.render();
		})
	},

	show: function (chat_id) {
		this.data.chat_id = chat_id;
		this.data.temp_string = ""
		this.data.searchTerm = ""
		this.data.search_type = SearchType.EXACT_TERM

        $('pop-up').innerHTML = this.html.base();

        $('#search-term').addEventListener('keyup', () => {
        	let value = $('#search-term').value
        	console.log("Search term: " + value);
        	SearchMessages_Popup.data.searchTerm = value.trim()
        });

        $forEach('input[name="search_type"]', (el) => {
        	el.onclick = function () {
        		SearchMessages_Popup.data.search_type = el.value
        	}
        })

        $('pop-up-container').style.display = "block";
        $("#search-btn").addEventListener('click', () => {
            //SearchMessages_Popup.addUser($("#adduser-username").value);
            SearchMessages_Popup.search_terms();
        })
	},

	render: function () {
		$('#search-result').innerHTML = "";

		if (this.data.chatMessages.length == 0) {
			$('#search-result').innerHTML += '<info-text>No messages</info-text>';
		}

		for (let message of this.data.chatMessages) {
			$('#search-result').innerHTML += this.html.message(message, (SearchMessages_Popup.data.temp_string != message.sender));
			SearchMessages_Popup.data.temp_string = message.sender;
		}

		this.data.temp_string = ""
	}
}

function getCleanDate (date_string) {
  let date = new Date(date_string);

  let days = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"];
  let months = ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

  let clean_date = `${days[date.getDay()]}, ${months[date.getMonth()]} ${date.getDate()}, ${date.getFullYear()} @ ${date.getHours()}:${date.getMinutes()}`;

  return clean_date;
}