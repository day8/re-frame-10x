#!/usr/bin/env bash

lein do clean
lein with-profile mranderson source-deps
# Then delete the META-INF directories
rm -r target/srcdeps/mranderson047/reagent/v0v8v0-alpha2/META-INF
rm -r target/srcdeps/mranderson047/re-frame
cp -r target/srcdeps/mranderson047 src
