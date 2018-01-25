#!/usr/bin/env bash

lein do clean
lein with-profile mranderson source-deps
cp -r target/srcdeps/mranderson047 src
# Then delete the META-INF directories

