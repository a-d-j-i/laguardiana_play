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
    oneInstance: false,
    onLoad : function(event) {
        working = false;
        if ( ! current_alert ) {
            working = true;
            this.close();
        }
    }, 
    onClose: function(event) {
        var o = this;
        setTimeout(function(){
            working = false;
            if ( current_alert ) {
                working = true;
                o.load();
            }
        },500);
    }
});

var current_alert = undefined;
var working = false;
function showAlert( a ) {
   
    if ( current_alert == a ) {
        return;
    }
    $( "#overlay_contents").html( $( a ).html() );
    $( a ).find( "a" ).each( function() {
        var id = $( this ).attr( "id" );
        if ( id ) {
            $( "#overlay_contents").find( "#" + id ).click( function () {
                $( a ).find( "#" + id ).click()
            });
        }
    });
    current_alert = a;
    if ( ! working ) {
        working = true;
        $( "#main_overlay" ).overlay().load();
    }
};

function closeAlert( a ) {
    if ( current_alert != a ) {
        return;
    }
    current_alert = undefined;

    if ( ! working ) {
        working = true;
        $( "#main_overlay" ).overlay().close();
    }
}

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

