(function() {
    var sw = document.getElementById("languageSwitcher");
    if (!sw) return;

    var languages = $(sw).data("languages").split('|')

    new Vue({
       el: sw,
       data: {
           languages: languages,
           codeLanguage: $.fn.cookie("codeLanguage") || languages[0]
       },
       mounted: function() {
           this.codeLanguageChanged();
       },
       methods: {
           codeLanguageChanged: function() {
               var lang = this.$data.codeLanguage;
               $.fn.cookie("codeLanguage", lang);

               var langClass = lang.toLowerCase();

               var preBlocks = this.$el.parentNode.getElementsByTagName('article')[0].getElementsByTagName('pre');
               for (var i = 0; i < preBlocks.length; i++) {
                   var codeBlock = preBlocks[i].getElementsByTagName('code');
                   if (codeBlock.length === 1) {
                       var block = $(preBlocks[i])
                       if (codeBlock[0].classList.contains(langClass)) block.show();
                       else block.hide();
                   }
               }
           }
       }
    });
})();
