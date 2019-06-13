;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(ns ring-app.core
  (:require [reitit.ring :as reitit]
            [ring.adapter.jetty :as jetty]
            [ring.util.http-response :as response]
            [ring.middleware.reload :refer [wrap-reload]]
            [muuntaja.middleware :refer [wrap-format]]))
(defn wrap-nocache [handler]
  (fn [request]
    (-> request
        handler
        (assoc-in [:headers "Pragma"] "no-cache"))))

(defn wrap-formats [handler]
  (wrap-format handler))
(def routes
  [["/"
    {:get
     (fn [request]
       (response/ok
         (str "<html><body> your IP is: "
              (:remote-addr request)
              "</body></html>")))}]
   ["/echo/:value"
    {:get
     (fn [{{:keys [id]} :path-params}]
       (response/ok (str "<p>the value is: " id "</p>")))}]
   ["/api"
    {:middleware [wrap-formats]}
    ["/multiply"
     {:post
      (fn [{{:keys [a b]} :body-params}]
        (response/ok {:result (* a b)}))}]]
   ])

(defn response-handler [request]
  (response/ok
    (str "<html><body> your IP is: "
         (:remote-addr request)
         "</body></html>")))
(def handler
  (reitit/routes
    (reitit/ring-handler
      (reitit/router routes))
    (reitit/create-resource-handler
      {:path "/"})
    (reitit/create-default-handler
      {:not-found
       (constantly (response/not-found "404 - Page not found"))
       :method-not-allowed
       (constantly (response/method-not-allowed "405 - Not allowed"))
       :not-acceptable
       (constantly (response/not-acceptable "406 - Not acceptable"))})))
(defn -main []
  (jetty/run-jetty
    (-> #'handler wrap-nocache wrap-reload)
    {:port  3000
     :join? false}))
