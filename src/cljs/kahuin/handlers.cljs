(ns kahuin.handlers
  (:require [re-frame.core :as re-frame :refer [register-handler dispatch]]
            [kahuin.db :as kdb]
            [kahuin.network.ecc :as ecc]
            [kahuin.network.peers :as peers]
            [kahuin.network.dht :as dht]
            [cljs-time.core :as t]))

(def save-middleware (re-frame/after kdb/->ls))
(def debug-middleware (when ^boolean js/goog.DEBUG re-frame/debug))
(def default-middleware [save-middleware
                         debug-middleware
                         re-frame/trim-v])

(register-handler
  :initialize-db
  default-middleware
  (fn [_ _]
    (let [db kdb/default-db]
      (when (get-in db [:user :node-id])
        (dispatch [:connect-peer (:user db)]))
      db)))

(register-handler
  :set-active-panel
  default-middleware
  (fn [db [active-panel]]
    (assoc db :active-panel active-panel)))

(register-handler
  :error
  default-middleware
  (fn [db [e m]]
    (case e
      :peer-connection (do (peers/reconnect! (:user db))
                           (assoc-in db [:pending :peer-connection] true))
      (js/alert (str "Error" e " - " m)))))


;;; USER

(register-handler
  :create-user
  default-middleware
  (fn [db [nick]]
    (do (ecc/generate-keys! nick)
        (assoc-in db [:pending :user-creation] true))))

(register-handler
  :connect-peer
  default-middleware
  (fn [db [user]]
    (-> db
        (assoc :user (merge user
                            (peers/create-peer! (:node-id user))))
        (assoc-in [:pending :user-creation] false))))

(register-handler
  :peer-connected
  default-middleware
  (fn [db [id brokering-id]]
    (print id " - " brokering-id)))

(register-handler
  :change-nick
  default-middleware
  (fn [db [nick]]
    (assoc-in db [:user :nick] nick)))

(register-handler
  :download-keys
  default-middleware
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
  default-middleware
  (fn [db [data]]
    (let [new-state (kdb/str-> data)]
      (-> (merge db
                 new-state
                 (peers/create-peer! (get-in new-state [:user :user-id])))
          (assoc-in [:pending :peer-connection] true)))))

(register-handler
  :keys-generated
  default-middleware
  (fn [db [kp]]
    (assoc-in db [:user :keypair] kp)))

(register-handler
  :log-out
  default-middleware
  (fn [db []]))

;;; SUBS

(register-handler
  :add-subscription
  default-middleware
  (fn [db [id]]
    (do (peers/with-connection (:user db) id identity)
        (assoc-in db [:pending :add-subscription id] true))))

(register-handler
  :connection-opened
  default-middleware
  (fn [db [id conn]]
    (-> db
        (assoc-in [:user :connections id] conn)
        (assoc-in [:subscriptions id] {:last-seen (js/Date.)})
        (dissoc [:pending :add-subscription]))))

(register-handler
  :remove-subscription
  default-middleware
  (fn [db [id]]))

;;; Ks

(register-handler
  :love-k
  default-middleware
  (fn [db [k]]))

(register-handler
  :hide-k
  default-middleware
  (fn [db [k]]))

(register-handler
  :load-reference
  default-middleware
  (fn [db [k]]))

(register-handler
  :reply-to-k
  default-middleware
  (fn [db [k text]]))

;;; MESSAGES

(register-handler
  :send-message
  default-middleware
  (fn [db [target data]]
    (ecc/sign-message! (:user db) {:data data :target-id target})
    db))

(register-handler
  :message-signed
  default-middleware
  (fn [db [msg]]
    (peers/send! (:user db) (:target-id msg) (dissoc msg :target-id))
    db))

(register-handler
  :message-received
  default-middleware
  (fn [db [id _ msg]]
    (ecc/verify-message! id msg)
    db))

(register-handler
  :message-verified
  default-middleware
  (fn [db [msg]]
    (print "recv" msg)
    (dht/parse-message db msg)))