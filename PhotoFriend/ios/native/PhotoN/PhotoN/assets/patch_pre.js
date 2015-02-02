window = {};
document = {};
console = {};
window.console = console;

console.log = function (txt) {
    log_native(txt);
};

printf = window.printf = function (txt) {
    console.log(txt);
};

alert = window.alert = function (txt) {
    console.log(txt);
};

timeouts = {};
timeout_count = 0;

getTimeoutHandle = function ()
{
    if (timeout_count > 2000000000) {
        timeout_count = 0;
    }
    return ++timeout_count;
};

// Needs better testing (cancellation and intervals)

do_setTimeout = function (cb, to, recurrent)
{
    var handle = getTimeoutHandle();
    timeouts["" + handle] = {"cb": cb, "to": (recurrent ? to : 0)};
    setTimeout_native(to, handle);
    return handle;
};

setTimeoutCallback = function (handle)
{
    var record = timeouts["" + handle];
    if (record) {
        if (record.to === 0) {
            delete timeouts["" + handle];
        }
        if (record.cb) {
            record.cb();
        }
        if (record.to !== 0) {
            setTimeout_native(record.to, record.handle);
        }
    }
};

setTimeout = window.setTimeout = function (cb, to)
{
    return do_setTimeout(cb, to, false);
};

setInterval = window.setInterval = function (cb, to)
{
    return do_setTimeout(cb, to, true);
};

clearTimeout = window.clearTimeout = function (handle)
{
    delete timeouts["" + handle];
};

clearInterval = window.clearTimeout = function (handle)
{
    delete timeouts["" + handle];
};