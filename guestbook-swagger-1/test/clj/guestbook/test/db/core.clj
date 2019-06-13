;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(ns guestbook.test.db.core
  (:require
    [guestbook.db.core :refer [*db*] :as db]
    [luminus-migrations.core :as migrations]
    [clojure.test :refer :all]
    [clojure.java.jdbc :as jdbc]
    [guestbook.config :refer [env]]
    [mount.core :as mount]))

;
(use-fixtures
  :once
  (fn [f]
    (mount/start
      #'guestbook.config/env
      #'guestbook.db.core/*db*)
    (migrations/migrate ["migrate"] (select-keys env [:database-url]))
    (f)))
;

(deftest test-messages
  (jdbc/with-db-transaction [t-conn *db*]
    (jdbc/db-set-rollback-only! t-conn)
    (is (= 1 (db/save-message!
              t-conn
              {:name "Bob"
               :message "Hello, World"}
              {:connection t-conn})))
   (is (= {:name "Bob"
            :message "Hello, World"}
           (-> (db/get-messages t-conn {})
            (first)
            (select-keys [:name :message]))))))
