(ns day8.re-frame.trace.styles)

(defonce panel-styles "
#--re-frame-trace-- {
  background: white;
  color: black;
  font-family: 'courier new', monospace;
}
#--re-frame-trace-- tbody {
  color: #aaa;
}
#--re-frame-trace-- tr:nth-child(even) {
  background: aliceblue;
}
#--re-frame-trace-- .button {
  padding: 5px 5px 3px;
  margin: 5px;
  border-radius: 2px;
  cursor: pointer;
}
#--re-frame-trace-- .text-button {
  border-bottom: 1px dotted #888;
  font-weight: normal;
}
#--re-frame-trace-- .icon-button {
  font-size: 10px;
}
#--re-frame-trace-- button.tab {

}
#--re-frame-trace-- .tab {
  background: transparent;
  border-radius: 0;
  text-transform: uppercase;
  font-family: monospace;
  letter-spacing: 2px;
  margin-bottom: 0;
  padding-bottom: 4px;
  vertical-align: bottom;
}
#--re-frame-trace-- .tab.active {
  background: transparent;
  border-bottom: 3px solid lightblue;
  border-radius: 0;
  padding-bottom: 1px;
}
#--re-frame-trace-- ul.filter-items {
  list-style-type: none;
  padding: 0;
  margin: 0 -5px;
}
#--re-frame-trace-- .filter-items li {
  color: #333;
  background: #efefef;
  display: inline-block;
  font-size: 0.9em;
  margin:  5px;
}
#--re-frame-trace-- .filter-items li .filter-item-string {
  color: #616cdb;
}
#--re-frame-trace-- .icon {
  display: inline-block;
  width: 1em;
  height: 1em;
  stroke-width: 0;
  stroke: currentColor;
  fill: currentColor;
}
#--re-frame-trace-- .icon-remove {
  margin-left: 10px;
}
#--re-frame-trace-- select {
  background: white;
  font-family: 'courier new', monospace;
  font-size: 1em;
}
#--re-frame-trace-- .nav {
  background: #efeef1;
  color: #222;
}
#--re-frame-trace-- .panel-content-top {
  flex: 1;
}
#--re-frame-trace-- .panel-content-scrollable {
  padding: 10px;
  flex: 1 0 auto;
  height: 100%;
  overflow: auto;
}
")
