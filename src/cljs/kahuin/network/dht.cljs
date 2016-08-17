(ns kahuin.network.dht
  (:require [kahuin.network.encoding :refer [base64->octets]]
            [kahuin.network.ecc :refer [on-sign on-verify]]
            [cljs.core.async :refer [put! chan <! >! timeout close!]]
            [re-frame.core :as re-frame]))

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

;;; FIXME: WIP below

(defn- send-msg!
  [target msg]
  (re-frame/dispatch [:send-message target msg]))

(defn parse-message
  [db msg]
  (case (first (:data msg))
    :ping (send-msg! (:source-id msg) [:pong])
    :find nil
    :store nil
    :get nil
    nil)
  db)

(defn- remove-node-from-bucket
  [node b]
  (remove #(= (:node-id b) (:node-id %)) node))

(defn- add-node-to-bucket
  [node b]
  (cons node b))

(defn- trim-bucket
  [kad b]
  (map #(do
         (remove-node-from-bucket % b)
         (send-msg! % [:ping]))
       (take (- (count b) *k*)
             (reverse b))))

(defn update-buckets
  [kad node]
  (when-let [bix (bucket-index (:node-id kad) (:node-id node))]
    (->> (get (:buckets kad) bix)
         (remove-node-from-bucket node)
         (add-node-to-bucket node)
         (trim-bucket kad))))