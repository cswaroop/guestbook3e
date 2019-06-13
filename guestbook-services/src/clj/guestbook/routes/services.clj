;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(ns guestbook.routes.services
  (:require
   [guestbook.messages :as messages]
   [guestbook.middleware :as middleware]
   [ring.util.http-response :as response]
   [guestbook.validation :refer [validate-message]]))


(defn service-routes []
  ["/api"
   {:middleware [middleware/wrap-formats]}
   ["/messages"
    {:get
     (fn [_]
       (response/ok (messages/message-list)))}]

   ["/message"
    {:post
     (fn [{:keys [params]}]
       (if-let [errors (validate-message params)]
         (response/bad-request {:errors errors})
         (try
           (messages/save-message! params)
           (response/ok {:status :ok})
           (catch Exception e
             (response/internal-server-error
              {:errors {:server-error ["Failed to save message!"]}})))))}]])
