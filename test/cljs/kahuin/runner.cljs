(ns kahuin.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [kahuin.core-test]))

(doo-tests 'kahuin.core-test)
