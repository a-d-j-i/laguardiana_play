/*$( "#main_overlay" ).overlay({
    top: 100,
    left: "center",
    mask: {
        color: "#000",
        loadSpeed: 1,
        closeSpeed: 1,
        opacity: 0.9
    },
    closeOnClick: false,
    closeOnEsc: false,
    speed: "fast",
    oneInstance: true
});
*/

var current_alert = undefined;
var current_overlay = undefined;

function showAlert( a ) {
    console.log( "showAlert start" + a );
    if ( current_alert != a ) {
        $( ".alerta_info" ).hide();
        $( a ).show();
        current_alert = a;
    }

    if ( current_overlay ) {
        return;
    }
    current_overlay = $( "#main_overlay" ).overlay({
        top: 100,
        left: "center",
        mask: {
            color: "#000",
            loadSpeed: 1,
            closeSpeed: 1,
            opacity: 0.9
        },
        closeOnClick: false,
        closeOnEsc: false,
        speed: "fast",
        oneInstance: true
    });
    current_overlay = $( "#main_overlay" ).overlay();
    console.log( "load overlay" + a );
    current_overlay.load();
}

function closeAlert() {
    if ( current_overlay ) {
        current_overlay.close().onClose( function(){
            current_overlay = undefined;
        });
    }
}

function showAlertOld( a ) {
    if ( current_alert == a ) {
        return;
    }
    console.log( "showAlert start" + a );
    if ( current_overlay ) {
        /*        console.log( "close overlay 1 " + a + " " + current_alert );
        current_overlay.close().onClose( function() {
            console.log( "onClose overlay 1 " + a + " " + current_alert );
            showAlert( a );
        } );
        current_alert = a;*/
        return;
    }
    current_alert = a;
    current_overlay = $( a ).overlay({
        top: 100,
        left: "center",
        mask: {
            color: "#000",
            loadSpeed: 0.1,
            closeSpeed: 0.1,
            opacity: 0.9
        },
        closeOnClick: false,
        closeOnEsc: false,
        loadSpeed: 0.1,
        closeSpeed: 0.1,
        speed: 0.1,
        oneInstance: true
    });
    current_overlay = $( a ).overlay();
    console.log( "load overlay" + a );
    current_overlay.load();
/*    $( "#overlay_contents" ).html( $( a ).html() );
    $( a ).find( "a" ).each( function() {
        var id = $( this ).attr( "id" );
        if ( id ) {
            $( "#overlay_contents" ).find( "#" + id ).click( function () {
                $( a ).find( "#" + id ).click()
            });
        }
    });*/
};

function closeAlertOld( a ) {
    if ( current_alert != a ) {
        return;
    }
    console.log( "close overlay 2 " + a + " " + current_alert );
    current_alert = undefined;
    current_overlay.close().onClose( function(){
        current_overlay = undefined;
    });
}

/*function testAlert() {
    var ov = $( "#main_overlay" ).overlay();
    
    if ( current_alert ) {
        if ( ov.isOpened() ) {
            return;
        } else {
            ov.load();
        }
    } else {
        if ( ov.isOpened() ) {
            ov.close();
        } else {
            return;
        }
    }
}*/
// Call refresh every 1.5 seconds
//setInterval( testAlert, 100);


var waiting = false;
var lastStatus = "false";

function doneRefresh() {
    waiting = false;
};

var doRefresh = function() {
    doneRefresh();
};

function refresh() {
    if ( waiting ) {
        return;
    }
    waiting = true;
    doRefresh();
};
// Call refresh every 1.5 seconds
setInterval(refresh, 500);
refresh();

