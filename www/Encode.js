var exec = require('cordova/exec');

module.exports = {
	encode: function (message, win, fail) {
		cordova.exec(win, fail, 'Encode', 'encode', [message]);
	}
};