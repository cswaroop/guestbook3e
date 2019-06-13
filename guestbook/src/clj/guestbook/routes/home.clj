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
   [guestbook.db.core :as db]
   [guestbook.middleware :as middleware]
   [ring.util.http-response :as response]))
;

;
(defn home-page [request]
  (layout/render
   request "home.html" {:messages (db/get-messages)}))
;

;
(defn save-message! [{:keys [params]}]
 (db/save-message! params)
 (response/found "/"))
;


(defn about-page [request]
  (layout/render
   request "about.html"))

;
(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ;
   ["/message" {:post save-message!}]
   ;
   ["/about" {:get about-page}]])
;
