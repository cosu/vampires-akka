//Establish the WebSocket connection and set up event handlers
var webSocket = new WebSocket("ws://" + location.hostname + ":" + location.port + "/events/");
webSocket.onmessage = function (msg) { updateEvents(msg); };
webSocket.onclose = function () { console.log("closed") };


function updateEvents(msg) {
    console.log(msg);
//    var data = JSON.parse(msg.data);
    insert("events", msg.data);
}

//Helper function for inserting HTML as the first child of an element
function insert(targetId, message) {
    id(targetId).insertAdjacentHTML("afterbegin", message);
}

//Helper function for selecting element by id
function id(id) {
    return document.getElementById(id);
}
