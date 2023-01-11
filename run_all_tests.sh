#!/usr/bin/env bash
sbt clean scalafmtAll scalafixAll compile coverage test it:test coverageReport
