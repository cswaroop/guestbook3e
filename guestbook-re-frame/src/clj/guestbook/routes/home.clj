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
  (:require [guestbook.layout :as layout]
            [guestbook.db.core :as db]
            [clojure.java.io :as io]
            [guestbook.middleware :as middleware]
            [ring.util.http-response :as response]
            [guestbook.validation :refer [validate-message]]))
;

;
(defn home-page [_]
  (layout/render
   "home.html"))
;

;
(defn save-message! [{:keys [params]}]
  (if-let [errors (validate-message params)]
    (response/bad-request {:errors errors})
    (try
      (db/save-message! params)
      (response/ok {:status :ok})
      (catch Exception e
        (response/internal-server-error
         {:errors {:server-error ["Failed to save message!"]}})))))
;

(defn about-page [_]
  (layout/render "about.html"))

;
(defn message-list [_]
  (response/ok {:messages (vec (db/get-messages))}))

;
(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/messages" {:get message-list}]
   ;
   ["/message" {:post save-message!}]
   ;
   ["/about" {:get about-page}]])
;
;
