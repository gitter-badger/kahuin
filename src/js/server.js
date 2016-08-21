var express = require('express');
var app = express();

var ExpressPeerServer = require('peer').ExpressPeerServer;

var port = process.env.PORT || 3450;

var server = app.listen(port, function () {
  console.log('Started server on port', port);
});

app.use(express.static('resources/public'));
app.use('/p', ExpressPeerServer(server, {}));