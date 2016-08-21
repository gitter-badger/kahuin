var express = require('express');
var app = express();

var ExpressPeerServer = require('peer').ExpressPeerServer;

var port = process.env.PORT || 3450;
var env = process.env.NODE_ENV || 'dev';

var forceSSL = function (req, res, next) {
  if (req.headers['x-forwarded-proto'] !== 'https') {
    return res.redirect(301, ['https://', req.get('Host'), req.url].join(''));
  }
  return next();
};

if (env === 'production') {
  app.use(forceSSL);
}

var server = app.listen(port, function () {
  console.log('Started server in', env, 'mode on port', port);
});

app.use(express.static('resources/public'));
app.use('/p', ExpressPeerServer(server, {}));
