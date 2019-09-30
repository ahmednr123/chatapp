const GetUsers_Popup = {
    data: {
        users: [],
        chat_id: -1
    },

    html: {
        base: () => `
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

    show: function (chat_id) {
        this.data.chat_id = chat_id
        $('pop-up').innerHTML = this.html.base()
        this.getUsers();
        $('pop-up-container').style.display = "block"
        $("#group-adduser").addEventListener('click', () => {
            GetUsers_Popup.addUser($("#adduser-username").value);
            $("#adduser-username").value = "";
        })
    },

    addUser: function (username) {
        $xhrPost(`/ChatApp/add_user`, {chat_id:GetUsers_Popup.data.chat_id, username}, (res) => {
            if (res.trim() == "true") {
                GetUsers_Popup.getUsers();
            } else {
                alert("User already added or doesn't exist");
            }
        })
    },

    getUsers: function () {
        $xhrRequest(`/ChatApp/get_users?chat_id=${GetUsers_Popup.data.chat_id}`, (res) => {
            res = res.trim();
            res = JSON.parse(res);

            GetUsers_Popup.data.users = []

            for (let user of res) {
                GetUsers_Popup.data.users.push(user);
            }

            GetUsers_Popup.render()
        })
    },

    render: () => {
        $('#group-user-container').innerHTML = ""

        for (let user of GetUsers_Popup.data.users) {
            $('#group-user-container').innerHTML += GetUsers_Popup.html.user(user)
        }

        setTimeout(function () {
            $forEach('#group-user-container .group-user .remove-user', (el) => {
                el.addEventListener('click', () => {
                    $xhrPost('/ChatApp/remove_user', {chat_id:GetUsers_Popup.data.chat_id, username:el.getAttribute('username')}, 
                        (res) => {
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