;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(ns guestbook.test.handler
  (:require [clojure.test :refer :all]
            [ring.mock.request :refer :all]
            [guestbook.handler :refer :all]
            [guestbook.middleware.formats :as formats]
            [muuntaja.core :as m]
            [mount.core :as mount]))

(defn parse-json [body]
  (m/decode formats/instance "application/json" body))

(use-fixtures
  :once
  (fn [f]
    (mount/start #'guestbook.config/env
                 #'guestbook.handler/app)
    (f)))

(deftest test-app
  (testing "main route"
    (let [response (app (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response (app (request :get "/invalid"))]
      (is (= 404 (:status response)))))

  (testing "services"

    (testing "success"
      (let [response (app (-> (request :post "/api/math/plus")
                              (json-body {:x 10, :y 6})))]
        (is (= 200 (:status response)))
        (is (= {:total 16} (m/decode-response-body response)))))

    (testing "parameter coercion error"
      (let [response (app (-> (request :post "/api/math/plus")
                              (json-body {:x 10, :y "invalid"})))]
        (is (= 400 (:status response)))))

    (testing "response coercion error"
      (let [response (app (-> (request :post "/api/math/plus")
                              (json-body {:x -10, :y 6})))]
        (is (= 500 (:status response)))))

    (testing "content negotiation"
      (let [response (app (-> (request :post "/api/math/plus")
                              (body (pr-str {:x 10, :y 6}))
                              (content-type "application/edn")
                              (header "accept" "application/transit+json")))]
        (is (= 200 (:status response)))
        (is (= {:total 16} (m/decode-response-body response)))))))
