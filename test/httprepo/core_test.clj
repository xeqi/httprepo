(ns httprepo.core-test
  (:use clojure.test
        httprepo.core)
  (:require [clojure.java.io :as io]
            [cemerick.pomegranate.aether :as aether]
            [ring.adapter.jetty :as jetty]))


(declare test-port)

(def tmp-dir (io/file (System/getProperty "java.io.tmpdir") "httprepo"))
(def tmp-local-repo-dir (io/file tmp-dir "local-repo"))
(def tmp-local-repo2-dir (io/file tmp-dir "local-repo2"))

(defn- run-test-app
  [f]
  (let [server (jetty/run-jetty repo-app {:port 0 :join? false})
        port (-> server .getConnectors first .getLocalPort)]
    (with-redefs [test-port port]
      (try
        (f)
        (finally
         (.stop server))))))

(defn delete-file-recursively
  "Delete file f. If it's a directory, recursively delete all its contents.
Raise an exception if any deletion fails unless silently is true."
  [f]
  (let [f (io/file f)]
    (when (.isDirectory f)
      (doseq [child (.listFiles f)]
        (delete-file-recursively child)))
    (io/delete-file f)))

(use-fixtures :once run-test-app)
(use-fixtures :each (fn [f]
                      (when (.exists tmp-dir)
                        (delete-file-recursively tmp-dir))
                      (f)))

(deftest a-test
  (aether/deploy
   :coordinates '[demo/demo "1.0.0"]
   :jar-file (io/file (io/resource "demo-1.0.0.jar"))
   :pom-file (io/file (io/resource "demo-1.0.0.pom"))
   :repository {"local" {:url (str "http://localhost:" test-port)
                         :username "admin"
                         :password "admin_password"}}
   :local-repo tmp-local-repo-dir)
  (is (= '{[demo "1.0.0"] nil}
         (aether/resolve-dependencies
          :coordinates '[[demo/demo "1.0.0"]]
          :repositories {"local" {:url (str "http://localhost:" test-port)}}
          :local-repo tmp-local-repo2-dir))))