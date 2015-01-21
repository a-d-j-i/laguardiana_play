#!/usr/bin/python
import os
import sys
import runpy



if __name__ == '__main__':

    main_dir = os.path.dirname( os.path.abspath( sys.argv[ 0 ] ) )
    sys.path.insert( 0, main_dir )
    args = sys.argv[ 1 : ]
    sys.argv = [ os.path.join( main_dir, "launcher.py" ) ]
    sys.argv.extend( args )
    runpy.run_path( sys.argv[ 0 ], run_name = '__main__' )
        
