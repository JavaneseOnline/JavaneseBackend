$('.sandbox').each(function() {
    'use strict';
    var $sandbox = $(this);

    var dialog = document.getElementById('sandbox_reportError');
    if (!dialog.showModal) {
        dialogPolyfill.registerDialog(dialog);
    }
    dialog.querySelector('.close').addEventListener('click', function() {
        dialog.close();
    });

    /*var sandbox = */new Vue({
        el: this, // .sandbox
        data: {
            code: null,
            systemIn: null,
            editor: null,

            /**
             * enum class MessageType {
             *     Compiling, Compiled, In, Oot, Err, Exit, Deadline, CorrectSolution
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
        mounted: function() {
            var textarea = this.$el.getElementsByClassName('editor')[0];
            this.editor = CodeMirror(function(elt) {
                textarea.parentNode.replaceChild(elt, textarea);
            }, {
                value: textarea.value,
                lineNumbers: true,
                matchBrackets: true,
                indentUnit: 4,
                mode: "text/x-java",
                theme: "ambiance"
            });
        },
        methods: {
            run: function () {
                var app = this;
                app.messages = [];
                this.connection = new WebSocket('ws://' + location.host + '/sandbox/ws?task=' + $sandbox.data('task'));
                this.connection.onopen = function() {
                    app.running = true;
                    this.send(app.editor.getValue());
                };
                this.connection.onclose = function() {
                    app.running = false;
                };
                this.connection.onmessage = function(e) {
                    var data = JSON.parse(e.data);
                    var last = app.messages[app.messages.length-1];
                    if (data.type === 'Compiling') {
                        data.type = 'Status'
                        data.data = sandboxLocale.compiling;
                        app.messages.push(data)
                    } else if (data.type === 'Compiled') {
                        last.data += " " + sandboxLocale.compiled;
                    } else if (data.type === 'Exit') {
                        if (data.data !== '0') {
                            data.data = sandboxLocale.exitCode + " " + data.data;
                            app.messages.push(data);
                        }
                    } else if (data.type === 'NoVar') {
                        data.data = sandboxLocale.noRequiredVar.format(data.name, data.value);
                        app.messages.push(data);
                    } else if (data.type === 'NoEq') {
                        data.data = sandboxLocale.noRequiredEq.format(data.name, data.value);
                        app.messages.push(data);
                    } else if (data.type === 'NotMatches') {
                        data.data = sandboxLocale.notMatches;
                        app.messages.push(data);
                    } else if (data.type === 'IllegalOutput') {
                        data.data = sandboxLocale.illegalOutput + ' ' + data.data;
                        app.messages.push(data);
                    } else if (data.type === 'CorrectSolution') {
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
                form.code.value = this.editor.getValue();
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
