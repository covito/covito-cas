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
var myRouters = [];
/**
 * 数据绑定的预处理数据
 * @type {{}}
 */
var m_preBinderData = {};

var rootPath = './views';
/**
 * 获取文件中对应的controller
 * @param routerPath
 * @param requirePath
 */
var getController = function(routerPath,requirePath){
    requirePath = '../' + requirePath;
    //var viewRoot = routerPath.substr(1);
    var controller = require(requirePath);
    for(var r in controller){
        // 查询每个具体的controller
        if(r == 'index'){
            // 不加$后缀，可能会把后面的index/next请求给拦截掉
            myRouters.push({
                reg : new RegExp('^'+ routerPath + '$','i'),
                fn : controller[r],
                defaultView : routerPath + 'index.html'
            });
            myRouters.push({
                reg : new RegExp('^'+routerPath+'index$','i'),
                fn : controller[r],
                defaultView : routerPath + 'index.html'
            });

        }else{
            if(config.debug) {
                console.log('router:' + '^'+ routerPath + '(' + r + ')$');
            }
            myRouters.push({
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
    var routerPath = ('.' + path + '/').substr(rootPath.length + 1);


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

    index = fileName.indexOf('.html');
    if(index != -1){
        // html页面，预编译
        var key = routerPath + fileName.substr(0,index);
        preBinder.compileHtmlFile(routerPath + fileName,m_preBinderData,key,myRouters,routerPath);
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
 * 转发后台的请求,可以请求多个url,可以请求多个模板
 */
router.all(config.context + '/m-proxy-action/*', function(req, res) {
    res.set('Content-type','text/json; charset=utf-8');
    res.removeHeader("X-Powered-By");
    var params = {};

    for(var key in req.body){           // post中的参数
        params[key] = req.body[key];
    }

    var tpl = params['_tpls'] || '';
    var tpls = tpl.length > 0 ? tpl.split("|") : [];

    var option=params['_options']||'';
    var options=option.length > 0 ? option.split("|") : [];

    var action=params['_action'];

    delete params['_tpls'];
    delete params['_options'];
    delete params['_action'];

    // 使用协作来同步异步调用
    var epAll = EventProxy.create('tplAll','optionAll','httpData',function(tpls,optionAll,jsonData){
        jsonData._tpls=tpls;
        jsonData._option=optionAll;
        res.json(jsonData);
    });

    /**
     * fs.readFile异步方法需要2次回调，不然取不到数据，这里可以提炼成公用方法
     */
    var readFile = function(name,callback){
        if(name.indexOf('.') == 0 || name.indexOf('/') == 0){
            // 必须以/开头，防止用户通过输入..返回上一页来尝试下载其他页面
            callback(null,name,'');
            return;
        }

        //相对路径模板
        var file = './views/' + name+ '.html';

        fs.readFile(file, 'utf-8', function (err, content) {
            if(err){
                callback(err);
            }else{
                callback(null,name,content);
            }

        });
    };

    if(tpls.length > 0) {
        var epTpl = new EventProxy();
        epTpl.after('one_tpl', tpls.length, function (list) {
            epAll.emit('tplAll',list);
        });
        for(var i=0;i<tpls.length;i++) {
            readFile(tpls[i], function (err,name,content) {
                if(err){
                    console.error(err);
                }
                epTpl.emit('one_tpl', {'name':name,'content':content});
            });
        }
    }else{
        epAll.emit('tplAll',[]);
    }


    if(options.length > 0) {
        var epOpt = new EventProxy();
        epOpt.after('one_option', options.length, function (list) {

            var obj={};
            for(var i=0;i<list.length;i++){
                for(var key in list[i]){
                    obj[key]=list[i][key];
                }
            }
            epAll.emit('optionAll',obj);
        });
        for(var i=0;i<options.length;i++) {
            var key=options[i];
            var option=preBinder.getOption(key);

            if(option){
                getOptionData(req,res,key,option,function(data){
                    // 回调后的数据自动注册，默认名字为data
                    epOpt.emit('one_option',data);
                });
            }else{
                var copyData = preBinder.getOption("other");
                var newData = {};
                newData.url = copyData.url + key;
                newData.clientShow = copyData.clientShow;
                newData.wrap = copyData.wrap;

                getOptionData(req,res,key,newData,function(data){
                    // 回调后的数据自动注册，默认名字为data
                    epOpt.emit('one_option',data);
                });
            }




        }
    }else{
        epAll.emit('optionAll',[]);
    }

    qhClient.post(action,params,function(data){
        epAll.emit('httpData', data);
    },new context(req,res),true);


})
/**
 * 转发后台的请求，可以接受tpl参数用于获取请求的模板
 */
router.all(config.context + '/proxy-action/*', function(req, res) {
    res.set('Content-type','text/json; charset=utf-8');
    res.removeHeader("X-Powered-By");
    var params = {};

    for(var key in req.body){           // post中的参数
        params[key] = req.body[key];
    }

    for(var key in req.query){          // get url中参数
        params[key] = req.query[key];
    }

    var tpl = params['_tpls'] || '';
    var tpls = tpl.length > 0 ? tpl.split("|") : [];

    var action = params['_action'];

    delete params['_tpls'];
    delete params['_action'];

    // 使用协作来同步异步调用
    var epAll = EventProxy.create('tplAll','httpData',function(tpls,jsonData){
        jsonData._tpls=tpls;
        res.json(jsonData);
    });

    /**
     * fs.readFile异步方法需要2次回调，不然取不到数据，这里可以提炼成公用方法
     */
    var readFile = function(name,callback){
        if(name.indexOf('.') == 0 || name.indexOf('/') == 0){
            // 必须以/开头，防止用户通过输入..返回上一页来尝试下载其他页面
            callback(null,name,'');
            return;
        }
        //stock-market项目以后会独立出去。所以模板也就拆分出来了。不能放在公共的./views/template里面。
        //这样的话，就需要支持独立的目录访问模板。
        //暂定使用@开头来作为views内的绝对路径访问独立项目的独立模板
        if(name.charAt(0)=="@"){
            var file = './views/' + (name.substring(1)) + '.html';
        }else{
            var file = './views/template/' + name + '.html';
        }
        fs.readFile(file, 'utf-8', function (err, content) {
            if(err){
                callback(err);
            }else{
                callback(null,name,content);
            }

        });
    };

    if(tpls.length > 0) {
        var epTpl = new EventProxy();
        epTpl.after('one_tpl', tpls.length, function (list) {
            epAll.emit('tplAll',list);
        });
        for(var i=0;i<tpls.length;i++) {
           // var name = tpls[i];
            readFile(tpls[i], function (err,name,content) {
                if(err){
                    console.error(err);
                }
                epTpl.emit('one_tpl', {'name':name,'content':content});
            });
        }
    }else{
        epAll.emit('tplAll',[]);
    }

    qhClient.post(action,params,function(data){
        epAll.emit('httpData', data);
    },new context(req,res),true);
});
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
        qhClient.post('/qhee-webapp/action/SampleBasicAction/getWebContext',{
        },function(data){
            ctx.data.g_context = data.data;
            //if(config.debug){
                // 开发环境，之前qhee-webapp的地址直接连接到ORG上去，本地不启动tomcat
           //     ctx.data.g_context.context = 'http://www.qhee.org/qhee-webapp';
           //}
            ctx.data.g_user = data.data.user;

            // 登录状态检查
            var preData = m_preBinderData[path];
            var matchRouter = function(){
                // 预处理数据绑定
                preBinder.preLoad(preData,ctx,function(list){
                    if(list == null || list.length==0){
                        ctx.render();
                        return;
                    }

                    var jsonTmp = {};   // 把数组转为json对象
                    for(var i=0;i<list.length;i++){
                        var obj = list[i];
                        for(var key in obj){
                            jsonTmp[key] = obj[key];
                        }
                    }

                    ctx.render(jsonTmp);
                });
            }
            // 匹配是否有对应的controller
            for (var i = 0; i < myRouters.length; i++) {
                var myRouter = myRouters[i];

                var m = myRouter.reg.exec(path);

                if (m) {
                    // 命中
                    // 以前这只支持正则的一个参数，现在修改为支持多个参数
                    if (m.length > 1) {
                        for (var i = 1; i < m.length; i++) {
                            req.query[i - 1] = decodeURIComponent(m[i]);
                        }
                    }

                    // 匹配的路由一般都有默认view，登入可以在对应的controll里面重新设置
                    if(myRouter.defaultView)
                        ctx.view = myRouter.defaultView.substr(1);  // 需要去掉开头的/

                    if(myRouter.fn){
                        // 如果有controll函数，则调用函数后直接返回
                        myRouter.fn(ctx);
                        return;
                    }else {
                        // 没有controll函数，只有页面，说明该url重写规则是在页面里面定义的
                        // 去掉.html的后缀
                        preData = m_preBinderData[myRouter.defaultView.substr(0,myRouter.defaultView.length-5)];
                    }

                    break;
                }
            }

            if(preData){
                loginFilter.check(preData.loginType,ctx,matchRouter);
            }else{
                matchRouter();
            }
        },ctx);
    }catch(err){
        console.error(err);
        // 防止异常导致进程挂掉
        renderError(ctx,err);
        throw err;
    }
});

module.exports = router;