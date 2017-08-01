(ns day8.re-frame.trace.styles)

(defonce panel-styles "
#--re-frame-trace-- tbody {
  color: #aaa;
}
#--re-frame-trace-- tr:nth-child(even) {
  background: aliceblue;
}
#--re-frame-trace-- .button
 {
  background: lightblue;
  padding: 5px 5px 3px;
  margin: 5px;
  border-radius: 2px;
  cursor: pointer;
}
#--re-frame-trace-- .icon-button {
  vertical-align: middle;
  font-size: 10px;
}
#--re-frame-trace-- .tab {
  background: transparent;
  border-bottom: 3px solid #eee;
  padding-bottom: 1px;
  border-radius: 0;
}
#--re-frame-trace-- .tab.active {
  background: transparent;
  border-bottom: 3px solid lightblue;
  padding-bottom: 1px;
  border-radius: 0;
}
#--re-frame-trace-- ul.filter-items {
  list-style-type: none;
  padding: 0;
  margin: 0 -5px;
}
#--re-frame-trace-- .filter-items li {
  background: lightblue;
  color: #666;
  display: inline-block;
  margin:  5px;
}
#--re-frame-trace-- .filter-items li .filter-item-string {
  color: #333;
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
")
