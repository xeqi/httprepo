(ns httprepo.core
  (:require [compojure.core :refer [PUT defroutes]]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [compojure.handler :as handler]
            [clojure.java.io :as io]
            [ring.middleware.file :as file]
            [environ.core :refer [env]]))

(def users {"root" {:username "root"
                    :password (creds/hash-bcrypt "admin_password")
                    :roles #{::admin}}})

(defroutes repo-routes
  (PUT "*" request
       (do (let [sent-file (io/file (str (env :repo)
                                         (:* (:route-params request))))]
             (-> sent-file
                 .getParentFile
                 .mkdirs)
             (with-open [wrtr (io/writer sent-file)]
               (.write wrtr (slurp (:body request)))))
           {:status 201 :headers {} :body nil})))

(def repo-app
  (handler/api
   (-> (friend/wrap-authorize repo-routes #{::admin})
       (friend/authenticate {:credential-fn
                             (partial creds/bcrypt-credential-fn users)
                             :workflows [(workflows/http-basic)]})
       (file/wrap-file (-> (io/file (env :repo))
                           (doto .mkdirs)
                           .getAbsolutePath)))))
