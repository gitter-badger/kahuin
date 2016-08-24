(ns kahuin.views.profile
  (:require [reagent.core :as r]
            [re-frame.core :as re-frame]
            [dommy.core :refer-macros [sel1]]
            [bouncer.core :as b]
            [bouncer.validators :as v]))

(defn add-subscription []
  (re-frame/dispatch [:add-subscription (.-value (sel1 :#subscription-id))]))

(defn subscription
  [s]
  [:div.subscription
   {:key (str "subscription-" (:id s))}
   [:span [:b (:id s)]]
   [:span (when (:nick s) "a.k.a.") [:b (:nick s)]]
   [:span (when (:last-seen s) (str "Last seen " (:last-seen s)))]
   [:a.subscription-remove
    {:on-click #(re-frame/dispatch [:remove-subscription (:id s)])}
    "Remove"]])

(defn profile-panel []
  (let [user (re-frame/subscribe [:user])
        subs (re-frame/subscribe [:subscriptions])
        logging-out (r/atom false)]
    (fn []
      [:main#profile-panel
       [:h2 "Your Profile"]
       [:div#profile.profile-row
        [:div
         [:span
          "ID "
          [:input#profile-id {:type      :text
                              :value     (:node-id @user)
                              :read-only true
                              :on-click  #(.select (.-target %))}]]
         [:span "a.k.a. "]
         [:input#profile-nick {:type          :text
                               :placeholder   "Nickname"
                               :default-value (:nick @user)
                               :max-length    60
                               :on-blur       #(re-frame/dispatch [:change-nick (.-value (sel1 :#profile-nick))])}]]]
       [:a#profile-download {:on-click #(re-frame/dispatch-sync [:download-keys])} "Download login credentials"]
       (if-not @logging-out
         [:a#profile-logout {:on-click #(reset! logging-out true)} "Log out"]
         [:span.profile-row "Are you sure you want to log out? You wont be able to log back in if you have not downloaded your credentials above."
          [:a#profile-logout-yes {:on-click #(re-frame/dispatch [:log-out])} "Yes, log out."]])
       [:h2 "Subscriptions"]
       [:div#subscriptions.profile-row
        (if (not-empty @subs) (map subscription @subs)
                              [:span "None, yet. Get a friend to send you their ID and enter it below!"])
        [:div#new-subscription
         [:input#subscription-id
          {:type        :text
           :placeholder "Enter an ID"
           :on-key-up   #(when (= 13 (.-keyCode %)) (add-subscription))}]
         [:a#subscription-add {:on-click add-subscription} "Add"]]]])))