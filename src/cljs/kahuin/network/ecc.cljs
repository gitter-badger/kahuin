(ns kahuin.network.ecc
  (:require [kahuin.network.encoding :as enc]
            [re-frame.core :as re-frame]))

(def -settings (clj->js {:name       "ECDSA"
                         :namedCurve "P-256"
                         :hash       {:name "SHA-256"}}))

(defn- key->id
  [key]
  (->> (.-x key)
       (enc/base64->octets)
       (take 20)
       (enc/octets->base64)))

(defn- on-export-keys
  [user f]
  (-> (js.crypto.subtle.exportKey "jwk" (.-publicKey (:keypair user)))
      (.catch (partial print "Public key export error: "))
      (.then (fn [public-key]
               (-> (js.crypto.subtle.exportKey "jwk" (.-privateKey (:keypair user)))
                   (.catch (partial print "Private key export error: "))
                   (.then #(f (-> user
                                  (assoc :private-key (js->clj %))
                                  (assoc :public-key (js->clj public-key))
                                  (assoc :node-id (key->id public-key))))))))))

(defn- on-import-keys
  [user f]
  (-> (js.crypto.subtle.importKey "jwk" (clj->js (:private-key user)) -settings false #js ["sign"])
      (.catch (partial print "Private key import error: "))
      (.then (fn [private-key]
               (-> (js.crypto.subtle.importKey "jwk" (clj->js (:public-key user)) -settings false #js ["verify"])
                   (.catch (partial print "Public key import error: "))
                   (.then #(f (assoc user
                                :keypair
                                (clj->js {:publicKey % :privateKey private-key})))))))))

(defn- on-import-msg-public-key
  [msg f]
  (-> (js.crypto.subtle.importKey "jwk" (clj->js (:public-key msg)) -settings false #js ["verify"])
      (.catch (partial print "Message key import error: "))
      (.then f)))

(defn- on-generate-keys
  [f]
  (-> (js.crypto.subtle.generateKey -settings true #js ["sign" "verify"])
      (.catch (partial print "Key generation error: "))
      (.then #(on-export-keys {:keypair %} f))))

(defn- on-sign
  [user data f]
  (let [g (fn [kp] (-> (js.crypto.subtle.sign -settings
                                              (.-privateKey kp)
                                              (enc/string->buffer data))
                       (.catch (partial print "Message signing error: "))
                       (.then #(f {:public-key (:public-key user)
                                   :data       data
                                   :signature  (enc/buffer->string %)}))))]
    (if (:keypair user)
      (g (:keypair user))
      (on-import-keys user (fn [{kp :keypair}]
                             (do (re-frame/dispatch [:keys-generated kp])
                                 (g kp)))))))

(defn- on-verify
  [source-id msg f]
  (on-import-msg-public-key
    msg
    (fn [k]
      (if (= source-id (key->id k))
        (-> (js.crypto.subtle.verify -settings k
                                     (enc/string->buffer (:signature msg))
                                     (enc/string->buffer (:data msg)))
            (.catch (partial print "Message verification error: "))
            (.then #(if % (f msg)
                          (print "Message verification failed"))))
        (print "Source ID does not match key")))))

(defn generate-keys!
  "Generate a pair of public keys. Dispatches [:connect-peer user] when done."
  [nick]
  (on-generate-keys #(re-frame/dispatch [:connect-peer (assoc % :nick nick)])))

(defn sign-message!
  "Signs the msg, dispatches [:message-signed signed-msg] when done."
  [user msg]
  (on-sign user (:data msg) #(re-frame/dispatch [:message-signed (merge msg %)])))

(defn verify-message!
  "Verifies the received msg (and that it matches the declared source-id), dispatches
  [:message-verified signed-msg] if the verification is passed, else ignores it."
  [source-id msg]
  (on-verify source-id msg #(re-frame/dispatch [:message-verified (assoc msg :source-id source-id)])))