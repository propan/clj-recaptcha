(ns clj-recaptcha.client-v2
    (:require [clj-http.client :as client]
              [clojure.data.json :as json]
              [clj-recaptcha.common :as common]))

(defn- parse-response [^String response]
  (if (seq response)
    (try
      (let [parsed-response (json/read-str response :key-fn keyword)
            valid (:success parsed-response)
            error (:error-codes parsed-response)]
        (merge {:valid? valid :error error}
               (select-keys parsed-response [:score :action :hostname])))
      (catch Exception _
        {:valid? false :error "recaptcha-not-reachable"}))
    {:valid? false :error "incorrect-captcha-sol"}))

(defn verify
      "Verifies a user's answer for a reCAPTCHA challenge.

       secret   - your secret provided by Google when you register
       response - the value of g-recaptcha-response sent via the form

       Optional parameters:
          :remote-ip          - the IP address of the user who solved the CAPTCHA
          :proxy-host         - a proxy host
          :proxy-port         - a proxy port
          :connection-manager - a connection manager to be used to speed up requests"
      [secret response & {:keys [remote-ip proxy-host proxy-port connection-manager]}]
      (if (seq response)
        (try
          (let [endpoint (str common/https-api "/siteverify")
                form-params {:secret   secret
                             :response response}
                form-params (if remote-ip (assoc form-params :remoteip remote-ip) form-params)
                response (client/post endpoint {:form-params        form-params
                                                :proxy-host         proxy-host
                                                :proxy-port         proxy-port
                                                :connection-manager connection-manager})]
               (parse-response (:body response)))
          (catch Exception _ex
            {:valid? false :error "recaptcha-not-reachable"}))
        {:valid? false :error "incorrect-captcha-sol"}))

(defn render
  "Renders the HTML snippet to prompt reCAPTCHA.

   public-key - your public key"
  [public-key]
  (str "<script src='https://www.google.com/recaptcha/api.js' async defer></script>"
       "<div class='g-recaptcha' data-sitekey=" public-key "></div>"))
