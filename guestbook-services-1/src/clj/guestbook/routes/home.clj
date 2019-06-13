;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
;
(ns guestbook.routes.home
  (:require
   [guestbook.layout :as layout]
   [guestbook.messages :as msg]
   [guestbook.middleware :as middleware]
   [ring.util.http-response :as response]))
;

(defn home-page [request]
  (layout/render
   request
   "home.html"))

;
;;...
(defn save-message! [{:keys [params]}]
  (try
    (msg/save-message! params)
    (response/ok {:status :ok})
    (catch Exception e
      (let [{id     :guestbook/error-id
             errors :errors} (ex-data e)]
        (case id
          :validation
          (response/bad-request {:errors errors})
          ;;else
          (response/internal-server-error
           {:errors {:server-error ["Failed to save message!"]}}))))))
;

(defn about-page [request]
  (layout/render
   request "about.html"))

;
;;...
(defn message-list [_]
  (response/ok (msg/message-list)))
;

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/messages" {:get message-list}]
   ["/message" {:post save-message!}]
   ["/about" {:get about-page}]])
