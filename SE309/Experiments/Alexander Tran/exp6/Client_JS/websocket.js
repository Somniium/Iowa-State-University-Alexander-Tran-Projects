let ws = null;
let currentRoomId = null;
let typingCooldown = false;

function logMessage(message) {
    const log = document.getElementById("log");
    log.value += message + "\n";
    log.scrollTop = log.scrollHeight;
}

function connectSocket() {
    const username = document.getElementById("username").value.trim();
    const wsserver = document.getElementById("wsserver").value.trim();

    if (!username) {
        alert("Enter a username first.");
        return;
    }

    const url = wsserver + encodeURIComponent(username);
    ws = new WebSocket(url);

    ws.onopen = function () {
        logMessage("Connected to " + url);
    };

    ws.onmessage = function (event) {
        try {
            const data = JSON.parse(event.data);

            switch (data.type) {
                case "SYSTEM":
                    logMessage("[SYSTEM] " + data.content);
                    break;

                case "CHAT_MESSAGE":
                    logMessage("[" + data.roomId + "] " + data.sender + ": " + data.content);
                    break;

                case "DIRECT_MESSAGE":
                    const currentUser = document.getElementById("username").value;
                    logMessage("[DM] " + data.sender + " -> " + currentUser + ": " + data.content);
                    break;

                case "USER_TYPING":
                    handleTypingEvent(data);
                    break;

                case "USER_JOINED":
                    logMessage("[JOIN] " + data.sender + " joined " + data.roomId);
                    break;

                case "USER_LEFT":
                    logMessage("[LEAVE] " + data.sender + " left " + data.roomId);
                    break;

                case "NOTIFICATION":
                    logMessage("[NOTIFICATION] " + data.content);
                    break;

                case "ROOM_LOCKED":
                    logMessage("[MODERATION] Room locked: " + data.roomId);
                    break;

                case "ROOM_UNLOCKED":
                    logMessage("[MODERATION] Room unlocked: " + data.roomId);
                    break;

                case "USER_MUTED":
                    logMessage("[MODERATION] User muted: " + data.targetUsername);
                    break;

                case "USER_UNMUTED":
                    logMessage("[MODERATION] User unmuted: " + data.targetUsername);
                    break;

                case "RATING_UPDATED":
                    logMessage("[RATING] " + data.mediaType + " " + data.mediaId + " new rating: " + data.rating);
                    break;

                case "REVIEW_COMMENT":
                    logMessage("[REVIEW " + data.reviewId + "] " + data.sender + ": " + data.content);
                    break;

                default:
                    logMessage("[UNKNOWN EVENT] " + event.data);
                    break;
            }
        } catch (e) {
            logMessage("[RAW] " + event.data);
        }
    };

    ws.onclose = function () {
        logMessage("Disconnected from server.");
    };

    ws.onerror = function () {
        logMessage("WebSocket error occurred.");
    };
}

function joinRoom() {
    const roomId = document.getElementById("roomId").value.trim();

    if (!ws || ws.readyState !== WebSocket.OPEN) {
        alert("Connect first.");
        return;
    }

    if (!roomId) {
        alert("Enter a room ID.");
        return;
    }

    currentRoomId = roomId;

    ws.send(JSON.stringify({
        type: "JOIN_ROOM",
        roomId: roomId
    }));

    logMessage("Joining room: " + roomId);
}

function sendChat() {
    const content = document.getElementById("msg").value.trim();

    if (!ws || ws.readyState !== WebSocket.OPEN) {
        alert("Connect first.");
        return;
    }

    if (!currentRoomId) {
        alert("Join a room first.");
        return;
    }

    if (!content) {
        return;
    }

    ws.send(JSON.stringify({
        type: "CHAT_MESSAGE",
        roomId: currentRoomId,
        content: content
    }));

    document.getElementById("msg").value = "";
}

let typingTimeout = null;

function sendTyping() {
    if (!ws || ws.readyState !== WebSocket.OPEN || !currentRoomId) return;

    // send START typing
    ws.send(JSON.stringify({
        type: "USER_TYPING",
        roomId: currentRoomId,
        status: "START"
    }));

    // clear previous timeout
    if (typingTimeout) {
        clearTimeout(typingTimeout);
    }

    // after 1 second of no typing → send STOP
    typingTimeout = setTimeout(() => {
        ws.send(JSON.stringify({
            type: "USER_TYPING",
            roomId: currentRoomId,
            status: "STOP"
        }));
    }, 1000);
}

function sendDirectMessage() {
    const target = document.getElementById("dmTarget").value.trim();
    const content = document.getElementById("msg").value.trim();

    if (!ws || ws.readyState !== WebSocket.OPEN) {
        alert("Connect first.");
        return;
    }

    if (!target || !content) {
        alert("Enter DM target and message.");
        return;
    }

    ws.send(JSON.stringify({
        type: "DIRECT_MESSAGE",
        targetUsername: target,
        content: content
    }));

    logMessage("[DM to " + target + "] " + content);
    document.getElementById("msg").value = "";
}

function sendNotification() {
    const content = document.getElementById("notificationText").value.trim();

    if (!ws || ws.readyState !== WebSocket.OPEN) {
        alert("Connect first.");
        return;
    }

    if (!content) {
        return;
    }

    ws.send(JSON.stringify({
        type: "NOTIFICATION",
        content: content
    }));

    document.getElementById("notificationText").value = "";
}

let typingUsers = {};
let typingDisplayTimeout = null;

function handleTypingEvent(data) {
    const currentUser = document.getElementById("username").value.trim();

    // don't show your own typing
    if (data.sender === currentUser) return;

    if (data.status === "START") {
        typingUsers[data.sender] = true;
    } else if (data.status === "STOP") {
        delete typingUsers[data.sender];
    }

    updateTypingIndicator();
}

function updateTypingIndicator() {
    const indicator = document.getElementById("typingIndicator");

    if (!indicator) return;

    const users = Object.keys(typingUsers);

    if (users.length === 0) {
        indicator.textContent = "";
        return;
    }

    if (users.length === 1) {
        indicator.textContent = users[0] + " is typing...";
    } else if (users.length === 2) {
        indicator.textContent = users[0] + " and " + users[1] + " are typing...";
    } else {
        indicator.textContent = "Several people are typing...";
    }

    if (typingDisplayTimeout) clearTimeout(typingDisplayTimeout);

    typingDisplayTimeout = setTimeout(() => {
        typingUsers = {};
        indicator.textContent = "";
    }, 2000);
}