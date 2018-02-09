(ns day8.re-frame.trace.metamorphic-test
  (:require [clojure.test :refer :all])
  (:require [day8.re-frame.trace.metamorphic :as m]))

(defn trace-events [file]
  (->> (slurp (str "test-resources/" file))
       (clojure.edn/read-string {:readers {'utc    identity
                                           'object (fn [x] "<object>")}})
       (sort-by :id)))

(deftest parse-app-trace1-test
  (let [rt      (m/parse-traces m/initial-parse-state (trace-events "app-trace1.edn"))
        matches (:partitions rt)
        [m1 m2 m3 m4 m5 m6] matches]
    (is (= (count matches) 6))

    (is (= (m/beginning-id m1) 1))
    (is (= (m/ending-id m1) 34))
    (is (= (:operation (m/matched-event m1)) :bootstrap))

    (is (= (m/beginning-id m2) 35))
    (is (= (m/ending-id m2) 38))
    (is (= (:operation (m/matched-event m2)) :acme.myapp.events/boot-flow))

    (is (= (m/beginning-id m3) 39))
    (is (= (m/ending-id m3) 42))
    (is (= (:operation (m/matched-event m3)) :acme.myapp.events/init-db))

    (is (= (m/beginning-id m4) 43))
    (is (= (m/ending-id m4) 47))
    (is (= (:operation (m/matched-event m4)) :acme.myapp.events/boot-flow))

    (is (= (m/beginning-id m5) 48))
    (is (= (m/ending-id m5) 49))
    (is (= (:operation (m/matched-event m5)) :acme.myapp.events/start-intercom))

    (is (= (m/beginning-id m6) 50))
    (is (= (m/ending-id m6) 181))
    (is (= (:operation (m/matched-event m6)) :acme.myapp.events/success-bootstrap))))

#_(deftest parse-todomvc-trace1-test
    (let [rt      (m/parse-traces (trace-events "todomvc-trace1.edn"))
          matches (:matches rt)
          [m1 m2 m3 m4 m5 m6] matches]
      (is (= (count matches) 3))

      (is (= (m/beginning-id m1) 1))
      (is (= (m/ending-id m1) 34))
      (is (= (:operation (m/matched-event m1)) :bootstrap))

      (is (= (m/beginning-id m2) 35))
      (is (= (m/ending-id m2) 38))
      (is (= (:operation (m/matched-event m2)) :acme.myapp.events/boot-flow))

      (is (= (m/beginning-id m3) 39))
      (is (= (m/ending-id m3) 42))
      (is (= (:operation (m/matched-event m3)) :acme.myapp.events/init-db))

      (is (= (m/beginning-id m4) 43))
      (is (= (m/ending-id m4) 47))
      (is (= (:operation (m/matched-event m4)) :acme.myapp.events/boot-flow))

      (is (= (m/beginning-id m5) 48))
      (is (= (m/ending-id m5) 49))
      (is (= (:operation (m/matched-event m5)) :acme.myapp.events/start-intercom))

      (is (= (m/beginning-id m6) 50))
      (is (= (m/ending-id m6) 181))
      (is (= (:operation (m/matched-event m6)) :acme.myapp.events/success-bootstrap))))

(def sub-state '{:reaction-state {"rx48" {:subscription [:todos], :value ({:id 3, :title "def", :done false})}, "rx47" {:subscription [:sorted-todos], :value {3 {:id 3, :title "def", :done false}}, :previous-value {3 {:id 3, :title "def", :done false}}, :run? true, :order [:sub/run]}, "rx52" {:subscription [:visible-todos], :value ({:id 3, :title "def", :done false})}, "rx51" {:subscription [:showing], :value :all, :previous-value :all, :run? true, :order [:sub/run]}, "rx53" {:subscription [:all-complete?], :value false}, "rx56" {:subscription [:footer-counts], :value [1 0]}, "rx57" {:subscription [:completed-count], :value 0}}
                 :last-matched-id 0})

(def filtered-traces '({:id 91, :operation :initialise-db, :op-type :event, :tags {:event [:initialise-db], :app-db-before {:todos {3 {:id 3, :title "def", :done false}}, :showing :all}, :app-db-after {:todos {3 {:id 3, :title "def", :done false}}, :showing :all}}, :child-of nil, :start 647756.055, :duration 0.8649999999906868, :end 647756.93} {:id 92, :operation :sorted-todos, :op-type :sub/run, :tags {:query-v [:sorted-todos], :reaction "rx47", :input-signals ("ra24"), :value {3 {:id 3, :title "def", :done false}}}, :child-of nil, :start 647757.0300000001, :duration 0.08999999996740371, :end 647757.1250000001} {:id 93, :operation :showing, :op-type :sub/run, :tags {:query-v [:showing], :reaction "rx51", :input-signals ("ra24"), :value :all}, :child-of nil, :start 647757.16, :duration 0.05500000005122274, :end 647757.2150000001} {:id 94, :operation "todomvc.views.todo_app", :op-type :render, :tags {:component-path "todomvc.views.todo_app", :reaction "rx49", :input-signals ("rx48")}, :child-of nil, :start 647757.6200000001, :duration 0.2999999999301508, :end 647757.925} {:id 95, :operation :todos, :op-type :sub/create, :tags {:query-v [:todos], :cached? true, :reaction "rx48"}, :child-of 94, :start 647757.655, :duration 0.07499999995343387, :end 647757.735} {:id 96, :operation "todomvc.views.task_entry", :op-type :render, :tags {:component-path "todomvc.views.todo_app >  >  > todomvc.views.task_entry", :reaction nil, :input-signals nil}, :child-of nil, :start 647758.14, :duration 0.1150000001071021, :end 647758.26} {:id 97, :operation "todomvc.views.todo_input", :op-type :render, :tags {:component-path "todomvc.views.todo_app >  >  > todomvc.views.task_entry >  > todomvc.views.todo_input", :reaction "rx50", :input-signals ("ra59")}, :child-of nil, :start 647758.4750000001, :duration 0.24999999988358468, :end 647758.725} {:id 98, :operation "ReagentInput", :op-type :render, :tags {:component-path "todomvc.views.todo_app >  >  > todomvc.views.task_entry >  > todomvc.views.todo_input > ReagentInput", :reaction nil, :input-signals nil}, :child-of nil, :start 647758.87, :duration 0.11000000010244548, :end 647758.9850000001} {:id 99, :operation "todomvc.views.task_list", :op-type :render, :tags {:component-path "todomvc.views.todo_app >  >  > todomvc.views.task_list", :reaction "rx54", :input-signals ("rx52" "rx53")}, :child-of nil, :start 647759.36, :duration 0.4050000000279397, :end 647759.77} {:id 100, :operation :visible-todos, :op-type :sub/create, :tags {:query-v [:visible-todos], :cached? true, :reaction "rx52"}, :child-of 99, :start 647759.39, :duration 0.06499999994412065, :end 647759.4600000001} {:id 101, :operation :all-complete?, :op-type :sub/create, :tags {:query-v [:all-complete?], :cached? true, :reaction "rx53"}, :child-of 99, :start 647759.4800000001, :duration 0.044999999925494194, :end 647759.525} {:id 102, :operation "ReagentInput", :op-type :render, :tags {:component-path "todomvc.views.todo_app >  >  > todomvc.views.task_list >  > ReagentInput", :reaction nil, :input-signals nil}, :child-of nil, :start 647759.95, :duration 0.06500000006053597, :end 647760.015} {:id 103, :operation "todomvc.views.todo_item", :op-type :render, :tags {:component-path "todomvc.views.todo_app >  >  > todomvc.views.task_list >  >  > todomvc.views.todo_item", :reaction "rx55", :input-signals ("ra60" "ra60")}, :child-of nil, :start 647760.255, :duration 0.2500000001164153, :end 647760.51} {:id 104, :operation "ReagentInput", :op-type :render, :tags {:component-path "todomvc.views.todo_app >  >  > todomvc.views.task_list >  >  > todomvc.views.todo_item >  >  > ReagentInput", :reaction nil, :input-signals nil}, :child-of nil, :start 647760.8550000001, :duration 0.08999999996740371, :end 647760.9450000001} {:id 105, :operation "todomvc.views.footer_controls", :op-type :render, :tags {:component-path "todomvc.views.todo_app >  >  > todomvc.views.footer_controls", :reaction "rx58", :input-signals ("rx56" "rx51")}, :child-of nil, :start 647761.39, :duration 0.8050000000512227, :end 647762.2} {:id 106, :operation :footer-counts, :op-type :sub/create, :tags {:query-v [:footer-counts], :cached? true, :reaction "rx56"}, :child-of 105, :start 647761.43, :duration 0.2850000000325963, :end 647761.7200000001} {:id 107, :operation :showing, :op-type :sub/create, :tags {:query-v [:showing], :cached? true, :reaction "rx51"}, :child-of 105, :start 647761.7550000001, :duration 0.05999999993946403, :end 647761.8150000001} {:id 108, :operation nil, :op-type :raf, :tags nil, :child-of nil, :start 647771.8600000001, :duration 0.09999999997671694, :end 647771.9600000001} {:id 109, :operation nil, :op-type :raf-end, :tags nil, :child-of 108, :start 647771.885, :duration 0.005000000121071935, :end 647771.8950000001} {:id 110, :operation nil, :op-type :reagent/quiescent, :tags nil, :child-of 108, :start 647771.9400000001, :duration 0.005000000004656613, :end 647771.9500000001}))

(def new-matches '([{:id 91, :operation :initialise-db, :op-type :event, :tags {:event [:initialise-db], :app-db-before {:todos {3 {:id 3, :title "def", :done false}}, :showing :all}, :app-db-after {:todos {3 {:id 3, :title "def", :done false}}, :showing :all}}, :child-of nil, :start 647756.055, :duration 0.8649999999906868, :end 647756.93} {:id 110, :operation nil, :op-type :reagent/quiescent, :tags nil, :child-of 108, :start 647771.9400000001, :duration 0.005000000004656613, :end 647771.9500000001}]))

(deftest subscription-match-state-test
  (let [sub-state2 (m/subscription-match-state sub-state filtered-traces new-matches)]
    (is (= '({:last-matched-id 0
              :reaction-state {"rx47" {:order          [:sub/run]
                                       :previous-value {3 {:done  false
                                                           :id    3
                                                           :title "def"}}
                                       :run?           true
                                       :subscription   [:sorted-todos]
                                       :value          {3 {:done  false
                                                           :id    3
                                                           :title "def"}}}
                               "rx48" {:subscription [:todos]
                                       :value        ({:done  false
                                                       :id    3
                                                       :title "def"})}
                               "rx51" {:order          [:sub/run]
                                       :previous-value :all
                                       :run?           true
                                       :subscription   [:showing]
                                       :value          :all}
                               "rx52" {:subscription [:visible-todos]
                                       :value        ({:done  false
                                                       :id    3
                                                       :title "def"})}
                               "rx53" {:subscription [:all-complete?]
                                       :value        false}
                               "rx56" {:subscription [:footer-counts]
                                       :value        [1
                                                      0]}
                               "rx57" {:subscription [:completed-count]
                                       :value        0}}}
              {:last-matched-id 0
               :reaction-state {"rx47" {:order          [:sub/run]
                                        :previous-value {3 {:done  false
                                                            :id    3
                                                            :title "def"}}
                                        :run?           true
                                        :subscription   [:sorted-todos]
                                        :value          {3 {:done  false
                                                            :id    3
                                                            :title "def"}}}
                                "rx48" {:subscription [:todos]
                                        :value        ({:done  false
                                                        :id    3
                                                        :title "def"})}
                                "rx51" {:order          [:sub/run]
                                        :previous-value :all
                                        :run?           true
                                        :subscription   [:showing]
                                        :value          :all}
                                "rx52" {:subscription [:visible-todos]
                                        :value        ({:done  false
                                                        :id    3
                                                        :title "def"})}
                                "rx53" {:subscription [:all-complete?]
                                        :value        false}
                                "rx56" {:subscription [:footer-counts]
                                        :value        [1
                                                       0]}
                                "rx57" {:subscription [:completed-count]
                                        :value        0}}})
           sub-state2))))
