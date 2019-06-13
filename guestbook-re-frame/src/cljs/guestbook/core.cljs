;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(ns guestbook.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ajax.core :refer [GET POST]]
            [clojure.string :as string]
            [cljs.pprint :refer [pprint]]
            [guestbook.validation :refer [validate-message]]))

(rf/reg-event-fx
 :app/initialize
 (fn [_ _]
   {:db {:messages/loading? true}}))

(rf/reg-sub
 :messages/loading?
 (fn [db _]
   (:messages/loading? db)))

(rf/reg-event-db
 :messages/set
 (fn [db [_ messages]]
   (-> db
       (assoc :messages/loading? false
              :messages/list messages))))

(rf/reg-sub
 :messages/list
 (fn [db _]
   (:messages/list db [])))

(defn get-messages []
  (GET "/messages"
       {:headers {"Accept" "application/transit+json"}
        :handler #(rf/dispatch [:messages/set (:messages %)])}))

(defn message-list [messages]
  [:ul.content
   (for [{:keys [timestamp message name]} @messages]
     ^{:key timestamp}
     [:li
      [:time (.toLocaleString timestamp)]
      [:p message]
      [:p " - " name]])])

;
(rf/reg-event-db
 :form/set-field
 [(rf/path :form/fields)] ;; Interceptor Chain
 (fn [fields [_ id value]]
   (assoc fields id value)))
;

;
(rf/reg-event-db
 :form/clear-fields
 [(rf/path :form/fields)]
 (fn [_ _]
   {}))
;

;
(rf/reg-sub
 :form/fields ;; Layer 2 Subscription
 (fn [db _]
   (:form/fields db)))

(rf/reg-sub
 :form/field ;; Layer 3 Subscription
 :<-[:form/fields]
 (fn [fields [_ id]]
   (get fields id)))
;

;
(rf/reg-event-db
 :form/set-errors
 [(rf/path :form/errors)]
 (fn [_ [_ errors]]
   errors))

(rf/reg-event-db
 :form/clear-errors
 [(rf/path :form/errors)]
 (fn [_ _]
   nil))

(rf/reg-sub
 :form/errors
 (fn [db _]
   (:form/errors db)))

(rf/reg-sub
 :form/error
 :<-[:form/errors]
 (fn [errors [_ id]]
   (get errors id)))
;

(rf/reg-event-db
 :message/add
 (fn [db [_ message]]
   (update db :messages/list conj message)))

(defn send-message! [fields]
  (if-let [validation-errors (validate-message @fields)]
    (rf/dispatch [:form/set-errors validation-errors])
    (POST "/message"
        {:format :json
         :headers
         {"Accept" "application/transit+json"
          "x-csrf-token" (.-value (.getElementById js/document "token"))}
         :params @fields
         :handler #(do
                     (rf/dispatch [:message/add (-> @fields
                                                    (assoc :timestamp (js/Date.)))])
                     (rf/dispatch [:form/clear-fields])
                     (rf/dispatch [:form/clear-errors]))
         :error-handler #(do
                           (.log js/console (str %))
                           (rf/dispatch [:form/set-errors (get-in % [:response :errors])]))})))

(defn errors-component [id]
  (when-let [error @(rf/subscribe [:form/error id])]
    [:div.alert.alert-danger (string/join error)]))

(defn message-form []
  [:div.content
   [errors-component :server-error]
   [:div.form-group
    [errors-component :name]
    [:p "Name:"
     [:input.form-control
      {:type :text
       :name :name
       :on-change #(rf/dispatch [:form/set-field
                                 :name
                                 (-> %
                                     .-target
                                     .-value)])
       :value @(rf/subscribe [:form/field :name])}]]
    [errors-component :message]
    [:p "Message:"
     [:textarea.form-control
      {:rows 4
       :cols 50
       :name :message
       :on-change #(rf/dispatch [:form/set-field
                                 :message
                                 (-> %
                                     .-target
                                     .-value)])
       :value @(rf/subscribe [:form/field :message])}]]
    [:input.btn.btn-primary
     {:type :submit
      :on-click #(send-message! (rf/subscribe [:form/fields]))
      :value "comment"}]]])

(defn home []
  (let [messages (rf/subscribe [:messages/list])]
    (fn []
      (if @(rf/subscribe [:messages/loading?])
        [:div>div.row>div.span12>h3 "Loading Messages..."]
        [:div
         [:div.row
          [:div.span12
           [message-list messages]]]
         [:div.row
          [:div.span12
           [message-form]]]]))))

(defn mount-components []
  (.log js/console "Mounting Components...")
  (r/render [#'home] (.getElementById js/document "content"))
  (.log js/console "Components Mounted!"))

(defn init! []
  (.log js/console "Initializing App...")
  (rf/dispatch [:app/initialize])
  (get-messages)
  (mount-components))

