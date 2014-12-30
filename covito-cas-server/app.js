/**
 * Module dependencies.
 */

var express = require('express'), 
	routes = require('./routes'), 
	http = require('http'), 
	path = require('path'), 
	log = require('./lib/utils/log-utils.js'),
	config = require('./app-config');

http.globalAgent.maxSockets = 50;

var app = express();

// all environments
app.set('port', config.port);

//注册模板引擎
app.engine('.html', require('ejs').__express);

//模板目录
app.set('views', __dirname + '/views');
app.set('view engine', 'html');
app.use(express.favicon());
app.use(express.logger('dev'));
app.use(express.bodyParser());
app.use(express.methodOverride());
app.use(app.router);
app.use(express.static(path.join(__dirname, 'public')));

app.set('env',config.debug ? 'development' :'production');

//开发环境下使用异常处理
if ('development' == app.get('env')) {
	app.use(express.errorHandler());
}

//生产环境下开启模板缓存
if('production' == app.get('env')){
    app.enable('view cache');
}

app.get('/', routes.index);

http.createServer(app).listen(app.get('port'), function() {
	console.log('Express server listening on port ' + app.get('port'));
});

process.on('uncaughtException', function(err) {
    try {
        log.error(process.pid,err);
        console.log(err);
    } catch (e) {
        console.log('error when exit', e.stack);
    }
});
