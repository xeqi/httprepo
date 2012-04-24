(ns httprepo.core
  (:require [compojure.core :refer [PUT defroutes]]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
            [compojure.handler :as handler]
            [clojure.java.io :as io]
            [ring.middleware.file :as file]
            [environ.core :refer [env]]))

(def users {"admin" {:username "admin"
                     :password (creds/hash-bcrypt "admin_password")
                     :roles #{::admin}}
            "user" {:username "user"
                    :password (creds/hash-bcrypt "user_password")
                    :roles #{"demo"}}})

(defn save-to-file [sent-file body]
  (-> sent-file
      .getParentFile
      .mkdirs)
  (with-open [wrtr (io/writer sent-file)]
    (.write wrtr (slurp body))))

(defroutes repo-routes
  (PUT ["/:group/:artifact/:file"
        :group #".+" :artifact #"[^/]+" :file #"maven-metadata\.xml[^/]*"]
       {body :body {:keys [group artifact file]} :params}
       (friend/authorize
        #{::admin group}
        (do (save-to-file (io/file (env :repo) group artifact file) body)
            {:status 201 :headers {} :body nil})))
  (PUT ["/:group/:artifact/:version/:file"
        :group #".+" :artifact #"[^/]+" :version #"[^/]+" :file #"[^/]+"]
       {body :body {:keys [group artifact version file]} :params}
       (friend/authorize
        #{::admin group}
        (do (save-to-file (io/file (env :repo) group artifact version file) body)
            {:status 201 :headers {} :body nil}))))

(def repo-app
  (handler/api
   (-> repo-routes
       (friend/authenticate {:credential-fn
                             (partial creds/bcrypt-credential-fn users)
                             :workflows [(workflows/http-basic)]})
       (file/wrap-file (-> (io/file (env :repo))
                           (doto .mkdirs)
                           .getAbsolutePath)))))
