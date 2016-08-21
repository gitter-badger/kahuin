(ns kahuin.network.dht
  (:require [kahuin.network.encoding :refer [base64->octets]]
            [kahuin.network.ecc :refer [on-sign on-verify]]
            [re-frame.core :as re-frame]
            [cljs-time.core :as t]))

(def *k* 20)

(defn- take-while-plus-one
  "Note this does not keep the correct order."
  [pred coll]
  (let [[h t] (split-with pred coll)]
    (conj h (first t))))

(defn- octet-bucket
  "Bucket corresponding to this XOR distance. This calculates it for just one octet."
  [o]
  (if (zero? o) 0 (inc (Math/floor (Math/log2 o)))))

(defn- ->octets-if-string
  [id]
  (if (string? id) (base64->octets id) id))

(defn bucket-index
  "Returns the bucket corresponding to the distance between the ids, or nil
  if they are equal."
  [id0 id1]
  (let [id0 (->octets-if-string id0)
        id1 (->octets-if-string id1)
        l (* 8 *k*)
        b (->> (map bit-xor id0 id1)
               (map octet-bucket)
               (take-while-plus-one zero?)
               (reduce #(+ -8 %1 %2) (dec l)))]
    (if (neg? b) nil
                 b)))

(defn- send-msg!
  [target msg]
  (re-frame/dispatch [:send-message target msg]))

(defn trim-bucket
  [b]
  (->> (vals b)
       (sort-by :last-seen)
       (take (- (count b) *k*))
       (map #(send-msg! % [:ping]))))

(defn update-buckets
  [db node-id]
  (when-let [bix (bucket-index (get-in db :user :node-id) node-id)]
    (update-in db [:dht :buckets bix]
               #(trim-bucket (assoc % node-id {:node-id node-id :last-seen (t/now)})))))

(defn nodes-in-bucket
  [db ix]
  (get-in db (:dht :buckets ix)))

(defn nodes-in-buckets-closest-to
  [db target]
  (->> (get-in db [:dht :buckets])
       (map vals)
       (apply concat)
       (sort-by #(bucket-index target (:node-id %)))))

(defn closest-nodes
  [db target]
  (let [bix (bucket-index (get-in db :user :node-id) (:node-id target))
        res (nodes-in-bucket db bix)
        nodes-missing (- *k* (count res))]
    (concat res (when (pos? nodes-missing)
                  (take nodes-missing (nodes-in-buckets-closest-to db (:node-id target)))))))

(defn parse-message
  [db {data :data source-id :source-id}]
  (update-buckets db source-id)
  (case (first data)
    :ping (send-msg! source-id [:pong])
    :find (send-msg! source-id [:nodes (closest-nodes db (second data))])
    :store (let [[k v] (rest data)] (assoc-in db [:dht :values k] v))
    :get (send-msg! source-id (let [k (second data)
                                    v (get-in db [:dht :values k])]
                                (if v [:store k v]
                                      [:nodes (closest-nodes db k)])))
    nil)
  db)