(ns dpkg-status-browser.test.handler
  (:require [clojure.test :refer :all]
            [clojure.string :as str]
            [ring.mock.request :refer :all]
            [dpkg-status-browser.handler :refer :all]
            [dpkg-status-browser.service.dpkg-parser :as dpkgstatus]))

(deftest test-app
  (testing "main route"
    (let [response ((app) (request :get "/"))]
      (is (= 200 (:status response)))))

  (testing "a package route"
    (let [response ((app) (request :get "/package/libc6"))]
      (is (= 200 (:status response)))))

  (testing "not-found route"
    (let [response ((app) (request :get "/invalid"))]
      (is (= 404 (:status response))))))

(defn has-mandatory-keys?
  "Determine that the function returns a map with these keys present.

   These keys are defined as mandatory in the Debian Policy Manual
   https://www.debian.org/doc/debian-policy/ch-controlfields.html"
  [package-map]
  (and (contains? package-map :Package)
       (contains? package-map :Description)))


(deftest test-status-parsing
  (testing "retrieve all package names"
    (let [pkg-names (dpkgstatus/get-all)]
      ; Does a well-known package name appear in the list?
      (is (some #{"libc6"} pkg-names))))

  (testing "retrieve a package"
    (let [a-package (dpkgstatus/get-package "logrotate")]
     (is (has-mandatory-keys? a-package))))

  (testing "status map contains valid keys"
    (is (every? has-mandatory-keys?
                (dpkgstatus/parse-dpkg-status))))

  (testing "reverse dependencies"
    (let [cron-reverse-deps (dpkgstatus/find-reverse-deps "cron")]
    (is (and (some #{"logrotate"} cron-reverse-deps)
             (not (some #{"asdfasda"} cron-reverse-deps)))))))

; cron is a dependency of logrotate and is installed on the example, should be a link
; anacron is an alternate dependency and it is not installed, therefore it should not be a link
; ubuntu-standard is a reverse-dependency, should be a link
(deftest package-details-ui
  (testing "alt-dependencies rendered correctly"
    (let [logrotate-page (slurp "http://localhost:8080/package/logrotate")]
      (is (and (str/includes? logrotate-page "<a href=\"/package/cron\">cron</a>")
               (str/includes? logrotate-page "<a href=\"/package/ubuntu-standard\">ubuntu-standard</a>")(not (str/includes? logrotate-page "<a href=\"/package/anacron\">anacron</a>")))))))


