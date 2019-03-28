#!/usr/bin/env bash

set -e

# Delete all generated sources
rm -r ./gen-src/

# Clean beforehand to remove anything left over from previous runs
# then create the inline dependencies.
lein with-profile mranderson do clean, inline-deps

# Create directories. If BSD cp had --parents like GNU coreutils
# we wouldn't need to do this, and could just add --parents to the cp below.
mkdir -p ./gen-src/day8/re_frame_10x/inlined_deps

# Copy the source dependencies into the src tree.
cp -r target/srcdeps/day8/re_frame_10x/inlined_deps ./gen-src/day8/re_frame_10x/
