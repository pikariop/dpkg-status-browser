(ns dpkg-status-browser.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [dpkg-status-browser.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[dpkg-status-browser started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[dpkg-status-browser has shut down successfully]=-"))
   :middleware wrap-dev})
