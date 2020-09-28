# TWS Install

Utility for installing Interactive Brokers API client.

## Usage

This utility will download the IB Java client code from [here](http://interactivebrokers.github.io/), compile it with the javac using the `-parameters` flag, and install it to your local maven repo.

To use:
```
clj -Sdeps '{:deps {tws-install/tws-install {:git/url "https://github.com/jjttjj/tws-install.git" :sha "76cb2184155eb1a08a35232aceb2d686739d419f"}}}' -m tws-install

```

Or, with powershell:
```
clj -Sdeps '{:deps {tws-install/tws-install {:git/url ""https://github.com/jjttjj/tws-install.git"" :sha ""76cb2184155eb1a08a35232aceb2d686739d419f""}}}' -m tws-install

```

You can now use the api client in your deps.edn as follows:

```
com.interactivebrokers/tws-api {:mvn/version "979.01-with-parameters"}
```
