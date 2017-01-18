#!/usr/bin/python
# vim: set fileencoding=utf-8
import time
import sys
import subprocess
import httplib
import os
import socket
from _winreg import *

CAJERO = r"C:\Documents and Settings\usuario\Escritorio\cajero"
CHROME = r"C:\Documents and Settings\usuario\Configuración local\Datos de programa\Google\Chrome\Application\chrome.exe"
JAVA = r"C:\Archivos de programa\Java\jre7\bin\java.exe"
RUNNER = r"PlayRunner.jar"
JAVA_CMD = [ JAVA, "-Xmx1024M", "-XX:-UseSplitVerifier", "-Dfile.encoding=utf-8", "-XX:CompileCommand=exclude,jregex/Pretokenizer,next", "-jar", os.path.join( CAJERO, RUNNER ) ]

PSQL_PORT=5432
PSQL_HOST='127.0.0.1'

if not os.path.exists( CAJERO ):
    print "invalid app path ", CAJERO
    sys.exit( 3 )
if not os.path.exists( JAVA ):
    print "invalid play path ", JAVA
    sys.exit( 4 )
if not os.path.exists( CHROME ):
    print "invalid chrome path ", CHROME
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
        print "Usage %s { install | uninstall }" % sys.argv[ 0 ]
    sys.exit( 1 )
elif len( sys.argv ) != 1:
    print "Usage %s { install | uninstall }" % sys.argv[ 0 ]
    sys.exit( 2 )


def list_processes():
    import platform
    if platform.system() == 'Windows':
        attrs=['pid', 'name', 'exe', 'cpu_times', 'cpu_percent', 'memory_info', 'memory_percent',
                    'nice', 'ionice', 'io_counters', 'num_ctx_switches', 'num_handles', 'num_threads']
    else:
        attrs=['pid', 'name', 'exe', 'cpu_times', 'cpu_percent', 'memory_info', 'memory_percent',
                    'nice', 'ionice', 'io_counters', 'num_ctx_switches', 'num_fds', 'num_threads']
    while True:
        procs = []
        for proc in psutil.process_iter():
            try:
                pinfo = proc.as_dict(attrs=attrs)
            except psutil.NoSuchProcess:
                pass
            else:
                procs.append( pinfo )
        data = {"curr_time": time.strftime("%a, %d %b %Y %H:%M:%S"), "cpu_times" : psutil.cpu_times(),
                    "cpu_times_percent" : psutil.cpu_times_percent(), "virtual_memory" : psutil.virtual_memory(),
                    "swap_memory" : psutil.swap_memory(), "disk_usage" : psutil.disk_usage("/"), "disk_io_counters" : psutil.disk_io_counters(),
                    "procs" : procs }
        with open(LOG_FILE, "a+b") as f:
            pickle.dump( data, f)
        time.sleep(3)


# fork a child and list processes_there
if True:
    import threading
    print "listing processes in the child"
    t = threading.Thread(target=list_processes)
    t.daemon = True
    t.start()


print "checking for database"
for i in range( 100 ):
    s = socket.socket( socket.AF_INET, socket.SOCK_STREAM )
    try:
        ret = s.connect_ex( ( PSQL_HOST, PSQL_PORT ) )
        if ret == 0:
            break
        else:
            # wait 10 seconds until start for dev mode.
            print "failed connecting to the database ", ret, " retry"
            time.sleep( 1 )
    finally:
        s.close()
print "checking for database done"

si = subprocess.STARTUPINFO()
#si.dwFlags = subprocess.STARTF_USESTDHANDLES | subprocess.STARTF_USESHOWWINDOW
si.dwFlags = subprocess.STARTF_USESHOWWINDOW
si.wShowWindow = subprocess.SW_HIDE
play_pid = subprocess.Popen( JAVA_CMD, cwd = CAJERO, startupinfo = si ).pid

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
    time.sleep( 1 )


chrome_pid = subprocess.Popen( [ CHROME, "--kiosk", "http://localhost:9000" ], cwd = CAJERO ).pid

