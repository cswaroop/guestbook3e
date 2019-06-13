;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(ns guestbook.db.core
  (:require
    [clojure.java.jdbc :as jdbc]
    [conman.core :as conman]
    [java-time :refer [java-date]]
    [java-time.pre-java8 :as jt]
    [mount.core :refer [defstate]]
    [guestbook.config :refer [env]]))

(defstate ^:dynamic *db*
          :start (conman/connect! {:jdbc-url (env :database-url)})
          :stop (conman/disconnect! *db*))
;
(conman/bind-connection *db* "sql/queries.sql")
;


(extend-protocol jdbc/IResultSetReadColumn
  java.sql.Timestamp
  (result-set-read-column [v _2 _3]
    (java-date (.atZone (.toLocalDateTime v) (java.time.ZoneId/systemDefault))))
  java.sql.Date
  (result-set-read-column [v _2 _3]
    (.toLocalDate v))
  java.sql.Time
  (result-set-read-column [v _2 _3]
    (.toLocalTime v)))

(extend-protocol jdbc/ISQLValue
  java.util.Date
  (sql-value [v]
    (java.sql.Timestamp. (.getTime v)))
  java.time.LocalTime
  (sql-value [v]
    (jt/sql-time v))
  java.time.LocalDate
  (sql-value [v]
    (jt/sql-date v))
  java.time.LocalDateTime
  (sql-value [v]
    (jt/sql-timestamp v))
  java.time.ZonedDateTime
  (sql-value [v]
    (jt/sql-timestamp v)))

