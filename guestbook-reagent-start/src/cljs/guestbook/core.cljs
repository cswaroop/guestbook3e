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
            [ajax.core :refer [GET POST]]
            [clojure.string :as string]
            [cljs.pprint :refer [pprint]]))

;
(defn get-messages [messages]
  (GET "/messages"
       {:headers {"Accept" "application/transit+json"}
        :handler #(reset! messages (:messages %))}))
;

;
(defn message-list [messages]
  (println messages)
  [:ul.messages
   (for [{:keys [timestamp message name]} @messages]
     ^{:key timestamp}
     [:li
      [:time (.toLocaleString timestamp)]
      [:p message]
      [:p " - " name]])])
;

;
(defn send-message! [fields errors]
  (POST "/message"
        {:format :json
         :headers
         {"Accept" "application/transit+json"
          "x-csrf-token" (.-value (.getElementById js/document "token"))}
         :params @fields
         :handler #(do
                     (.log js/console (str "response:" %))
                     (reset! errors nil))
         :error-handler #(do
                           (.log js/console (str %))
                           (reset! errors (get-in % [:response :errors])))}))

(defn errors-component [errors id]
  (when-let [error (id @errors)]
    [:div.notification.is-danger (string/join error)]))

;
(defn message-form []
  (let [fields (r/atom {})]
    (fn []
      [:div
       ;
       ;
       [:p "Name: " (:name @fields)]
       [:p "Message: " (:message @fields)]
       ;
       [errors-component errors :server-error]
       ;
       [:div.field
        [:label.label {:for :name} "Name"]
        ;
        [errors-component errors :name]
        ;
        [:input.input
         {:type :text
          :name :name
          :on-change #(swap! fields assoc :name (-> % .-target .-value))
          :value (:name @fields)}]]
       [:div.field
        [:label.label {:for :message} "Message"]
        ;
        [errors-component errors :message]
        ;
        [:textarea.textarea
         {:name :message
          :value (:message @fields)
          :on-change #(swap! fields assoc :message (-> % .-target .-value))}]]
       [:input.button.is-primary
        {:type :submit
         ;
         :on-click #(send-message! fields errors)
         ;
         :value "comment"}]])))
;
;
;
;
(defn home []
  ;
  (let [messages (r/atom nil)]
    (get-messages messages)
    (fn []
      ;
      [:div.content>div.columns.is-centered>div.column.is-two-thirds
       ;
       [:div.columns>div.column
        [:h3 "Messages"]
        [message-list messages]]
       ;
       [:div.columns>div.column
        [message-form]]])))
;
;

(r/render [home] (.getElementById js/document "content"))
