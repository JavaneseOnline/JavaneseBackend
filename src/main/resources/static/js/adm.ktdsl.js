$('.ktdsl-button').click(function(){
    var target = document.getElementById('field_' + $(this).data('field'));
    var insertionPolicy = $(this).data('insertionPolicy');

    var dialog = document.getElementById('ktdsl_dialog');
    if (!dialog.showModal) {
        dialogPolyfill.registerDialog(dialog);
        dialog.querySelector('.close').addEventListener('click', function() {
            dialog.close();
        });
        document.getElementById('ktdsl_form').addEventListener('submit', function(e) {
            e.preventDefault();
            $.post('/admin/plugin/ktdsl/', 
                $(document.getElementById('ktdsl_form')).serialize(),
                function(d) {
                    switch (insertionPolicy) {
                        case "SHOW":
                            dialog.querySelector('textarea').value = d;
                            break;
                        
                        case "INSERT":
                            insertAtCursor(target, d);
                            break;
                        
                        case "REPLACE":
                            target.value = d;
                            break;
                        
                        default:
                            alert("unsupported insertion policy");
                    }
                })
        });
    }
    dialog.showModal();
});