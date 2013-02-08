$( "#main_overlay" ).overlay({
    top: 100,
    left: "center",
    mask: {
        color: "#000",
        loadSpeed: 100,
        opacity: 0.9
    },
    closeOnClick: false,
    closeOnEsc: false,
    speed: "fast",
    oneInstance: true
});

var current_alert = undefined;
function showAlert( a ) {
    if ( current_alert == a ) {
        return;
    }
    current_alert = a;
    $( "#overlay_contents" ).html( $( a ).html() );
    $( a ).find( "a" ).each( function() {
        var id = $( this ).attr( "id" );
        if ( id ) {
            $( "#overlay_contents" ).find( "#" + id ).click( function () {
                $( a ).find( "#" + id ).click()
            });
        }
    });
};

function closeAlert( a ) {
    if ( current_alert != a ) {
        return;
    }
    current_alert = undefined;
}

function testAlert() {
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
}
// Call refresh every 1.5 seconds
setInterval( testAlert, 100);


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

