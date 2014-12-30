/**
 * 用于判断请求类型
 */

module.exports = {
	/**
	 * 是否来自客户端的请求
	 * 
	 * @param request
	 */
	isNative : function(request) {
		var ua = request.get('User-Agent');
		if (!ua)
			return false;
		ua = ua.toLowerCase();
		// crosswalk in android
		return ua.indexOf('crosswalk') != -1 || ua.indexOf('cordova') != -1;
	},

	isIos : function(request) {
		var ua = request.get('User-Agent');
		if (!ua)
			return false;
		ua = ua.toLowerCase();
		if (ua.indexOf('iphone') != -1 || ua.indexOf('ipod') != -1
				|| ua.indexOf('itouch') != -1) {
			return true;
		} else {
			return false;
		}
	},

	isAndroid : function(request) {
		var ua = request.get('User-Agent');
		if (!ua)
			return false;
		ua = ua.toLowerCase();
		if (ua.indexOf('android') != -1) {
			return true;
		} else {
			return false;
		}
	},

	isMobile : function(request) {
		var ua = request.get('User-Agent');
		if (!ua)
			return false;
		ua = ua.toLowerCase();
		if (module.exports.isIos(request) || module.exports.isAndroid(request)
				|| ua.indexOf('mobile ') != -1) {
			return true;
		} else {
			return false;
		}
	}
}
