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

(ns nvd.config-test
  (:require
   [clojure.java.io :as io]
   [clojure.java.shell :refer [sh]]
   [clojure.string :as string]
   [clojure.test :refer [are deftest is testing]]
   [nvd.config :as sut])
  (:import
   (java.util UUID)))

(def dependency-check-version
  (let [dependencies (-> "project.clj" io/file slurp read-string (nth 10))
        _ (assert (vector? dependencies))
        _ (assert (vector? (first dependencies)))
        found (->> dependencies
                   (some (fn [[d v]]
                           (when (= d 'org.owasp/dependency-check-core)
                             v))))]
    (assert (string? found))
    found))

(deftest check-app-name
  (is (= "stdin" (sut/app-name {:nome "hello-world" :version "0.0.1"})))
  (is (= "hello-world" (sut/app-name {:name "hello-world" :version "0.0.1"})))
  (is (= "hello-world" (sut/app-name {:name "hello-world" :group "hello-world" :version "0.0.1"})))
  (is (= "fred/hello-world" (sut/app-name {:name "hello-world" :group "fred" :version "0.0.1"}))))

(deftest check-with-config
  (let [expected-suppression-filename "suppress.xml"]
    (try
      (sut/with-config [project "test/resources/opts.json"]
        (let [path (-> project (get-in [:nvd :data-directory]) io/file .getAbsolutePath)
              suffix-1 (-> dependency-check-version
                           (string/split #"\.")
                           first
                           (str ".0"))
              suffix-2 (->> (string/split dependency-check-version #"\.")
                            (take 2)
                            (string/join "."))
              expected-1 (str "/.m2/repository/org/owasp/dependency-check-utils/"
                              dependency-check-version
                              "/data/"
                              suffix-1)
              expected-2 (str "/.m2/repository/org/owasp/dependency-check-utils/"
                              dependency-check-version
                              "/data/"
                              suffix-2)]
          (is (or (.endsWith path expected-1)
                  (.endsWith path expected-2)
                  (.endsWith path "9.0") ;; In recent releases, there's e.g. .../org/owasp/dependency-check-utils/8.0.2/data/7.0 which breaks the traditional match between versions
                  (.endsWith path "7.0"))
              (pr-str {:expected-1 expected-1
                       :expected-2 expected-2
                       :actual path})))
        (is (= (get-in project [:nvd :suppression-file]) expected-suppression-filename))
        (is (false? (get-in project [:nvd :analyzer :assembly-enabled])))
        (is (true? (get-in project [:nvd :analyzer :cmake-enabled])))
        (is (not (nil? (get-in project [:engine]))))
        (is (= (get project :title) "barry-fungus/test-project 1.0.3-SNAPSHOT"))
        (is (= (get project :config-file) "test/resources/opts.json"))
        (is (= (get project :cmd-args) "12 -t hello"))
        (is (= (get project :classpath) ["file1.jar", "file2.jar"])))
      (finally
        (-> expected-suppression-filename io/file .delete)))))

(deftest maybe-create-edn-file!
  (-> sut/default-edn-config-filename io/file .delete)

  (try
    (testing "Does not overwrite a config file with a name different from the default filename"
      (let [other-filename (-> (UUID/randomUUID) (str ".edn"))
            content (-> (UUID/randomUUID) str)]
        (try
          (spit other-filename content)
          (sut/maybe-create-edn-file! other-filename)
          (is (= content
                 (slurp other-filename)))
          (finally
            (-> other-filename io/file .delete)))))

    (testing "Does not overwrite a config file that happened to have the default filename"
      (let [content (-> (UUID/randomUUID) str)]
        (spit sut/default-edn-config-filename content)
        (sut/maybe-create-edn-file! sut/default-edn-config-filename)
        (is (= content
               (slurp sut/default-edn-config-filename)))))

    (-> sut/default-edn-config-filename io/file .delete)

    (testing "Writes the default content when the file didn't exist"
      (sut/maybe-create-edn-file! sut/default-edn-config-filename)
      (is (= @sut/default-config-content
             (slurp sut/default-edn-config-filename))))

    (finally
      (-> sut/default-edn-config-filename io/file .delete)
      ;; restore the file, which is version-controlled and necessary for .github/integration_test.sh to succeed:
      (sh "git" "checkout" sut/default-edn-config-filename))))

(deftest maybe-create-suppression-file!
  (let [distinct-content                (-> (UUID/randomUUID) str)
        existing-file                   (-> (UUID/randomUUID) (str ".xml") (doto (spit distinct-content)))
        non-existing-file               (-> (UUID/randomUUID) (str ".xml"))
        existing-folder                 (-> (UUID/randomUUID) str)
        non-existing-folder             (-> (UUID/randomUUID) str)
        _                               (-> existing-folder io/file .mkdirs)
        existing-file-within-folder     (-> (str (io/file existing-folder
                                                          (-> (UUID/randomUUID) (str ".xml"))))
                                            (doto (spit distinct-content)))
        non-existing-file-within-folder (str (io/file non-existing-folder
                                                      (-> (UUID/randomUUID) (str ".xml"))))]

    (assert (not (-> non-existing-file io/file .exists)))
    (assert (not (-> non-existing-folder io/file .exists)))
    (assert (not (-> non-existing-file-within-folder io/file .exists)))

    (try
      (testing "Creates files and intermediate directories only when appropiate"
        (are [input expected] (testing input
                                (sut/maybe-create-suppression-file! {:suppression-file input})
                                (is (= expected
                                       (slurp input)))
                                true)
          existing-file                   distinct-content
          existing-file-within-folder     distinct-content
          non-existing-file               @sut/default-suppression-content
          non-existing-file-within-folder @sut/default-suppression-content))

      (is (-> non-existing-file-within-folder io/file .exists)
          "An intermediate folder can get created")

      (finally
        (-> existing-file io/file .delete)
        (-> existing-file-within-folder io/file .delete)
        (-> non-existing-file io/file .delete)
        (-> non-existing-file-within-folder io/file .delete)
        (-> existing-folder io/file .delete)
        (-> non-existing-folder io/file .delete)))))
