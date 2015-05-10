(ns doo.core
  (:require [clojure.java.shell :refer [sh]]
            [clojure.java.io :as io]
            [clojure.pprint :refer [pprint]]))

;; Inside this ns all js-envs are keywords.
(def js-envs #{:phantom :slimer :node})

(defn valid-js-env? [js-env]
  {:pre [(keyword? js-env)]}
  (contains? js-envs js-env))

(defn assert-js-env [js-env]
  (assert (valid-js-env? js-env)
    (str "The js-env should be one of: "
      (clojure.string/join ", " (map name js-envs))
      " and we got: " js-env)))

(def base-dir "runners/")

(defn get-resource [rs]
  (.getPath (io/resource (str base-dir rs))))

(defn js->command [js]
  {:pre [(keyword? js)]
   :post [(not (nil? %))]}
  (case js
    :phantom ["phantomjs" (get-resource "unit-test.js")
              (get-resource "phantomjs-shims.js")]
    :slimer ["slimerjs" (get-resource "unit-test.js")]
    :node "node"))

(def valid-optimizations #{:simple :whitespace :advanced})

(defn valid-complier-opts? [opts]
  {:pre [(map? opts)]}
  (contains? valid-optimizations (:optimizations opts)))

(defn assert-compiler-opts [opts]
  (assert (valid-complier-opts? opts)
    (str ":optmimizations should be one of: "
      (clojure.string/join ", " (map str valid-optimizations))
      ". It currently is " (:optimizations opts))))

(defn run-script [js-env compiler-opts]
  {:pre [(valid-js-env? js-env)]}
  (let [r (apply sh (conj (js->command js-env)
                      (:output-to compiler-opts)))]
    (println (:out r))))