(defproject httprepo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.0.1"]]
  :profiles {:test {:resource-paths ["test-resources"]
                    :dependencies [[com.cemerick/pomegranate "0.0.12-SNAPSHOT"]
                                   [ring "1.0.2"]
                                   [com.cemerick/friend "0.0.7"]]}})
