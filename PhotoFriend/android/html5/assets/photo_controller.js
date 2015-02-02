// ## -*- coding: utf-8 -*-

/*jslint browser:true, sub:true, white:false */

var tweaks = [ "aperture", "shutter", "iso", "ev" ];
var selects = [ "distance", "focallength" ]; // sensor

var controls = null;

function init()
{
	var el, eld, i, j;

	var microsoft = (window.navigator && window.navigator.msPointerEnabled && true);

	var touch = 'ontouchstart' in document.getElementById(tweaks[0] + "1");
	var attribute = touch ? "ontouchstart" : "onclick";
	for (i = 1; i <= 2; ++i) {
		for (j = 0; j < tweaks.length; ++j) {
			el = tweaks[j] + ("" + i);
			if (microsoft) {
				(function (i, j) {
					var handler = function (x) {
						model_input(tweaks[j], ["", "minus", "plus"][i]);
						e.preventDefault();
					};
					document.getElementById(el).addEventListener("MSPointerDown", handler, true);
   
				})(i, j);
			} else {
				(function (i, j) {
					document.getElementById(el)[attribute] = function (e) {
						model_input(tweaks[j], ["", "minus", "plus"][i]);
						e.preventDefault();
					};
				})(i, j);
			}
		}
	}

	for (j = 0; j < selects.length; ++j) {
		el = selects[j];
		(function (j) {
			var eld = document.getElementById(el);
			eld.onchange = function (e) {
				model_input(selects[j], eld.selectedIndex);
				e.preventDefault();
			};
		})(j);
	}

	var opt = function (txt, i) {
		var oel = document.createElement('option');
		oel.text = txt;
		oel.value = "" + i;
		return oel;
	};

	/*
	el = document.getElementById("sensor");
	var l = model_sensor_list();
	for (i = 0; i < l[0].length; ++i) {
		el.add(opt(l[0][i], i));
	}
	el.value = l[1];
	*/
	
	el = document.getElementById("focallength");
	l = model_length_list();
	for (i = 0; i < l[1].length; ++i) {
		el.add(opt(l[1][i], i));
	}
	el.value = l[2];
	
	el = document.getElementById("distance");
	l = model_distance_list();
	for (i = 0; i < l[1].length; ++i) {
		el.add(opt(l[1][i], i));
	}
	el.value = l[2];
	
	model_init();
}

function load_cb()
{
	var data = localStorage.getItem('data1');
	if (! data) {
		data = "";
	}
	return data;
}

function save_cb(data)
{	
	localStorage.setItem('data1', data);
}

function result_cb(item, value, index)
{
	if (item === "sensor" || item === "focallength" || item === "distance") {
		document.getElementById(item).value = index;
	} else {
		document.getElementById(item).innerHTML = value;
	}
}

window.addEventListener("load", function (e) {
	// Set a timeout...
	setTimeout(function () {
		// Hide the address bar (iPhone)
		window.scrollTo(0, 1);
	}, 0);
});
