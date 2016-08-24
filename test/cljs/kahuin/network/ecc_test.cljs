(ns kahuin.network.ecc-test
  (:require [cljs.test :refer-macros [deftest testing is async]]
            [re-frame.core :as re-frame :refer [register-handler]]
            [kahuin.network.ecc :as ecc]
            [kahuin.handlers :refer [non-saving]]
            [kahuin.network.encoding :as enc]))

(def test-user
  {:private-key {"crv"     "P-256",
                 "d"       "aydfXQcJe-AkrtoQY2KvI7KPWhi9AL9Dd2xjZNXLT5g",
                 "ext"     true,
                 "key_ops" ["sign"],
                 "kty"     "EC",
                 "x"       "1pgfjwBKL3QOYJ5Y_6YAfwwPfENnoLf_b_0tIbQsWWs",
                 "y"       "lJtjdGCQjvsRW6JFL8e3WHdp3eqkEOxlQzdAo4eVSdA"},
   :public-key  {"crv"     "P-256",
                 "ext"     true,
                 "key_ops" ["verify"],
                 "kty"     "EC",
                 "x"       "1pgfjwBKL3QOYJ5Y_6YAfwwPfENnoLf_b_0tIbQsWWs",
                 "y"       "lJtjdGCQjvsRW6JFL8e3WHdp3eqkEOxlQzdAo4eVSdA"},
   :node-id     "1pgfjwBKL3QOYJ5Y_6YAfwwPfEM",
   :nick        "test user"})

(deftest key-generation
  (async done
    (ecc/on-generate-keys (fn [k]
                            (is (= (ecc/key->id (clj->js (:public-key k)))
                                   (:node-id k)))
                            (is (= "P-256"
                                   (get-in k [:public-key "crv"])
                                   (get-in k [:private-key "crv"])))
                            (done)))))

(def test-msg {:data "blah"})

(deftest signing
  (async done
    (register-handler
      :message-verified
      non-saving
      (fn [_ [m]]
        (is (= (:data test-msg) (:data m)))
        (done)))
    (register-handler
      :message-signed
      non-saving
      (fn [db [m]]
        (is (= (:data test-msg) (:data m)))
        (is (= (:public-key test-user) (:public-key m)))
        (ecc/verify-message! (:node-id test-user) (enc/transit-> (enc/->transit m)))
        db))
    (ecc/sign-message! test-user test-msg)))