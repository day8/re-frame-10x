#!/usr/bin/env bash

# Clean beforehand to remove anything left over from previous runs
# then create the inline dependencies.
lein with-profile mranderson do clean, inline-deps

# Delete all generated sources
rm -r ./gen-src/

# Delete the extra META-INF directories, we can't use -delete here, as find's -delete only deletes empty directories.
# See https://unix.stackexchange.com/a/164882 for more details.
# We use -depth for a depth first search, so that we don't delete a directory and then try to traverse into it.
find ./target -type d -name META-INF -depth -exec rm -r "{}" \;

# Delete any remaining empty directories, see https://unix.stackexchange.com/a/24163 for more details.
find ./target -type d -depth -empty -delete;

# Create directories
mkdir -p ./gen-src/day8/re_frame_10x/inlined_deps

# Copy the source dependencies into the src tree.
cp -r target/srcdeps/day8/re_frame_10x/inlined_deps ./gen-src/day8/re_frame_10x/
