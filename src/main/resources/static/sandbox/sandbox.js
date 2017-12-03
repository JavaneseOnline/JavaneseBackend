$('.sandbox').each(function() {
    'use strict';
    var $sandbox = $(this);
    
    /*var editor = ace.edit($sandbox.find('.editor')[0]);
    editor.setTheme('ace/theme/twilight');
    editor.setOptions({
        enableBasicAutocompletion: true,
        enableSnippets: true,
        enableLiveAutocompletion: false
    });
    editor.getSession().setMode('ace/mode/java');*/

    var textarea = $sandbox.find('.editor')[0];
    var editor = CodeMirror(function(elt) {
        textarea.parentNode.replaceChild(elt, textarea);
    }, {
        value: textarea.value,
        lineNumbers: true,
        matchBrackets: true,
        indentUnit: 4,
        mode: "text/x-java",
        theme: "ambiance"
    });

    var dialog = document.getElementById('sandbox_reportError');
    if (!dialog.showModal) {
        dialogPolyfill.registerDialog(dialog);
    }
    dialog.querySelector('.close').addEventListener('click', function() {
        dialog.close();
    });

    /*var sandbox = */new Vue({
        el: $sandbox[0],
        data: {
            /**/code: null,
            systemIn: null,

            /**
             * enum class MessageType {
             *     STATUS, IN, OUT, ERR, EXIT, DEADLINE, CORRECT_SOLUTION
             * }
             * {@see .runtime-status, .runtime-in, .runtime-out, and so on in sandbox.scss}
             *
             * message format:
             * {
             *     "type": MessageType,
             *     "data": String,
             * }
             */
            messages: [],

            running: false,
            connection: null
        },
        methods: {
            run: function () {
                var app = this;
                app.messages = [];
                this.connection = new WebSocket('ws://' + location.host + '/sandbox/ws?task=' + $sandbox.data('task'));
                this.connection.onopen = function() {
                    app.running = true;
                    this.send(editor.getValue());
                };
                this.connection.onclose = function() {
                    app.running = false;
                };
                this.connection.onmessage = function(e) {
                    var data = JSON.parse(e.data);
                    console.log(data);
                    var last = app.messages[app.messages.length-1];
                    if (last !== undefined && data.type === 'STATUS' && last.type === 'STATUS') {
                        last.data += " " + data.data;
                    } else if (data.type === 'EXIT') {
                        if (data.data !== '0') {
                            data.data = sandboxLocale.exitCode + " " + data.data;
                            app.messages.push(data);
                        }
                    } else if (data.type === 'NO_VAR') {
                        data.data = sandboxLocale.noRequiredVar.format(data.name, data.value);
                        app.messages.push(data);
                    } else if (data.type === 'NO_EQ') {
                        data.data = sandboxLocale.noRequiredEq.format(data.name, data.value);
                        app.messages.push(data);
                    } else if (data.type === 'NOT_MATCHES') {
                        data.data = sandboxLocale.notMatches;
                        app.messages.push(data);
                    } else if (data.type === 'ILLEGAL_OUTPUT') {
                        data.data = sandboxLocale.illegalOutput + ' ' + data.data;
                        app.messages.push(data);
                    } else if (data.type === 'CORRECT_SOLUTION') {
                        data.data = sandboxLocale.correctSolution;
                        app.messages.push(data);
                    } else {
                        app.messages.push(data);
                    }
                };
                this.connection.onerror = function(e) {
                    console.error(e);
                    app.messages.push({type: "ERR", data: sandboxLocale.webSocketError});
                }
            },
            reportError: function() {
                var form = dialog.getElementsByTagName('form')[0]
                form.taskId.value = $sandbox.data('task');
                form.code.value = editor.getValue();
                dialog.showModal();
            },
            send: function(e) {
                e.preventDefault();
                this.connection.send(this.systemIn);
                this.messages.push({
                    type: "IN",
                    data: this.systemIn
                });
                this.systemIn = "";
            }
        }
    });
});

if (!String.prototype.format) {
    String.prototype.format = function() {
        var args = arguments;
        return this.replace(/{(\d+)}/g, function(match, number) {
            return typeof args[number] != 'undefined' ? args[number] : match;
        });
    };
}