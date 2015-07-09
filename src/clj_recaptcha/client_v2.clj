(ns clj-recaptcha.client-v2
    (:require [clj-http.client :as client]
              [clojure.data.json :as json]
              [clj-recaptcha.common :as common]))

(defn- parse-response [^String response]
  (if (seq response)
    (let [parsed-response (json/read-str response :key-fn keyword)
          valid (:success parsed-response)
          error (:error-codes parsed-response)]
      {:valid? valid :error error})
    {:valid? false :error "incorrect-captcha-sol"}))

(defn verify
      "Verifies a user's answer for a reCAPTCHA challenge.

       secret   - your secret provided by Google when you register
       response - the value of recaptcha_response_field sent via the form

       Optional parameters:
          :remote-ip          - the IP address of the user who solved the CAPTCHA -optional version 2
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

