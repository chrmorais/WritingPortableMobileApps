function load_cb()
{
	var s = load_cb_native();
	return "" + s; // s come as native type, not js string
}

function save_cb(data)
{
	save_cb_native(data);
}

function result_cb(type, text, index)
{
	result_cb_native(type, text, index);
}

/*
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
	model_list_cb_native(name, JSON.stringify([res[0], res[1], res[2]]));
}
*/
