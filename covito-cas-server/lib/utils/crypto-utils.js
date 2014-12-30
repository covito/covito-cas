/**
 * 加密、解密算法
 */
var crypto = require('crypto'), fs = require('fs'), os = require('os');

var key = "InmbuvP6Z8";

var networks = os.networkInterfaces();
for ( var k in networks) {
	if (key != "InmbuvP6Z8") {
		break;
	}
	var list = networks[k];
	for (var i = 0; i < list.length; i++) {
		if (list[i].family == 'IPv6') {
			key = list[i].address;
			break;
		}
	}
}

module.exports = {
	/**
	 * hash加密，返回base64格式字符串
	 * 
	 * @param str
	 */
	sha : function(str) {
		var shasum = crypto.createHash('sha1');
		shasum.update(str);
		return shasum.digest('base64');
	},
	/**
	 * 计算文件的hash值
	 * 
	 * @param fileName
	 */
	shaFile : function(fileName) {
		var buffer = fs.readFileSync(fileName);
		var shasum = crypto.createHash('sha1');
		shasum.update(buffer);
		return shasum.digest('hex');
	},
	/**
	 * 基于KEY的加密
	 * 
	 * @param str
	 */
	encrypt : function(str) {
		var cipher = crypto.createCipher('aes-256-cbc', key);
		var crypted = cipher.update(str, 'utf8', 'hex');
		crypted += cipher.final('hex');

		return crypted;
	},
	/**
	 * 基于KEY的解密
	 * 
	 * @param str
	 */
	decrypt : function(str) {
		var decipher = crypto.createDecipher('aes-256-cbc', key);
		var dec = decipher.update(str, 'hex', 'utf8');
		dec += decipher.final('utf8');

		return dec;
	}
}
