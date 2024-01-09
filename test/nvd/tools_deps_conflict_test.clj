(ns nvd.tools-deps-conflict-test
  (:require [clojure.set :as set]
            [clojure.test :as t]
            [clojure.tools.deps :as deps]
            [clojure.edn :as edn]
            [clojure.tools.deps.util.session :as session]))

(defn get-libs
  [deps]
  (let [master-edn (merge {:mvn/repos {"central" {:url "https://repo1.maven.org/maven2/"}, "clojars" {:url "https://repo.clojars.org/"}}}
                          {:deps deps})
        combined-aliases (deps/combine-aliases master-edn [])
        basis (session/with-session
                (deps/calc-basis master-edn {:resolve-args   (merge combined-aliases {:trace true})
                                             :classpath-args combined-aliases}))
        libs (:libs basis)]
    (into (sorted-map) libs)))

(defn show-diff [lib]
  (let [base-deps (-> (edn/read-string (slurp "deps.edn"))
                      :deps)
        new-dep (get-libs {lib (get base-deps lib)})
        base-deps (get-libs (dissoc base-deps lib))
        td-set (into #{} (keys new-dep))
        base-set (into #{} (keys base-deps))]
    (doseq [shared-lib (into (sorted-set) (set/intersection td-set base-set))]
      (when-not (= (get-in new-dep [shared-lib :mvn/version])
                   (get-in base-deps [shared-lib :mvn/version]))
        (println shared-lib
                 "deps.edn:" (get-in base-deps [shared-lib :mvn/version])
                 "vs"
                 (str lib ":") (get-in new-dep [shared-lib :mvn/version]))))))

(comment
  (show-diff 'org.clojure/tools.deps))

(t/deftest tools-deps-conflict-test
  (let [td-deps (get-libs {'org.clojure/tools.deps {:mvn/version "0.18.1354"}})
        base-deps (get-libs (-> (edn/read-string (slurp "deps.edn"))
                                :deps
                                (dissoc 'org.clojure/tools.deps)))
        td-set (into #{} (keys td-deps))
        base-set (into #{} (keys base-deps))]
    (doseq [shared-lib (into (sorted-set) (set/intersection td-set base-set))]
      (when-not (= (get-in td-deps [shared-lib :mvn/version])
                   (get-in base-deps [shared-lib :mvn/version]))
        (println shared-lib
                 (get-in base-deps [shared-lib :mvn/version])
                 "vs"
                 (get-in td-deps [shared-lib :mvn/version]))))))

