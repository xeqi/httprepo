(ns httprepo.core-test
  (:use clojure.test
        httprepo.core
        compojure.core)
  (:require [cemerick.pomegranate.aether :as aether]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [compojure.route :as route]
            [compojure.handler :as handler]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.file :as file]))


(declare test-port)
(def tmp-dir (io/file (System/getProperty "java.io.tmpdir") "httprepo"))
(def tmp-local-repo-dir (io/file tmp-dir "local-repo"))
(def tmp-local-repo2-dir (io/file tmp-dir "local-repo2"))
(def tmp-remote-repo (io/file tmp-dir "remote-repo"))

(def users {"root" {:username "root"
                    :password (creds/hash-bcrypt "admin_password")
                    :roles #{::admin}}})

(defroutes mock-routes
  (PUT "*" request
       (do (let [sent-file (io/file (str (.getAbsolutePath tmp-remote-repo)
                                         (:* (:route-params request))))]
             (-> sent-file
                 .getParentFile
                 .mkdirs)
             (with-open [wrtr (io/writer sent-file)]
               (.write wrtr (slurp (:body request)))))
           {:status 201 :headers {} :body nil})))

(def mock-app
  (handler/api
   (-> (friend/wrap-authorize mock-routes #{::admin})
       (friend/authenticate {:credential-fn
                             (partial creds/bcrypt-credential-fn users)
                             :workflows [(workflows/http-basic)]})
       (file/wrap-file (-> tmp-remote-repo
                           (doto .mkdirs)
                           .getAbsolutePath)))))

(defn- run-test-app
  [f]
  (let [server (jetty/run-jetty #'mock-app {:port 0 :join? false})
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
                         :username "root"
                         :password "admin_password"}}
   :local-repo tmp-local-repo-dir)
  (is (= '{[demo "1.0.0"] nil}
         (aether/resolve-dependencies
          :coordinates '[[demo/demo "1.0.0"]]
          :repositories {"local" {:url (str "http://localhost:" test-port)}}
          :local-repo tmp-local-repo2-dir))))