(ns dpkg-status-browser.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[dpkg-status-browser started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[dpkg-status-browser has shut down successfully]=-"))
   :middleware identity})
