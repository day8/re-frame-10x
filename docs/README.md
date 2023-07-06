# Usage

## App DB path expansions

re-frame-10x preserves path expansions by using the JSONML 
that is rendered, rather than the actual data path. This has the advantage 
of being feasible, but the disadvantage that if the HTML layout changes, 
then that can trigger expansion changes. This means that if the map keys 
change ordering (say when switching from an ArrayMap to a HashMap), then 
different items will be expanded/contracted.
