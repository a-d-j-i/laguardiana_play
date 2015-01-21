from distutils.core import setup
import py2exe
import sys
from glob import glob

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
    data_files = [ ( "Microsoft.VC90.CRT",  glob( r"F:\laguardiana\py2exe\vcredist\amd64\Microsoft.VC90.CRT\*.*" ) ) ]
else:
    data_files = [ ( "Microsoft.VC90.CRT",  glob( r"F:\laguardiana\py2exe\vcredist\x86\Microsoft.VC90.CRT\*.*" ) ) ]





options = { 'py2exe': { \
                        'dll_excludes': ['w9xpopen.exe'], \
                        'unbuffered' : True, \
                        'includes' : includes, \
                        'dist_dir' : dist_dir, \
                        'optimize' : 2, \
                        'bundle_files' : 1,\
                } \
            }


setup( script_args = copy_args, options = options, console = [ { "script": "main.py" } ], zipfile = None )
#setup( script_args = copy_args, options = options, windows = [ { "script": "main.py" } ], zipfile = None, data_files=data_files )






