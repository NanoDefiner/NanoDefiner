# Developer manual

This manual is meant for people mainting and extending the code base. It does not document or explain the code, but it outlines the overall structure of the application as well as how the components work together, to make getting familiar with the code easier and to prevent wasting time solving problems that have already been solved.

Domain-specific terminology is not re-introduced, please refer to the user manual for explanations on the abbreviations.

## Git

Git is used as VCS in our project. If you're not already familiar with Git, we recommend making yourself familiar with not only the basics of pulling, committing, and pushing, but also with techniques like pulling with rebasing, branching, merging. Git is a distributed VCS, if you do nothing but pull/commit/push, you may as well use SVN instead.

### Submodules

We're using [submodules](https://git-scm.com/book/en/v2/Git-Tools-Submodules) for most of our JS dependencies (see section [JS libraries](#libraries)), make sure to initialize them when you clone, and sync them when you make changes.

To initialize submodules during cloning:

    git clone --recursive […]

To initialize them after cloning:

    git submodule init
    git submodule update

See the git documentation for information on updating submodules, you essentially have to `fetch` and then `checkout` the desired commit or tag inside the submodule directory, and then commit the changes.

### .gitignore

By default, `.properties` files in the `config/` directory are ignored, so you will need to force-add them (`git add -f`). This is because most `.properties` files are included as `.properties.default` standard distributions, which the user then copies to `.properties` and make changes to them which are not tracked by git.

## Maven

We use [Maven](http://maven.apache.org/) for deployment and dependency management. Our `pom.xml` inherits from the `spring-boot-starter-parent` `pom.xml`, which pulls in most of the necessary Spring (see section TODO) dependencies and provides up-to-date versions for other optional dependencies. Which means updating the version of the parent `pom.xml` will also update the version of most dependencies automatically.

For deployment, we provide detailed `packagingIncludes` and `packagingExcludes` to prevent unnecessary files (especially for the submodules) form being packaged into the `war` archive.

Two profiles are provided for standalone (default) packaging and tomcat (`war`) packaging. The latter is more leightweight as it does not include an embedded tomcat and a couple of libraries which are provided by the servlet container. It should still be possible to run the standalone version inside a tomcat server.

[Maven Central](https://search.maven.org/) does not contain all of our dependencies, so we additionally reference the [jitpack.io](https://jitpack.io/) Maven repository in our `pom.xml`.

### Dependencies

Here's a non-exhaustive list of (direct) Java dependencies with a short description for each:

- Apache POI: Java API for Microsoft Documents, used to read xls(x) files.
- Apache Shiro: Security framework, used for authentication and authorization.
- Drools: Rule engine and management system, used for decisions on material/technique compatibility.
- Spring: Java application framework with support for MVC.
- Thymeleaf: Template engine supporting natural templates.
- OpenCSV: CSV parser, used for locale files and the knowledge base.
- Hibernate: ORM library, used to map Java entities to DBMS entities.
- Guava: Google Core Libraries for Java.
- cglib: Byte Code Generation Library, used to generate and change classes at runtime.
- HikariCP: JDBC connection pool, used to manage the connection to the DBMS.
- Gral: Java library for creating and displaying plots.
- JasperReports: Reporting engine, used for fine-tuning and for features not supported by DynamicReports.
- DynamicReports: Reporting engine built on top of JasperReports, used to create PDF reports.
- JBCrypt: Java implementation of the Blowfish hashing algorithm, used for password and hash management.

## Spring

We use the [Spring Framework](https://spring.io/), more specifically Spring Boot, Spring MVC, and Spring Data Repositories.

TODO what else? Write about configuration classes, controllers, repositories etc.?

## Configuration

The application is started in the `NanoDefiner` class, which then initializes the application configuration using the `MvcConfig` class, which does all the heavy lifting when it comes to configuring the Spring Framework using annotations. Make yourself familiar with the annotations, there are a lot of them.

Here's a list and short descriptions of the configuration classes in `eu.nanodefine.etool.config`:

- `AsyncConfig`: Sets up asynchronous processing, currently only used for sending mails.
- `DroolsConfig`: Sets up the Drools rule engine (see section TODO).
- `HibernateConfig`: Sets up Hibernate and HikariCP, as well as the transaction management.
- `ImageConfig`: Provides dynamic icons for attributes in the MCS which are loaded once at startup
- `KnowledgeConfig`: Loads and sets up the knowledge base (see section TODO).
- `LocaleConfig`: Sets up locale management (available locales, locale resolver, message source, see section TODO).
- `MailConfig`: Sets up the mail subsystem using the values in the `application.properties`.
- `ShiroConfig`: Sets up authentication and authorization with Apache Shiro (see section TODO).
- `ThymeleafConfig`: Sets up the Thymeleaf template engine (see section TODO).
- `TomcatConfig`: Increases the tomcat cache size.

The configuration files are located in the `config/` directory:

- `img/`: User image folder, currently containing an example for a company brand for the PDF report.
- `knowledge/`: Containts the knowledge base. Relevant files are in `.csv` format, the `.xls` versions are not read by the software.
  - `hashes.properties`: Contains the validated hashes for the MTs, the check is done in the `KnowledgeConfig`.
- `locales/`: The locale files are stored in this directory. These can be in `.xml`, `.properties`, or `.csv` format, and usually follow the naming scheme `messages_language_variant.ext`, e.g. `messages_en_US.csv`. For more information, see section TODO and the `README.txt` in this folder.
- `application.properties.default`: Conatains general application configuration settings, most of them are documented directly in that file.
- `hibernate.properties.default`: Hibernate and DBMS settings. At the time of writing, only MySQL-compatible DBMS have been tested, others should work as well though.
- `hikari.properties.default`: Data source settings. See the comment above about MySQL.

## Controller

The controller layer is mostly made up of the `@Controller` annotated classes described in the following section.

TODO what else? Describe stuff like model attributes?

### Controllers

In the controller classes, requests are processed using the services (and stuff like [validators](#validators)). At the time of writing, many controllers still directly access repositories, this should ideally be avoided in the future, instead using service methods which in turn access the repositories.

#### Controller base class

All controllers extend the `AbstractController` which provides:

- access to the service and repository managers
- access to session attributes as well as to the current user
- [entity validation](#entity-validation) (i.e. whether the current user has access to entities referenced in the request)

#### Controller methods

Most of the stuff (and everything that is not) covered here can also be found in the [Spring documentation](https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-controller).

Some general things, use

- `@RequestMethod` to specify the request path (this can also be done at the class level) and  request method (only at the method level)
- `@RequiresUser` and other shiro annotations to control access (TODO link)
- `@ModelAttribute` to inject model attributes
- `@RequestParam` to access URL parameters
- `@PathVariable` to access URL path variables
- `@RequestAttribute` to access things like the [history](#history) or [messages](#messages)
- `@ResponseBody` to create a JSON response (just return a map or something and it will automatically be converted)
- the [`UriService`](#uri-building) for building redirects
- the `Templates` class to manage template names for direct rendering
- use `@InitBinder` and `@Valid` for [form validation](#form-validation)

#### Entity validation

Entity validation happens in `@RequestMapping` methods and must be performed for all entities extracted from the request. Here's an example:

    this.validateUserAwareEntities("Invalid dossier ID or dossier access denied", dossier);

If entity validation fails, the user will be redirected to an error page.

#### Form validation

[Form validation](https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-config-validation) happens via the Spring [`Validator`](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/validation/Validator.html) class. See existing validators to discover its basic usage.

In the controller, you inject your validator, and set it up inside an `@InitBinder` method, which accepts a `WebDataBinder` parameter. On this binder you can set your validator as well as configure other aspects of form binding (allowed fields etc.), see existing form binding for basic usage. On the controller method, you then annotate the form-backing bean with `@Valid` and add a `BindingResult` parameter.

### Helpers

In this section all components of the controller layer will be described that are not controller classes.

#### Advices

Advices are weaved into controllers, they are most useful to provide model attributes or exception handlers. When you can't find the origin of a model attribute, this is a good place to start looking.

- `ConfigAdvice`: Makes important configuration variables available as model attributes.
- `DefaultModelAttributes`: As the name suggests, this adds some default model attributes, at the time of writing these are an empty action list (more on actions in section TODO) and a list of breadcrumbs used in the templates later.
- `ErrorAdvice`: Adds a number of exception handlers which automatically deal with recurring errors.
- `RequestIdsAdvice`: Extracts entities from the request. Let's say you have something like `/dossier.id=10` or `?dossier.id=10` in the request URL, then the dossier with ID 10 will automatically be loaded from the DBMS and made available in the model (binding for this entity is disabled). Note that even if no entity ID was found in the request, a model attribute will still be added. The ID of these entities will be set to 0.
- `UriAnchorAdvice`: This will extract anchor information from the request. Normally, anchors are client-side only. So when a user navigates around a page without leaving it (e.g. accessing different tabs), the server will not know the exact state of the page when the user left it. To mitigate this, we add anchor information to the request URL via JS. These information are extracted here and made available in the model.

TODO document entity binding

#### Interceptors

Interceptors hook into different types of processes to execute logic before or after certain points in these processes. `HandlerInterceptor` classes cannot change the model, only the request, which is why attributes provided by interceptors have to be accessed using the `@RequestAttribute` annotation instead of the `@ModelAttribute` annotation.

- `HibernateStatisticsInterceptor`: Hooks into the Hibernate query execution process to keep track of the number of queries. If you want to debug DBMS access, this is one place to do it.
- `HistoryInterceptor`: Keeps track of history information, see section [History](#history) for more information on the history.
- `MessageInterceptor`: Adds message lists to the request before it is handled and encodes the lists as flash attributes after the request is handled. See section TODO for more information on messages.
- `RequestStatisticsInterceptor`: This prints the information collected by the `HibernateStatisticsInterceptor`, and also keeps track of how long request processing took.


#### History

The `History` keeps track of which URLs the user has requested and if those requests were `GET` or `POST` requests. It provides access to `HistoryEntry` objects by entity, request method, or both, and it also holds redirect information which is kept track of in the `HistoryInterceptor`.

To access the history inside a controller, simply add a method parameter `@RequestAttribute History history` to the controller method. Within the templates, it can be accessed using `${history}` as well.

TODO more details?

#### Messages

Messages can be error messages, success messages or info messages. They are displayed using the template fragment in `fragments/messages.html` which is included in the `layout.html` template. The idea is to add these messages (as locale strings or formatted messages) in the controler method, and then display them on the next page the user is shown.

The `MessageInterceptor` adds three lists to the requests: `errors`, `successes`, and `messages`. To access these lists in a controller method, simply add a method parameter `@RequestAttribute("errors") List<String> errors` to the controller method. You can then just add items to the list, everything else is done automatically.

If, at the end of the controller method, a template is rendered, the messages can simply be taken from the current request. If, on the other hand, a redirect is executed, things become a bit more complicated. To ensure the messages are passed to the next request without changing the URL, we use [flash attributes](https://docs.spring.io/spring/docs/current/spring-framework-reference/web.html#mvc-flash-attributes). This is also done in the `MessageInterceptor`, where at the end of request processing, it is checked whether the response is a redirect, and if so it stores the messages as flash attributes to be used in the next request. Before the next request, the messages are restored and made available in the controller as request attributes.

#### Actions

Actions are are a list of links in a box, by default near the top of the page. Adding these links happens in the controller, sometimes within `@RequestMapping` methods, but sometimes (if they are valid for more than one such methods) in dedicated `@ModelAttribute` methods. We will use one of the latter to demonstrate how to work with actions:

    @ModelAttribute
    public void actionList(@ModelAttribute("actionList") List<ActionListEntry> actionList) {
      // Build the link for the action
      String uri = this.uriService.builder("entity", "action").build();

      // Add the action to the list
      actionList.add(new ActionListEntry(uri, "some.label.locale.string"));
    }

There is also the option to protect the action with a disclaimer modal. For that just add a third parameter to the `ActionListEntry` constructor which is one of the disclaimer types (see locale strings starting with `"disclaimer."`, e.g. `update` or `archive`).

In the templates, the actions are added in `fragments/actions.html` which is included in the `layout.html` template. The box is moved via JS (`actions.js`) to where it is supposed to be after loading the page.

## Model

The model consists primarily of the DTOs, [services](#services), and [repositories](#repositories).

The DTOs will not be documented much further at this point, since they are very simple classes outfitted with `javax.persistence` annotations which contain the JPA DBMS mapping. Methods not directly related to the underlying model have to be annotated with `@Transient` to avoid being picked up and misinterpreted by the ORM layer. At the time of writing, the DTO classes do not depend on Hibernate, instead using only JPA annotations.

### Services

Services are usually stateless classes annotated with `@Service` and mostly implement the tagging interface `IService` (see the [model helpers](#model-helpers) section for more details). They do the heavy lifting for the controllers and interact with the model using the [repositories](#repositories). Service methods should be free of side effects unless otherwise noted.

Services are collections of related methods and are usually separated by entity and by whether their methods are used in controllers or in templates. Keeping service classes well sorted so that they don't grow too big and methods can still be found quickly is the most difficult part here.

#### URI building

URIs within the NanoDefiner e-tool usually look something like this:

    https://domain.tld/contextPath/entity/action/path.param1=value1?query.param1=value2

The domain and context path are configured in the `application.properties` (see section [configuration](#configuration)), in most cases, this part of a URI is handled by thymeleafs `@` URI builder (see section TODO).

##### UriBuilder

To make building a URI easier, the `UriBuilder` class was created. It provides a fluent interface for URI creation which supports path and query parameters, anchors, Spring redirects, and entities.

Here is an example (to make things clearer, constants for entities and actions are not used, see existing code for normal usage):

    Dossier dossier = new Dossier();
    dossier.setId(5);

    // Create with entity and action
    UriBuilder builder = UriBuilder.create("dossier", "read");

    // Results in "redirect:/dossier/read/dossier.id=5/test=value?testQuery=valueQuery#anchor"
    String redirect = builder.addEntityId(dossier).addPathParam("test", "value")
      .addQueryParam("testQuery", "valueQuery").setAnchor("anchor").buildRedirect();

The resulting URIs can be used for redirects and within templates. If you want to use them inside of e-mails, for example, you will want to use the `UriService#builderAbsolute` method to get a builder with the correct prefix (including domain and context path).

##### Uri service

The `UriService` is mostly used to build URIs from within a template (`@U.builder([…])`), and to create a builder for absolute links (e.g. for e-mails). It also contains method to build redirects or redirect parameters (this basically persists the current state of the history within the URI).

TODO more details?

### TranslationService

The translation service provides, besides `translate*` methods for translations within Java code, helper methods for locale string escaping (for use in CSS selectors) and conversion of string locale representations to `Locale` instances. It also provides access to the current user locale.

### Repositories

[Spring data repositories](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories) are interfaces which allow access to the underlying model stored in a DBMS using annotations only (the actual code is generated by Spring). This is usually done via [query methods](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#repositories.query-methods), but you can also use the [`@Query` annotation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#jpa.query-methods.at-query).

The repositories extend the `CrudRepository<Entity, Integer>` interface.

TODO more details?

### Model helpers

#### Bean type Managers

To avoid excessive dependency injection, bean type managers for services and repositories have been introduced. These managers use a Spring feature which allows you to autowire collections of objects by an interface or abstract class, thus allowing access to all services and repositories through a single dependency.

Here are two examples for accessing a service and repository:

    @Controller
    public class ExampleController {

      @Autowired
      private ServiceManager serviceManager;

      @Autowired
      private RepositoryManager repositoryManager;

      @RequestMapping("/example")
      public String example() {
        DossierService dossierService = this.serviceManager.getBean(DossierService.class);
        DossierRepository dossierRepository = this.repositoryManager.getBean(DossierRepository.class);

        […]
      }

Try to avoid field injection outside of controllers to improve reusability.

#### Predicates

Predicates extend the Google class `Predicate` and are used for filtering. `IDossierAwarePredicate` is an interface for predicates which need dossier access.

## View

### Reports

PDF reports are created using the [DynamicReports](http://www.dynamicreports.org/) library, which is based on [JasperReports](https://community.jaspersoft.com/project/jasperreports-library). Reports are created purely in Java, no XML templates or similar necessary.

#### DynamicPdfReport

A report according to JasperReports consists (very roughly) of a heading, a table with data, and a summary, and it is represented in DynamicReports by the `JasperReportBuilder` class. Our PDF report consists of many such sub-reports, and because we found the `JasperReportBuilder` lacking in functionality and convenience, we created our own `DynamicPdfReport` on top of it (using composition, not inheritance).

As documented in its JavaDoc, our class adds a model which allows reports to store and transfer data, fonts/styles management, as well as sub-report creation. Additionally, it provides a simple interface to the underlying report builder.

For more details on its usage, see the JavaDoc as well as the classes documented in the following sections.

#### DynamicReportService

In this class, the PDF report is created. In `provideModelAttributes`, all necessary information for report creation is gathered. The resulting model is then passed to all sub-reports. The service implements its own `translate` method which uses the configured report locale instead of the user locale, so that generated reports are always in the same language.

Other than that it's pretty much a collection of methods which add information to the report.

#### DynamicReportComponentService

This service provides creation methods for report components like fillers, headlines, texts, and tables. See the code, its documentation, and its usage in `DynamicReportService` for more details.

### Templates

[Thymeleaf](http://www.thymeleaf.org/) is used as the template engine in our application. It supports natural templates (plain HTML) and has lots of features (it also has a nice [documentation](http://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html)).

A couple of things before we dive in:

- use `th:text` whenever dealing with user input, use `th:utext` in all other cases (to allow HTML formatting inside of locale strings, for example)
- keep processing and non-view code to a minimum inside templates, use template services or model attributes added in the controller method instead


#### Files and folders

A short overview of files and folders:

- `layout.html`: contains the header, naviagation and footer for every page (needs to be included in every file, see `example.html`).
- `example.html`: copy this when creating a new template, it contains the necessary includes from `layout.html`.
- `index.html` (TODO `devel.html`?): home page of the application.
- `fragments/`: this folder contains global fragments. These files do no have to be valid HTML documents, just code fragments which can be included from other templates.
- `error/`: this folder contains error templates / fragments.
- `mail/`: this folder contains mail templates. Mails are also parsed by Thymeleaf.
- all other folders contain templates / fragments for specific controllers / entities.

#### Important elements

The thymeleaf syntax is documented well in its documentation, this section will just highlight some of the most important elements used in templates.

- model attributes are directly accessible everywhere by their names
- use `|` for string building, e.g. `th:href="|#${idMethod}|"` (alternatively use `th:href="'#' + ${idMethod}"`).
- use `@{}` for links, e.g. `@{|/${@E.DASHBOARD}/${@A.READ}|}` or `@{${@U.builder(@E.DOSSIER, @A.READ).addEntityId(report.dossier)}}`.
- use `#{}` for locale strings, e.g. `th:text="#{layout.title}"`, with parameters and dynamic locale string: `th:text="#{|method.${history.current.action}.jumbo.headline|(${tierString})}"`.
- use `[[]]` for inline expressions, e.g. `[[#{mail.global.salutation}]]`.
- use `@` for access to Spring beans, e.g. `th:with="borderline=${@methodService.isBorderline(method)}"`, since mails are processed by a separate engine, the `@` notation is not available there, all beans are instead available like model attributes, e.g. `th:utext="#{mail.user_created.body.activate_accounts(${U.builderAbsolute(E.USER, A.ACTIVATE).build()})}"`, i.e. the same just without the `@`.
- use the CSS class `variables` for JS locale strings, see [below](#locale-strings).



### JavaScript

JavaScript is used relatively heavily in the application, and it does not work when JS is disabled. To keep things clean and separated, JS files are separated similar to templates (by controller/entity, while includable files are in `incl/`), and namespaces are used:

    // Namespace for the file js/user/update.js
    ND.user = ND.user || {};
    ND.user.update = ND.user.update || {};

The main namespace `ND` is defined in `incl/first.js` and its existence can be taken for granted.

Whenever forms are used, the file `incl/form.js` can be included to support automatic entity name update within breadcrumbs (add the attribute `data-entity` to your form with the name of the entity to enable this) and `incl/multistep.js` for multi-step forms (see existing ones for usage). All other files are automatically included.

#### Libraries

Here's a list of JavaScript libraries (most of them are included as Git submodules) with a short description:

- jQuery: Powerful JS library.
- Bootstrap: We are no designers. Thanks to Bootstrap, we don't have to be.
- FileSaver: Save files for browsers that don't natively support it.
- Holder: Renders image placeholders.
- html5shiv: HTML5 support for older browsers.
- JavaScript Cookie: Cookie-handling in JS
- Pace: Fancy loading animations.
- Respond.js: Responsive web stuff.

#### Locale strings

If you need locale strings in JavaScript, you first need to make them available at some point in the template, e.g.

    <div class="variables">
      <span id="locale-report.create.method.select_alert"
          th:text="#{report.create.method.select_alert}"></span>
    </div>

It does not matter where this block is positioned (usually somewhere at the end, before template-specific JS and the footer). Inside of your JS, this locale string can be accessed as follows:

    ND.util.getLocaleString("report.create.method.select_alert")

### CSS

Thanks to Bootstrap, there's not a lot of CSS'ing going on in the application for now, all customiziations are in the `css/style.css` which is approaching 500 lines and could use some splitting (in, for example, bootstrap-related customization and everything else).

## Knowledge base

The knowledge base (KB) is the heart of the application, as it contains all the information necessary to make MT recommendations.

It consists of:

- *Explanation dictionary and sheet:* Contains explanation information in case of match/mismatch/uncertainty for each attribute.
- *Material dictionary and sheet:* Contains material types / groups.
- *Performance dictionary and sheet:* Contains detailed MT properties.
- *Priority dictionary and sheet:* Property priorities for each MT.
- *Reference dictionary and sheet:* Reference and test materials.
- *Technique dictionary and sheet:* Contains basic information about MTs.

For further information about the KB, see our publications.

TODO link to publications

### Files

The KB is located in the `config/knowledge` directory by default. There is a file called `hashes.properties` in this directory, which contains the validated hashes. If the properties of existing techniques are changed in the KB, a warning will be printed in the generated PDF report. To update the validated hashes, delete this file and turn off production mode, and the file will be re-generated. The KB consists of one CSV file for each dictionary and sheet.

There are XLS versions of the KB as well, but these are only to make use of the advanced features of that format--when changes are made to the KB, they have to be reflected in the CSV files or they will not take effect.

Both the location of the KB CSV files and the hashes file can be changed in the `application.properties`.

### Loading and access

The KB is loaded in the `KnowledgeConfig` class, more specifically by the assoicated `*Dictionary` and `*Config` classes for each dictionary and sheet, which are made available as beans afterwards.

Here's a list of these classes and what they provide access to:

- `ExplanationConfiguration`: Provides access to `ConfiguredExplanation` instances, which represent a row in the explanation sheet. `ConfigiredExplanation` instances can be accessed by attribute name.
- `ExplanationDictionary`: Contains only constants for the column names.
- `MaterialConfiguration`: Provides access to `ConfiguredMaterial` instances, which represent a row in the material sheet. `ConfiguredMaterial` instances can be accessed by signifier.
- `MaterialDictionary`: Contains only constants for the column names.
- `PerformanceConfiguration`: Provides access to `ConfiguredPerformance` instances, which consist of a set of `PerformanceCriterion` instances and represent a column in the performance sheet. `ConfiguredPerformance` instances can be accessed by material and technique signifier. The `PerformanceCriterion` instances can be accessed by their names.
- `PerformanceDictionary`: Provides access to `Attribute` instances, which represent a row in the performance dictionary. It also provides translation of attribute values (see the "Options" column in the performance dictionary).
- `PriorityConfiguration`: Provides access to `ConfiguredPriority` instances, which consist of a set of `PriorityCriterion` instances and represent a column in the explanation sheet. `ConfiguredPriority` instances can be accessed by material and technique signifier. The `PriorityCriterion` instances can be accessed by their names.
- `PriorityDictionary`: Contains only constants for the column names.
- `ReferenceConfiguration`: Provides access to `ConfiguredReference` instances, which consist of a set of `ReferenceCriterion` instances and represent a colum in the reference sheet. `ConfiguredReference` instances can be accessed by reference signifier. The `ReferenceCriterion` instances can be accessed by their names.
- `ReferenceDictionary`: Provides access to `Attribute` instances, which represent a row in the reference dictionary. It also provides translation of attribute values (see the "Options" column in the reference dictionary).
- `TechniqueConfiguration`: Provides access to `ConfiguredTechnique` instances, which represent a row in the technique sheet. `ConfiguredTechnique` instances can be accessed by signifier.
- `TechniqueDictionary`: Contains only constants for the column names.

When accessing attributes of the performance or reference sheet, be aware that while the same set of attributes is contained in both, the properties of these attributes are different (option values, label, description etc.). The MCS uses the reference attributes, so manual links are only taken from the reference dictionary (the column exists in both dicionaries).

## Drools

The Drools rule engine is used to determine compatibility between material and MT properties, which is then used to create MT recommendations based on the material properties. The rules are stored in `src/main/resources/drools`, most importantly `monoconst.drl` (property compatibility) and `monoconst_expl.drl` (explanations).

The actual matching happens in `DroolsService#performMatching`, which matches the given material criteria against the configured MT criteria. Returned is a `PerformanceCriterionDDOMap` for each MT, which is marked as either matching or non-matching (`getMatch()`), which indicates if the MT as a whole is recommended for (i.e., compatible with) the material. Each individual `PerformanceCriterionDDO` contains attribute-level match information as well, and it contains a `matchReason`, which is one of the column headings in the explanation sheet.

## Locales

At the time of writing, the application is only available in English, but making it available in other languages is a matter of translating a couple hundred locale strings. For locale management, the Spring `MessageSource` is used, which uses the JDK standard message parsing provided by `MessageFormat` (which has some implications on our locale strings, as documented in the `README.txt` inside the locale directory).

See the [configuration](#configuration) section for information on the location of locale files, the [thymeleaf](#important-elements) section for information on how to use locale strings in templates, and the [JavaScript](#locale-strings) section for information on how to use locale strings in JS files.

## Misc

- Analysis processors?
- shiro


