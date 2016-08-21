(defproject kahuin "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.93"]
                 [reagent "0.5.1"]
                 [binaryage/devtools "0.6.1"]
                 [re-frame "0.7.0"]
                 [secretary "1.2.3"]
                 [garden "1.3.2"]
                 [prismatic/dommy "1.1.0"]
                 [com.andrewmcveigh/cljs-time "0.4.0"]
                 [com.cognitect/transit-cljs "0.8.239"]
                 [bouncer "1.0.0"]
                 [clojure-humanize "0.2.0"]]

  :plugins [[lein-cljsbuild "1.1.3"]
            [lein-garden "0.2.6"]
            [lein-npm "0.6.2"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :main kahuin.core

  :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                    "out"
                                    "target"
                                    "resources/public/css"]

  :figwheel {:css-dirs   ["resources/public/css"]
             :nrepl-port 7888}

  :garden {:builds [{:id           "screen"
                     :source-paths ["src/clj"]
                     :stylesheet   kahuin.css/screen
                     :compiler     {:output-to "resources/public/css/screen.css"}}]}

  :doo {:build "test"
        :alias {:default [:firefox :chrome]}}

  :profiles {:dev        {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                         [figwheel-sidecar "0.5.4"]]
                          :plugins      [[lein-figwheel "0.5.4"]
                                         [lein-doo "0.1.7"]]
                          :source-paths ["dev"]}
             :production {:prep-tasks   ["compile" ["cljsbuild" "once" "min"]]}}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :npm {:dependencies [[peer "0.2.8"]
                       [peerjs "0.3.14"]
                       [express "4.14.0"]]}

  :cljsbuild {:builds
              {:dev  {:source-paths ["src/cljs"]
                      :figwheel     {:on-jsload "kahuin.core/mount-root"}
                      :compiler     {:main                 kahuin.core
                                     :output-to            "resources/public/js/compiled/app.js"
                                     :output-dir           "resources/public/js/compiled/out"
                                     :asset-path           "js/compiled/out"
                                     :source-map-timestamp true}}
               :min  {:source-paths ["src/cljs"]
                      :compiler     {:main            kahuin.core
                                     :output-to       "resources/public/js/compiled/app.js"
                                     :optimizations   :simple
                                     :closure-defines {goog.DEBUG false}
                                     :pretty-print    false}}
               :test {:source-paths ["src/cljs" "test/cljs"]
                      :compiler     {:main          kahuin.runner
                                     :output-to     "out/testable.js"
                                     :optimizations :none}}}})
