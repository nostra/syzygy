#!/bin/sh
# Automatically build on changes in source directory on Linux
while inotifywait `find . -iname '*.adoc'` ; do 
    for a in *.adoc ; do 
        asciidoctor -D/tmp/ $a
    done
    echo "Doc built to /tmp/"
done

