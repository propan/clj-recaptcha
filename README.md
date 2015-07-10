# clj-recaptcha [![Build Status](https://travis-ci.org/propan/clj-recaptcha.svg?branch=master)](https://travis-ci.org/propan/clj-recaptcha)

a simple Clojure client for [reCAPTCHA] [1] API (v1.0 and v2.0).

## Usage

Include the library in your leiningen project dependencies:

```clojure
[clj-recaptcha "0.0.2"]
```

## Displaying reCAPTCHA

To make the reCAPTCHA widget appear when your page loads, you will need to insert a snippet of JavaScript & non-JavaScript code in your `<form>` element. To generate the snippet, use:

### reCAPTCHA v1.0

```clojure
(ns your.namespace
    (:require [clj-recaptcha.client :as c]))

(c/render "your-public-key" :ssl? true :display {:theme "clean" :lang "de"})
```

**Optional parameters:**

* :error         - an error message to display (default nil)
* :ssl?          - use HTTPS or HTTP? (default false)
* :noscript?     - include <noscript> content (default true)
* :display       - a map of attributes for reCAPTCHA custom theming (default nil)
* :iframe-height - the height of noscript iframe (deafult 300)
* :iframe-width  - the width of noscript iframe (default 500)

### reCAPTCHA v2.0

```clojure
(ns your.namespace
    (:require [clj-recaptcha.client-v2 :as c]))

(c/render "your-public-key")
```

## Verifying the User's Answer

After your page is successfully displaying reCAPTCHA, you need to configure your form to check whether the answers entered by the users are correct.
Here's how it can be done:

### reCAPTCHA v1.0

```clojure
(ns your.namespace
    (:require [clj-recaptcha.client :as c]))

(c/verify "your-private-key" "challenge" "response" "127.0.0.1")
;; {:valid? false :error "incorrect-captcha-sol"}
```
**Optional parameters:**

* :ssl?               - use HTTPS or HTTP? (default false)
* :proxy-host         - a proxy host
* :proxy-port         - a proxy port
* :connection-manager - a connection manager to be used to speed up requests

### reCAPTCHA v2.0

```clojure
(ns your.namespace
    (:require [clj-recaptcha.client-v2 :as c]))

(c/verify "your-private-key" "response" :remote-ip "127.0.0.1")
;; {:valid? false :error "incorrect-captcha-sol"}
```
**Optional parameters:**

* :remote-ip          - the IP address of the user who solved the CAPTCHA
* :proxy-host         - a proxy host
* :proxy-port         - a proxy port
* :connection-manager - a connection manager to be used to speed up requests

For better performance, you can use a pooled connection manager, that can be passed via :connection-manager option.
To create a connection manager:

```clojure
(create-conn-manager {:threads 5})
```
It's just a shortcut for `clj-http.conn-mgr/make-reusable-conn-manager`, check [clj-http] [2] documentation for more details.


## License

Copyright © 2013 Pavel Prokopenko

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

[1]: https://developers.google.com/recaptcha/intro
[2]: https://github.com/dakrone/clj-http
