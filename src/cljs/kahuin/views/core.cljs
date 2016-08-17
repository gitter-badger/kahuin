(ns kahuin.views.core
  (:require [re-frame.core :as re-frame]
            [kahuin.views.home :refer [home-panel]]
            [kahuin.views.profile :refer [profile-panel]]
            [kahuin.views.connect :refer [connect-panel]]
            [kahuin.config :as config]))

(def panels
  {:home    [#'home-panel]
   :profile [#'profile-panel]})

(defn header []
  (let [active-panel (re-frame/subscribe [:active-panel])
        user (re-frame/subscribe [:user])]
    (fn []
      [:nav
       [:a#logo {:href "#/", :class (if (= :home @active-panel) "active" "")} "Kahuin"]
       (when @user
         [:a#profile {:href  "#/profile"
                      :class (if (= :profile @active-panel) "active")}
          (:nick @user)])])))

(defn main []
  (let [active-panel (re-frame/subscribe [:active-panel])
        user (re-frame/subscribe [:user])]
    (fn [] (if @user (get panels @active-panel)
                     [#'connect-panel]))))

(defn spinner
  []
  [:div#spinner
   [:div#spinner-1]
   [:div#spinner-2]
   [:div#spinner-3]])

(defn footer []
  (let [user (re-frame/subscribe [:user])
        pending (re-frame/subscribe [:pending])
        kahuines (re-frame/subscribe [:kahuines])
        db (re-frame/subscribe [:db-as-string])]
    [:div
     (cond
       (nil? @user) [:span "Not connected"]
       (:user-creation @pending) [:div#status [:span "Creating user profile..."] [spinner]]
       (:peer-connection @pending) [:div#status [:span "Connecting to network..."] [spinner]]
       (empty? @kahuines) [:div#status [:span "Looking for gossip..."] [spinner]]
       )
     (when config/debug? [:div#debug [:h2 "Debug information"] [:h3 "db"] [:pre @db]])]))