from distutils.core import setup
import py2exe
import sys
import os
from glob import glob

my_path = os.path.dirname( os.path.realpath(__file__) )

includes = [ 'cStringIO', 'ctypes', 'StringIO', 'threading', 'array', 'copy', 'errno', 'httplib', 'locale', 'math', \
                'multiprocessing', 'os', 'platform', 'pprint', 'Queue', 'random', 're', 'select', 'shutil', \
                'socket', 'string', 'subprocess', 'sys', 'threading', 'time', 'traceback', 'urllib', 'tempfile', \
                'types', 'gc', 'signal', 'msvcrt', 'struct',  'zipimport', 'zlib', '_winreg', "logging", "logging.handlers" \
            ]

dist_dir = "i386"
copy_args = sys.argv[ 1 : ]
if '--amd64' in copy_args:
    copy_args.remove( '--amd64' )
    dist_dir = "amd64"
    data_files = [ ( "Microsoft.VC90.CRT",  glob( os.path.join( my_path, r"vcredist\amd64\Microsoft.VC90.CRT\*.*" ) ) ) ]
else:
    data_files = [ ( "Microsoft.VC90.CRT",  glob( os.path.join( my_path, r"vcredist\x86\Microsoft.VC90.CRT\*.*" ) ) ) ]


options = { 'py2exe': { \
                        'dll_excludes': ['w9xpopen.exe'], \
                        'unbuffered' : True, \
                        'includes' : includes, \
                        'dist_dir' : dist_dir, \
                        'optimize' : 2, \
                        'bundle_files' : 1,\
                } \
            }

if '--win' in copy_args:
    copy_args.remove( '--win' )
    app_name="main_window"
    setup( script_args = copy_args, options = options, windows = [ { "script": "main.py" } ], zipfile = None, data_files=data_files )
else:
    app_name="main_console"
    setup( script_args = copy_args, options = options, console = [ { "script": "main.py" } ], zipfile = None )
dest = os.path.join( my_path, 'i386', app_name + '.exe' ) 
os.remove( dest )
os.rename( os.path.join( my_path, 'i386', 'main.exe' ), dest )






