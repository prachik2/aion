#!/bin/bash 


# TODO: need a more intelligent way to add aion_api to classpath
env EVMJIT="-cache=1" java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5055 -Xms4g  \
        -cp "./lib/*:./lib/libminiupnp/*:./mod/*:aion_api/pack/modAionApi-v0.1.7.fe02037-2018-06-11.jar:aion_api/lib/*" org.aion.AionGraphicalFrontEnd "$@"
