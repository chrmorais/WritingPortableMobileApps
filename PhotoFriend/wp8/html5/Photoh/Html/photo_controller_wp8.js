// ## -*- coding: utf-8 -*-

/*jslint browser:true, sub:true, white:false */

window.addEventListener("load", function (e) {
	setTimeout(function () {
		init();
	}, 0);
});

function save_cb(data)
{
	window.external.notify("s" + data);
}

var wp8_data = "";

function preload(data)
{
	wp8_data = data;
	window.external.notify("e " + data.length);
}

function load_cb()
{
	window.external.notify("l"); // causes callback to preload()
	return wp8_data;
}
