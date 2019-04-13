var SeleniumXMLHttpRequest = window.SeleniumXMLHttpRequest = window.XMLHttpRequest;
SeleniumXMLHttpRequest.requests = [];
SeleniumXMLHttpRequest.get = function() {
	var requests = SeleniumXMLHttpRequest.requests.splice(0);
	SeleniumXMLHttpRequest.requests = [];
	return requests;
}
window.XMLHttpRequest = function() {
	this.xhr = new SeleniumXMLHttpRequest();
};
[ 
	'open', 
	'abort', 
	'setRequestHeader', 
	'send', 
	'addEventListener', 
	'removeEventListener', 
	'getResponseHeader', 
	'getAllResponseHeaders', 
	'dispatchEvent', 
	'overrideMimeType'
].forEach(function(method){
	window.XMLHttpRequest.prototype[method] = function() {
		if (method == 'open') {
			this.open = arguments;
		}
		if (method == 'send') {
			this.send = arguments;
		}
		return this.xhr[method].apply(this.xhr, arguments);
	}
});
	[
	'onabort',
	'onerror',
	'onload',
	'onloadstart',
	'onloadend',
	'onprogress',
	'readyState',
	'responseText',
	'responseType',
	'responseXML',
	'status',
	'statusText',
	'upload',
	'withCredentials',
	'DONE',
	'UNSENT',
	'HEADERS_RECEIVED',
	'LOADING',
	'OPENED'
].forEach(function(scalar){
	Object.defineProperty(window.XMLHttpRequest.prototype, scalar, {
		get: function(){
			return this.xhr[scalar];
		},
		set: function(obj){
			this.xhr[scalar] = obj;
		}
	});
});
Object.defineProperty(window.XMLHttpRequest.prototype, 'onreadystatechange', {
	get: function(){
		return this.xhr.onreadystatechange;
	},
	set: function(onreadystatechange){
		var _ = this;
		_.xhr.onreadystatechange = function(){
			if (_.xhr.readyState == 4) {
				debugger
				SeleniumXMLHttpRequest.requests.push([_.xhr.status, _.open[0], _.open[1], _.send[0], _.xhr.responseText]);
			}
			onreadystatechange.call(_.xhr);
		};
	}
});