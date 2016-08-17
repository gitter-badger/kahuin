(ns kahuin.network.peers
  (:require [re-frame.core :as re-frame]
            [kahuin.config :as config]))

(goog-define peerjs-host "0.peerjs.com")
(goog-define peerjs-port 9000)

(defn- id->peer-id
  "Replaces invalid characters in id to make it peerjs-compatible"
  [id]
  (-> (str "kah" id)
      (clojure.string/replace #"x" "xx")
      (clojure.string/replace #"_" "xU")
      (clojure.string/replace #"-" "xD")))

(defn create-peer!
  "Creates a peer js object, by connecting to the host configured in the the peerjs-host and
  peerjs-port defines."
  [id]
  (let [p (js/Peer. (id->peer-id id) #js {:key   "ndytcfc5opdmfgvi"
                                          :host  peerjs-host
                                          :port  peerjs-port
                                          :debug (if config/debug? 3 0)})]
    (-> p
        (.on "connection" #(.on % "data" (fn [brokering-id]
                                           (re-frame/dispatch [:peer-connected id brokering-id]))))
        (.on "error" #(re-frame/dispatch [:error :peer-connection %])))
    {:handle p :connections {}}))

(defn reconnect!
  [user]
  (.reconnect (:handle user)))

(defn destroy!
  [user]
  (.destroy (:handle user)))

(defn connect!
  [user id]
  (let [conn (.connect (:handle user) (id->peer-id id))]
    (.on conn "open" #(re-frame/dispatch [:connection-opened id conn]))
    (.on conn "data" #(re-frame/dispatch [:message-received id conn %]))
    (.on conn "close" #(re-frame/dispatch [:connection-closed id conn %]))))

(defn send!
  "Send a message containing data from user to target-id. This is the last message sending step,
  data should be signed."
  [user target-id data]
  (.send (get-in user [:connections target-id]) data))