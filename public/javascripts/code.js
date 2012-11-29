$(function() {

    // default value

    $("[placeholder]").defaultValue();

    // teclados
	
    var focused_input = null;
    var valid_inputs = "input:text, input:password, textarea";
    $(valid_inputs).focus(function() {
        focused_input = $(this);
    });

    $(".keyboard .element.key").bind("click", function() {
        if (!focused_input || (focused_input && !$(focused_input).is(":visible"))) {
            focused_input = $("input:text:visible, input:number:visible" );
            focused_input.focus();
        }
        if (focused_input) {
            focused_input.keydown();
            var key_value = $(this).attr("data-key");
            switch (key_value) {
                case "{none}":
                    break;
                case "{backspace}":
                    focused_input.val(focused_input.val().substr(0, focused_input.val().length - 1));
                    break;
                case "{accept}":
                    var type = $(this).attr("data-type");
                    switch (type) {
                        case "qwerty":
                            var tracked_input = $("#popup_keyboard_qwerty_tracked_input").val();
                            var tracked_value = $("#popup_keyboard_qwerty_input").val();
                            $("#" + tracked_input).val(tracked_value);
                            $("#popup_keyboard_qwerty").hide();
                            break;
                    }
                    break;
                default:
                    focused_input.val(focused_input.val() + key_value);
            }
            focused_input.focus();
            focused_input[0].selectionStart = focused_input[0].selectionEnd = focused_input.val().length;
            focused_input.keyup();
        }
    });

    $(".popup_keyboard").focus(function() {
        var tracked_input = $(this);
        var type = $(this).attr("data-type");
        jQuery.fx.off = true;
        switch (type) {
            case "qwerty":
                $("#popup_keyboard_qwerty_tracked_input").val($(this).attr("id"));
                $("#popup_keyboard_qwerty").show(1,function() {
                    Elastic.refresh();
                });
                $("#popup_keyboard_qwerty_input").val(tracked_input.val()).focus();
                caret_to_end(document.getElementById("popup_keyboard_qwerty_input"));
                break;
        }
    });
    
    
    // focus me

    $(".focus_me").focus();

    // alertas

    $(".alerta").overlay({
        top: 100,
        left: "center",
        mask: {
            color: "#000",
            loadSpeed: 100,
            opacity: 0.9
        },
        closeOnClick: false,
        closeOnEsc: false,
        speed: "fast"
    });
    
    $(".alerta_cerrar").bind("click", function() {
        cerrar_alerta();
    });

});

// alertas

/**
 * tipos de alerta: info, success, error, continue
 */
function abrir_alerta(tipo) {
    $("#alerta").removeClass().addClass(tipo);
    $("#alerta").overlay().load();
}

function cerrar_alerta() {
    $("#alerta").overlay().close();
}


// cursor

function caret_to_end(el) {
    if (typeof el.selectionStart == "number") {
        el.selectionStart = el.selectionEnd = el.value.length;
    } else if (typeof el.createTextRange != "undefined") {
        el.focus();
        var range = el.createTextRange();
        range.collapse(false);
        range.select();
    }
}