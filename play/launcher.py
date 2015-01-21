#!/usr/bin/python
# -*- coding: latin-1 -*-
import time
import sys
import subprocess
import httplib
import logging
from logging import handlers
import os
from _winreg import *

LOG_FILENAME = 'launcher.log'
my_logger = logging.getLogger( 'launcher' )
my_logger.setLevel( logging.DEBUG )
handler = logging.handlers.RotatingFileHandler( LOG_FILENAME, maxBytes = 2000000, backupCount = 15 )
my_logger.addHandler( handler )

def log( *msg ):
    print msg
    my_logger.debug( msg )


CAJERO = r"C:\Documents and Settings\usuario\Escritorio\cajero"
CHROME = r"C:\Documents and Settings\usuario\Configuración local\Datos de programa\Google\Chrome\Application\chrome.exe"
JAVA = r"C:\Archivos de programa\Java\jre7\bin\java.exe"
RUNNER = r"PlayRunner.jar"
JAVA_CMD = [ JAVA, "-Xmx1024M", "-XX:-UseSplitVerifier", "-Dfile.encoding=utf-8", "-XX:CompileCommand=exclude,jregex/Pretokenizer,next", "-jar", os.path.join( CAJERO, RUNNER ) ]
log( "JAVA cmd: ", JAVA_CMD )


if not os.path.exists( CAJERO ):
    log( "invalid app path ", CAJERO )
    sys.exit( 3 )
if not os.path.exists( JAVA ):
    log( "invalid play path ", JAVA )
    sys.exit( 4 )
if not os.path.exists( CHROME ):
    log( "invalid chrome path ", CHROME )
    sys.exit( 5 )

if len( sys.argv ) == 2:
    main_dir = os.path.dirname( os.path.abspath( sys.argv[ 0 ] ) )
    if  ( sys.argv[ 1 ] ).lower() == "install":
        key = OpenKey( HKEY_LOCAL_MACHINE, r'Software\Microsoft\Windows NT\CurrentVersion\Winlogon', 0, KEY_ALL_ACCESS )
        SetValueEx( key, "Shell", 0, REG_SZ, os.path.join( main_dir, "main.exe" ) )
        CloseKey( key )
    elif ( sys.argv[ 1 ] ).lower() == "uninstall":
        key = OpenKey( HKEY_LOCAL_MACHINE, r'Software\Microsoft\Windows NT\CurrentVersion\Winlogon', 0, KEY_ALL_ACCESS )
        SetValueEx( key, "Shell", 0, REG_SZ, "Explorer.exe" )
        CloseKey( key )
    else:
        log( "argv == 2 Usage %s { install | uninstall }" % sys.argv[ 0 ] )
        print "Usage %s { install | uninstall }" % sys.argv[ 0 ]
    sys.exit( 1 )
elif len( sys.argv ) != 1:
    log( "argv == 1 Usage %s { install | uninstall }" % sys.argv[ 0 ] )
    print "Usage %s { install | uninstall }" % sys.argv[ 0 ]
    sys.exit( 2 )
    
si = subprocess.STARTUPINFO()
#si.dwFlags = subprocess.STARTF_USESTDHANDLES | subprocess.STARTF_USESHOWWINDOW
si.dwFlags = subprocess.STARTF_USESHOWWINDOW
si.wShowWindow = subprocess.SW_HIDE
log( "running app" )
play_pid = subprocess.Popen( JAVA_CMD, cwd = CAJERO, startupinfo = si ).pid

log( "running app pid : ", play_pid )
status = 500
for i in range( 10 ):
    while status != 200 and status != 302:
        try:
            conn = httplib.HTTPConnection( 'localhost', port = 9000 )
            conn.request( "GET", "/" )
            r1 = conn.getresponse()
            status = r1.status
            conn.close()
            #print "Wait...", status
        except:
            pass
    # wait 10 seconds until start for dev mode.
    log( "failed connecting, retry" )
    time.sleep( 1 )
    
  
log( "running chrome" )
chrome_pid = subprocess.Popen( [ CHROME, "--kiosk", "http://localhost:9000" ], cwd = CAJERO ).pid
log( "launcher done" )

