(ns kahuin.network.peers
  (:require [re-frame.core :as re-frame]
            [kahuin.network.encoding :as enc]
            [kahuin.config :as config]))

(goog-define host "0.peerjs.com")
(goog-define port 9000)
(goog-define path "/")

(defn- id->peer-id
  "Replaces invalid characters in id to make it peerjs-compatible"
  [id]
  (-> (str "kah" id)
      (clojure.string/replace #"x" "xx")
      (clojure.string/replace #"_" "xU")
      (clojure.string/replace #"-" "xD")))

(defn- peer-id->id
  [id]
  (assert (clojure.string/starts-with? id "kah"))
  (-> (subs id 3)
      (clojure.string/replace #"xx" "x")
      (clojure.string/replace #"xU" "_")
      (clojure.string/replace #"xD" "-")))

(defn- on-connect
  ([conn] (on-connect conn nil))
  ([conn f]
   (let [id (peer-id->id (.-peer conn))]
     (doto conn
       (.on "open" #(do (when f (f conn))
                        (re-frame/dispatch [:connection-opened id conn])))
       (.on "data" #(re-frame/dispatch [:message-received id conn (enc/transit-> %)]))
       (.on "close" #(re-frame/dispatch [:connection-closed id conn %]))))))

(defn create-peer!
  "Creates a peer js object, by connecting to the host configured in the the peerjs-host and
  peerjs-port defines."
  [id]
  (let [p (js/Peer. (id->peer-id id) #js {:key   "ndytcfc5opdmfgvi"
                                          :host  host
                                          :port  (js/parseInt port)
                                          :path  path
                                          :debug (if config/debug? 3 0)})]
    (-> p
        (.on "connection" on-connect)
        (.on "error" #(re-frame/dispatch [:error :peer-connection %])))
    {:handle p :connections {}}))

(defn reconnect!
  [user]
  (.reconnect (:handle user)))

(defn destroy!
  [user]
  (.destroy (:handle user)))

(defn- with-connection
  [user target-id f]
  (let [conn (get-in user [:connections target-id])]
    (if conn (f conn)
             (on-connect (.connect (:handle user) (id->peer-id target-id)) f))))

(defn send!
  "Send a message containing data from user to target-id. This is the last message sending step,
  data should be signed."
  [user target-id data]
  (with-connection user target-id #(do (.send % (enc/->transit data))
                                       (re-frame/dispatch [:message-sent target-id data]))))