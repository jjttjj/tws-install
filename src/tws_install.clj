(ns tws-install
  (:require [hato.client :as hc]
            [clojure.java.io :as io]
            [badigeon.javac :as javac]
            [badigeon.jar :as jar]
            [badigeon.install :as install]
            [clojure.string :as str]
            [clojure.edn :as edn]
            [clojure.java.shell :refer [sh]])
  (:import (java.io File)
           (java.nio.file Files)
           (java.nio.file.attribute FileAttribute)))

;;https://bit.ly/2ZZLpJR
(defn download-unzip [url dir]
  (let [stream (-> (hc/get url {:as :stream}) :body java.util.zip.ZipInputStream.)]
    (loop [entry (.getNextEntry stream)]
      (if entry
        (let [savePath (.resolve dir (.getName entry))
              saveFile (.toFile savePath)]
          (if (.isDirectory entry)
            (when-not (.exists saveFile) (.mkdirs saveFile))
            (let [parentDir (.toFile (.getParent savePath))]
              (if-not (.exists parentDir) (.mkdirs parentDir))
              (io/copy stream saveFile)))
          (recur (.getNextEntry stream)))))))

(defn compile-tws-source [source-path output-path]
  (javac/javac (str (.resolve source-path "IBJts/source/JavaClient"))
    {:compile-path  (str output-path)
     :javac-options ["-cp" (str (.resolve source-path "IBJts/source/JavaClient")) "-parameters" ]}))

;;https://bit.ly/2WafGob
(def ^"[Ljava.nio.file.attribute.FileAttribute;" tmp-attrs
  (into-array FileAttribute []))

(defn -main [& args]
  (let [{:strs [mvn-suffix]
         :or   {mvn-suffix "-with-parameters"}
         :as   args}
        (apply hash-map
          (map #(try (let [ret (edn/read-string %)]
                       (if (symbol? ret) % ret))
                     (catch Exception _ %))
            args))
        tws-version "979.01"
        dl-url      (format
                      "http://interactivebrokers.github.io/downloads/twsapi_macunix.%s.zip"
                      tws-version)
        dl-dir      (Files/createTempDirectory "tws-source" tmp-attrs)
        target-dir  (Files/createTempDirectory "iboga" tmp-attrs)
        class-dir   (.resolve target-dir "classes")
        jar-path    (.resolve target-dir "tws-api.jar")
        mvn-id      'com.interactivebrokers/tws-api
        mvn-version (str tws-version mvn-suffix)]
    (println "Downloading tws-api...")
    (download-unzip dl-url dl-dir)
    (println "Compiling tws-api...")
    (compile-tws-source dl-dir class-dir)
    (println "Building jar...")

    (sh "jar"  "cf" (str jar-path) "-C" (str class-dir) ".")

    ;;doesn't work properly, using shell above
    #_(jar/jar mvn-id {:mvn/version mvn-version}
      {:out-path (str jar-path)
       :paths    [(str class-dir)]
       :deps     {}})
    (println (str "Installing jar to local maven at " mvn-id "/" mvn-version))
    
    (install/install mvn-id {:mvn/version mvn-version}
      (str jar-path)
      (str (.resolve dl-dir "IBJts/source/JavaClient/pom.xml")))
    (println "DONE")))


;;usage: run
;;clj -Sdeps '{:deps {tws-install {:local/root "tws-install"}}}' -m tws-install
;;powershell
;;clj -Sdeps '{:deps {tws-install {:local/root ""tws-install""}}}' -m tws-install
