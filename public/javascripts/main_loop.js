var waiting = false;
var lastStatus = "false";
var alertStack = new Array();
var currentAlert = false;
function doneRefresh() {
    waiting = false;
};
var doRefresh = function() {
    doneRefresh();
};
function showAlert( a ){
    if ( currentAlert == a ) {
        return;
    }
    if ( currentAlert ) {
        alertStack.push( currentAlert );
        $( currentAlert ).overlay().close();  
    } 
    currentAlert = a;
    $( currentAlert ).overlay().load();
};
function closeAlert( a ) {
    // remove from stack
    n = new Array();
    do {
        i = alertStack.shift();
        if ( i != a ) {
            n.unshift( i )
        }
    } while( i );
    alertStack = n;
        
    if ( currentAlert == a ) {
        $( currentAlert ).overlay().close();
        currentAlert = alertStack.pop();
        $( currentAlert ).overlay().load();
    }
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

