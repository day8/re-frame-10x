(ns day8.re-frame.trace.graph-test
  (:require [day8.re-frame.trace.graph :as graph]
            [clojure.test :refer :all]))

(def t1
  '({:id 1, :operation :initialise-db, :type :event, :tags {:event [:initialise-db]}, :child-of nil}
     {:id 2, :operation "todomvc.core.wrapper", :type :render, :tags {:component-path "todomvc.core.wrapper", :reaction "rx2", :input-signals ("ra18")}, :child-of nil}
     {:id 5, :operation :sorted-todos, :type :sub/create, :tags {:query-v [:sorted-todos], :cached? false, :reaction "rx3"}, :child-of 4}
     {:id 4, :operation :todos, :type :sub/create, :tags {:query-v [:todos], :cached? false, :reaction "rx4"}, :child-of 3}
     {:id 7, :operation :sorted-todos, :type :sub/run, :tags {:query-v [:sorted-todos], :reaction "rx3", :input-signals ["ra5"]}, :child-of 6}
     {:id 6, :operation :todos, :type :sub/run, :tags {:query-v [:todos], :reaction "rx4", :input-signals ["rx3"]}, :child-of 3}
     {:id 3, :operation "todomvc.views.todo_app", :type :render, :tags {:component-path "todomvc.core.wrapper > todomvc.views.todo_app", :reaction "rx6", :input-signals ("rx4")}, :child-of nil}
     {:id 8, :operation "todomvc.views.task_entry", :type :render, :tags {:component-path "todomvc.core.wrapper > todomvc.views.todo_app > todomvc.views.task_entry", :reaction nil, :input-signals nil}, :child-of nil}
     {:id 9, :operation "todomvc.views.todo_input", :type :render, :tags {:component-path "todomvc.core.wrapper > todomvc.views.todo_app > todomvc.views.task_entry > todomvc.views.todo_input", :reaction "rx7", :input-signals ("ra19")}, :child-of nil}
     {:id 10, :operation "ReagentInput", :type :render, :tags {:component-path "todomvc.core.wrapper > todomvc.views.todo_app > todomvc.views.task_entry > todomvc.views.todo_input > ReagentInput", :reaction nil, :input-signals nil}, :child-of nil}
     {:id 13, :operation :todos, :type :sub/create, :tags {:query-v [:todos], :cached? true, :reaction "rx4"}, :child-of 12}
     {:id 14, :operation :showing, :type :sub/create, :tags {:query-v [:showing], :cached? false, :reaction "rx8"}, :child-of 12}
     {:id 12, :operation :visible-todos, :type :sub/create, :tags {:query-v [:visible-todos], :cached? false, :reaction "rx9"}, :child-of 11}
     {:id 16, :operation :todos, :type :sub/create, :tags {:query-v [:todos], :cached? true, :reaction "rx4"}, :child-of 15}
     {:id 15, :operation :all-complete?, :type :sub/create, :tags {:query-v [:all-complete?], :cached? false, :reaction "rx10"}, :child-of 11}
     {:id 17, :operation :all-complete?, :type :sub/run, :tags {:query-v [:all-complete?], :reaction "rx10", :input-signals ["rx4"]}, :child-of 11}
     {:id 19, :operation :showing, :type :sub/run, :tags {:query-v [:showing], :reaction "rx8", :input-signals ["ra5"]}, :child-of 18}
     {:id 18, :operation :visible-todos, :type :sub/run, :tags {:query-v [:visible-todos], :reaction "rx9", :input-signals ("rx4" "rx8")}, :child-of 11}
     {:id 11, :operation "todomvc.views.task_list", :type :render, :tags {:component-path "todomvc.core.wrapper > todomvc.views.todo_app > todomvc.views.task_list", :reaction "rx11", :input-signals ("rx10" "rx9")}, :child-of nil}
     {:id 20, :operation "ReagentInput", :type :render, :tags {:component-path "todomvc.core.wrapper > todomvc.views.todo_app > todomvc.views.task_list > ReagentInput", :reaction nil, :input-signals nil}, :child-of nil}
     {:id 21, :operation "todomvc.views.todo_item", :type :render, :tags {:component-path "todomvc.core.wrapper > todomvc.views.todo_app > todomvc.views.task_list > todomvc.views.todo_item", :reaction "rx12", :input-signals ("ra20" "ra20")}, :child-of nil}
     {:id 22, :operation "ReagentInput", :type :render, :tags {:component-path "todomvc.core.wrapper > todomvc.views.todo_app > todomvc.views.task_list > todomvc.views.todo_item > ReagentInput", :reaction nil, :input-signals nil}, :child-of nil}
     {:id 24, :operation :footer-counts, :type :sub/create, :tags {:query-v [:footer-counts], :cached? false, :reaction "rx13"}, :child-of 23}
     {:id 25, :operation :showing, :type :sub/create, :tags {:query-v [:showing], :cached? true, :reaction "rx8"}, :child-of 23}
     {:id 27, :operation :todos, :type :sub/create, :tags {:query-v [:todos], :cached? true, :reaction "rx4"}, :child-of 26}
     {:id 29, :operation :todos, :type :sub/create, :tags {:query-v [:todos], :cached? true, :reaction "rx4"}, :child-of 28}
     {:id 28, :operation :completed-count, :type :sub/create, :tags {:query-v [:completed-count], :cached? false, :reaction "rx14"}, :child-of 26}
     {:id 30, :operation :completed-count, :type :sub/run, :tags {:query-v [:completed-count], :reaction "rx14", :input-signals ["rx4"]}, :child-of 26}
     {:id 26, :operation :footer-counts, :type :sub/run, :tags {:query-v [:footer-counts], :reaction "rx13", :input-signals ("rx4" "rx14")}, :child-of 23}
     {:id 23, :operation "todomvc.views.footer_controls", :type :render, :tags {:component-path "todomvc.core.wrapper > todomvc.views.todo_app > todomvc.views.footer_controls", :reaction "rx15", :input-signals ("rx13" "rx8" "rx8" "rx8")}, :child-of nil}))

(deftest sub-graph-test
  (is (= {:links []
          :nodes [{:id    "rx4"
                   :r     10
                   :title ""
                   :group 2
                   :data  {:id   1
                           :tags {:cached?  false
                                  :reaction "rx4"}
                           :type :sub/create}}]}
         (graph/trace->sub-graph [{:id 1 :type :sub/create :tags {:cached? false :reaction "rx4"}}] []))))

(deftest dispose-view-test
  (is (= {:links []
          :nodes []}
         (graph/trace->sub-graph [{:id 1 :type :render :tags {:cached? false :reaction "rx4"}}
                                  {:id 2 :type :componentWillUnmount :tags {:reaction "rx4"}}] []))))
