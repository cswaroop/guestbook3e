;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
;
;
(ns guestbook.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [ajax.core :refer [GET POST]]
            [clojure.string :as string]
            [cljs.pprint :refer [pprint]]
            [guestbook.validation :refer [validate-message]]))
;
;

;
(rf/reg-event-fx
 :app/initialize
 (fn [_ _]
   {:db {:messages/loading? true}}))
;

;
(rf/reg-sub
 :messages/loading?
 (fn [db _]
   (:messages/loading? db)))
;

;
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

;
(defn get-messages []
  (GET "/api/messages"
       ;
       {:headers {"Accept" "application/transit+json"}
        :handler #(rf/dispatch [:messages/set (:messages %)])}
       ;
       ;;...
       ))
;;...
;
;

;
(defn message-list [messages]
  [:ul.content
   (for [{:keys [timestamp message name]} @messages]
     ^{:key timestamp}
     [:li
      [:time (.toLocaleString timestamp)]
      ;; We're receiving strings from the server, so this exhibits different representation for client generated vs server generated times
      [:p message]
      [:p " - " name]])])
;

(rf/reg-event-db
 :message/add
 (fn [db [_ message]]
   (update db :messages/list conj message)))

;
(defn send-message! [fields errors]
  (if-let [validation-errors (validate-message @fields)]
    (reset! errors validation-errors)
    (POST "/api/message"
          ;
        {:format :json
         :headers
         {"Accept" "application/transit+json"
          "x-csrf-token" (.-value (.getElementById js/document "token"))}
         :params @fields
         :handler #(do
                     (rf/dispatch [:message/add (-> @fields
                                                    (assoc :timestamp (js/Date.)))])
                     (reset! fields nil)
                     (reset! errors nil))
         :error-handler #(do
                           (.log js/console (str %))
                           (reset! errors (get-in % [:response :errors])))}
        ;
        ;;...
        )))
;

;
(defn errors-component [errors id]
  (when-let [error (id @errors)]
    [:div.alert.alert-danger (string/join error)]))
;

;
(defn message-form []
  (let [fields (r/atom {})
        errors (r/atom nil)]
    (fn []
      [:div.content
       [errors-component errors :server-error]
       [:div.form-group
        [errors-component errors :name]
        [:p "Name:"
         [:input.form-control
          {:type :text
           :name :name
           :on-change #(swap! fields assoc :name (-> % .-target .-value))
           :value (:name @fields)}]]
        [errors-component errors :message]
        [:p "Message:"
         [:textarea.form-control
          {:rows 4
           :cols 50
           :name :message
           :value (:message @fields)
           :on-change #(swap! fields assoc :message (-> % .-target .-value))}]]
        [:input.btn.btn-primary
         {:type :submit
          :on-click #(send-message! fields errors)
          :value "comment"}]]])))

;
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
;

(defn mount-components []
  (.log js/console "Mounting Components...")
  (r/render [#'home] (.getElementById js/document "content"))
  (.log js/console "Components Mounted!"))

(defn init! []
  (.log js/console "Initializing App...")
  (rf/dispatch [:app/initialize])
  (get-messages)
  (mount-components))
;

