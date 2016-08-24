(ns kahuin.db
  (:require [kahuin.network.encoding :as enc]
            [bouncer.core :refer [valid? validate]]
            [bouncer.validators :as v]
            [kahuin.network.ecc :as ecc]))

(def lsk "kahuin")
(def keys-to-save
  [[:user :node-id]
   [:user :public-key]
   [:user :private-key]
   [:user :nick]
   [:subscriptions]
   [:kahuines]])

(defn key-validator
  [& _]
  {"crv"      [v/required #(= "P-256" %)]
   "d"        [v/required enc/base64-43-chars?]
   "ext"      [v/required #(= "true" %)]
   "key_opts" [v/required [v/every #(contains? #{"sign" "verify"} %)]]
   "x"        [v/required enc/base64-43-chars?]
   "y"        [v/required enc/base64-43-chars?]})

(defn user-validator
  [& opts]
  (let [opts (set opts)
        cljs-keys (contains? opts :keys-generated)
        js-keys (contains? opts :keys-imported)
        key-v (key-validator opts)]
    (cond-> {:nick [v/string [v/max-count 50]]}
            js-keys (assoc :keypair [v/required ecc/keypair?])
            cljs-keys (assoc :node-id [v/required v/string])
            cljs-keys (assoc :public-key [v/required key-v])
            cljs-keys (assoc :private-key [v/required key-v]))))

(defn subscription-validator
  [& _]
  {})

(defn kahuin-validator
  [& _]
  {})

(defn db-validator
  [& opts]
  {:user          (user-validator opts)
   :subscriptions [v/required [v/every (subscription-validator opts)]]
   :kahuines      [v/required [v/every (kahuin-validator opts)]]})

(defn validate-db
  [db & opts]
  (let [[err db] (validate db (db-validator opts))]
    (when err (println "DB validation error: " err))
    db))

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