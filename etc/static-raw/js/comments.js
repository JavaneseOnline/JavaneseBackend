(function() {
    'use strict';

    function serialize(obj) {
        var pairs = [];
        for (var key in obj) {
            var val = obj[key]
            if (val != null) {
                pairs.push(encodeURIComponent(key) + '=' + encodeURIComponent(val))
            }
        }
        return pairs.join('&')
    }

    function removeComment(comments, id) {
        for (var i = 0; i < comments.length; i++) {
            var c = comments[i];
            if (c.id === id) {
                c.removed = true;
                c.text = null;
                c.canRemove = null;
            }
        }
    }

    Vue.component('v-comments-form', {
        props: ['vType', 'vId', 'vParentId'],
        template: '#commentsFormTemplate',
        data: function() {
            return {
                sending: false,
                text: ''
            }
        },
        computed: {
            empty: function() {
                return this.text.length === 0;
            }
        },
        mounted: function() {
            var tf = this.$el.getElementsByClassName('mdl-textfield');
            if (tf.length > 0)
                componentHandler.upgradeElement(tf[0]);
        },
        methods: {
            send: function(e) {
                e.preventDefault();
                this.sending = true;

                var form = e.target;
                var vue = this
                // from form.js
                ajax(
                    form.getAttribute('method'), form.getAttribute('action'), serialize({
                        type: this.vType,
                        id: this.vId,
                        parentId: this.vParentId,
                        text: this.text
                    }), null,
                    function(data) {
                        vue.$emit('comment-added', JSON.parse(data));
                        vue.text = ''; // fixme: this breaks custom placeholder
                    }, function(e) {
                        snackbar(form.dataset.errorMessage); // from form.js
                    }, function() {
                        vue.sending = false
                    }
                )
            }
        }
    });

    function lz(n) {
        return n >= 0 && n < 10 ? ('0' + n) : n;
    }

    var now = new Date();
    Vue.component('v-comment', {
        props: ['vComment', 'vType', 'vId', 'vUsers'],
        template: '#commentTemplate',
        data: function() {
            return {
                answering: false, // todo: save text when opening-closing
                removing: false
            };
        },
        computed: {
            compiledCommentText: function() {
                return marked(this.vComment.text, { sanitize: true }) // marked.js
            },
            readableCreationDate: function() {
                var d = new Date(this.vComment.added);
                var time = lz(d.getHours()) + ':' + lz(d.getMinutes());
                return d.getFullYear() === now.getFullYear() && d.getMonth() === now.getMonth() && d.getDate() === now.getDate()
                    ? time : time + ' ' + lz(d.getDate()) + '.' + lz(1 + d.getMonth()) + '.' + d.getFullYear();
            },
            commenterAvatarUrl: function() {
                var comment = this.vComment;
                var sourceUsers = this.vUsers[comment.authorSrc];
                if (sourceUsers == null) return null;

                var user = sourceUsers[comment.authorId];
                if (user == null) return null;

                return user.avatarUrl;
            }
        },
        methods: {
            reply: function() {
                this.answering = !this.answering;
            },
            onCommentAdded: function(data) {
                this.vComment.answers.push(data);
                this.answering = false;
            },
            onCommentRemoved: function(id) {
                removeComment(this.vComment.answers, id);
            },
            remove: function(ev) {
                if (confirm('Пути назад нет.')) {
                    this.removing = true;
                    var action = ev.target.getAttribute('formAction');
                    var vue = this;
                    ajax('delete', action, serialize({ id: this.vComment.id }), null,
                        function() {
                            vue.$emit('comment-removed', vue.vComment.id);
                        },
                        function() {
                            snackbar(ev.target.dataset.errorMessage); // from form.js
                        },
                        function() {
                            vue.removing = false;
                        }
                    )
                }
            },
            enter: function(el, done) {
                $.scrollTo({
                    endY: $(el).position().top - 100 /* exact science! */,
                    duration: 350
                });
                done();
            }
        }
    });

    var commentsContainers = document.getElementsByClassName('comments')
    for (var i = 0; i < commentsContainers.length; i++) {
        var container = commentsContainers[i];
        var content = JSON.parse(container.innerText);
        var current = content.current;
        var users = {};

        new Vue({
            el: container,
            template: '#commentsTemplate',
            data: {
                type: content.type,
                id: content.id,
                comments: content.comments,
                /*[
                    {
                        "id":"e62cbd9d-2756-43c9-b4fa-832bbaf4d738",
                        "authorSrc":"GitHub", "authorId":"Miha-x64",
                        "text":"zzz" | "removed":true,
                        "added":1546968100000,
                        "answers": [ ... ]
                    },
                    ...
                ]*/

                users: users
                /*{
                    source: {
                        id: {
                            displayName: string,
                            avatarUrl: string,
                            pageUrl: string
                        }
                    }
                }*/
            },
            mounted: function() {
                /*this.$nextTick(function () {
                    gatherSourcesAndIds(this.comments, this.users);
                    console.log(this.users);
                })*/
            },
            methods: {
                onCommentAdded: function(data) {
                    this.comments.push(data);
                },
                onCommentRemoved: function(id) {
                    removeComment(this.comments, id);
                }
            }
        });
        content = null;
        current = null;
    }
})()
