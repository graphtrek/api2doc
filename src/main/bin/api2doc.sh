#!/bin/bash

API2DOC_SCRIPT=$(readlink -f "$0")
API2DOC_APPDIR=$(dirname "$API2DOC_SCRIPT")

API2DOC_LIBDIR=${API2DOC_APPDIR}/lib/

CLASSPATH=""
for file in `find ${API2DOC_LIBDIR} -name *.jar`
do
  CLASSPATH=${CLASSPATH}:$file
done

java -classpath ${CLASSPATH} co.grtk.api2doc.Api2DocApplication "$@"