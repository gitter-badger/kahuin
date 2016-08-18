(ns kahuin.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [kahuin.core-test]
              [kahuin.db-test]
              [kahuin.network.ecc-test]
              [kahuin.network.encoding-test]))

(doo-tests 'kahuin.core-test
           'kahuin.db-test
           'kahuin.network.encoding-test
           'kahuin.network.ecc-test)
