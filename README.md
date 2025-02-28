
# To Build

Java 17 should be installed

Build Genome browser

```sh
mvn package -DskipTests
```

# To Run

Change paths in config file webapps/config2.yml

```
repositories:
- genome-browser/data/src/main/resources/repo
- genome-browser/data/src/main/resources/databases
roots:
- repo
- databases
```

"repositories" list should contain absolute path to folders that will be loaded as repositories
"roots" list is a list of top folder names used as tree tab roots. It replaces 'perspective' root fields.


Then launch Genome browser Web edition.

```sh
mvn jetty:run
```

Use your browser to open it at http://localhost:8000/

