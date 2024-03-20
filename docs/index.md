vault-catalog
=============
Command line tool to interact with the `dd-vault-catalog` service.

SYNOPSIS
--------

```shell
vaul-catalog [OPTIONS] COMMAND [ARGS]...
```

DESCRIPTION
-----------
This package provides the `vault-catalog` command, to interact with the `dd-vault-catalog` service via its [REST API]{:target=_blank}.

[REST API]: https://dans-knaw.github.io/dd-vault-catalog/swagger-ui/

INSTALLATION AND CONFIGURATION
------------------------------
Currently, this project is built as an RPM package for RHEL8 compatible OSes and later. The RPM will install the binaries to
`/opt/dans.knaw.nl/dd-vault-catalog-cli` and the configuration files to `/etc/opt/dans.knaw.nl/dd-vault-catalog-cli`. The configuration options are documented by
comments in the default configuration file `config.yml`.

To install the module on systems that do not support RPM, you can copy and unarchive the tarball to the target host. You will have to take care of placing the
files in the correct locations for your system yourself. For instructions on building the tarball, see next section.

BUILDING FROM SOURCE
--------------------
Prerequisites:

* Java 17 or higher
* Maven 3.6.3 or higher
* RPM

Steps:

```shell 
git clone https://github.com/DANS-KNAW/dd-vault-catalog-cli.git
cd dd-vault-catalog-cli
mvn clean install
```

If the `rpm` executable is found at `/usr/local/bin/rpm`, the build profile that includes the RPM packaging will be activated. If `rpm` is available, but at a
different path, then activate it by using Maven's `-P` switch: `mvn -Pprm install`.

Alternatively, to build the tarball execute:

```bash
mvn clean install assembly:single
```

