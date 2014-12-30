/**
 * 基于winton写的日志组件，主要用于记录一些服务器运行时候的异常
 */
var winston = require('winston'),
    config = require('../../app-config');

/*
    level: Level of messages that this transport should log.
    silent: Boolean flag indicating whether to suppress output.
    colorize: Boolean flag indicating if we should colorize output.
    timestamp: Boolean flag indicating if we should prepend output with timestamps (default true). If function is specified, its return value will be used instead of timestamps.
    filename: The filename of the logfile to write output to.
    maxsize: Max size in bytes of the logfile, if the size is exceeded then a new file is created.
    maxFiles: Limit the number of files created when the size of the logfile is exceeded.
    stream: The WriteableStream to write output to.
    json: If true, messages will be logged as JSON (default true)
*/
/**
 * 自定义的异常数据
 */
var errLogger = new (winston.Logger)({
    transports: [
        new (winston.transports.File)({
            level : 'error',
            json : false,
            maxsize : 1024 * 1024 * 10,     // 文件大小10M
            filename: 'logs/error.log'
        })
    ]
});

var infoLogger = new (winston.Logger)({
    transports: [
        new (winston.transports.File)({
            level : 'info',
            json : false,
            filename: 'logs/info.log',
            maxsize : 1024 * 1024 * 10     // 文件大小10M
        })
    ]
});

var info = function(obj){
    if(config.debug){
        console.log.apply(console,arguments);
        return;
    }

    try {
        if (obj instanceof Error) {
            infoLogger.info(obj.stack);
        } else {
            infoLogger.info.apply(infoLogger, arguments);
        }
    }catch(e){
        console.log(e);
    }
};


exports.error = function(obj){
    if(config.debug){
        console.error.apply(console,arguments);
        return;
    }

    try {
        if (obj instanceof Error) {
            errLogger.error(obj.stack);
        } else {
            errLogger.error.apply(errLogger, arguments);
        }
    }catch(e){
        console.log(e);
    }
};
exports.log = info;
exports.info = info;
