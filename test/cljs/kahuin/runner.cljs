(ns kahuin.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [kahuin.core-test]
              [kahuin.db-test]
              [kahuin.network.peers-test]
              [kahuin.network.ecc-test]
              [kahuin.network.encoding-test]
              [kahuin.network.dht-test]))

(doo-tests 'kahuin.core-test
           'kahuin.db-test
           'kahuin.network.encoding-test
           'kahuin.network.peers-test
           'kahuin.network.ecc-test
           'kahuin.network.dht-test)
