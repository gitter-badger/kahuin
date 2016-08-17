(ns kahuin.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]
            [cljs.pprint :refer [pprint]]
            [clojure.contrib.humanize :as human]))

(re-frame/register-sub
  :db-as-string
  (fn [db]
    (reaction (with-out-str (pprint @db)))))

(re-frame/register-sub
  :name
  (fn [db]
    (reaction (:name @db))))

(re-frame/register-sub
  :active-panel
  (fn [db _]
    (reaction (:active-panel @db))))

(re-frame/register-sub
  :pending
  (fn [db _]
    (reaction (:pending @db))))

(re-frame/register-sub
  :user
  (fn [db _]
    (reaction (when (get-in @db [:user :node-id]) (:user @db)))))

(re-frame/register-sub
  :kahuines
  (fn [db _]
    (reaction (:kahuines @db))))

(re-frame/register-sub
  :subscriptions
  (fn [db _]
    (reaction (map (fn [[id s]] (-> s
                                    (assoc :id id)
                                    (update :last-seen #(human/datetime %))))
                   (:subscriptions @db)))))