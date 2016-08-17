(ns kahuin.css
  (:require [garden.def :refer [defstyles defkeyframes]]
            [garden.color :refer [as-rgb as-hex darken lighten]]))

(defn px [p] (str p "px"))
(defn em [p] (str p "em"))
(defn font [k] (str "'" (name k) "'"))

(def accent (as-rgb "ffaa00"))
(def accent-shadow (as-hex (darken accent 3)))
(def accent-light (as-hex (lighten accent 6)))

(def icons-fontfamily
  ["@font-face" {:font-family (font :icons)
                 :src         "url('/fonts/icons.woff') format('woff')"
                 :font-weight :normal
                 :font-style  :normal}])

(def titillium-fontfamily
  ["@font-face" {:font-family (font :text)
                 :src         (str "url('/fonts/TitilliumWeb-Light.ttf') format('truetype')")
                 :font-weight :normal
                 :font-style  :normal}])

(def titillium-fontfamily-bold
  ["@font-face" {:font-family (font :text)
                 :src         (str "url('/fonts/TitilliumWeb-SemiBold.ttf') format('truetype')")
                 :font-weight :bold
                 :font-style  :normal}])

(def border "1px solid #ddd")

(defkeyframes bounce
              ["0%, 80%, 100%" {:transform "scale(0.2)"}]
              ["40%" {:transform "scale(0.8)"}])

(defn icon [x]
  [:&:before
   {:font-family  "'icons' !important"
    :speak        "none"
    :font-weight  :normal
    :margin-right (px 6)
    :margin-left  (px 6)
    :font-size    (px 20)
    :position     :relative
    :content      (get {:plus     "'\\e114'"
                        :minus    "'\\e115'"
                        :confirm  "'\\e116'"
                        :cancel   "'\\e117'"
                        :download "'\\e122'"
                        :upload   "'\\e123'"
                        :link     "'\\e005'"
                        :share    "'\\e081'"
                        :power    "'\\e086'"
                        :profile  "'\\e074'"
                        :love     "'\\e024'"
                        :loved    "'\\e124'"
                        :say      "'\\e076'"}
                       x)}])

(def body
  [:body {:font-family    (font :text)
          :font-size      (px 16)
          :color          :#444
          :margin         0
          :padding        0
          :vertical-align :baseline}
   [:form :fieldset
    {:padding 0
     :border  :none}]
   [:a {:color           accent
        :cursor          :pointer
        :font-weight     :bold
        :text-decoration :none}]
   [:h2 {:font-size     (px 16)
         :margin-bottom (px 5)}]
   [:main {:margin-top  (px 47)
           :line-height 2
           :padding     (px 10)}]
   [:input :textarea
    {:font-family      (font :text)
     :background-color :white
     :min-height       (px 25)
     :font-size        (px 16)
     :font-weight      :bold
     :border           :none
     :border-bottom    border}
    [:&:focus
     {:outline :none}]]
   [:.divider
    {:border-bottom border
     :height        (px 25)
     :margin-top    (px 12)
     :margin-bottom (px 24)
     :text-align    :center
     :z-index       1}
    [:span.divider-text
     {:position         :relative
      :top              (px 8)
      :padding          (px 4)
      :z-index          10
      :background-color :white}]]
   [:div.indent
    {:margin-left (em 1)}]
   [:footer {:position   :fixed
             :font-size  (px 12)
             :bottom     0
             :left       0
             :right      0
             :border-top border}]
   [:div#status
    {:margin (px 5)
     :transition "ease-in-out, height .15s ease-in-out"}
    [:span {:float :left}]]
   [:#spinner
    {:float      :right
     :width      (px 50)
     :text-align :center}
    [:div
     {:width            (px 10)
      :height           (px 10)
      :background-color accent-light
      :display          :inline-block
      :animation        [[bounce "2s" :infinite :ease-in :both]]}]]
   [:div#debug {:background-color :#ddd
                :padding          (px 5)
                :width            "100%"
                :max-height       "40vh"
                :overflow         :scroll}]])

(def header
  [:header
   {:position      :fixed
    :top           0
    :left          0
    :right         0
    :z-index       10
    :height        (px 50)
    :border-bottom (str "2px solid " accent-shadow)
    :background    (str "radial-gradient(at 50px, rgba(255, 255, 255, 0.45), transparent 66%),"
                        "repeating-linear-gradient(-35deg,"
                        (as-hex accent) " 0px, "
                        (as-hex accent) " 2px, "
                        accent-light " 2px,"
                        accent-light " 4px)")}
   [:nav
    {:display :flex}
    [:a
     {:margin      (px 13)
      :font-weight :bold
      :color       :white}
     [:&#profile
      (icon :profile)]
     [:&#logo
      {:margin    (px 9)
       :font-size (px 20)
       :flex-grow 1}]
     [:&.active
      {:text-shadow (str "0 -1px" accent-shadow ","
                         "0 2px" accent-light)}]]]])

(def home
  [:main#ks-panel
   {:display   :flex
    :flex-wrap :wrap}
   [:div.k
    {:padding-right  (em 1)
     :padding-bottom (em 2)
     :word-wrap      :break-word
     :width          "calc(100% - 6px)"
     :max-width      (em 30)}
    [:div.k-header
     {:font-size (px 12)}
     [:a.k-hide
      {:float :right}
      (icon :cancel)]]]
   [:div.k-footer :div.k-new
    {:display :flex}
    [:a.k-reply (icon :say)]
    [:a.k-love (icon :love)]
    [:a.k-loved (icon :loved)]
    [:textarea
     {:flex-grow  1
      :transition "ease-in-out, height .15s ease-in-out"
      :height     (px 25)}
     [:&:focus
      {:height (px 75)}]]]
   [:div.warning :div.k-new {:width "100%"}]
   [:a#profile-link (icon :profile)]])

(def profile
  [:main#profile-panel
   {:display        :flex
    :flex-wrap      :wrap
    :flex-direction :column
    :padding-top    0}
   [:div.profile-row
    {:display        :flex
     :flex-direction :column
     :padding-right  (px 10)}
    [:div
     {:display        :flex
      :flex-wrap      :wrap
      :flex-direction :row
      :padding-bottom (em 1)}
     [:input {:flex-grow 2}]
     [:span {:flex-grow 1}]
     [:a {:align-self :flex-end}]]]
   [:a {:margin (px 5)}]
   [:#profile-share (icon :share)]
   [:#profile-download (icon :download)]
   [:#profile-logout (icon :power)]
   [:#subscription-add (icon :plus)]
   [:.subscription-remove (icon :minus)]
   [:#profile-id {:width "calc(100% - 50px)"}]])

(def connect
  [:main#connect-panel
   {:margin-left  :auto
    :margin-right :auto
    :max-width    (px 400)}
   [:.warning {:border "2px solid #bb0000"
               :padding (px 6)
               :background-color :#ffbbbb}]
   [:.connect-row
    {:display        :flex
     :flex-direction :row
     :width          "100%"}
    [:input :span {:flex-grow 1}]]
   [:a#connect-create (icon :confirm)]
   [:input#connect-upload {:display :none}]
   [:label#connect-upload-label (icon :upload) {:float :right
                                                :color accent
                                                :cursor :pointer
                                                :font-weight :bold}]])

(defstyles screen
           icons-fontfamily
           titillium-fontfamily
           titillium-fontfamily-bold
           bounce
           body
           header
           home
           profile
           connect)
