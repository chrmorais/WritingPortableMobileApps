function load_cb()
{
	return Gateway.getPrefs();
}

function save_cb(data)
{
	Gateway.savePrefs(data);
}

window.addEventListener("load", function (e) {
	init();
});
