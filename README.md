# Kronicle Argo CD Config

[Argo CD](https://argoproj.github.io/argo-cd/) config for the Kronicle live demo 


## Testing Helm config

This command will run tests for all the Helm config in the repo: 

```shell
$ ./gradlew test
```

The following will automatically update the YAML files in the `expected-test-output` directories in this codebase.  
These directories are used to test the Helm config in the repo.  The command can be quicker and less error-prone than
manually updating the `expected-test-output` directories after making changes to the repo's Helm config.  After using
this command, remember to review the changes it makes before committing the changes to the Git repo.  

```shell
$ ./gradlew test -Pupdate
```
