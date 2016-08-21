(ns kahuin.db
  (:require [kahuin.network.encoding :as enc]))

(def lsk "kahuin")
(def keys-to-save
  [[:user :node-id]
   [:user :public-key]
   [:user :private-key]
   [:user :nick]
   [:subscriptions]
   [:kahuines]])


(defn select-nested-keys
  [m kss]
  (reduce #(assoc-in %1 %2 (get-in m %2)) {} kss))

(defn str->
  "Converts a transit encoded string from local storage or upload to a map."
  [s]
  (try (enc/transit-> s)
       (catch :default {})))

(defn ->str
  [state]
  "Serializes a state map to a transit encoded string."
  (->> (select-nested-keys state keys-to-save)
       (enc/->transit)))

(defn ls->
  "Loads state from localstorage"
  []
  (some->> (.getItem js/localStorage lsk)
           (str->)))

(defn ->ls
  "Saves state to localstorage"
  [db]
  (.setItem js/localStorage lsk (->str db)))

(def default-db
  {:user          nil
   :kahuines      []
   :subscriptions {}})

(def initial-db
  (merge default-db
         (ls->)))