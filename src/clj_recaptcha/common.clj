(ns clj-recaptcha.common (:require  [clj-http.conn-mgr :as manager]))

(def http-api  "http://www.google.com/recaptcha/api")
(def https-api "https://www.google.com/recaptcha/api")

(defn create-conn-manager
  "Creates a reusable connection manager.

   It's just a shortcut for clj-http.conn-mgr/make-reusable-conn-manager.

   Example:
       (create-conn-manager {:timeout 5 :threads 4})"
  [opts]
  (manager/make-reusable-conn-manager opts))

(defn to-json
  "A JSON serializer for simple Clojure maps."
  [o]
  (cond
   (string? o)
   (str "'" o "'")

   (keyword? o)
   (name o)

   (map? o)
   (let [obj-str (->>
                   (for [[k v] o]
                        (str (to-json k) ":" (to-json v)))
                   (interpose ", ")
                   (apply str))]
        (str "{" obj-str "}"))

   :default
   o))
