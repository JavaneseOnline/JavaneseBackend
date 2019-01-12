$(function() {
    $('form.ajax-form').submit(function(e) {
        e.preventDefault();

        var $form = $(this);
        var $submit = $form.find('[type=submit]').attr('disabled', 'disabled');

        var headers = {};
        headers[$("meta[name='_csrf_header']").attr("content")] =
            $("meta[name='_csrf']").attr("content");
        
        ajax(
            $form.attr('method'), $form.attr('action'), $form.serialize(), headers,
            function(/*data , status, xhr*/) {
                var successMessage = $form.data('successMessage');
                if (successMessage) {
                    snackbar(successMessage);
                }

                var successCallback = $form.data('successCallback');
                if (successCallback) {
                    window[successCallback]();
                }

                var $dialog = $form.closest('dialog');
                if ($dialog.length === 1) {
                    $dialog[0].close();
                }
            },
            function(/*xhr, errorType, error*/) {
                var errorMessage = $form.data('errorMessage');
                if (errorMessage) {
                    snackbar(errorMessage);
                }

                var errorCallback = $form.data('errorCallback');
                if (errorCallback) {
                    window[errorCallback]();
                }
            },
            function(xhr, status) {
                var completeCallback = $form.data('completeCallback');
                if (completeCallback) {
                    if (window[completeCallback](status === 'success')) {
                        $submit.removeAttr('disabled');
                    } /* if callback returns false, do nothing */
                } else {
                    $submit.removeAttr('disabled');
                }
            }
        );
    });
});

function ajax(method, action, data, headers, success, error, complete) {
    $.ajax({
        type: method,
        url: action,
        data: data,
        headers: headers,
        processData: false,
        timeout: 10000,
        success: success,
        error: error,
        complete: complete
    });
}

function snackbar(message) {
    document.getElementById('toast-container').MaterialSnackbar.showSnackbar({
        message: message
    });
}