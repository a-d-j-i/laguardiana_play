@echo off

python -OO setup.py py2exe
python -OO setup.py py2exe --win
rem python setup.py py2exe --amd64
