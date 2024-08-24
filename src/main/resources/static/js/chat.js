var stompClient = null;
var username = null;

function connect(event) {
    username = prompt('Please enter your name:');
    if (username) {
        var socket = new SockJS('/chat-websocket');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, onConnected, onError);
    }
    event.preventDefault();
}

function onConnected() {
    stompClient.subscribe('/user/queue/specific-user', onMessageReceived);

    stompClient.send("/app/chat.addUser", {},
        JSON.stringify({sender: username, type: 'JOIN'})
    )

    document.querySelector('#messageForm').addEventListener('submit', sendMessage, true)
}

function onError(error) {
    console.log('Could not connect to WebSocket server. Please refresh this page to try again!');
}

function sendMessage(event) {
    var messageContent = document.querySelector('#message').value.trim();
    var receiver = document.querySelector('#receiver').value.trim();

    if (messageContent && stompClient) {
        var chatMessage = {
            sender: username,
            content: messageContent,
            type: 'CHAT',
            receiver: receiver
        };

        stompClient.send("/app/chat.sendMessage", {}, JSON.stringify(chatMessage));
        document.querySelector('#message').value = '';
    }
    event.preventDefault();
}

function onMessageReceived(payload) {
    var message = JSON.parse(payload.body);

    var messageElement = document.createElement('li');

    if (message.type === 'JOIN') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' joined!';
    } else if (message.type === 'LEAVE') {
        messageElement.classList.add('event-message');
        message.content = message.sender + ' left!';
    } else {
        messageElement.classList.add('chat-message');

        var avatarElement = document.createElement('i');
        var avatarText = document.createTextNode(message.sender[0]);
        avatarElement.appendChild(avatarText);

        messageElement.appendChild(avatarElement);

        var usernameElement = document.createElement('span');
        var usernameText = document.createTextNode(message.sender);
        usernameElement.appendChild(usernameText);
        messageElement.appendChild(usernameElement);
    }

    var textElement = document.createElement('p');
    var messageText = document.createTextNode(message.content);
    textElement.appendChild(messageText);

    messageElement.appendChild(textElement);

    document.querySelector('#messageArea').appendChild(messageElement);
    document.querySelector('#messageArea').scrollTop = document.querySelector('#messageArea').scrollHeight;
}

document.addEventListener('DOMContentLoaded', (event) => {
    connect(event);
});
