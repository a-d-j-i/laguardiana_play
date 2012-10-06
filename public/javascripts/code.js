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
            $("input:text:visible").focus();
        }
        if (focused_input) {
            focused_input.keydown();
            var key_value = $(this).attr("data-key");
            switch (key_value) {
                case "{backspace}":
                    focused_input.val(focused_input.val().substr(0, focused_input.val().length - 1));
                    break;
                default:
                    focused_input.val(focused_input.val() + key_value);
            }
            focused_input.focus();
            focused_input[0].selectionStart = focused_input[0].selectionEnd = focused_input.val().length;
            focused_input.keyup();
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
