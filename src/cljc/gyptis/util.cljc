(ns gyptis.util
  #?(:clj
     (:require [clojure.data.json :as json])))

(defn merge-spec
  "Similar to how leiningen merges profiles:
  keys are assoc'd, vectors are appended, sets are unioned.
  :replace metadata on b causes the contents of b to completely override a."
  ([a b]
   (let [result (cond
                  (nil? a) b
                  (nil? b) a
                  (:replace (meta b)) b
                  (map? a) (merge-with merge-spec a b)
                  (vector? a) (vec (concat a b))
                  :else b)]
     (if (:replace (meta a))
       ^:replace result
       result)))
  ([a b & more]
   (apply merge-with merge-spec a b more)))

(defn ->json
  "Converts clojure datastructure to json"
  [x]
  #?(:clj
     (json/write-str x)

     :cljs
     (.stringify js/JSON x)))

(defn ensure-key
  "assocs a val of nil to the given key if the map doesn't contain key"
  ([k default m]
   (if (contains? m k)
     m
     (assoc m k default)))
  ([k m]
   (ensure-key k nil m)))

(defn ->epoch-millis
  "Takes a java.util.Date or joda DateTime or js/Date
  instance and returns milliseconds since UNIX epoch. Returns a nil if passed none of those."
  [date]
  #?(:clj
           (condp #(isa? %2 %1) (type date)
             java.util.Date (.getTime date)
             org.joda.time.ReadableInstant (.getMillis date)
             java.lang.Object nil)
     :cljs (when (isa? js/Date (type date))
             (.getTime date))))

(def date? (comp not nil? ->epoch-millis))

(defn dedup
  [xs]
  (second (reduce (fn [[seen?, deduped] x]
                    (if (seen? x)
                      [seen? deduped]
                      [(conj seen? x) (conj deduped x)]))
                  [#{} []] xs)))

(defn last-unique-elem
  [xs]
  (second
    (reduce (fn [[seen last] next]
              (if (seen next)
                [seen last]
                [(conj seen next) next]))
            [#{} nil] xs)))
