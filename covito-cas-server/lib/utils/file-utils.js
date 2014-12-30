/**
 * 文件操作类
 */
var fs = require('fs'),
    n_path = require('path');

var m_copyPathSync = function( src, dst ) {
    // 读取目录中的所有文件/目录
    var paths = fs.readdirSync(src);

    paths.forEach(function (path) {
        var _src = src + '/' + path,
            _dst = dst + '/' + path,
            readable, writable;

        var st = fs.statSync(_src);
        // 判断是否为文件
        if (st.isFile()) {
            // 先删除文件
//            if(fs.existsSync(_dst)){
//                fs.unlinkSync(_dst);
//            }
            // 创建读取流
            readable = fs.createReadStream(_src);
            // 创建写入流
            writable = fs.createWriteStream(_dst);
            // 通过管道来传输流
            readable.pipe(writable);
        }
        // 如果是目录则递归调用自身
        else if (st.isDirectory()) {
            m_copyPathSync(_src, _dst);
        }
    });
}
module.exports = {
    readFile : function (file,callback) {
        fs.readFile(file, 'utf-8', function (err, content) {
            if(err){
                callback(err);
            }else{
                callback(null,content);
            }
        });
    },
    readRawFile : function (file,callback) {
        fs.readFile(file, function (err, content) {
            if(err){
                callback(err);
            }else{
                callback(null,content);
            }
        });
    },
    /**
     * 获取文件后缀
     * @param fileName
     * @return {string} 返回文件后缀，比如 .js .html
     */
    getFileExt : function(fileName){
        var index = fileName.lastIndexOf('.');
        if(index !== -1){
            return fileName.substr(index);
        }else{
            return "";
        }
    },
    /**
     * 删除文件，当文件存在的时候
     */
    deleteFileSync : function(file){
        if(fs.existsSync(file)){
            fs.unlinkSync(file);
        }
    },
    deleteFile : function(file,cb){
        fs.exists(file,function(exists){
            if(exists){
                fs.unlink(file,cb);
            }else{
                if(cb)
                    cb();
            }
        });
    },
    copyPathSync : function( src, dst ){
        m_copyPathSync(src,dst);
    },
    /**
     * 创建目录，fs.mkdirSync必须保证父目录存在，才能创建，所以要一级一级的判断
     * @param path
     */
    mkPath : function(path){
        if(fs.existsSync(path))
            return;

        var paths = path.split('/');
        var completePath = '';
        for(var i=0;i<paths.length;i++){
            completePath += paths[i] + '/';
            var phPath = n_path.resolve(completePath);
            if(!fs.existsSync(phPath)){
                fs.mkdirSync(phPath);
            }
        }

    },
    /**
     * 递归扫描某个目录的所有文件，并根据exts后缀进行过滤，没扫描到1个文件，就调用callback
     * @param path
     * @param exts 数组,['.js','.html']
     * @param {function} callback(file),回调函数，file:文件名
     */
    recuFiles : function(path,exts,callback){
        var lIndex = path.lastIndexOf('/');
        if(lIndex != (path.length -1)){
            path += '/';
        }
        var dirList = fs.readdirSync(path);
        dirList.forEach(function(item){

            var filename = path + item;
            if(fs.statSync(filename).isDirectory()){
                // 如果目录中带了.目录的，则不需要扫描，可能是SVN目录
                module.exports.recuFiles(filename,exts,callback);
            }else{
                var ext = module.exports.getFileExt(filename);
                for(var i=0;i<exts.length;i++){
                    if(exts[i] == ext){
                        callback(filename);
                        break;
                    }
                }
            }
        });
    }
}
