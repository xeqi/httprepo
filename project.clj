(defproject httprepo "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.0.1"]
                 [environ "0.2.1"]
                 [com.cemerick/friend "0.0.7"]]
  :plugins [[lein-ring "0.6.4"]]
  :ring {:handler httprepo.core/repo-app}
  :profiles {:test {:resource-paths ["test-resources"]
                    :dependencies
                    [[ring/ring-jetty-adapter "1.0.1"]

                     ;; TODO remove exclusions when updated
                     ;; Fix pomegranate http deploy
                     [com.cemerick/pomegranate "0.0.11"
                      :exclusions
                      [[org.apache.maven.wagon/wagon-http]
                       [org.apache.maven.wagon/wagon-provider-api]]]
                     [org.apache.maven.wagon/wagon-http "2.2"]
                     [org.apache.maven.wagon/wagon-provider-api "2.2"]]}})
