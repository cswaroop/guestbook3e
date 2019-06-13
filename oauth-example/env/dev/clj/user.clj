;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(ns user
  (:require [mount.core :as mount]
            oauth-example.core))

(defn start []
  (mount/start-without #'oauth-example.core/repl-server))

(defn stop []
  (mount/stop-except #'oauth-example.core/repl-server))

(defn restart []
  (stop)
  (start))


