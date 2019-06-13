;---
; Excerpted from "Web Development with Clojure, Third Edition",
; published by The Pragmatic Bookshelf.
; Copyrights apply to this code. It may not be used to create training material,
; courses, books, articles, and the like. Contact us if you are in doubt.
; We make no guarantees that this code is fit for any purpose.
; Visit http://www.pragmaticprogrammer.com/titles/dswdcloj3 for more book information.
;---
(defproject guestbook "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[cheshire "5.8.1"]
                 [clojure.java-time "0.3.2"]
                 [com.h2database/h2 "1.4.197"]
                 [conman "0.8.3"]
                 [cprop "0.1.13"]
                 [funcool/struct "1.3.0"]
                 [luminus-immutant "0.2.5"]
                 [luminus-migrations "0.6.5"]
                 [luminus-transit "0.1.1"]
                 [luminus/ring-ttl-session "0.3.2"]
                 [markdown-clj "1.0.7"]
                 [metosin/muuntaja "0.6.3"]
                 [metosin/reitit "0.3.1"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [nrepl "0.6.0"]
                 [org.clojure/clojure "1.10.0"]
                 ;
                 [cljs-ajax "0.7.3"]
                 [org.clojure/clojurescript "1.10.238" :scope "provided"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.6"] ;add re-frame
                 ;
                 [org.clojure/tools.cli "0.4.1"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.webjars.npm/bulma "0.7.4"]
                 [org.webjars.npm/material-icons "0.3.0"]
                 [org.webjars/webjars-locator "0.36"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [selmer "1.12.11"]]

  :min-lein-version "2.0.0"
  ;
  :source-paths ["src/clj" "src/cljc"]
  ;
  :test-paths ["test/clj"]
  ;
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"
  :main ^:skip-aot guestbook.core

  :plugins [[lein-immutant "2.1.0"]
            [lein-cljsbuild "1.1.7"]]


  :clean-targets
  ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]
  ;

  :profiles
  ;
  {:uberjar {:omit-source true
             :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
             :aot :all
             :uberjar-name "guestbook.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]

             :cljsbuild
             {:builds
              {:min {:source-paths
                     ["src/cljs" "src/cljc" "env/prod/cljs"]

                     :compiler
                     {:output-to "target/cljsbuild/public/js/app.js"
                      :output-dir "target/cljsbuild/public/js"
                      :source-map "target/cljsbuild/public/js/app.js.map"
                      :optimizations :advanced
                      :pretty-print false

                      :closure-warnings
                      {:externs-validation :off
                       :non-standard-jsdoc :off}}}}}}
   ;

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn"]
                  ;
                  :dependencies [[binaryage/devtools "0.9.10"] ;; cljs-devtools
                                 [expound "0.7.2"]
                                 [figwheel-sidecar "0.5.18"] ;; figwheel helper
                                 [pjstadig/humane-test-output "0.9.0"]
                                 [prone "1.6.1"]
                                 [ring/ring-devel "1.7.1"]
                                 [ring/ring-mock "0.3.2"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.23.0"]
                                 [lein-figwheel "0.5.18"]] ;;figwheel

                  :cljsbuild
                  {:builds
                   {:app {:source-paths
                          ["src/cljs" "src/cljc" "env/dev/cljs"]

                          :figwheel ;; Ensure that cljsbuild hooks into figwheel
                          {:on-jsload "guestbook.core/mount-components"}

                          :compiler
                          {:output-to "target/cljsbuild/public/js/app.js"
                           :output-dir "target/cljsbuild/public/js/out"
                           :main "guestbook.app"
                           :asset-path "/js/out"
                           :optimizations :none
                           :source-map true
                           :pretty-print true}}}}
                  :figwheel
                  {:http-server-root "public"
                   :nrepl-port 7002
                   :css-dirs ["resources/public/css"]} ;; Reload CSS Too
                  ;
                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:jvm-opts ["-Dconf=test-config.edn"]
                  :resource-paths ["env/test/resources"]}
   :profiles/dev {}
   :profiles/test {}})
