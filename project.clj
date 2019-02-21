(defproject ajenda "0.1.16"
  :description "A Reagent Wrapper for Full Calendar "
  :url "https://github.com/SVMBrown/ajenda"
  :license {:name "MIT"
            :url  "https://opensource.org/licenses/MIT"}

  :dependencies
  [[org.clojure/clojure "1.9.0" :scope "provided"]
   [reagent "0.8.1"]
   [org.clojure/clojurescript "1.10.339" :scope "provided"]
   [org.webjars/jquery "3.3.1-1"]
   [org.webjars.bower/moment "2.22.2"]
   [org.webjars.bowergithub.fullcalendar/fullcalendar "3.9.0"
    :exclusions [org.webjars.bowergithub.jquery/jquery-dist
                 org.webjars.bowergithub.moment/moment]]]

  :plugins
  [[lein-cljsbuild "1.1.7"]
   [lein-figwheel "0.5.16"]
   [cider/cider-nrepl "0.15.1"]]

  :clojurescript? true
  :jar-exclusions [#"\.swp|\.swo|\.DS_Store"]
  :clean-targets ^{:protect false}
  [:target-path
   [:cljsbuild :builds :app :compiler :output-dir]
   [:cljsbuild :builds :app :compiler :output-to]]

  :source-paths ["src/clj" "src/cljc" "src/cljs"]

  :profiles
  {:dev
   {:dependencies
    [[ring-server "0.5.0"]
     [ring-webjars "0.2.0"]
     [ring "1.7.0"]
     [ring/ring-defaults "0.3.1"]
     [compojure "1.6.1"]
     [hiccup "1.0.5"]
     [nrepl "0.4.4"]
     [binaryage/devtools "0.9.10"]
     [cider/piggieback "0.3.9"]
     [figwheel-sidecar "0.5.16"]]

    :source-paths ["src/clj" "src/cljc" "src/cljs" "env/dev/clj" "env/dev/cljs"]
    :resource-paths ["resources" "env/dev/resources" "target/cljsbuild"]

    :figwheel
    {:server-port      3450
     :nrepl-port       7003
     :nrepl-middleware [cider.piggieback/wrap-cljs-repl
                        cider.nrepl/cider-middleware]
     :css-dirs         ["resources/public/css" "env/dev/resources/public/css"]
     :ring-handler     ajenda.server/app}
    :cljsbuild
    {:builds {:app
              {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
               :figwheel     {:on-jsload "ajenda.test-page/mount-root"}
               :compiler     {:main          ajenda.dev
                              :asset-path    "/js/out"
                              :output-to     "target/cljsbuild/public/js/app.js"
                              :output-dir    "target/cljsbuild/public/js/out"
                              :source-map-timestamp true
                              :source-map    true
                              :optimizations :none
                              :pretty-print  true}}}}}})
