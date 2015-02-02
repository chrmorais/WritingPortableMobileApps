function load_cb()
{
	var s = gateway.retrieveMemory();
	console.log(s);
	return "" + s; // s come as native type, not js string
}

function save_cb(data)
{
	gateway.saveMemory(data);
}

function result_cb(type, text, index)
{
	gateway.result_cb(type, text, index);
}

function af(b)
{
	a = [];
	for (var i = 0; i < b.length; ++i) {
		a.push(java.lang.Double(b[i]));
	}
	return a;
}

function as(b)
{
	var a = [];
	for (var i = 0; i < b.length; ++i) {
		a.push(java.lang.String(b[i]));
	}
	return a;
}

var tables = {"aperture": model_aperture_list,
		"focallength": model_length_list,
		"sensor": model_sensor_list,
        "shutter": model_shutter_list,
		"distance": model_distance_list,
		"iso": model_iso_list,
		"ev": model_ev_list};

function model_list(name)
{
	var res = tables[name]();
	return [af(res[0]), as(res[1]),  new java.lang.Integer(res[2])];
}
