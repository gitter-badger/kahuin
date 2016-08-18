(ns kahuin.db-test
  (:require [cljs.test :refer-macros [use-fixtures deftest testing is]]
            [kahuin.db :as db]))

(def payload {:user          {:node-id     1
                              :public-key  :k
                              :private-key nil
                              :nick        "test"}
              :subscriptions [{:last-seen (js/Date. 0)}]
              :kahuines      {}})

(use-fixtures :once {:before #(db/->ls payload)})

(deftest transit-encoding
  (is (= (db/str-> (db/->str payload)) payload)))

(deftest localstorage
  (is (= payload (db/ls->))))
