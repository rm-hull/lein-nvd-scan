## Changes from 3.6.0 to 4.0.0

* Update `dependency-check-core` to the 9.x series ([9.0.8](https://github.com/jeremylong/DependencyCheck/blob/v9.0.8/CHANGELOG.md))
  * This **requires** nvd-clojure users to request a NVD API key and configure it correctly.
    * You can [obtain an API key](https://nvd.nist.gov/developers/request-an-api-key) in a few minutes - it's an automated process.
    * Then, you can configure it in nvd-clojure by setting it in the `:nvd-api :key` path, or as a `NVD_API_TOKEN` environment variable.

## Changes from 3.5.0 to 3.6.0

* Update `dependency-check-core`.

## Changes from 3.4.0 to 3.5.0

* Update `dependency-check-core`.

## Changes from 3.3.0 to 3.4.0

* Update `dependency-check-core`.

## Changes from 3.2.0 to 3.3.0

* Update `dependency-check-core`.

## Changes from 3.1.0 to 3.2.0

* Update `dependency-check-core`.

## Changes from 3.0.1 to 3.1.0

* Update `dependency-check-core`.

## Changes from 3.0.0 to 3.0.1

* Parse classpaths in a cross-platform manner.
  * Closes [#158](https://github.com/rm-hull/nvd-clojure/issues/158)

## Changes from 2.13.0 to 3.0.0

* Introduce .edn configuration format.
  * .json files will remain working as-is indefinitely.
  * If you wish to migrate to the .edn format, doing so is easy - please see [FAQ](https://github.com/rm-hull/nvd-clojure/blob/v3.2.0/FAQ.md#how-do-i-migrate-from-the-json-config-format-to-edn).
  * If you specify the blank string as the config file to be used, a useful, sample .edn file will be generated.
* Automatically create a .xml suppression file when a `:suppression-file` is specified and no such file exists
  * In practice, this means that on the first run, if you specify the blank string as the config file to be used, two files will be created for you:
    * `nvd-clojure.edn`
    * `nvd_suppressions.xml`
  * After this automatic creation, you are free to tweak and version-control these files as desired.
* Update `dependency-check-core`.

## Changes from 2.12.0 to 2.13.0

* Update `dependency-check-core`.
* Introduce new `[:analyzer :ossindex-warn-only-on-remote-errors]` configuration option.
  * You can set this option to `true` in order to not hard-fail if [OSS Index](https://ossindex.sonatype.org/) fails with HTTP 500 errors.
    * This is at the risk of false negatives; but currently, while OSS Index keeps facing issues, might be the only feasible choice.

## Changes from 2.11.0 to 2.12.0

* Update `dependency-check-core`.
  * Fixes [#154](https://github.com/rm-hull/nvd-clojure/issues/154)

## Changes from 2.10.0 to 2.11.0

* Update `dependency-check-core`.

## Changes from 2.9.0 to 2.10.0

* Update `dependency-check-core`.

## Changes from 2.8.0 to 2.9.0

* Update `dependency-check-core`.

## Changes from 2.7.0 to 2.8.0

* Update `dependency-check-core`.

## Changes from 2.6.0 to 2.7.0

* Update `dependency-check-core`.

## Changes from 2.5.0 to 2.6.0

* Update `dependency-check-core`.

## Changes from 2.4.0 to 2.5.0

* Update `dependency-check-core`.

## Changes from 2.3.0 to 2.4.0

* [#123](https://github.com/rm-hull/nvd-clojure/issues/123): Explicitly only analyze dependencies/artifacts that are relevant to JVM projects.
  * i.e. the internal analyzers that are specialized in other ecosystems e.g. .NET, Ruby, Node.js, etc will not be run at all, improving performance and accuracy.
  * The nvd-clojure implementation never allowed non-jar files to be analyzed, so in practice no behavior has possibly been changed.  
* Update `dependency-check-core`.
* Misc cosmetic improvements for what is printed during execution.

## Changes from 2.2.0 to 2.3.0

* Update `dependency-check-core`.

## Changes from 2.1.0 to 2.2.0

* Update `dependency-check-core`.

## Changes from 2.0.0 to 2.1.0

* Update `dependency-check-core`.

## Changes from 1.9.0 to 2.0.0

* [#113](https://github.com/rm-hull/nvd-clojure/issues/113): Remove all unsafe APIs.
  * Please refer to the README for the recommended installation/usage patterns.
* [#117](https://github.com/rm-hull/nvd-clojure/issues/117): Detect when `nvd-clojure` is being used in a likely-incorrect way, and fail the program when that happens.
* Remove deprecated tasks, related to DB management.

## Changes from 1.8.0 to 1.9.0

#### New

* Offer a new API oriented for Clojure CLI Tools users.
* Skip analyzing source directories (as opposed to .jar files), which are irrelevant (as analyzing sources is beyond the scope of `dependency-check-core`) and can hinder Clojure CLI usage patterns.

## Changes from 1.7.0 to 1.8.0

#### New

* Update `dependency-check-core`.

## Changes from 1.6.0 to 1.7.0

#### New

* Update `dependency-check-core`.

## Changes from 1.5.0 to 1.6.0

#### New

* Implement `:throw-if-check-unsuccessful?` option.
  * Fixes https://github.com/rm-hull/nvd-clojure/issues/50
* Upgrade `dependency-check-core` dependency.

## Changes from 0.6.0 to 1.0

#### Breaking

From now on, the program will only show a summary table of packages that
are demarcated as having a CVSS score greater than zero (i.e any that are
rated OK, are now not shown by default). Any that are rated low or high severity
continue to be shown. To revert to pre-1.0 behavior, add `:verbose-summary true`
to your project [configuration](#configuration-options).
