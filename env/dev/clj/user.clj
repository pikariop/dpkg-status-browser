(ns user
  (:require [mount.core :as mount]
            dpkg-status-browser.core))

(defn start []
  (mount/start-without #'dpkg-status-browser.core/repl-server))

(defn stop []
  (mount/stop-except #'dpkg-status-browser.core/repl-server))

(defn restart []
  (stop)
  (start))


