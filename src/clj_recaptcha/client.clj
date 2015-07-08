(ns clj-recaptcha.client
  (:require [clj-http.client :as client]
            [clj-http.conn-mgr :as manager]))

(def http-api  "http://www.google.com/recaptcha/api")
(def https-api "https://www.google.com/recaptcha/api")

(defn- parse-response
  [^String response]
  (if-not (nil? response)
    (let [[valid error] (clojure.string/split-lines response)
          valid         (= "true" valid)]
      {:valid? valid :error error})
    {:valid? false :error "incorrect-captcha-sol"}))

(defn verify
  "Verifies a user's answer for a reCAPTCHA challenge.

   private-key - your private key
   response    - the value of recaptcha_response_field sent via the form

   Optional parameters:
      :version            - version of the recaptcha protocol - 2 (default) or 1
      :remote-ip          - the IP address of the user who solved the CAPTCHA - required version 1, optional version 2
      :challenge          - the value of recaptcha_challenge_field sent via the form (needed for version 1 only)
      :ssl?               - use HTTPS or HTTP? (default false)
      :proxy-host         - a proxy host
      :proxy-port         - a proxy port
      :connection-manager - a connection manager to be used to speed up requests"
  [private-key response  & {:keys [ssl? proxy-host proxy-port connection-manager version challenge remote-ip]
                                               :or {ssl? false, version 2}}]
  (if-not (or (and (= version 1) (empty? challenge))
              (empty? response))
    (try
      (let [endpoint (if ssl? https-api http-api)
            post-v1 (fn []
                      (let [endpoint (str endpoint "/verify")]
                        (client/post endpoint {:form-params        {:privatekey private-key
                                                                    :remoteip   remote-ip
                                                                    :challenge  challenge
                                                                    :response   response}
                                               :proxy-host         proxy-host
                                               :proxy-port         proxy-port
                                               :connection-manager connection-manager})))

            post-v2 (fn []
                      (let [endpoint (str endpoint "/siteverify")
                            form-params {:secret   private-key
                                         :response response}
                            form-params (if remote-ip (assoc form-params :remoteip remote-ip) form-params)]
                        (client/post endpoint {:form-params        form-params
                                               :proxy-host         proxy-host
                                               :proxy-port         proxy-port
                                               :connection-manager connection-manager})))

            resp  (if (= version 2) (post-v2) (post-v1))]
        (parse-response (:body resp)))
      (catch Exception ex
        {:valid? false :error "recaptcha-not-reachable"}))
    {:valid? false :error "incorrect-captcha-sol"}))

(defn create-conn-manager
  "Creates a reusable connection manager.

   It's just a shortcut for clj-http.conn-mgr/make-reusable-conn-manager.
   
   Example:
       (create-conn-manager {:timeout 5 :threads 4})"
  [opts]
  (manager/make-reusable-conn-manager opts))

(defn- to-json
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


(defn render
  "Renders the HTML snippet to prompt reCAPTCHA.

  public-key - your public key

  Optional parameters:
      :error         - an error message to display (default nil)
      :ssl?          - use HTTPS or HTTP? (default false)
      :noscript?     - include <noscript> content (default true)
      :display       - a map of attributes for reCAPTCHA custom theming (default nil)
      :iframe-height - the height of noscript iframe (deafult 300)
      :iframe-width  - the width of noscript iframe (default 500)"
  [public-key & {:keys [error ssl? noscript? display iframe-height iframe-width]
                 :or {ssl? false noscript? true iframe-height 300 iframe-width 500}}]
  (let [endpoint (if ssl? https-api http-api)
        error    (if (nil? error) "" (str "&error=" error))]
    (str (when display
           (str "<script type='text/javascript'>var RecaptchaOptions=" (to-json display) ";</script>"))
         (str "<script type='text/javascript' src='" endpoint "/challenge?k=" public-key error "'></script>")
         (when (true? noscript?) 
           (str "<noscript>"
                (str "<iframe src='" endpoint "/noscript?k=" public-key error "' height='" iframe-height "' width='" iframe-width "' frameborder='0'></iframe><br/>")
                "<textarea name='recaptcha_challenge_field' rows='3' cols='40'></textarea>"
                "<input type='hidden' name='recaptcha_response_field' value='manual_challenge'/>"
                "</noscript>")))))
