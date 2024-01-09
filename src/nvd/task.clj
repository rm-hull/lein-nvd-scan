;; The MIT License (MIT)
;;
;; Copyright (c) 2021- Richard Hull
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

(ns nvd.task
  "Clojure CLI tool entry points: `check`."
  (:require
    [clojure.tools.deps :as deps]
    [clojure.tools.deps.util.session :as session]
    [nvd.task.check :refer [-main]]))

(defn- get-classpath [{:keys [aliases]}]
  (let [{:keys [root-edn user-edn project-edn]} (deps/find-edn-maps "deps.edn")
        master-edn (deps/merge-edns [root-edn user-edn project-edn])
        aliases (or aliases [])
        combined-aliases (deps/combine-aliases master-edn aliases)
        basis (session/with-session
                (deps/calc-basis master-edn {:resolve-args   (merge combined-aliases {:trace true})
                                             :classpath-args combined-aliases}))]
    (deps/join-classpath (:classpath-roots basis))))

(defn check
  "Arguments:
    `:config-filename` (optional),
    `:classpath` (optional, defaults to the classpath of deps.edn in the current directory)
    `:aliases` (optional, defaults to [])."
  [{:keys [config-filename classpath] :as opts}]
  (-main (or config-filename "") (or classpath
                                     (get-classpath opts))))
