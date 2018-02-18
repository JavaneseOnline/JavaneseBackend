// $(function() {
    /*$('a[href^=#]').click(function(){
     var hash = $(this).attr('href').substr(1);
     scrollToAnchor(hash);
     return true;
     });*/
    /*function scrollToAnchor(anchor) {
        scrollToElement($('body').find("a[name=" + anchor + "]"))
    }
    function scrollToElement($el) {
        scrollTo($el.offset().top);
    }
    function scrollTo(y) {
        if (typeof y === 'undefined') {
            y = 0;
        }
        $('html,body').scrollTo({endY:y, duration:500});
    }*/
// });

// «up» button
var $up = $(document.getElementById('up-button')).click(function() {
    $('html,body').scrollTo();
});
$(window).on('scroll', function(e) {
    if (e.pageY > 300) {
        if ($up.css('display') === 'none') {
            $up.fadeIn();
        }
    } else {
        if ($up.css('opacity') == 1) {
            $up.fadeOut();
        }
    }
    scrollTop = e.pageY;
});

// unfocus link when clicked
$('a').click(function() {
    $(this).blur();
});

// switch to tab if location.hash set
function onHashSet() {
    var s = window.location.hash
    var u = document
    var k = 'is-active'
    if (s.length > 1) {
        var t = u.querySelector('a[href="' + s + '"]');
        var p = u.getElementById(s.substr(1));
        if (t == null || p == null) {
            return;
        }
        $(p.parentNode).find('.' + k).removeClass(k);
        $(t).addClass(k);
        $(p).addClass(k);
        window.location.hash = s;
    }
}

window.addEventListener("hashchange", onHashSet, false);
document.addEventListener("DOMContentLoaded", onHashSet, false);

// change hash when tab selected
$('.mdl-tabs__tab').click(function() {
    window.location.hash = $(this).attr('href').substr(1);
});
