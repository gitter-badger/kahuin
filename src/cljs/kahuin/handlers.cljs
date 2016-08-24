(ns kahuin.handlers
  (:require [re-frame.core :as re-frame :refer [register-handler dispatch]]
            [kahuin.db :as kdb]
            [kahuin.network.ecc :as ecc]
            [kahuin.network.peers :as peers]
            [kahuin.network.dht :as dht]
            [cljs-time.core :as t]))

(def non-saving [(when ^boolean js/goog.DEBUG re-frame/debug)
                 re-frame/trim-v])
(def saving [(re-frame/after kdb/->ls)
             (when ^boolean js/goog.DEBUG re-frame/debug)
             re-frame/trim-v])

(defn validating-handler
  [validator-opts handler]
  (fn [db v]
    (handler (apply kdb/validate-db db validator-opts) v)))
(def validating (partial validating-handler []))
(def validating-keys-generated (partial validating-handler [:keys-generated]))
(def validating-keys-imported (partial validating-handler [:keys-imported]))
(def validating-keys (partial validating-handler [:keys-generated :keys-imported]))

(register-handler
  :initialize-db
  [saving]
  (fn [_ _]
    (let [db kdb/initial-db]
      (when (get-in db [:user :node-id])
        (dispatch [:connect-peer (:user db)]))
      db)))

(register-handler
  :set-active-panel
  non-saving
  (fn [db [active-panel]]
    (assoc db :active-panel active-panel)))

(register-handler
  :error
  non-saving
  (fn [db [e m]]
    (case e
      :peer-connection (try (peers/reconnect! (:user db))
                            (assoc-in db [:pending :peer-connection] true)
                            (catch :default _ nil))
      (js/alert (str "Error" e " - " m)))
    db))


;;; USER

(register-handler
  :create-user
  [validating-keys-generated saving]
  (fn [db [nick]]
    (do (ecc/generate-keys! nick)
        (assoc-in db [:pending :user-creation] true))))

(register-handler
  :connect-peer
  [validating-keys non-saving]
  (fn [db [user]]
    (-> db
        (assoc :user (merge user
                            (peers/create-peer! (:node-id user))))
        (assoc-in [:pending :user-creation] false))))

(register-handler
  :peer-connected
  non-saving
  (fn [db [id brokering-id]]
    (print id " - " brokering-id)))

(register-handler
  :change-nick
  [validating-keys saving]
  (fn [db [nick]]
    (assoc-in db [:user :nick] nick)))

(register-handler
  :download-keys
  [validating-keys non-saving]
  (fn [db []]
    (do (let [encoded (->> (kdb/->str db)
                           (str "data:application/transit+json;charset=utf-8,")
                           (js/encodeURI))]
          (doto
            (js/document.createElement "a")
            (aset "href" encoded)
            (aset "download" (str "kahuin-" (get-in db [:user :node-id]) ".tj"))
            (js/document.body.appendChild)
            (.click)
            (js/document.body.removeChild)))
        db)))

(register-handler
  :upload-keys
  [validating-keys-imported saving]
  (fn [db [data]]
    (let [new-state (kdb/str-> data)]
      (-> (merge db
                 new-state
                 (peers/create-peer! (get-in new-state [:user :user-id])))
          (assoc-in [:pending :peer-connection] true)))))

(register-handler
  :keys-generated
  [validating-keys saving]
  (fn [db [kp]]
    (assoc-in db [:user :keypair] kp)))

(register-handler
  :log-out
  [validating saving]
  (fn [_ []]
    kdb/default-db))

;;; SUBS

(register-handler
  :add-subscription
  [validating-keys saving]
  (fn [db [id]]
    (do (peers/with-connection (:user db) id identity)
        (assoc-in db [:pending :add-subscription id] true))))

(register-handler
  :connection-opened
  [validating-keys saving]
  (fn [db [id conn]]
    (-> db
        (assoc-in [:user :connections id] conn)
        (assoc-in [:subscriptions id] {:last-seen (js/Date.)})
        (dissoc [:pending :add-subscription]))))

(register-handler
  :remove-subscription
  [validating-keys saving]
  (fn [db [id]]))

;;; Ks

(register-handler
  :love-k
  [validating-keys saving]
  (fn [db [k]]))

(register-handler
  :hide-k
  [validating-keys non-saving]
  (fn [db [k]]))

(register-handler
  :load-reference
  [validating-keys non-saving]
  (fn [db [k]]))

(register-handler
  :reply-to-k
  [validating-keys saving]
  (fn [db [k text]]))

;;; MESSAGES

(register-handler
  :send-message
  [validating-keys non-saving]
  (fn [db [target data]]
    (ecc/sign-message! (:user db) {:data data :target-id target})
    db))

(register-handler
  :message-signed
  [validating-keys non-saving]
  (fn [db [msg]]
    (peers/send! (:user db) (:target-id msg) (dissoc msg :target-id))
    db))

(register-handler
  :message-received
  [validating-keys non-saving]
  (fn [db [id _ msg]]
    (ecc/verify-message! id msg)
    db))

(register-handler
  :message-verified
  [validating-keys non-saving]
  (fn [db [msg]]
    (print "recv" msg)
    (dht/parse-message db msg)))