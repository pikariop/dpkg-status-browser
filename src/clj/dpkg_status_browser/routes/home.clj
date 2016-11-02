(ns dpkg-status-browser.routes.home
  (:require [dpkg-status-browser.layout :as layout]
            [compojure.core :refer [defroutes GET]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]
            [dpkg-status-browser.service.dpkg-parser :as dpkgstatus]))

(defn home-page []
  (layout/render
    "package-list.html"
    {:packages (dpkgstatus/get-all)}))

(defn package-details [pkg-name]
  (layout/render
    "package-details.html"
    (dpkgstatus/get-package pkg-name)))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/package/:pkg-name" [pkg-name] (package-details pkg-name)))

