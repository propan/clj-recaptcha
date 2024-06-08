(defproject clj-recaptcha "0.0.4-SNAPSHOT"
  :description "a Clojure client for reCAPTCHA API"
  :url "http://github.com/propan/clj-recaptcha"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clj-http "1.1.2" :exclusions [cheshire crouton org.clojure/tools.reader ring/ring-codec]]
                 [org.clojure/data.json "0.2.6"]])
