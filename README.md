# httprepo

A proof of concept http based maven repo with an admin user for deploy.

## Usage

```REPO=/path/to/repo lein ring server-headless```

In leiningen use:

```
{"httprepo" "http://localhost:3000"}
```
for full read access.

```
{"httprepo" {:url "http://localhost:3000"
             :username "user"
             :password "user_password"}}
```
to get full read access, and deploy to the "demo" groupid access.

```
{"httprepo" {:url "http://localhost:3000"
             :username "admin"
             :password "admin_password"}}
```
to get full read + deploy access.

## Testing

Using leiningen v2: ```REPO=/tmp/httprepo/remote-repo lein test```

## License

Copyright © 2012 Nelson Morris

Distributed under the Eclipse Public License, the same as Clojure.
