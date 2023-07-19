;; TODO: make this a standalone library

(ns day8.re-frame-10x.tools.highlight-hiccup
  (:require [clojure.walk :as walk]
            [rewrite-clj.zip :as rz]
            [rewrite-clj.node.token :refer [SymbolNode TokenNode]]
            [rewrite-clj.node.whitespace :refer [WhitespaceNode NewlineNode CommaNode]]
            [rewrite-clj.node.keyword :refer [KeywordNode]]
            [rewrite-clj.node.stringz :refer [StringNode]]
            [rewrite-clj.node.seq :refer [SeqNode]]
            [day8.re-frame-10x.styles :as styles]
            [day8.re-frame-10x.inlined-deps.re-frame.v1v1v2.re-frame.core :as rf]
            [day8.re-frame-10x.panels.event.subs :as event.subs]))

(def clj-core-macros #{'and 'binding 'case 'catch 'comment 'cond 'cond-> 'cond->> 'condp 'def
                       'defmacro 'defn 'defn- 'defmulti 'defmethod 'defonce 'defprotocol 'deftype
                       'do 'dotimes 'doseq 'dosync 'fn 'for 'future 'if 'if-let 'if-not 'import 'let
                       'letfn 'locking 'loop 'ns 'or 'proxy 'quote 'recur 'set! 'struct-map 'sync 'throw
                       'try 'when 'when-first 'when-let 'when-not 'when-some 'while})

(defn selected-style [{:keys [position]}]
  (when @(rf/subscribe [::event.subs/highlighted? position])
    (styles/clj-highlighted)))

(defmulti form type)

(defmethod form :default [{:keys [string-value] :as node}]
  [:span.clj-unknown {:data-clj-node (str (type node))}
   string-value])

(defmulti token-form (comp type :value))

(defmethod token-form (type true) [{:keys [string-value]}]
  [:code.clj__boolean {:class (styles/clj-boolean)}
   string-value])

(defmethod token-form (type 0) [{:keys [string-value]}]
  [:code.clj__number {:class (styles/clj-number)}
   string-value])

(defmethod token-form (type nil) [{:keys [string-value]}]
  [:code.clj__nil {:class (styles/clj-nil)}
   string-value])

(defmethod token-form :default [{:keys [string-value]}]
  [:span.clj__token string-value])

(defmethod form TokenNode [node]
  [token-form node])

(defmethod form CommaNode [_]
  [:span.clj__comma ","])

(defmulti seq-form :tag)

(defmethod seq-form :default [_]
  [:code.clj__unknown])

(defmethod seq-form :list [{:keys [children] :as node}]
  (into [:code.seq {:class [(styles/clj-seq)
                            (selected-style node)]}]
        (concat ["("] children [")"])))

(defmethod seq-form :vector [{:keys [children] :as node}]
  (into [:code.clj__seq {:class [(selected-style node)]}]
        (concat ["["] children ["]"])))

(defmethod seq-form :map [{:keys [children] :as node}]
  (into [:code.clj__map {:class [(selected-style node)]}]
        (concat ["{"] children ["}"])))

(defmethod seq-form :set [{:keys [children] :as node}]
  (into [:code.seq {:class [(styles/clj-seq)
                            (selected-style node)]}]
        (concat ["#{"] children ["}"])))

(defmethod form SeqNode [node]
  (seq-form node))

(defmethod form SymbolNode [{:keys [value string-value] :as node}]
  [:code.clj__symbol {:class [(if (clj-core-macros value)
                                (styles/clj-core-macro)
                                (styles/clj-symbol))
                              (selected-style node)]}
   string-value])

(defmethod form WhitespaceNode [{:keys [whitespace]}]
  [:code.clj__whitespace
   whitespace])

(defmethod form NewlineNode [_]
  [:br])

(defmethod form KeywordNode [{:keys [k] :as node}]
  [:code.clj__keyword {:class [(styles/clj-keyword)
                               (selected-style node)]}
   (str k)])

(defmethod form StringNode [{:keys [lines]}]
  [:code.clj__string {:class (styles/clj-string)}
   \" (apply str lines) \"])

(defn str->hiccup [s]
  (let [positional-ast
        (-> s
            (rz/of-string {:track-position? true})
            (rz/postwalk #(rz/edit* % assoc
                                    :position (rz/position %)))
            rz/node)]
    (walk/postwalk #(cond-> % (record? %) form) positional-ast)))
