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
