;; The MIT License (MIT)
;;
;; Copyright (c) 2016- Richard Hull
;;
;; Permission is hereby granted, free of charge, to any person obtaining a copy
;; of this software and associated documentation files (the "Software"), to deal
;; in the Software without restriction, including without limitation the rights
;; to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
;; copies of the Software, and to permit persons to whom the Software is
;; furnished to do so, subject to the following conditions:
;;
;; The above copyright notice and this permission notice shall be included in all
;; copies or substantial portions of the Software.
;;
;; THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
;; IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
;; FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
;; AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
;; LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
;; OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
;; SOFTWARE.

(ns nvd.report
  (:require
   [clansi :refer [style]]
   [clojure.java.io :as io]
   [clojure.string :as s]
   [nvd.log :as log]
   [table.core :refer [table]])
  (:import
   (java.util Arrays)
   (org.owasp.dependencycheck Engine)
   (org.owasp.dependencycheck.dependency Dependency Vulnerability)
   (org.owasp.dependencycheck.exception ExceptionCollection)
   (org.owasp.dependencycheck.reporting ReportGenerator)))

(def default-output-dir "target/nvd")

(defn  generate-report [project]
  (let [^Engine engine (:engine project)
        title (:title project)
        output-dir (get-in project [:nvd :output-dir] default-output-dir)
        output-fmt (get-in project [:nvd :output-format] "ALL")
        db-props (.getDatabaseProperties (.getDatabase engine))
        deps (Arrays/asList (.getDependencies engine))
        analyzers (.getAnalyzers engine)
        settings (.getSettings engine)
        exception-collection (ExceptionCollection.)
        rg (ReportGenerator. title deps analyzers db-props settings exception-collection)]
    (.write rg ^String output-dir ^String output-fmt)
    project))

(defn- score [^Vulnerability vulnerability]
  (let [cvss2 (.getCvssV2 vulnerability)
        cvss3 (.getCvssV3 vulnerability)]
    (cond
      cvss2 (max (double (or (.getExploitabilityScore cvss2)
                             0))
                 (double (or (.getImpactScore cvss2)
                             0)))
      cvss3 (max (double (or (.getExploitabilityScore cvss3)
                             0))
                 (double (or (.getImpactScore cvss3)
                             0)))
      :else (do
              (.warn log/logger (str "No CVSS found for: " (pr-str vulnerability)))
              1))))

(defn- severity [^long cvssScore]
  (cond
    (= cvssScore 0) :none
    (< cvssScore 4) :low
    (>= cvssScore 7) :high
    :else :medium))

(defn- color [severity]
  (get {:none :green :low :cyan :medium :yellow :high :red} severity))

(defn- vulnerable? [^Dependency dep]
  (not-empty (.getVulnerabilities dep)))

(defn- vuln-status [^Dependency dep]
  (if-not (vulnerable? dep)
    (style "OK" :green :bright)
    (s/join ", "
            (for [^Vulnerability v (reverse (sort-by score (.getVulnerabilities dep)))
                  :let [color (-> v score severity color)]]
              (style (.getName v) color :bright)))))

(defn- vulnerabilities [project ^Engine engine]
  (sort-by :dependency
           (for [^Dependency dep (.getDependencies engine)
                 :when (or (vulnerable? dep) (:verbose-summary project))]
             {:dependency (.getFileName dep) :status (vuln-status dep)})))

(defn- scores [^Engine engine]
  (flatten (for [^Dependency dep (.getDependencies engine)
                 ^Vulnerability vuln (.getVulnerabilities dep)]
             (score vuln))))

(defn print-summary [project]
  (let [^Engine engine (:engine project)
        output-dir (get-in project [:nvd :output-dir] default-output-dir)
        summary (vulnerabilities project engine)
        scores  (scores engine)
        highest-score (apply max 0 scores)
        color (-> highest-score severity color)
        severity (-> highest-score severity name s/upper-case)]

    (when (or (:verbose-summary project) (pos? (count scores)))
      (table summary))

    (println)
    (print (count scores) "vulnerabilities detected. Severity: ")
    (println (style severity color :bright))
    (println "Detailed reports saved in:" (style (.getAbsolutePath (io/file output-dir)) :bright))
    (let [file (io/file output-dir "dependency-check-report.html")]
      (when (-> file .exists)
        (println "HTML report :" (style (.getAbsolutePath file) :bright))))
    (println)
    (println (style "   *** THIS REPORT IS WITHOUT WARRANTY ***" :magenta :bright))
    project))

(defn fail-build? [project]
  (let [^Engine engine (:engine project)
        highest-score (long (apply max 0 (scores engine)))
        fail-threshold (long (get-in project [:nvd :fail-threshold] 0))]
    (->
     project
     (assoc-in [:nvd :highest-score] highest-score)
     (assoc :failed? (> highest-score fail-threshold)))))
