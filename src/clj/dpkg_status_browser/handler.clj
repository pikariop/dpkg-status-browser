(ns dpkg-status-browser.handler
  (:require [compojure.core :refer [routes wrap-routes]]
            [dpkg-status-browser.layout :refer [error-page]]
            [dpkg-status-browser.routes.home :refer [home-routes]]
            [compojure.route :as route]
            [dpkg-status-browser.env :refer [defaults]]
            [mount.core :as mount]
            [dpkg-status-browser.middleware :as middleware]))

(mount/defstate init-app
                :start ((or (:init defaults) identity))
                :stop  ((or (:stop defaults) identity)))

(def app-routes
  (routes
    (-> #'home-routes
        (wrap-routes middleware/wrap-csrf)
        (wrap-routes middleware/wrap-formats))
    (route/not-found
      (:body
        (error-page {:status 404
                     :title "page not found"})))))


(defn app [] (middleware/wrap-base #'app-routes))
