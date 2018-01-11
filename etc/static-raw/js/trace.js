(function() {
    "use strict";

    var generateDomFor = function (call, stack) {
        stack.s.push(0);
        var callDom = $('<span/>').css('min-width', (call.name.length / 1.5) + 'em');
        if (call.type && call.type === "crash") {
            callDom.addClass("crash");
        }

        var textDom = $('<kbd/>').text(call.name);
        textDom.appendTo(callDom);

        if (call.subCalls.length > 0) {
            var subcallsDom = $('<samp/>');
            for (var i = 0; i < call.subCalls.length; i++) {
                generateDomFor(call.subCalls[i], stack).appendTo(subcallsDom)
            }
            subcallsDom.appendTo(callDom)
        }

        if (stack.s.length > stack.n) {
            stack.n = stack.s.length;
        }
        stack.s.pop();

        return callDom;
    };

    $.fn.trace = function(call) {
        var zis = $(this);
        zis.addClass("trace trace-initialized");
        zis.html("");

        var stack = {
            s: [],
            n: 0
        };
        var dom = generateDomFor(call, stack);
        dom.appendTo(zis);
        var height = stack.n * 1.5;
        zis.css("height", (height + 1.5) + "em");
        // $(zis.find('span')[0]).css("top", (height - 1.5) + "em");
    };

    $('.trace:not(.trace-initialized)').each(function() {
        var trace = JSON.parse($(this).text());
        $(this).trace(trace);
    });

})();