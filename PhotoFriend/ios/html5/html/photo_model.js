if (! console) {
	console = {};
}
if (! console.log) {
	console.log = function (s) {};
}

var result_delay = 100;

var shutters, sshutters;
var apertures, sapertures;
var isos, sisos;
var evs, sevs;

var evtexts = {
	"16": "Sunlight + snow",
	"15": "Sunlight ('sunny 16')",
	"14": "Sunlight, soft shadows",
	"13": "Cloudy bright",
	"12": "Heavy overcast, sunset",
	"11": "Sunlight, open shade",
	"10": "Right after sunset",
	 "9": "10 min after sunset",
	 "8": "Bright interiors",
	 "7": "Bright nighttime streets",
	 "6": "Bright home interiors",
	 "5": "Home interiors at night",
	 "4": "Under bright street lamps",
	 "3": "Brightly lit monuments",
	 "2": "Distant lighted buildings",
	 "1": "Distant skyline",
	 "0": "Dim artificial light",
	"-1": "Dimmer artificial light",
	"-2": "Night full moon",
	"-3": "Night under full moon",
	"-4": "Night half moon",
	"-5": "Night crescent moon",
	"-6": "Starlight",
};
var lengths = [10, 12, 14, 15, 18, 20, 24, 28, 35, 40, 50, 75, 85, 100, 135, 200, 300, 400];
var slengths;
var sensors      = ['1/3"', '1/2.3"', '2/3"', '1" CX', 'Four Thirds', 'APS-C DX', '35mm FX'];
var crops = [7.21,        5.62,      3.93,   2.7,    2,             1.50,        1];
var distances = [.5, .6, .8, 1, 1.5, 2, 3, 5, 8, 10, 20, 30, 50, 999999999];
var sdistances;

isos = [25, 32, 50, 100, 200, 320, 400, 800, 1000, 1600,
	3200, 6400, 12800, 25600, 51200];
shutters = [240, 120, 60, 30, 15, 8, 4, 2, 1, 1/2, 1/4, 1/8, 1/15,
	  1/30, 1/60, 1/100, 1/125, 1/250, 1/500, 1/1000,
	  1/2000, 1/4000, 1/8000, 1/16000];
shutters.reverse();
apertures = [0.5, 0.7, 1, 1.4, 2, 2.8, 4, 5.6, 8, 11, 16, 22, 32, 45, 64, 90, 128];
evs = [-6, -5, -4, -3, -2, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16];

sapertures = [];
for (var i = 0; i < apertures.length; ++i) {
	var s = apertures[i].toFixed(1);
	if (s.charAt(s.length - 1) === "0") {
		s = s.substr(0, s.length - 2);
	}
	sapertures.push("f/" + s);
}

sevs = [];
for (i = 0; i < evs.length; ++i) {
	sevs.push("EV " + evs[i]);
}

sisos = [];
for (i = 0; i < isos.length; ++i) {
	sisos.push("ISO " + isos[i]);
}

sdistances = [];
for (i = 0; i < distances.length; ++i) {
	sdistances.push(format_distance(distances[i]));
}

sshutters = [];
for (i = 0; i < shutters.length; ++i) {
	var c;
	if (shutters[i] >= 1) {
		c = "" + shutters[i] + "s";
	} else {
		c = "1/" + (1 / shutters[i]).toFixed() + "s";
	}
	sshutters.push(c);
}

slengths = [];
for (i = 0; i < lengths.length; ++i) {
	slengths.push(lengths[i].toFixed(0) + "mm");
}

var state = {};

// sunny 16 rule
var std_shutter = 1 / 125;
var std_iso = 100;
var std_aperture = 16;
var std_ev = 15;
var std_2ev = Math.pow(2, std_ev);
var K = std_shutter * std_iso * std_2ev / (std_aperture * std_aperture);
// another definition: EV=0 for f/1, 1s and ISO 100, so K=100
// sunny-16 yields K=102.4, not very far.

function model_init()
{
	state.aperture = find_nearest(apertures, 16);
	state.iso = find_nearest(isos, 100);
	state.ev = find_nearest(evs, 15);
	state.shutter = find_nearest(shutters, 1 / 100);
	state.aperture_priority = true;

	state.sensor = find_nearest(crops, 1);
	state.distance = find_nearest(distances, 1);
	state.length = find_nearest(lengths, 50);

	var data = load_cb();

	if (data.length > 0) {
		console.log(data);
		data = JSON.parse(data);
		state.aperture = data.aperture || 0;
		state.iso = data.iso || 0;
		state.ev = data.ev || 0;
		state.aperture_priority = data.aperture_priority || 0;
		// state.sensor = data.sensor || 0;
		state.distance = data.distance || 0;
		state.length = data.length || 0;
		cast_vars();
		recalculate_exposure("");
	}

	changed(true);
}

function cast_vars()
{
	if (state.aperture === -1) {
		state.aperture = 0;
	} else if (state.aperture === -2) {
		state.aperture = apertures.length - 1;
	}
	state.aperture = Math.max(0, state.aperture);
	state.aperture = Math.min(apertures.length - 1, state.aperture);

	if (state.shutter === -1) {
		state.shutter = 0;
	} else if (state.shutter === -2) {
		state.shutter = shutters.length - 1;
	}
	state.shutter = Math.max(0, state.shutter);
	state.shutter = Math.min(shutters.length - 1, state.shutter);

	state.iso = Math.max(0, state.iso);
	state.iso = Math.min(isos.length - 1, state.iso);
	state.ev = Math.max(0, state.ev);
	state.ev = Math.min(evs.length - 1, state.ev);
	state.sensor = Math.max(0, state.sensor);
	state.sensor = Math.min(sensors.length - 1, state.sensor);
	state.length = Math.max(0, state.length);
	state.length = Math.min(lengths.length - 1, state.length);
	state.distance = Math.max(0, state.distance);
	state.distance = Math.min(distances.length - 1, state.distance);
}

function do_sample_picture(iso, aperture, shutter)
{
	// we assume that picture exposure is perfect, like sunny-16
	var ev2 = K * aperture * aperture / (iso * shutter);
	var ev = Math.log(ev2) / Math.log(2);
	console.log("sample EV " + ev + " (ISO " + iso + " f/" + aperture + " 1/" + (1/shutter) + "s)");
	if (ev !== ev) {
		return;
	}
	var i = find_nearest(evs, ev);
	if (i >= 0) {
		state.ev = i;
	}
	changed();
}

function recalculate_exposure(hint)
{
	cast_vars();

	// e.g. ISO 200 x EV 16 (4 times sunny 16)
	var tev = isos[state.iso] * Math.pow(2, evs[state.ev]) / K;
	// resolve 2^ev.iso = K.f^2/s, or tev=f^2/s
	var shutter, aperture;
	
	if (hint === "A") {
		// change shutter
		// s=f^2/tev
		shutter = Math.pow(apertures[state.aperture], 2) / tev;
		console.log("A shutter 1/" + 1 / shutter);
		state.shutter = find_nearest(shutters, shutter);
	} else if (hint === "S") {
		// change aperture
		// f = sqrt(tev * s)
		aperture = Math.sqrt(tev * shutters[state.shutter]);
		state.aperture = find_nearest(apertures, aperture);
		console.log("S aperture f/" + aperture);
	} else {
		// change both respecting priority
		for (var j = 0; j < 30; ++j) {
			var i;

			if (state.aperture_priority) {
				shutter = Math.pow(apertures[state.aperture], 2) / tev;
				console.log("shutter " + shutter + " ap " + state.aperture);

				i = find_nearest(shutters, shutter);

				if (i >= 0) {
					state.shutter = i;
					break;
				} else if (i == -1) {
					// less light
					state.aperture = Math.min(state.aperture + 1,
								apertures.length - 1);
					console.log("increasing aperture");
				} else {
					// more light
					state.aperture = Math.max(0, state.aperture - 1);
					console.log("decreasing aperture");
				}
			} else {
				aperture = Math.sqrt(tev * shutters[state.shutter]);
				console.log("aperture " + aperture + " sh " + state.shutter);
	
				i = find_nearest(apertures, aperture);
	
				if (i >= 0) {
					state.aperture = i;
					break;
				} else if (i == -1) {
					// more light
					state.shutter = Math.min(state.shutter + 1,
								shutters.length - 1);
					console.log("increasing shutter");
				} else {
					// less light
					state.shutter = Math.max(0, state.shutter - 1);
					console.log("decreasing shutter");
				}
			}
		}
	}
}

function changed(do_not_save)
{
	show_exposure();
	show_dof();
	if (! do_not_save) {
		save_cb(JSON.stringify(state));
	}
}

function stepper(code)
{
	return [-2, -1, 1, 2][code];
}

function do_step(state_var, list, code)
{
	var j;
	if (code === "plus") {
		j = state[state_var] + 1;
	} else if (code === "minus") {
		j = state[state_var] - 1;
	} else {
		j = parseInt(code);
	}
	j = Math.max(0, j);
	j = Math.min(list.length - 1, j);
	state[state_var] = j;
}

function input_aperture(code)
{
	do_step("aperture", apertures, code);
	state.aperture_priority = true;
	recalculate_exposure("A");
	changed();
}

function input_shutter(code)
{
	do_step("shutter", shutters, code);
	state.aperture_priority = false;
	recalculate_exposure("S");
	changed();
}

function input_iso(code)
{
	do_step("iso", isos, code);
	recalculate_exposure("");
	changed();
}

function input_ev(code)
{
	do_step("ev", evs, code);
	recalculate_exposure("");
	changed();
}

function input_distance(code)
{
	do_step("distance", distances, code);
	changed();
}

function input_length(code)
{
	do_step("length", lengths, code);
	changed();
}

function model_input(type, code)
{
	if (type === "aperture") {
		input_aperture(code);
	} else if (type === "shutter") {
		input_shutter(code);
	} else if (type === "iso") {
		input_iso(code);
	} else if (type === "ev") {
		input_ev(code);
	} else if (type === "distance") {
		input_distance(code);
	} else if (type === "sensor") {
		state.sensor = code;
	} else if (type === "focallength") {
		input_length(code);
	}
}

function model_sample_picture(iso, aperture, shutter)
{
	// Set EV, and other settings if possible, based on
	// picture, probably coming from EXIF data

	niso = parseFloat("" + iso);
	while (niso !== niso && iso.length > 0) {
		iso = iso.substr(1);
		niso = parseFloat("" + iso);
	}
	iso = niso;
	aperture = parseFloat("" + aperture);
	shutter = parseFloat("" + shutter);

	if ((iso !== iso) || (iso < 1) || (iso > 999999) ||
			(aperture !== aperture) || (aperture < 0.1) || (aperture > 200) ||
			(shutter !== shutter) || (shutter > 10000) || (shutter < (1/50000))) {
		console.log("Picture data invalid or outside bounds " + iso + " " +
				aperture + " " + shutter);
		return;
	}

	do_sample_picture(iso, aperture, shutter);
}

function find_nearest(a, value)
{
	var res = (value < a[0]) ? -1 : -2;

	for (var j = 0; j < (a.length - 1); ++j) {
		if (value >= a[j] && value <= a[j + 1]) {
			res = j;
			if (value > ((a[j] + a[j + 1]) / 2)) {
				res = j + 1;
			}
			break;
		}
	}

	return res;
}

function find_names()
{
	var j;

	state.aperture_name = state.shutter_name = state.ev_name = "Hi";
	state.ev_name = state.ev_text = state.iso_name = "Err";

	j = state.aperture;
	if (j >= 0) {
		state.aperture_name = sapertures[j];
	} else if (j === -1) {
		state.aperture_name = "Lo";
	}
	j = state.shutter;
	if (j >= 0) {
		state.shutter_name = sshutters[j];
	} else if (j === -1) {
		state.shutter_name = "Lo";
	}
	j = state.iso;
	if (j >= 0) {
		state.iso_name = sisos[j];
	}
	j = state.ev;
	if (j >= 0) {
		state.ev_name = sevs[j];
		state.ev_text = evtexts["" + evs[j]];
	}
}

function show_exposure()
{
	find_names();

	show("aperture", state.aperture_name, state.aperture);
	show("shutter", state.shutter_name, state.shutter);
	show("ev", state.ev_name, state.ev);
	show("evdesc", state.ev_text, state.ev);
	show("iso", state.iso_name, state.iso);
}

function show(type, text, index)
{
	setTimeout(function () {
		result_cb(type, text, index);
	}, result_delay);
}

function model_length_list()
{
	return [lengths, slengths, state.length];
}

function model_sensor_list()
{
	return [crops, sensors,  state.sensor];
}

function model_aperture_list()
{
	return [apertures, sapertures, state.aperture];
}

function model_shutter_list()
{
	return [shutters, sshutters, state.shutter];
}

function model_iso_list()
{
	return [isos, sisos, state.iso];
}

function model_ev_list()
{
	return [evs, sevs, state.ev];
}

function model_distance_list()
{
	return [distances, sdistances, state.distance];
}

function format_distance(d)
{
	var txt = d.toFixed(2);
	if (txt.charAt(txt.length - 1) === "0") {
		if (txt.charAt(txt.length - 2) === "0") {
			txt = txt.substr(0, txt.length - 3);
		} else {
			txt = txt.substr(0, txt.length - 1);
		}
	}
	txt += "m";

	if (d > 500 || d < 0) {
		txt = "∞";
	}

	return txt;
}

function show_dof()
{
	var distance = distances[state.distance];
	// var crop = crops[state.sensor];
	var length = lengths[state.length] / 1000;
	var aperture;
	if (state.aperture == -1) {
		aperture = apertures[0];
	} else if (state.aperture < -1 || state.aperture >= apertures.length) {
		aperture = apertures[apertures.length - 1];
	} else {
		aperture = apertures[state.aperture];
	}

	// Zeiss Formula: diagonal divided by 1730
	// 35mm film's diagonal is 43mm
	var confusion = 0.043 / 1730;

	var near = (distance * length * length) / 
			(length * length +
				aperture * confusion *
					(distance - length));

	var far = (distance * length * length) / 
			(length * length -
				aperture * confusion *
					(distance - length));

	show("near",  format_distance(near), near);
	show("far",  format_distance(far), far);
	show("focallength", slengths[state.length], state.length);
	show("distance", sdistances[state.distance], state.distance);
	// show("sensor", state.sensor);
}
