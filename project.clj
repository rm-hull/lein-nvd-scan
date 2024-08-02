(defproject nvd-clojure "4.0.1"
  :description "National Vulnerability Database dependency checker"
  :url "https://github.com/rm-hull/nvd-clojure"
  :license {:name "The MIT License (MIT)"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [clansi "1.0.0"]
                 [org.clojure/data.json "2.5.0"]
                 [org.slf4j/slf4j-simple "2.0.10"]
                 [org.owasp/dependency-check-core "10.0.3"]
                 [rm-hull/table "0.7.1"]
                 [trptcolin/versioneer "0.2.0"]
                 ;; Explicitly depend on a certain Jackson, consistently.
                 ;; (See also: https://github.com/jeremylong/DependencyCheck/issues/3441)
                 [com.fasterxml.jackson.core/jackson-databind "2.16.1"]
                 [com.fasterxml.jackson.core/jackson-annotations "2.16.1"]
                 [com.fasterxml.jackson.core/jackson-core "2.16.1"]
                 [com.fasterxml.jackson.module/jackson-module-afterburner "2.16.1"]
                 [org.apache.maven.resolver/maven-resolver-transport-http "1.9.18" #_"Fixes a CVE"]
                 [org.yaml/snakeyaml "2.2" #_"Fixes a CVE"]
                 [org.apache.maven/maven-core "3.9.6" #_"Fixes a CVE"]
                 [org.eclipse.jetty/jetty-client "12.0.5" #_"Fixes a CVE" :exclusions [org.slf4j/slf4j-api]]
                 [org.apache.maven.resolver/maven-resolver-spi "1.9.18" #_"Satisfies :pedantic?"]
                 [org.apache.maven.resolver/maven-resolver-api "1.9.18" #_"Satisfies :pedantic?"]
                 [org.apache.maven.resolver/maven-resolver-util "1.9.18" #_"Satisfies :pedantic?"]
                 [org.apache.maven.resolver/maven-resolver-impl "1.9.18" #_"Satisfies :pedantic?"]
                 [org.apache.maven/maven-resolver-provider "3.9.6" #_"Satisfies :pedantic?"]
                 [org.codehaus.plexus/plexus-utils "4.0.0" #_"Satisfies :pedantic?"]]
  :managed-dependencies [[com.google.code.gson/gson "2.10.1"]]
  :scm {:url "git@github.com:rm-hull/nvd-clojure.git"}
  :source-paths ["src"]
  :jar-exclusions [#"(?:^|/).git"]
  :codox {:source-paths ["src"]
          :output-path "doc/api"
          :source-uri "https://github.com/rm-hull/nvd-clojure/blob/main/{filepath}#L{line}"}
  :min-lein-version "2.8.1"
  :target-path "target/%s"
  :jvm-opts ["-Dclojure.main.report=stderr"]
  :profiles {:dev {:plugins [[lein-cljfmt "0.7.0"]
                             [lein-codox "0.10.7"]
                             [lein-cloverage "1.2.3"]
                             [lein-ancient "0.7.0"]
                             [jonase/eastwood "1.4.0"]]
                   :eastwood {:add-linters [:boxed-math
                                            :performance]}
                   :dependencies [[clj-kondo "2023.12.15"]
                                  [commons-collections "20040616"]]}
             :ci {:pedantic? :abort}
             :clj-kondo {:dependencies [[clj-kondo "2023.12.15"]]}
             :skip-self-check {:jvm-opts ["-Dnvd-clojure.internal.skip-self-check=true"]}}
  :deploy-repositories [["clojars" {:url "https://clojars.org/repo"
                                    :username :env/clojars_username
                                    :password :env/clojars_password
                                    :sign-releases false}]])
