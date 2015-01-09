/**
 * 自动扫描的路由
 */
var express = require('express'),
    fs = require('fs'),
    fileUtils = require('../utils/file-utils.js'),
    EventProxy = require('eventproxy'),
    router = express.Router(),
    config = require('../../app-config');

/**
 * 自定义的一些路由
 * @type {Array}
 */
var controllers = [];


/**
 * 获取文件中对应的controller
 * @param routerPath
 * @param requirePath
 */
var getController = function(routerPath,requirePath){
    var controller = require(requirePath);
    for(var r in controller){
        // 查询每个具体的controller
        if(r == 'index'){
            // 不加$后缀，可能会把后面的index/next请求给拦截掉
            controllers.push({
                reg : new RegExp('^'+ routerPath + '$','i'),
                fn : controller[r],
                defaultView : routerPath + 'index.html'
            });
            controllers.push({
                reg : new RegExp('^'+routerPath+'index$','i'),
                fn : controller[r],
                defaultView : routerPath + 'index.html'
            });

        }else{
            if(config.debug) {
                console.log('router:' + '^'+ routerPath + '(' + r + ')$');
            }
            controllers.push({
                reg : new RegExp('^'+routerPath+'(' + r + ')$','i'),
                fn : controller[r],
                defaultView : routerPath  + r + '.html'
            });
        }

    }
}
/**
 * 搜索文件内容
 * @param path
 * @param fileName
 */
var searchFile = function(path,fileName){
    var index = fileName.indexOf('.js');
    // /当前文件相对views所在目录
    var routerPath = ('.' + path + '/').substr(config.route_root.length + 1);


    if(index != -1){
        // requirePath
        var allPath = '.' + path + '/' + fileName.substr(0,index);
        getController(routerPath,allPath);

        if(config.debug){
            // 监控文件变换，3秒轮询一次
            fs.watchFile(path + '/' + fileName,{ persistent: true, interval: 3007 },function(cur,prex){
                getController(routerPath,allPath);
            });
        }
        return;
    }

}
/**
 * 递归目录
 * @param path
 */
var recurPath = function(path){
    var dirList = fs.readdirSync(path);
    dirList.forEach(function(item){
        if(fs.statSync(path + '/' + item).isDirectory() && item.indexOf('.') === -1){
            // 如果目录中带了.目录的，则不需要扫描，可能是SVN目录
            recurPath(path + '/' + item);
        }else{
            searchFile(path,item);
        }
    });
};
recurPath(rootPath);

var renderError = function (ctx, msg, body) {
    ctx.res.status(500);

    var _msg=msg,_body=body,_stack='';
    if(msg instanceof Error){
        _msg = msg.message;
        _stack = msg.stack;
        _body = '';
    }
    ctx.render('500.html', {
        message: _msg,
        'body': _body,
        stack : _stack
    });
}


/**
 * 默认的路由，不处理后端代码，直接render某个html
 */
router.use('/', function(req, res) {
    res.set('Content-type','text/html; charset=utf-8');
    res.removeHeader("X-Powered-By");

    var path = req.path;
    if(path.indexOf('.') !== -1){
        var ext = fileUtils.getFileExt(path);
        if(ext !== '.html') {
            // 请求有后缀，不是我们需要的请求
            res.status(404);
            res.end('page not found');
            return;
        }else{
            // 带.html的访问和不带后缀的访问效果是一样的，带html是方便搜索引擎处理的静态URL
            path = path.substr(0,path.length-5);
        }
    }
    if(path.lastIndexOf('/') == (path.length - 1)){
        // end with /
        path = path + 'index';
    }
    //console.log(path);
    // 需要去掉context
    if(path.indexOf(config.context) == 0){
        path = path.substr(config.context.length);
    }

    var view = path;
    if(view.length > 0){
        view = view.substr(1);
    }

    var ctx = new context(req,res,view + '.html');
    // 获取用户登录信息
    try{

    }catch(err){
        console.error(err);
        // 防止异常导致进程挂掉
        renderError(ctx,err);
        throw err;
    }
});

module.exports = router;