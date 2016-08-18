(ns kahuin.network.ecc-test
  (:require [cljs.test :refer-macros [deftest testing is async]]
            [kahuin.network.ecc :as ecc]))

(deftest key-generation
  (async done
    (ecc/on-generate-keys (fn [k]
                            (is (= (ecc/key->id (clj->js (:public-key k)))
                                   (:node-id k)))
                            (is (= "P-256"
                                   (get-in k [:public-key "crv"])
                                   (get-in k [:private-key "crv"])))
                            (done)))))