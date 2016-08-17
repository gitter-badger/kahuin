(ns kahuin.views.connect
  (:require [re-frame.core :as re-frame]
            [dommy.core :refer-macros [sel1]]))

(defn create-profile!
  []
  (re-frame/dispatch [:create-user (.-value (sel1 :input))]))

(defn upload-keys!
  [ev]
  (let [reader (js/FileReader.)
        file (aget ev "target" "files" 0)]
    (set! (.-onload reader)
          #(re-frame/dispatch [:upload-keys (.-result reader)]))
    (.readAsText reader file)))

(defn connect-panel []
  (fn []
    [:main#connect-panel
     [:h2 "Welcome to Kahuin"]
     (when (= "Unsupported" js/util.browser)
       [:div.warning
        [:b "Sorry, but your browser is not supported. "]
        "Kahuin requires Firefox or Chrome, versions 40 and up."])
     [:span.connect-row "You are not logged in. Create a new profile:"]
     [:span.connect-row
      [:input#connect-create-nick
       {:type        :text
        :placeholder "Optional username"
        :on-key-up   #(when (= 13 (.-keyCode %)) (create-profile!))}]
      [:a#connect-create {:on-click create-profile!} "Go!"]]
     [:div.divider [:span.divider-text "or"]]
     [:input#connect-upload
      {:type      :file
       :name      :connect-upload
       :on-change upload-keys!}]
     [:label#connect-upload-label {:for :connect-upload}
      "Upload a saved profile"]
     [:div.divider]
     [:a.connect-row {:href   "https://github.com/polymeris/kahuin"
                      :target :_blank}
      "Learn more about Kahuin on Github"]]))