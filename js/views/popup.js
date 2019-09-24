const PopUp = {
	data: {
		users: [],
		chat_id: -1
	},

    html: {
        group_users: () => `
            <div class="horizontal full">
                <input type="text" class="full" placeholder="Username" id="adduser-username"/>
                <input type="button" value="Add User" id="group-adduser" style="margin-left: 6px">
            </div>
            <div class="vertical full" id="group-user-container">
            </div>
        `,
        user: (username) => `
        	<div class="group-user">${username} <span class="remove-user" username="${username}">Remove</span></div>
        `
    },

    init: function () {
    	$("#close-popup").addEventListener('click', () => {
    		PopUp.hide()
    	})
    },

    show: function (chat_id) {
    	this.data.chat_id = chat_id
    	$('pop-up').innerHTML = this.html.group_users()
    	this.getUsers();
    	$('pop-up-container').style.display = "block"
    	$("#group-adduser").addEventListener('click', () => {
    		PopUp.addUser($("#adduser-username").value);
    		$("#adduser-username").value = "";
    	})
    },

    hide: function () {
    	$('pop-up-container').style.display = "none"
		$('pop-up').innerHTML = ""
    },

    addUser: function (username) {
    	$xhrPost(`/ChatApp/add_user`, {chat_id:PopUp.data.chat_id, username}, (res) => {
    		if (res.trim() == "true") {
    			//alert("User was added");
    			PopUp.getUsers();
    		} else {
    			alert("User already added or doesn't exist");
    		}
    	})
    },

    getUsers: function () {
    	$xhrRequest(`/ChatApp/get_users?chat_id=${PopUp.data.chat_id}`, (res) => {
    		res = res.trim();
    		res = JSON.parse(res);

    		PopUp.data.users = []

    		for (let user of res) {
    			PopUp.data.users.push(user);
    		}

    		PopUp.render()
    	})
    },

    render: () => {
    	$('#group-user-container').innerHTML = ""

    	for (let user of PopUp.data.users) {
    		$('#group-user-container').innerHTML += PopUp.html.user(user)
    	}

    	setTimeout(function () {
    		$forEach('#group-user-container .group-user .remove-user', (el) => {
    			el.addEventListener('click', () => {
    				$xhrRequest('/ChatApp/remove_user', {chat_id:PopUp.data.chat_id, username:el.getAttribute('username')}, (res) => {
    					if (res.trim() == "true") {
    						alert("User removed");
    					} else {
    						alert("User already removed or doesn't exist");
    					}
    				})
    			});
    		})
    	}, 100)
    }
}

PopUp.init()