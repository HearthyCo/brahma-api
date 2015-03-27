#!/bin/sh
cd apidoc
open http://localhost:8789
python -m SimpleHTTPServer 8789
