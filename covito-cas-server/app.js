/**
 * Module dependencies.
 */

var express = require('express'), 
	routes = require('./lib/routes/static-router'),
	favicon = require('static-favicon'),
    cookieParser = require('cookie-parser'),
    bodyParser = require('body-parser'),
	http = require('http'),
	path = require('path'), 
	log = require('./lib/utils/log-utils.js'),
	config = require('./app-config');

//默认5个
http.globalAgent.maxSockets = 50;

var app = express();

app.set('env',config.debug ? 'development' :'production');

//注册模板引擎
app.engine('.html', require('ejs').__express);

//严格路由，默认情况下 "/foo" 和 "/foo/" 是被同样对待的
app.enable('strict routing');

//禁用etag缓存
app.disable('etag');

//模板目录
app.set('views', __dirname + '/views');
app.set('view engine', 'html');

//定义favicon
app.use(favicon(__dirname + '/public/favicon.ico',{maxAge:31536000000}));

app.use(bodyParser.json());
app.use(bodyParser.urlencoded());
app.use(cookieParser());

//静态目录
app.use(config.context +'public',express.static(path.join(__dirname, 'public')));

//开发环境下使用异常处理
if (config.debug) {
	app.use(express.errorHandler());
}

app.locals.g_config = config;

//路由配置
routes.setRequestUrl(app);

http.createServer(app).listen(config.port, function() {
	console.log('Express server listening on port ' + config.port);
});

process.on('uncaughtException', function(err) {
    try {
        log.error(process.pid,err);
        console.log(err);
    } catch (e) {
        console.log('error when exit', e.stack);
    }
});
