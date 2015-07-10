(ns clj-recaptcha.client-v2-test
    (:require [clojure.test :refer :all]
      [clj-recaptcha.client-v2 :as c]))

(deftest parse-response-test
  (let [parse-response #'c/parse-response]
    (testing "Recognises valid response"
      (let [res (parse-response "{\"success\" : true}")]
        (is (= {:valid? true, :error nil} res))))

    (testing "Survives non-JSON response"
      (let [res (parse-response "<html><body>404 Page not found</body></html>")]
        (is (= {:valid? false :error "recaptcha-not-reachable"} res))))

    (testing "Recognises invalid response"
      (let [res (parse-response "{\"success\" : false ,\"error-codes\": [ \"invalid-input-secret\"]}")]
        (is (= {:valid? false, :error ["invalid-input-secret"]} res))))))

(deftest verify
  (testing "No challenge or response is given - version 2"
    (let [res (c/verify "KEY" nil)]
         (is (= {:valid? false :error "incorrect-captcha-sol"} res))))

  (testing "Handles exceptions - version 2"
    (with-redefs-fn {#'clj-http.client/post (fn [_url _query] (throw (Exception. "Troubles!")))}
                     #(let [res (c/verify "KEY" "123")]
                          (is (= {:valid? false :error "recaptcha-not-reachable"} res)))))

  (testing "Successful case - version 2"
    (with-redefs-fn {#'clj-http.client/post (fn [url query]
                                              (when (and (= url "https://www.google.com/recaptcha/api/siteverify")
                                                         (= query {:form-params        {:secret   "KEY"
                                                                                        :response "678"}
                                                                   :proxy-host         nil
                                                                   :proxy-port         nil
                                                                   :connection-manager nil}))
                                                {:status 200
                                                 :body   "{\"success\" : true}\n"}))}
                    #(let [res (c/verify "KEY" "678")]
                          (is (= {:valid? true :error nil} res)))))

  (testing "Incorrect user's input - API version 2"
    (with-redefs-fn {#'clj-http.client/post (fn [url query]
                                             (when (and (= url "https://www.google.com/recaptcha/api/siteverify")
                                                        (= query {:form-params        {:secret   "KEY"
                                                                                       :response "678"}
                                                                  :proxy-host         "localhost"
                                                                  :proxy-port         456
                                                                  :connection-manager :CM}))
                                                   {:status 200
                                                    :body   "{\"success\" : false ,\"error-codes\": [ \"some-error-code\"]}"}))}
                 #(let [res (c/verify "KEY" "678" :proxy-host "localhost" :proxy-port 456 :connection-manager :CM)]
                       (is (= {:valid? false :error ["some-error-code"]} res))))))
