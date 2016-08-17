(ns kahuin.views.home
  (:require [clojure.contrib.humanize :as human]
            [re-frame.core :as re-frame]
            [dommy.core :refer-macros [sel1]]))

(defn reply-to-k
  [k text]
  (re-frame/dispatch [:reply-to-k k text]))

(defn kahuin [k]
  [:div.k
   {:id (str "k-" (:id k))}
   [:div.k-header
    [:a.k-hide
     {:on-click #(re-frame/dispatch [:hide-k k])}]
    [:span "Re "]
    [:a.k-reference
     {:on-click #(re-frame/dispatch [:load-reference k])}
     (str ">" (:reference k))]]
   [:div.k-contents (:text k)]
   (let [reply-id (str "k-reply-" (:id k))
         reply-el #(sel1 (keyword (str "#" reply-id)))
         reply-value #(.-value (reply-el))]
     [:div.k-footer
      [:textarea.k-reply
       {:type      :text
        :id        reply-id
        :on-key-up #(when (= 13 (.-keyCode %)) (reply-to-k k (reply-value)))}]
      [:a.k-reply {:on-click #(if (empty? (reply-value))
                               (.focus (reply-el))
                               (reply-to-k k (reply-value)))}]
      [:a.k-love {:on-click #(re-frame/dispatch [:love-k k])}]])])


(defn home-panel []
  (let [kahuines (re-frame/subscribe [:kahuines])
        subs (re-frame/subscribe [:subscriptions])]
    (fn []
      [:main#ks-panel
       (when (empty? @subs) [:div.warning "You are not subscribed to anyone... Edit your"
                             [:a#profile-link {:href "#/profile"} "profile"]
                             " to add subscriptions."])
       (let [el #(sel1 :#k-new-input)
             val #(.-value (el))]
         [:div.k-new
          [:textarea#k-new-input.k-reply
           {:type        :text
            :placeholder "Share something..."
            :on-key-up   #(when (= 13 (.-keyCode %)) (reply-to-k nil (val)))}]
          [:a.k-reply {:on-click #(if (empty? (val))
                                   (.focus (el))
                                   (reply-to-k nil (val)))}]])
       (map kahuin @kahuines)])))