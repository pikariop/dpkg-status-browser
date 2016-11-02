(ns dpkg-status-browser.service.dpkg-parser
  (:require [clojure.string :as str]))

; Example file. The filename should be configurable in env/[env]/resources/config.edn 
(defn read-dpkg-status
  "Read the example status file"
  []
  (slurp "dpkgstatus"))

(defn parse-dpkg-status
  "Reconstruct the status file to a clojure map"
  []
  (let [package->map (fn [pkg-status-line]
                       (->> (re-seq #"(\S+):\s(.+)" pkg-status-line)
                            (map (fn [[_ key value]] [(keyword key) value]))
                            (into {})))
        status->packages (fn [dpkg-status]
                           (-> dpkg-status
                             (str/replace "\n .\n" "<br /><br />")
                             (str/replace "\n " "<br /> ")
                             (str/split #"\n\n")))]
  (map package->map
       (status->packages (read-dpkg-status)))))

(defn parse-dependencies
  "Update the status map's `Depends` field with sanitized package names"
  []
  (let [deps->map (fn [& dependencies] (into [] dependencies))

        ; Sanitize package names from version numbers etc.
        ; https://www.debian.org/doc/debian-policy/ch-controlfields.html#s-f-Source
        pkg-name-canonical (fn [pkg-seq]
                             (map #(first (re-find #"([a-z]{1}[a-z0-9-_\+\.]+)" %))
                                  pkg-seq))

        parse-alt-deps (fn [deps]
                         (str/split deps #"\|"))

        split-deps-str (fn [pkg-dependencies]
                           (str/split pkg-dependencies #","))

        update-deps-field (fn [deps-str]
                            (if (some? deps-str)
                              (->> (split-deps-str deps-str)
                                   (map parse-alt-deps)
                                   (map pkg-name-canonical)
                                   (distinct)
                                   (map #(apply deps->map %))
                                   (into []))))]

    (map #(update % :Depends update-deps-field)
         (parse-dpkg-status))))


(defn find-reverse-deps
  "Find packages that depend on `pkg-name`"
  [pkg-name]
  (let [contains-match? (fn [pkg-deps]
                     (< 0
                        (count
                          (filter some?
                                  (map #(some #{pkg-name} %)
                                       pkg-deps)))))]
    (map :Package
         (map #(select-keys % [:Package])
              (filter #(contains-match? (:Depends %))
                      (parse-dependencies))))))

; Sorry for the messy function - ran out of time.
; This should be optimized to not call `parse-dependencies` for each item in `parse-dependencies` itself.
(defn get-package
  "Get a single package by name"
  [pkg-name]
  (let [find-package (fn [pkg-name]
                       (first
                         (filter (fn [dpkg-package]
                                   (= pkg-name (:Package dpkg-package)))
                                 (parse-dependencies))))
        select-status-keys (fn [pkg-name]
                             (select-keys
                               (find-package pkg-name)
                               [:Package :Description :Depends]))
        assoc-reverse-deps (fn [pkg-map]
                             (if (not (empty? pkg-map))
                                 (assoc pkg-map
                                        :Reverse-Dependencies
                                        (into [] (find-reverse-deps pkg-name)))))
        update-installed-status (fn [dependencies]
                                  (map (fn [dep-seq]
                                         (map (fn [dep]
                                                (into {} [[:Package dep]
                                                          [:Installed (some? (find-package dep))]]))
                                              dep-seq))
                                       dependencies))]
    (-> pkg-name
        (select-status-keys)
        (assoc-reverse-deps)
        (update :Depends update-installed-status))))


(defn get-all
  "Get the names of all packages"
  []
  (sort (map :Package (parse-dependencies))))

