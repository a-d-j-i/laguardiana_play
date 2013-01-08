var waiting = false;
var lastStatus = "false";
var alertStack = new Array();
var working = false;
var currentAlert = undefined;
function doneRefresh() {
    waiting = false;
};
var doRefresh = function() {
    doneRefresh();
};
function showAlert( a ) {
    var oldAlert = alertStack.pop();
    if ( oldAlert && oldAlert != a ) {
        alertStack.push( oldAlert );
    }
    alertStack.push( a );
    if ( working ) {
        return;
    }
    working = true;
    if ( currentAlert ) {
        if ( currentAlert == a ) {
            working = false;
            return;
        }
        $( currentAlert ).overlay().onClose( function() {
            currentAlert = undefined;
            working = false;
            var b = alertStack.pop();
            if ( b ) {
                alertStack.push( b );
                showAlert( b );
            }
        }).close();  
    } else {
        $( a ).overlay().onLoad(function(){
            currentAlert = a;
            working = false;
            var b = alertStack.pop();
            if ( b ) {
                alertStack.push( b );
                showAlert( b );
            }
        }).load();
    }
};

function closeAlert( a ) {

    // remove from stack
    var n = new Array();
    var i;
    do {
        i = alertStack.shift();
        if ( i && i != a ) {
            n.unshift( i )
        }
    } while( i );
    alertStack = n;

    if ( working || ! currentAlert || currentAlert != a ) {
        return;
    }
    working = true;

    $( currentAlert ).overlay().onClose( function() {
        currentAlert = undefined;
        working = false;
        var b = alertStack.pop();
        if ( b ) {
            alertStack.push( b );
            showAlert( b );
        }
    } ).close();
}




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

