## tangly OS Components

The _tangly_ open source components are a set of open source libraries released under Apache 2.0 licence and are available through maven central.
The components are

* [*BDD*](net.tangly.bdd/readme.md) behavior driven development library
* [*COMMONS*](net.tangly.commons/readme.md) commons library
* [*DEV*](net.tangly.dev/readme.md) development tools library
  [*FSM*](net.tangly.fsm/readme.md) hierarchical finite state machine library
* [*GLEAM*](net.tangly.gleam/readme.md) declarative model transformation frameworks library. Approaches for TSV and JSON representations are available.
* [*PORTS*](net.tangly.ports/readme.md) ports library providing examples to connect a business domain model to external systems
* [*UI*](net.tangly.ui/readme.md) Vaadin user interface library
* ERP Components
    * [*ERP CRM*](net.tangly.erp.crm/readme.md) business components library
    * [*ERP LEDGER*](net.tangly.erp.ledger/readme.md) business components library
    * [*ERP INVOICES*](net.tangly.erp.invoices/readme.md) business components library
    * [*ERP PRODUCTS*](net.tangly.erp.products/readme.md) business components library
    * [*ERP SHARED*](net.tangly.erp.shared/readme.md) business components library

The documentation is located under [tangly-team](https://tangly-team.bitbucket.io/).

The repository is hosed under bitbucket [tangly OS bitbucket](https://bitbucket.org/tangly-team/tangly-os.git).

A mirror is also hosted under github [tangly OS github](https://github.com/marcelbaumann/tangly-os.git).

The issue tracker for errors, improvements, and corrections is [tangly OS issues](https://bitbucket.org/tangly-team/tangly-os/issues).

For any further question and discussion you can use the forum [tangly-OS-Components](https://groups.google.com/g/tangly-os-components)

Beware we are experimenting with the most current versions of components, libraries and JDK.
The components are stored in a single git repository and are build using a multi-modules gradle build script.

## Contribution

You are welcome to contribute to the product with pull requests on Bitbucket.
You can download the source files from the [bitbucket git repository](https://bitbucket.org/tangly-team/tangly-os.git).
Build  the library with the provided gradle configuration file.

If you find a bug or want to request a feature, please use the [issue tracker](https://bitbucket.org/tangly-team/tangly-os/issues).

## License

The source code is licensed under [Apache license 2.0](https://www.apache.org/licenses/LICENSE-2.0).

The documentation and examples are licensed under [Creative Common (CC Attribution 4.0 International)](https://creativecommons.org/licenses/by/4.0/).

## Compile the product

You shall have access to a https://git-scm.com/[Git] installation to clone the repository and download the files.
You must install https://openjdk.java.net/install/index.html[JDK] version 15 to compile the source code.
The https://gradle.org/[Gradle] build file can be triggered without any supplemental installation with

````shell
./gradlew build
````

The first run will take time because all dependencies will be downloaded from Maven repositories.
Ensure you have enough bandwidth to provide a smooth experience.

All development activities are performed with probably the best Java IDE IntelliJ IDEA.

## Awesome Sponsors and Developers

Corporate sponsors are

* [tangly llc](https://www.tangly.net)

Individual developers are

* [Marcel Baumann](https://linkedin.com/in/marcelbaumann)
