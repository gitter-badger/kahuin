(ns kahuin.network.encoding-test
  (:require [cljs.test :refer-macros [deftest testing is]]
            [kahuin.network.encoding :as e]))


(deftest base64
  (is (= (e/base64->octets "") nil))
  (is (= (e/octets->base64 '()) ""))
  (is (= (e/base64->octets "abcde_-") '(105 183 29 123 255)))
  (is (= (e/octets->base64 '(0 0 0 0)) "AAAAAA"))
  (is (= (e/octets->base64 (e/base64->octets "kahuin0")) "kahuin0")))

(deftest utf8-buffer
  (is (= (e/buffer->string (e/string->buffer "kahuin")) "kahuin")))