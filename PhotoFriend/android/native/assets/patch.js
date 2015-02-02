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
	gateway.model_list_cb(name, JSON.stringify([res[0], res[1], res[2]]));
}
