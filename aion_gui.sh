#!/bin/bash 


# TODO: need a more intelligent way to add aion_api to classpath
env EVMJIT="-cache=1" java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5055 -Xms4g  \
        -cp "./lib/*:./lib/libminiupnp/*:./mod/*:aion_api/pack/modAionApi.jar:aion_api/lib/*" org.aion.AionGraphicalFrontEnd "$@"
