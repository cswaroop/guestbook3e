;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
;
(ns guestbook.routes.websockets
  (:require [clojure.tools.logging :as log]
            [immutant.web.async :as async]
            [clojure.edn :as edn]
            [guestbook.messages :as msg]))
;

;
(defonce channels (atom #{}))

(defn connect! [channel]
  (log/info "Channel opened")
  (swap! channels conj channel))

(defn disconnect! [channel {:keys [code reason]}]
  (log/info "Channel closed. close code:" code "reason:" reason)
  (swap! channels disj channel))
;

;
(defn handle-message! [channel ws-message]
  (let [message (edn/read-string ws-message)
        response (try
                   (msg/save-message! message)
                   (assoc message :timestamp (java.util.Date.))
                   (catch Exception e
                     (let [{id     :guestbook/error-id
                            errors :errors} (ex-data e)]
                       (case id
                         :validation
                         {:errors errors}
                         ;;else
                         {:errors {:server-error ["Failed to save message!"]}}))))]
    (if (:errors response)
      (async/send! channel (pr-str response))
      (doseq [channel @channels]
        (async/send! channel (pr-str response))))))
;

;
(defn handler [request]
  (async/as-channel
   request
   {:on-open connect!
    :on-close disconnect!
    :on-message handle-message!}))

(defn websocket-routes []
  ["/ws"
   {:get handler}])
;
