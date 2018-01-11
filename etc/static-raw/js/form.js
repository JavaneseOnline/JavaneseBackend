$(function() {
    $('form').submit(function(e) {
        e.preventDefault();

        var $form = $(this);
        var $submit = $form.find('[type=submit]').attr('disabled', 'disabled');

        var headers = {};
        headers[$("meta[name='_csrf_header']").attr("content")] =
            $("meta[name='_csrf']").attr("content");
        
        $.ajax({
            type:   $form.attr('method'),
            url:    $form.attr('action'),
            data:   $form.serialize(),
            headers: headers,
            processData: false,
            timeout: 10000,
            success: function(/*data, status, xhr*/) {
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
            error: function(/*xhr, errorType, error*/) {
                var errorMessage = $form.data('errorMessage');
                if (errorMessage) {
                    snackbar(errorMessage);
                }

                var errorCallback = $form.data('errorCallback');
                if (errorCallback) {
                    window[errorCallback]();
                }
            },
            complete: function(xhr, status) {
                var completeCallback = $form.data('completeCallback');
                if (completeCallback) {
                    if (window[completeCallback](status === 'success')) {
                        $submit.removeAttr('disabled');
                    } /* if callback returns false, do nothing */
                } else {
                    $submit.removeAttr('disabled');
                }
            }
        });
    });
});

function snackbar(message) {
    document.getElementById('toast-container').MaterialSnackbar.showSnackbar({
        message: message
    });
}