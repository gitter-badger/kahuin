(ns kahuin.network.encoding
  (:require [goog.crypt.base64 :as b64]))

(defn octets->base64
  [arr]
  (-> (clj->js arr)
      (b64/encodeByteArray)
      ; these replacements are so that we can safely use the encoded string in urls
      ; peerjs doesn't like $, ~ or !
      (clojure.string/replace #"/" "_")
      (clojure.string/replace #"\+" "-")
      (clojure.string/replace #"=" "")))

(defn base64->octets
  [s]
  (-> s
      (clojure.string/replace #"[_]" "/")
      (clojure.string/replace #"[-]" "+")
      (b64/decodeStringToByteArray)
      (seq)))

(defn string->buffer
  [s]
  (-> (js/TextEncoder. "utf-8")
      (.encode s)))

(defn buffer->string
  [b]
  (-> (js/TextDecoder. "utf-8")
      (.decode b)))