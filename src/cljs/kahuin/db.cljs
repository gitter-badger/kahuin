(ns kahuin.db
  (:require [cognitect.transit :as t]))

(def lsk "kahuin")
(def keys-to-save
  [[:user :node-id]
   [:user :public-key]
   [:user :private-key]
   [:user :nick]
   [:subscriptions]
   [:kahuines]])

(def reader (t/reader :json))
(def writer (t/writer :json))

(defn select-nested-keys
  [m kss]
  (reduce #(assoc-in %1 %2 (get-in m %2)) {} kss))

(defn str->
  "Converts a transit encoded string from local storage or upload to a map."
  [s]
  (try (t/read reader s)
       (catch :default {})))

(defn ->str
  [state]
  "Serializes a state map to a transit encoded string."
  (->> (select-nested-keys state keys-to-save)
       (t/write writer)))

(defn ls->
  "Loads state from localstorage"
  []
  (print "Loading state")
  (some->> (.getItem js/localStorage lsk)
           (str->)))

(defn ->ls
  "Saves state to localstorage"
  [db]
  (print "Saving state")
  (.setItem js/localStorage lsk (->str db)))

(def default-db
  (merge {:user          nil
          :kahuines      []
          :subscriptions {}}
         (ls->)))