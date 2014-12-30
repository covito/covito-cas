/**
 * 字符串操作
 */

module.exports ={
    trim : function(str){
        if(!str)
            return '';

        return str.replace(/\s/ig,'');
    }
}