#!/usr/bin/env bash

lein do clean
lein with-profile mranderson source-deps
# Then delete the META-INF directories
rm -r target/srcdeps/mranderson047/reagent/v0v7v0/META-INF
rm -r target/srcdeps/mranderson047/re-frame
rm -r target/srcdeps/mranderson047/garden/v1v3v3/META-INF
cp -r target/srcdeps/mranderson047 src
