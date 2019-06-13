;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
;
(ns guestbook.routes.services
  (:require
   [reitit.swagger :as swagger]
   [reitit.swagger-ui :as swagger-ui]
   [guestbook.messages :as msg]
   [guestbook.middleware :as middleware]
   [ring.util.http-response :as response]))


(defn service-routes []
  ["/api"
   {:middleware [middleware/wrap-formats]
    :swagger {:id ::api}}

   ["" {:no-doc true}
    ["/swagger.json"
     {:get (swagger/create-swagger-handler)}]
    ["/swagger-ui*"
     {:get (swagger-ui/create-swagger-ui-handler
            {:url "/api/swagger.json"})}]]
   ;;...
   ;
   ["/messages"
    {:get
     (fn [_]
       (response/ok (msg/message-list)))}]

   ["/message"
    {:post
     (fn [{:keys [params]}]
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
                {:errors {:server-error ["Failed to save message!"]}}))))))}]
;
   ])
;
