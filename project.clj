(defproject analytics-clj "0.3.0"
  :description "Idiomatic Clojure wrapper for the segment.io Java client"
  :url "http://ardoq.com"
  :license {:name "MIT License"
            :url "http://opensource.org/licenses/MIT"}
  :plugins [[codox "0.6.6"]]
  :codox {:src-linenum-anchor-prefix "L"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.github.segmentio/analytics "1.0.4"]]
  :profiles {:dev {:dependencies [[ch.qos.logback/logback-classic "1.0.13"]
                                  [ch.qos.logback/logback-core "1.0.13"]
                                  [org.slf4j/jcl-over-slf4j "1.7.5"]]}})
