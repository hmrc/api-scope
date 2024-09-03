#!/usr/bin/env bash

sbt "run -Drun.mode=Dev -Dhttp.port=9690 $*"
